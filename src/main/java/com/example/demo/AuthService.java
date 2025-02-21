package com.example.demo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${jwt.token.refresh-token.expiry}")
    private long refreshTokenExpiry;

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public SignInResponseDTO signIn(SignInDTO signInDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInDTO.getUsernameOrEmail(), signInDTO.getPassword())
        );
        CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
        Collection<String> roles = authenticatedUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        String accessToken = jwtService.generateAccessToken(authenticatedUser.getUsername(), roles);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser.getUsername());
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail()
        );
        TokensDTO tokensDTO = new TokensDTO(accessToken, refreshToken);
        return new SignInResponseDTO(userDetailsDTO, tokensDTO);
    }

    public TokensDTO refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtService.extractRefreshTokenFromRequest(request);
        if (!jwtService.validateToken(refreshToken)) {
            return null;
        }
        Jwt decodedToken = jwtService.decodeToken(refreshToken);
        String username = decodedToken.getSubject();
        if (refreshTokenService.isTokenRevoked(refreshToken)) {
            return null;
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Collection<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();
        String newAccessToken = jwtService.generateAccessToken(username, roles);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        refreshTokenService.revokeToken(refreshToken);
        refreshTokenService.saveToken(newRefreshToken);
        Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/auth/refresh");
        refreshTokenCookie.setMaxAge((int) refreshTokenExpiry);
        response.addCookie(refreshTokenCookie);
        return new TokensDTO(newAccessToken, null);
    }

}
