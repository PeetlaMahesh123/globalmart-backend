package com.kodnest.learn.filter;

import com.kodnest.learn.entity.Role;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.UserRepository;
import com.kodnest.learn.service.AuthService;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
public class AuthenticationFilter implements Filter {

    private final AuthService authService;
    private final UserRepository userRepository;

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/auth/login",
            "/api/users/register"
    };

    public AuthenticationFilter(AuthService authService,
                                UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        System.out.println("Authentication Filter Loaded");
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 🔥 Always set CORS headers FIRST
        setCORSHeaders(httpResponse);

        // 🔥 Allow preflight
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String requestURI = httpRequest.getRequestURI();

        // Allow login and register without authentication
        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Get token from cookies
        String token = getAuthTokenFromCookies(httpRequest);

        if (token == null || !authService.validateToken(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        // Extract username from token
        String username = authService.extractUsername(token);

        Optional<User> userOptional =
                userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"User not found\"}");
            return;
        }

        User authenticatedUser = userOptional.get();

        // 🔥 Restrict only admin URLs
        if (requestURI.startsWith("/admin/")
                && authenticatedUser.getRole() != Role.ADMIN) {

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden\"}");
            return;
        }

        // Attach user to request
        httpRequest.setAttribute("authenticatedUser",
                authenticatedUser);

        chain.doFilter(request, response);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials",
                "true");
    }

    private String getAuthTokenFromCookies(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> "authToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}