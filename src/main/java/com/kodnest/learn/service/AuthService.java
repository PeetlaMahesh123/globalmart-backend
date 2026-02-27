package com.kodnest.learn.service;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import java.security.Key;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kodnest.learn.entity.JWTToken;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.JWTTokenRepository;
import com.kodnest.learn.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

    private final Key SIGNING_KEY;

    private final UserRepository userRepository;
    private final JWTTokenRepository jwtTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Injecting jwt.secret from application.properties
    @Autowired
    public AuthService(UserRepository userRepository,
                       JWTTokenRepository jwtTokenRepository,
                       @Value("${jwt.secret}") String jwtSecret) {

        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();

        // Ensure the key length is at least 64 bytes for HS512
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException(
                "JWT_SECRET in application.properties must be at least 64 bytes long for HS512."
            );
        }

        this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ================= AUTHENTICATE USER =================
    public User authenticate(String username, String password) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    // ================= GENERATE TOKEN =================
    public String generateToken(User user) {

        String token;
        LocalDateTime now = LocalDateTime.now();

        JWTToken existingToken = jwtTokenRepository.findByUserId(user.getUserId());

        if (existingToken != null && now.isBefore(existingToken.getExpiresAt())) {
            token = existingToken.getToken();
        } else {

            token = generateNewToken(user);

            if (existingToken != null) {
                jwtTokenRepository.delete(existingToken);
            }

            saveToken(user, token);
        }

        return token;
    }

    // ================= CREATE NEW JWT =================
    private String generateNewToken(User user) {

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 5))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // ================= SAVE TOKEN TO DB =================
    public void saveToken(User user, String token) {

        JWTToken jwtToken = new JWTToken(
                user,
                token,
                LocalDateTime.now().plusHours(1)
        );

        jwtTokenRepository.save(jwtToken);
    }
 // ================= VALIDATE TOKEN =================
    public boolean validateToken(String token) {
        try {
            System.err.println("VALIDATING TOKEN...");

            // Parse and validate the token (signature + expiration)
            Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token);

            // Check if the token exists in the database and is not expired
            Optional<JWTToken> jwtToken = jwtTokenRepository.findByToken(token);

            if (jwtToken.isPresent()) {
                System.err.println("Token Expiry: " + jwtToken.get().getExpiresAt());
                System.err.println("Current Time: " + LocalDateTime.now());

                return jwtToken.get()
                        .getExpiresAt()
                        .isAfter(LocalDateTime.now());
            }

            return false;

        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // ================= EXTRACT USERNAME =================
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}