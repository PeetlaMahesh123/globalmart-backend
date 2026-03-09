package com.kodnest.learn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Allow preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public APIs
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/logout",
                        "/api/users/register"
                ).permitAll()

                // PRODUCTS accessible to both ADMIN and CUSTOMER
                .requestMatchers("/api/products/**")
                .hasAnyRole("ADMIN", "CUSTOMER")

                // CART accessible to CUSTOMER
                .requestMatchers("/api/cart/**")
                .hasRole("CUSTOMER")

                // ADMIN APIs
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // Everything else must be authenticated
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
