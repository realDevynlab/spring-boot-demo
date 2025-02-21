package com.example.demo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.token.type.access-token}")
    private String accessToken;

    @Value("${jwt.token.type.refresh-token}")
    private String refreshToken;

    @Value("${jwt.token.access-token.expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.token.refresh-token.expiry}")
    private long refreshTokenExpiry;

    public String generateAccessToken(String username, Collection<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiry, ChronoUnit.SECONDS))
                .claims(claims -> {
                    claims.put("roles", roles);
                    claims.put("token_type", accessToken);
                    claims.put("aud", "http://localhost:3000");
                    claims.put("jti", UUID.randomUUID().toString());
                })
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpiry, ChronoUnit.SECONDS))
                .claims(claims -> {
                    claims.put("token_type", refreshToken);
                    claims.put("jti", UUID.randomUUID().toString());
                })
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }

    public boolean validateToken(String token) {
        try {
            decodeToken(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
