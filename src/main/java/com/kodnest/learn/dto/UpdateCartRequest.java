package com.kodnest.learn.dto;

public class UpdateCartRequest {

    private String username;
    private int productId;
    private int quantity;

    public UpdateCartRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}