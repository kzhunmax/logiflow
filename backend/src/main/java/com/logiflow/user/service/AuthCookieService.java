package com.logiflow.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @Value("${jwt.expiration}")
    private int accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private int refreshTokenExpiration;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, accessToken, accessTokenExpiration / 1000));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, refreshToken, refreshTokenExpiration / 1000));
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, "", 0));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, "", 0));
    }

    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);           // Not accessible via JavaScript
        cookie.setSecure(secureCookie);     // Only sent over HTTPS (configurable for dev)
        cookie.setPath("/api");             // Only sent to API endpoints
        cookie.setMaxAge(maxAgeSeconds);    // Expiration
        cookie.setAttribute("SameSite", "Strict"); // CSRF protection
        return cookie;
    }
}
