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
            .cors() // Enable CORS
            .and()
            .csrf().disable() // Disable CSRF for APIs
            .authorizeHttpRequests(auth -> auth

                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Allow login & register without authentication
                .requestMatchers(
                        "/api/users/login",
                        "/api/users/register"
                ).permitAll()

                // All other APIs require authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
