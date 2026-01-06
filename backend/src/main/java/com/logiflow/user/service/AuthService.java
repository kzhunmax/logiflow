package com.logiflow.user.service;

import com.logiflow.shared.exception.InvalidRefreshTokenException;
import com.logiflow.user.dto.AuthResponse;
import com.logiflow.user.dto.LoginRequest;
import com.logiflow.user.dto.RefreshTokenRequest;
import com.logiflow.user.model.RefreshToken;
import com.logiflow.user.model.User;
import com.logiflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticateUser(request.username(), request.password());

        User user = findUserByUsernameOrThrow(request.username());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = findRefreshTokenOrThrow(request.refreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        // Rotate refresh token for security
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        User user = newRefreshToken.getUser();

        String accessToken = generateAccessToken(user);

        log.info("Token refreshed for user: {}", user.getUsername());
        return new AuthResponse(accessToken, newRefreshToken.getToken(), user.getUsername(), user.getRole().name());
    }

    @Transactional
    public void logout(String username) {
        User user = findUserByUsernameOrThrow(username);
        refreshTokenService.deleteByUser(user);
        log.info("User logged out: {}", username);
    }

    private void authenticateUser(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    private User findUserByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private RefreshToken findRefreshTokenOrThrow(String token) {
        return refreshTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getUsername(), user.getRole().name());
    }

    private String generateAccessToken(User user) {
        var userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtService.generateToken(userDetails);
    }
}

