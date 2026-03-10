package com.kodnest.learn.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kodnest.learn.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://zippy-parfait-f89cac.netlify.app"
        },
        allowCredentials = "true"
)
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {

        try {

            User user = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            String token = authService.generateToken(user);

            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);       // Required for HTTPS (Render)
            cookie.setPath("/");
            cookie.setMaxAge(3600);

            response.addCookie(cookie);

            response.addHeader(
                    "Set-Cookie",
                    String.format(
                            "authToken=%s; HttpOnly; Secure; Path=/; Max-Age=3600; SameSite=None",
                            token
                    )
            );

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("role", user.getRole().name());
            responseBody.put("username", user.getUsername());

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response) {

        try {

            User user = (User) request.getAttribute("authenticatedUser");

            authService.logout(user);

            Cookie cookie = new Cookie("authToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logout successful");

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Logout failed");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
