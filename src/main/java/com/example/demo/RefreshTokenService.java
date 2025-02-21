package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.token.refresh-token.expiry}")
    private long refreshTokenExpiry;

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveToken(String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenExpiry, ChronoUnit.SECONDS)); // Example expiry
        refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenRevoked(String token) {
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(token);
        return storedToken.isEmpty() || storedToken.get().isRevoked();
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}
