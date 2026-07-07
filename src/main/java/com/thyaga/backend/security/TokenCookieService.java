package com.thyaga.backend.security;

import com.thyaga.backend.config.CookieProperties;
import com.thyaga.backend.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    public TokenCookieService(JwtProperties jwtProperties, CookieProperties cookieProperties) {
        this.jwtProperties = jwtProperties;
        this.cookieProperties = cookieProperties;
    }

    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_TOKEN_COOKIE, accessToken,
                Duration.ofMillis(jwtProperties.accessTokenExpiration())).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_TOKEN_COOKIE, refreshToken,
                Duration.ofMillis(jwtProperties.refreshTokenExpiration())).toString());
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_TOKEN_COOKIE, "", Duration.ZERO).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_TOKEN_COOKIE, "", Duration.ZERO).toString());
    }

    public String extractToken(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
