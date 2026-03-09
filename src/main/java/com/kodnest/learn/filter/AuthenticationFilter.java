package com.kodnest.learn.filter;

import com.kodnest.learn.entity.Role;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.UserRepository;
import com.kodnest.learn.service.AuthService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(urlPatterns = {"/api/*","/admin/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    // Allow both localhost and netlify
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "https://zippy-parfait-f89cac.netlify.app"
    };

    // Public APIs
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/me",
            "/api/auth/logout",
            "/api/products"
    };

    public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        System.out.println("Authentication Filter Started");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String origin = httpRequest.getHeader("Origin");

        setCORSHeaders(httpResponse, origin);

        logger.info("Request URI: {}", requestURI);

        // Allow OPTIONS requests
        if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Allow public paths
        if (isPublicPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        try {

            String token = getAuthTokenFromCookies(httpRequest);

            if (token == null || !authService.validateToken(token)) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            String username = authService.extractUsername(token);

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }

            User authenticatedUser = userOptional.get();
            Role role = authenticatedUser.getRole();

            logger.info("Authenticated user: {} Role: {}", username, role);

            // Role validation
            if (requestURI.startsWith("/admin") && role != Role.ADMIN) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                return;
            }

            // Attach user to request
            httpRequest.setAttribute("authenticatedUser", authenticatedUser);

            chain.doFilter(request, response);

        } catch (Exception e) {

            logger.error("Authentication error", e);

            sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal Server Error");

        }
    }

    // Check public URLs
    private boolean isPublicPath(String uri) {

        for (String path : PUBLIC_PATHS) {

            if (uri.startsWith(path)) {
                return true;
            }

        }

        return false;
    }

    // Set CORS headers
    private void setCORSHeaders(HttpServletResponse response, String origin) {

        if (origin != null && Arrays.asList(ALLOWED_ORIGINS).contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

    // Extract JWT token
    private String getAuthTokenFromCookies(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("authToken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }

        return null;
    }

    // Send error response
    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");

    }
}
