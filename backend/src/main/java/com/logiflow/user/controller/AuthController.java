package com.logiflow.user.controller;

import com.logiflow.user.dto.AuthResponse;
import com.logiflow.user.dto.LoginRequest;
import com.logiflow.user.dto.RefreshTokenRequest;
import com.logiflow.user.dto.UserInfoResponse;
import com.logiflow.user.service.AuthCookieService;
import com.logiflow.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

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
        authCookieService.addTokenCookies(response, authResponse.accessToken(), authResponse.refreshToken());
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
            @CookieValue(name = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        AuthResponse authResponse = authService.refreshToken(new RefreshTokenRequest(refreshToken));
        authCookieService.addTokenCookies(response, authResponse.accessToken(), authResponse.refreshToken());
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
        authCookieService.clearTokenCookies(response);
        return ResponseEntity.noContent().build();
    }
}

