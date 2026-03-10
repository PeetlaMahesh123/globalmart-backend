package com.kodnest.learn.filter;

import com.kodnest.learn.entity.Role;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.UserRepository;
import com.kodnest.learn.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout",
            "/api/products"
    };

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (isPublic(uri)) {
            chain.doFilter(request, response);
            return;
        }

        try {

            String token = getTokenFromCookies(request);

            if (token == null || !authService.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = authService.extractUsername(token);

            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User user = userOpt.get();

            request.setAttribute("authenticatedUser", user);

            chain.doFilter(request, response);

        } catch (Exception e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        }
    }

    private boolean isPublic(String uri) {

        for (String path : PUBLIC_PATHS) {

            if (uri.startsWith(path)) {
                return true;
            }

        }

        return false;
    }

    private String getTokenFromCookies(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if ("authToken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }

        return null;
    }
}
