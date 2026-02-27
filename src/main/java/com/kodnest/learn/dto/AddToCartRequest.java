package com.kodnest.learn.dto;

public class AddToCartRequest {

    private String username;
    private Integer productId;
    private Integer quantity;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}