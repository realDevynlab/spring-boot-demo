package com.example.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class TokenValidationFilter extends OncePerRequestFilter {

    @Value("${security.permit-all}")
    private String[] permitAllEndpoints;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip filter if authentication already exists in SecurityContext
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        String bearerToken = request.getHeader(AUTHORIZATION);
        String token = extractBearerToken(bearerToken);
        if (!StringUtils.hasText(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization token is missing");
            return;
        }
        try {
            Jwt decodedToken = jwtService.decodeToken(token);
            if (refreshTokenService.isTokenRevoked(token)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                return;
            }
            String username = decodedToken.getSubject();
            Set<String> roles = decodedToken.getClaim("roles");
            if (roles == null) roles = Set.of();
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
            // Set SecurityContext authentication (only if SecurityContext is empty)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            // Clear the SecurityContext and send custom error response
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        for (String permittedEndpoint : permitAllEndpoints) {
            if (pathMatcher.match(permittedEndpoint, requestURI)) {
                return true; // Bypass filter for matching endpoints
            }
        }
        return false; // Apply filter for other endpoints
    }

    private String extractBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

}
