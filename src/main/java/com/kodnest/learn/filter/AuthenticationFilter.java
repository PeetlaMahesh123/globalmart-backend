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
import org.springframework.stereotype.Component;

@Component
@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthService authService;
    private final UserRepository userRepository;

    // APIs that do NOT need authentication
    private static final String[] UNAUTHENTICATED_PATHS = {
            "/api/users/register",
            "/api/users/login"
    };

    public AuthenticationFilter(AuthService authService,
                                UserRepository userRepository) {

        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        logger.info("Request URI: {}", requestURI);

        // Allow login & register
        if (Arrays.asList(UNAUTHENTICATED_PATHS)
                .contains(requestURI)) {

            chain.doFilter(request, response);
            return;
        }

        // Allow OPTIONS requests (CORS preflight)
        if (httpRequest.getMethod()
                .equalsIgnoreCase("OPTIONS")) {

            chain.doFilter(request, response);
            return;
        }

        // Get token from cookies
        String token = getAuthTokenFromCookies(httpRequest);

        if (token == null || !authService.validateToken(token)) {

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter()
                    .write("Unauthorized: Invalid or missing token");

            return;
        }

        // Extract username
        String username = authService.extractUsername(token);

        Optional<User> userOptional =
                userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {

            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter()
                    .write("Unauthorized: User not found");

            return;
        }

        User authenticatedUser = userOptional.get();
        Role role = authenticatedUser.getRole();

        logger.info("Authenticated User: {} Role: {}",
                authenticatedUser.getUsername(), role);

        // Admin access control
        if (requestURI.startsWith("/admin/")
                && role != Role.ADMIN) {

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter()
                    .write("Forbidden: Admin access required");

            return;
        }

        // Customer access control
        if (requestURI.startsWith("/api/")
                && role != Role.CUSTOMER) {

            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter()
                    .write("Forbidden: Customer access required");

            return;
        }

        // Attach authenticated user to request
        httpRequest.setAttribute("authenticatedUser",
                authenticatedUser);

        chain.doFilter(request, response);
    }

    private String getAuthTokenFromCookies(
            HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {

            return Arrays.stream(cookies)
                    .filter(cookie ->
                            "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
