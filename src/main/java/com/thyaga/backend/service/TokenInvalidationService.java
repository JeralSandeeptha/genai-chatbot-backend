package com.thyaga.backend.service;

import com.thyaga.backend.entity.RefreshToken;
import com.thyaga.backend.entity.RevokedToken;
import com.thyaga.backend.entity.TokenType;
import com.thyaga.backend.repository.RefreshTokenRepository;
import com.thyaga.backend.repository.RevokedTokenRepository;
import com.thyaga.backend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class TokenInvalidationService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public TokenInvalidationService(
            RevokedTokenRepository revokedTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService
    ) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public boolean isTokenRevoked(String token) {
        return token != null && !token.isBlank() && revokedTokenRepository.existsByToken(token);
    }

    public void invalidateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }

        if (!jwtService.isTokenValid(accessToken) || !jwtService.isAccessToken(accessToken)) {
            return;
        }

        saveRevokedToken(accessToken, TokenType.ACCESS);
    }

    public void invalidateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(storedToken -> {
                    storedToken.setRevoked(true);
                    refreshTokenRepository.save(storedToken);
                });

        if (jwtService.isTokenValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {
            saveRevokedToken(refreshToken, TokenType.REFRESH);
        }
    }

    private void saveRevokedToken(String token, TokenType tokenType) {
        if (revokedTokenRepository.existsByToken(token)) {
            return;
        }

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);
        revokedToken.setTokenType(tokenType);
        revokedToken.setExpiresAt(LocalDateTime.ofInstant(
                jwtService.extractExpiration(token).toInstant(),
                ZoneId.systemDefault()
        ));
        revokedTokenRepository.save(revokedToken);
    }
}
