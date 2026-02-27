package com.kodnest.learn.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kodnest.learn.entity.JWTToken;

public interface JWTTokenRepository extends JpaRepository<JWTToken, Integer> {

    // Find token by user ID
    @Query("SELECT t FROM JWTToken t WHERE t.user.userId = :userId")
    JWTToken findByUserId(@Param("userId") int userId);

    // ✅ ADD THIS METHOD
    Optional<JWTToken> findByToken(String token);
}