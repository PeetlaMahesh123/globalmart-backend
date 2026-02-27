package com.kodnest.learn.controller;


import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // React Vite default port
public class UserController {

    private final UserService userService;

    // Constructor Injection (Best Practice)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        try {
            User registeredUser = userService.registerUser(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "User registered successfully",
                            "user", registeredUser
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}