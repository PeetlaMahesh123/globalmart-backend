package com.kodnest.learn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kodnest.learn.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // ✅ Find products by categoryId (derived query)
    List<Product> findByCategory_CategoryId(Integer categoryId);

    // ✅ Custom query to get category name by productId
    @Query("SELECT p.category.categoryName FROM Product p WHERE p.productId = :productId")
    String findCategoryNameByProductId(Integer productId);
}