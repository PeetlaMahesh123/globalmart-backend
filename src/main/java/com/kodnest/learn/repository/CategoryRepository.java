package com.kodnest.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kodnest.learn.entity.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String categoryName);
}
