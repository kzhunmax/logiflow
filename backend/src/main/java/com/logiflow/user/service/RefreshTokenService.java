package com.logiflow.user.service;

import com.logiflow.shared.exception.RefreshTokenExpiredException;
import com.logiflow.user.model.RefreshToken;
import com.logiflow.user.model.User;
import com.logiflow.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateToken())
                .expiryDate(calculateExpiryDate())
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", user.getUsername());
        return saved;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        User user = oldToken.getUser();

        refreshTokenRepository.delete(oldToken);

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token(generateToken())
                .expiryDate(calculateExpiryDate())
                .build();

        RefreshToken saved = refreshTokenRepository.save(newToken);
        log.info("Rotated refresh token for user: {}", user.getUsername());
        return saved;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("Deleted refresh token for user: {}", user.getUsername());
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token has expired. Please login again.");
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private Instant calculateExpiryDate() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }
}

