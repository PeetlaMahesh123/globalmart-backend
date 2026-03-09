package com.kodnest.learn.config;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.AuthService;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/users/login",
            "/api/auth/login",
            "/api/auth/logout"
    };

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean isPublic = Arrays.stream(UNAUTHENTICATED_PATHS)
                .anyMatch(path::startsWith);

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        if (token == null || !authService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        User user = authService.getUserFromToken(token);
        request.setAttribute("user", user);

        filterChain.doFilter(request, response);
    }
}
