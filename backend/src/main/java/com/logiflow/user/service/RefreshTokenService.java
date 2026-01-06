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

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    // ==================== Query Methods ====================

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // ==================== Command Methods ====================

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Delete existing refresh token for user (single session)
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

        // Delete old token
        refreshTokenRepository.delete(oldToken);

        // Create new token (token rotation for security)
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

    // ==================== Validation ====================

    public void verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token has expired. Please login again.");
        }
    }

    // ==================== Helpers ====================

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private Instant calculateExpiryDate() {
        return Instant.now().plusMillis(refreshTokenExpiration);
    }
}

