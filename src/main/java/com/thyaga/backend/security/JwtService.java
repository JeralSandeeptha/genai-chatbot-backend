package com.thyaga.backend.security;

import com.thyaga.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(resolveSecretBytes(jwtProperties.secret()));
    }

    public String generateAccessToken(UUID userId, String email) {
        return buildToken(userId, email, ACCESS_TOKEN_TYPE, jwtProperties.accessTokenExpiration());
    }

    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(userId, email, REFRESH_TOKEN_TYPE, jwtProperties.refreshTokenExpiration());
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private String buildToken(UUID userId, String email, String tokenType, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    private byte[] resolveSecretBytes(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length >= 32) {
            return keyBytes;
        }

        return Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(keyBytes)
        );
    }
}
