package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Configure allowed origins
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://your-production-domain.com"));

        // Allow cookies and credentials
        config.setAllowCredentials(true);

        // Configure allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Configure allowed headers
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // Add exposed headers (useful for custom headers like tokens)
        config.setExposedHeaders(List.of("Authorization"));

        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;

    }

}
