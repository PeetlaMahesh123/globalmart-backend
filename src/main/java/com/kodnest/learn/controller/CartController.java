package com.kodnest.learn.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.kodnest.learn.entity.User;
import com.kodnest.learn.repository.UserRepository;
import com.kodnest.learn.service.CartService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;


    // =====================================================
    // GET CART COUNT
    // URL: /api/cart/items/count?username=Harish
    // =====================================================
    @GetMapping("/items/count")
    public ResponseEntity<Integer> getCartItemCount(
            @RequestParam String username) {

        System.out.println("Fetching cart count for username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + username));

        int count = cartService.getCartItemCount(user.getUserId());

        return ResponseEntity.ok(count);
    }


    // =====================================================
    // GET CART ITEMS
    // URL: /api/cart/items?username=Harish
    // =====================================================
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems(
            @RequestParam String username) {

        System.out.println("Fetching cart items for username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + username));

        Map<String, Object> cart =
                cartService.getCartItems(user.getUserId());

        return ResponseEntity.ok(cart);
    }


    // =====================================================
    // ADD TO CART
    // URL: /api/cart/add
    // BODY:
    // {
    //   "username": "Harish",
    //   "productId": 27,
    //   "quantity": 1
    // }
    // =====================================================
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestBody Map<String, Object> request) {

        try {

            String username =
                    request.get("username").toString();

            int productId =
                    Integer.parseInt(
                            request.get("productId").toString());

            int quantity =
                    Integer.parseInt(
                            request.get("quantity").toString());

            System.out.println("Add to cart → username="
                    + username + " productId=" + productId);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            cartService.addToCart(
                    user.getUserId(),
                    productId,
                    quantity
            );

            return ResponseEntity.ok("Product added to cart");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding to cart");
        }
    }


    // =====================================================
    // UPDATE CART
    // =====================================================
    @PutMapping("/update")
    public ResponseEntity<String> updateCart(
            @RequestBody Map<String, Object> request) {

        String username =
                request.get("username").toString();

        int productId =
                Integer.parseInt(
                        request.get("productId").toString());

        int quantity =
                Integer.parseInt(
                        request.get("quantity").toString());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        cartService.updateCartItemQuantity(
                user.getUserId(),
                productId,
                quantity
        );

        return ResponseEntity.ok("Cart updated");
    }


    // =====================================================
    // DELETE CART ITEM
    // =====================================================
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCartItem(
            @RequestBody Map<String, Object> request) {

        String username =
                request.get("username").toString();

        int productId =
                Integer.parseInt(
                        request.get("productId").toString());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        cartService.deleteCartItem(
                user.getUserId(),
                productId
        );

        return ResponseEntity.ok("Item deleted");
    }

}