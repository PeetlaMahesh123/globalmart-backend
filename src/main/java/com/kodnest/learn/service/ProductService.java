package com.kodnest.learn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kodnest.learn.entity.Category;
import com.kodnest.learn.entity.Product;
import com.kodnest.learn.entity.ProductImage;
import com.kodnest.learn.repository.CategoryRepository;
import com.kodnest.learn.repository.ProductImageRepository;
import com.kodnest.learn.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ✅ Get Products by Category
    public List<Product> getProductsByCategory(String categoryName) {

        if (categoryName != null && !categoryName.isEmpty()) {

            Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);

            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                return productRepository.findByCategory_CategoryId(category.getCategoryId());
            } else {
                throw new RuntimeException("Category not found");
            }

        } else {
            return productRepository.findAll();
        }
    }

    // ✅ Get Product Images by Product ID
    public List<String> getProductImages(Integer productId) {

        List<ProductImage> productImages =
                productImageRepository.findByProduct_ProductId(productId);

        List<String> imageUrls = new ArrayList<>();

        for (ProductImage image : productImages) {
            imageUrls.add(image.getImageUrl());
        }

        return imageUrls;
    }
}