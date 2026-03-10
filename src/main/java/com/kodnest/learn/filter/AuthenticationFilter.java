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

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "https://zippy-parfait-f89cac.netlify.app"
    };

    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/auth/login"
    };

    public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
        System.out.println("Authentication Filter Started");
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Always attach CORS headers
        setCORSHeaders(httpRequest, httpResponse);

        String requestURI = httpRequest.getRequestURI();
        logger.info("Request URI: {}", requestURI);

        // Allow preflight requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public endpoints
        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        try {

            String token = getAuthTokenFromCookies(httpRequest);

            if (token == null || !authService.validateToken(token)) {
                sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: Invalid or missing token");
                return;
            }

            String username = authService.extractUsername(token);

            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: User not found");
                return;
            }

            User authenticatedUser = userOptional.get();
            Role role = authenticatedUser.getRole();

            logger.info("Authenticated user: {}, role: {}",
                    authenticatedUser.getUsername(), role);

            if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
                sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden: Admin access required");
                return;
            }

            if (requestURI.startsWith("/api/") && role != Role.CUSTOMER) {
                sendErrorResponse(httpResponse,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden: Customer access required");
                return;
            }

            httpRequest.setAttribute("authenticatedUser", authenticatedUser);

            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Authentication filter error", e);
            sendErrorResponse(httpResponse,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error");
        }
    }

    private void setCORSHeaders(HttpServletRequest request, HttpServletResponse response) {

        String origin = request.getHeader("Origin");

        if (origin != null && Arrays.asList(ALLOWED_ORIGINS).contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        response.setHeader("Access-Control-Allow-Headers", "*");

        response.setHeader("Access-Control-Allow-Credentials", "true");

        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   int statusCode,
                                   String message) throws IOException {

        response.setStatus(statusCode);
        response.getWriter().write(message);
    }

    private String getAuthTokenFromCookies(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
