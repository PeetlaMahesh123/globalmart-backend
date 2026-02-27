package com.kodnest.learn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kodnest.learn.entity.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Integer> {

    // Fetch cart item for a given userId and productId
    @Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<CartItem> findByUserAndProduct(int userId, int productId);


    // Fetch all cart items with product and image details
    @Query("""
           SELECT c FROM CartItem c
           JOIN FETCH c.product p
           LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId
           WHERE c.user.userId = :userId
           """)
    List<CartItem> findCartItemsWithProductDetails(int userId);


    // Update quantity of a specific cart item
    @Modifying
    @Transactional
    @Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.id = :cartItemId")
    void updateCartItemQuantity(int cartItemId, int quantity);


    // Delete a specific product from cart
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteCartItem(int userId, int productId);


    // Count total quantity of items in cart
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.user.userId = :userId")
    int countTotalItems(int userId);


    // Delete all cart items for a user
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId")
    void deleteAllCartItemsByUserId(int userId);

}