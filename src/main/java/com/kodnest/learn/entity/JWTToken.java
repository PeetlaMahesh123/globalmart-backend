package com.kodnest.learn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_tokens")
public class JWTToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Specifies that the tokenId will be auto-generated.
    private Integer tokenId; 
    // Stores the unique identifier for each token.

    @ManyToOne 
    // Establishes a Many-to-one relationship with the User entity.
    @JoinColumn(name = "user_id", nullable = false) 
    // Links the token to a specific user in the Users table.
    private User user; 
    // Represents the user associated with the token.

    @Column(nullable = false) 
    // Ensures that the token cannot be null.
    private String token; 
    // Stores the JWT token string.

    @Column(nullable = false) 
    // Ensures that the expiration time cannot be null.
    private LocalDateTime expiresAt; 
    // Stores the expiration time of the token.

    // Default constructor (Required by JPA)
    public JWTToken() {
    }

    // Constructor without tokenId (Recommended for auto-generated ID)
    public JWTToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Constructor with tokenId
    public JWTToken(Integer tokenId, User user, String token, LocalDateTime expiresAt) {
        this.tokenId = tokenId;
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}