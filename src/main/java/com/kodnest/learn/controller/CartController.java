package com.kodnest.learn.controller;

import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.CartService;
import com.kodnest.learn.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    // Get cart item count
    @GetMapping("/items/count")
    public ResponseEntity<Integer> getCartItemCount(@RequestParam String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int count = cartService.getCartItemCount(user.getUserId());

        return ResponseEntity.ok(count);
    }

    // Get all cart items
    @GetMapping("/items")
    public ResponseEntity<Map<String,Object>> getCartItems(@RequestParam String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String,Object> cartItems = cartService.getCartItems(user.getUserId());

        return ResponseEntity.ok(cartItems);
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestBody Map<String,Object> request){

        String username = (String) request.get("username");
        int productId = (int) request.get("productId");

        int quantity = request.containsKey("quantity")
                ? (int) request.get("quantity")
                : 1;

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        cartService.addToCart(user.getUserId(),productId,quantity);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Update cart quantity
    @PutMapping("/update")
    public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String,Object> request){

        String username = (String) request.get("username");
        int productId = (int) request.get("productId");
        int quantity = (int) request.get("quantity");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        cartService.updateCartItemQuantity(user.getUserId(),productId,quantity);

        return ResponseEntity.ok().build();
    }

    // Delete cart item
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String,Object> request){

        String username = (String) request.get("username");
        int productId = (int) request.get("productId");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        cartService.deleteCartItem(user.getUserId(),productId);

        return ResponseEntity.noContent().build();
    }
}
