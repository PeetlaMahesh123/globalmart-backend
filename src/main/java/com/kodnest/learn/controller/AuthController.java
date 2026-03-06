package com.kodnest.learn.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kodnest.learn.dto.LoginRequest;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.AuthService;

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

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {

        try {

            User user = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            String token = authService.generateToken(user);

            String cookie = "authToken=" + token +
                    "; Path=/" +
                    "; HttpOnly" +
                    "; Secure" +
                    "; SameSite=None" +
                    "; Max-Age=3600";

            response.addHeader(HttpHeaders.SET_COOKIE, cookie);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Login successful");
            body.put("username", user.getUsername());
            body.put("role", user.getRole().name());

            return ResponseEntity.ok(body);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= LOGOUT =================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        String cookie = "authToken=; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=0";

        response.addHeader(HttpHeaders.SET_COOKIE, cookie);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}