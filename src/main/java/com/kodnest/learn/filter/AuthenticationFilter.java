package com.kodnest.learn.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.kodnest.learn.entity.Role;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.UserRepository;
import com.kodnest.learn.service.AuthService;

import java.io.IOException;
import java.util.Optional;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
        System.out.println("Filter Started.");
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        setCORSHeaders(httpResponse);

        String requestURI = httpRequest.getRequestURI();
        logger.info("Request URI: {}", requestURI);

        // ✅ Allow public endpoints
        if (requestURI.startsWith("/api/users/register") ||
            requestURI.startsWith("/api/auth/login")) {

            chain.doFilter(request, response);
            return;
        }

        // ✅ Allow preflight requests
        if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        // ✅ Validate token
        String token = getAuthTokenFromCookies(httpRequest);

        if (token == null || !authService.validateToken(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Invalid or missing token");
            return;
        }

        String username = authService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: User not found");
            return;
        }

        User authenticatedUser = userOptional.get();
        Role role = authenticatedUser.getRole();

        // Role-based check
        if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("Forbidden: Admin access required!");
            return;
        }

        httpRequest.setAttribute("authenticatedUser", authenticatedUser);

        chain.doFilter(request, response);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    private String getAuthTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}