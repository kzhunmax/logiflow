package com.logiflow.user.controller;

import com.logiflow.user.dto.AuthResponse;
import com.logiflow.user.dto.LoginRequest;
import com.logiflow.user.dto.RefreshTokenRequest;
import com.logiflow.user.dto.UserInfoResponse;
import com.logiflow.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;

    @Value("${jwt.expiration}")
    private int accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private int refreshTokenExpiration;

    @Value("${app.cookie.secure:true}")
    private boolean secureCookie;

    @Operation(summary = "Login", description = "Authenticate user and set tokens in HTTP-only cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/login")
    public ResponseEntity<UserInfoResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);

        addTokenCookies(response, authResponse.accessToken(), authResponse.refreshToken());

        return ResponseEntity.ok(new UserInfoResponse(authResponse.username(), authResponse.role()));
    }

    @Operation(summary = "Refresh token", description = "Get new access token using refresh token from cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<UserInfoResponse> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        AuthResponse authResponse = authService.refreshToken(new RefreshTokenRequest(refreshToken));

        addTokenCookies(response, authResponse.accessToken(), authResponse.refreshToken());

        return ResponseEntity.ok(new UserInfoResponse(authResponse.username(), authResponse.role()));
    }

    @Operation(summary = "Logout", description = "Clear auth cookies and invalidate refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully logged out")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {
        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }

        clearTokenCookies(response);

        return ResponseEntity.noContent().build();
    }

    // ==================== Cookie Helpers ====================

    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, accessToken, accessTokenExpiration / 1000));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, refreshToken, refreshTokenExpiration / 1000));
    }

    private void clearTokenCookies(HttpServletResponse response) {
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

