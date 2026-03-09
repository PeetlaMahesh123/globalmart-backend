package com.kodnest.learn.controller;

import com.kodnest.learn.entity.Product;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        try {

            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                authenticatedUser = new User();
                authenticatedUser.setUsername("Guest");
            }

            List<Product> products = productService.getProductsByCategory(category);

            Map<String, Object> response = new HashMap<>();

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", authenticatedUser.getUsername());

            if (authenticatedUser.getRole() != null) {
                userInfo.put("role", authenticatedUser.getRole().name());
            } else {
                userInfo.put("role", "CUSTOMER");
            }

            response.put("user", userInfo);

            List<Map<String, Object>> productList = new ArrayList<>();

            for (Product product : products) {

                Map<String, Object> productDetails = new HashMap<>();

                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price", product.getPrice());
                productDetails.put("stock", product.getStock());

                List<String> images = productService.getProductImages(product.getProductId());

                productDetails.put("images", images);

                productList.add(productDetails);
            }

            response.put("products", productList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
