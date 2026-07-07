package com.thyaga.backend.controller;

import com.thyaga.backend.dto.AuthResponse;
import com.thyaga.backend.dto.LoginRequest;
import com.thyaga.backend.security.TokenCookieService;
import com.thyaga.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieService tokenCookieService;

    public AuthController(AuthService authService, TokenCookieService tokenCookieService) {
        this.authService = authService;
        this.tokenCookieService = tokenCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        log.info("POST /api/v1/auth/login - login attempt for email={}", request.email());
        try {
            AuthResponse authResponse = authService.login(request, response);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", authResponse);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Login query was successful");

            log.info("Login query was successful, userId={}", authResponse.id());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Login query failed for email={}", request.email(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "statusCode", HttpStatus.UNAUTHORIZED.value(),
                            "message", "Login query failed",
                            "error", ex.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("POST /api/v1/auth/refresh - refreshing access token");
        try {
            String refreshToken = tokenCookieService.extractToken(
                    request.getCookies(),
                    TokenCookieService.REFRESH_TOKEN_COOKIE
            );

            AuthResponse authResponse = authService.refresh(refreshToken, response);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", authResponse);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Refresh token query was successful");

            log.info("Refresh token query was successful, userId={}", authResponse.id());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Refresh token query failed", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "statusCode", HttpStatus.UNAUTHORIZED.value(),
                            "message", "Refresh token query failed",
                            "error", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("POST /api/v1/auth/logout - logging out user");
        try {
            String accessToken = tokenCookieService.extractToken(
                    request.getCookies(),
                    TokenCookieService.ACCESS_TOKEN_COOKIE
            );
            String refreshToken = tokenCookieService.extractToken(
                    request.getCookies(),
                    TokenCookieService.REFRESH_TOKEN_COOKIE
            );

            authService.logout(accessToken, refreshToken, response);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", null);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Logout query was successful");

            log.info("Logout query was successful");
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Logout query failed", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Logout query internal server error",
                            "error", ex.getMessage()));
        }
    }
}
