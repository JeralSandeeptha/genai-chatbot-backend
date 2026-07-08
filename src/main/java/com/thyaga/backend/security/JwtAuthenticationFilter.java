package com.thyaga.backend.security;

import com.thyaga.backend.service.TokenInvalidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenCookieService tokenCookieService;
    private final TokenInvalidationService tokenInvalidationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            TokenCookieService tokenCookieService,
            TokenInvalidationService tokenInvalidationService
    ) {
        this.jwtService = jwtService;
        this.tokenCookieService = tokenCookieService;
        this.tokenInvalidationService = tokenInvalidationService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth")
                || ("POST".equalsIgnoreCase(request.getMethod())
                    && "/api/v1/users".equals(path));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = tokenCookieService.extractToken(request.getCookies(), TokenCookieService.ACCESS_TOKEN_COOKIE);

        if (accessToken == null && request.getHeader("Authorization") != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }
        }

        if (accessToken == null) {
            sendUnauthorized(response, "Access token is missing");
            return;
        }

        if (!jwtService.isTokenValid(accessToken) || !jwtService.isAccessToken(accessToken)) {
            sendUnauthorized(response, "Access token is invalid or expired");
            return;
        }

        if (tokenInvalidationService.isTokenRevoked(accessToken)) {
            sendUnauthorized(response, "Access token has been revoked");
            return;
        }

        UUID userId = jwtService.extractUserId(accessToken);
        String email = jwtService.extractEmail(accessToken);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                email,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"statusCode":401,"message":"%s"}
                """.formatted(message));
    }
}
