package com.kodnest.learn.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    // ================= CONSTRUCTORS =================

    public ProductImage() {
        // Default constructor
    }

    public ProductImage(Integer imageId, Product product, String imageUrl) {
        this.imageId = imageId;
        this.product = product;
        this.imageUrl = imageUrl;
    }

    public ProductImage(Product product, String imageUrl) {
        this.product = product;
        this.imageUrl = imageUrl;
    }

    // ================= GETTERS =================

    public Integer getImageId() {
        return imageId;
    }

    public Product getProduct() {
        return product;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // ================= SETTERS =================

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}