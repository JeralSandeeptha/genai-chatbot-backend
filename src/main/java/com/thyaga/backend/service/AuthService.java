package com.thyaga.backend.service;

import com.thyaga.backend.dto.AuthResponse;
import com.thyaga.backend.dto.LoginRequest;
import com.thyaga.backend.entity.RefreshToken;
import com.thyaga.backend.entity.User;
import com.thyaga.backend.exceptions.UnauthorizedException;
import com.thyaga.backend.repository.RefreshTokenRepository;
import com.thyaga.backend.repository.UserRepository;
import com.thyaga.backend.security.JwtService;
import com.thyaga.backend.security.TokenCookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenCookieService tokenCookieService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenCookieService tokenCookieService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenCookieService = tokenCookieService;
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return issueTokens(user, response);
    }

    public AuthResponse refresh(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }

        if (!jwtService.isTokenValid(refreshTokenValue) || !jwtService.isRefreshToken(refreshTokenValue)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return issueTokens(storedToken.getUser(), response);
    }

    public void logout(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }

        tokenCookieService.clearTokenCookies(response);
    }

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.ofInstant(
                jwtService.extractExpiration(refreshTokenValue).toInstant(),
                ZoneId.systemDefault()
        ));
        refreshTokenRepository.save(refreshToken);

        tokenCookieService.setTokenCookies(response, accessToken, refreshTokenValue);

        return new AuthResponse(user.getId(), user.getEmail());
    }
}
