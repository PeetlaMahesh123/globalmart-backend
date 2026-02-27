package com.kodnest.learn.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kodnest.learn.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // ✅ Find category by name
    Optional<Category> findByCategoryName(String categoryName);
}