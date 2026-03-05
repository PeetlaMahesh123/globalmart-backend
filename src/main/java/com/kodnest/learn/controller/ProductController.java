package com.kodnest.learn.controller;

import com.kodnest.learn.entity.Product;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        // ✅ Get authenticated user from filter
        User user = (User) request.getAttribute("authenticatedUser");

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized"));
        }

        List<Product> products = productService.getProductsByCategory(category);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());

        List<Map<String, Object>> productList = new ArrayList<>();

        for (Product product : products) {

            Map<String, Object> productData = new HashMap<>();

            productData.put("product_id", product.getProductId());
            productData.put("name", product.getName());
            productData.put("description", product.getDescription());
            productData.put("price", product.getPrice());
            productData.put("stock", product.getStock());

            // ✅ IMPORTANT – Add images
            List<String> images = productService.getProductImages(product.getProductId());
            productData.put("images", images != null ? images : new ArrayList<>());

            productList.add(productData);
        }

        response.put("products", productList);

        return ResponseEntity.ok(response);
    }
}