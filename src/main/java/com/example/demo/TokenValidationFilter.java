package com.example.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            /* if (refreshTokenService.isTokenRevoked(token)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                return;
            } */
            String username = decodedToken.getSubject();
            log.info("User '{}' authenticated.", username);
            Collection<String> roles = decodedToken.getClaim("roles");
            log.info("Roles: {}", roles);
            if (roles == null) roles = Set.of();
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
            log.info("Authorities: {}", authorities);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        for (String permittedEndpoint : permitAllEndpoints) {
            if (pathMatcher.match(permittedEndpoint, requestURI)) {
                return true;
            }
        }
        return false;
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
