package com.kodnest.learn.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kodnest.learn.entity.CartItem;
import com.kodnest.learn.entity.Product;
import com.kodnest.learn.entity.ProductImage;
import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.CartRepository;
import com.kodnest.learn.repository.ProductRepository;
import com.kodnest.learn.repository.ProductImageRepository;
import com.kodnest.learn.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;


    // Get total cart item count
    public int getCartItemCount(int userId) {
        return cartRepository.countTotalItems(userId);
    }


    // Add item to cart
    public void addToCart(int userId, int productId, int quantity) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + productId));

        Optional<CartItem> existingItem =
                cartRepository.findByUserAndProduct(userId, productId);

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartRepository.save(cartItem);

        } else {

            CartItem newItem = new CartItem(user, product, quantity);
            cartRepository.save(newItem);
        }
    }


    // Get cart items with full details
    public Map<String, Object> getCartItems(int userId) {

        List<CartItem> cartItems =
                cartRepository.findCartItemsWithProductDetails(userId);

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        response.put("username", user.getUsername());
        response.put("role", user.getRole().toString());

        List<Map<String, Object>> products = new ArrayList<>();

        double overallTotalPrice = 0;

        for (CartItem cartItem : cartItems) {

            Map<String, Object> productDetails = new HashMap<>();

            Product product = cartItem.getProduct();

            List<ProductImage> images =
                    productImageRepository.findByProduct_ProductId(
                            product.getProductId());

            String imageUrl = null;

            if (images != null && !images.isEmpty()) {
                imageUrl = images.get(0).getImageUrl();
            } else {
                imageUrl = "default-image-url";
            }

            productDetails.put("product_id", product.getProductId());
            productDetails.put("image_url", imageUrl);
            productDetails.put("name", product.getName());
            productDetails.put("description", product.getDescription());
            productDetails.put("price_per_unit", product.getPrice());
            productDetails.put("quantity", cartItem.getQuantity());

            double totalPrice =
                    cartItem.getQuantity() * product.getPrice().doubleValue();

            productDetails.put("total_price", totalPrice);

            products.add(productDetails);

            overallTotalPrice += totalPrice;
        }

        Map<String, Object> cart = new HashMap<>();
        cart.put("products", products);
        cart.put("overall_total_price", overallTotalPrice);

        response.put("cart", cart);

        return response;
    }


    // Update cart item quantity
    public void updateCartItemQuantity(int userId, int productId, int quantity) {

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<CartItem> existingItem =
                cartRepository.findByUserAndProduct(userId, productId);

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            if (quantity <= 0) {
                deleteCartItem(userId, productId);
            } else {
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
            }
        }
    }


    // Delete cart item
    public void deleteCartItem(int userId, int productId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        cartRepository.deleteCartItem(userId, productId);
    }

}