package com.logiflow.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String username,
        String role
) {}

