package com.kodnest.learn.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kodnest.learn.dto.LoginRequest;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://zippy-parfait-f89cac.netlify.app"
        },
        allowCredentials = "true"
)
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
            cookie.setSecure(false);   // IMPORTANT for localhost
            cookie.setPath("/");
            cookie.setMaxAge(3600);

            response.addCookie(cookie);

            Map<String,Object> responseBody = new HashMap<>();
            responseBody.put("message","Login successful");
            responseBody.put("username",user.getUsername());
            responseBody.put("role",user.getRole().name());

            return ResponseEntity.ok(responseBody);

        } catch(RuntimeException e){

            return ResponseEntity
                    .status(401)
                    .body(Map.of("error",e.getMessage()));

        }
    }

    // CURRENT USER
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request){

        User user = (User) request.getAttribute("authenticatedUser");

        if(user == null){
            return ResponseEntity.ok(Map.of("username","Guest"));
        }

        Map<String,Object> response = new HashMap<>();
        response.put("username",user.getUsername());
        response.put("role",user.getRole().name());

        return ResponseEntity.ok(response);
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of("message","Logout successful")
        );
    }
}
