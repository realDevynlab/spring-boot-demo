package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

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

    private String generateToken(Authentication auth, String tokenType, long expiry) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(auth.getName())
                .issuedAt(now)
                .expiresAt(now.plus(expiry, ChronoUnit.SECONDS))
                .claim("token_type", tokenType);
        if (this.accessToken.equals(tokenType)) {
            claimsBuilder.claim("roles", auth.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet()));
        }
        JwtClaimsSet claimsSet = claimsBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String getAccessToken(Authentication auth) {
        return generateToken(auth, accessToken, accessTokenExpiry);
    }

    public String getRefreshToken(Authentication auth) {
        return generateToken(auth, refreshToken, refreshTokenExpiry);
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

}
