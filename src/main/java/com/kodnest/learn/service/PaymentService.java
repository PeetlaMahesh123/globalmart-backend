package com.kodnest.learn.service;

import com.kodnest.learn.entity.CartItem;
import com.kodnest.learn.entity.Order;
import com.kodnest.learn.entity.OrderItem;
import com.kodnest.learn.entity.OrderStatus;
import com.kodnest.learn.repository.CartRepository;
import com.kodnest.learn.repository.OrderItemRepository;
import com.kodnest.learn.repository.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public PaymentService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * Create Razorpay Order
     */
    @Transactional
    public String createOrder(int userId,
                              BigDecimal totalAmount,
                              List<OrderItem> cartItems) throws RazorpayException {

        RazorpayClient razorpayClient =
                new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount",
                totalAmount.multiply(BigDecimal.valueOf(100)).intValue());

        orderRequest.put("currency", "INR");

        orderRequest.put("receipt",
                "txn_" + System.currentTimeMillis());

        // Create Razorpay Order
        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(orderRequest);

        // Save order in database
        Order order = new Order();

        order.setOrderId(razorpayOrder.get("id")); // Razorpay order ID
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return razorpayOrder.get("id");
    }


    /**
     * Verify Razorpay Payment
     */
    @Transactional
    public boolean verifyPayment(String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String razorpaySignature,
                                 int userId) {

        try {

            JSONObject attributes = new JSONObject();

            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            // Verify signature
            boolean isSignatureValid =
                    com.razorpay.Utils.verifyPaymentSignature(
                            attributes,
                            razorpayKeySecret
                    );

            if (!isSignatureValid) {
                return false;
            }

            // Fetch order
            Order order = orderRepository.findById(razorpayOrderId)
                    .orElseThrow(() ->
                            new RuntimeException("Order not found"));

            // Update order status
            order.setStatus(OrderStatus.SUCCESS);
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            // Fetch cart items
            List<CartItem> cartItems =
                    cartRepository.findCartItemsWithProductDetails(userId);

            // Save order items
            for (CartItem cartItem : cartItems) {

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);

                orderItem.setProductId(
                        cartItem.getProduct().getProductId());

                orderItem.setQuantity(
                        cartItem.getQuantity());

                orderItem.setPricePerUnit(
                        cartItem.getProduct().getPrice());

                orderItem.setTotalPrice(
                        cartItem.getProduct().getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                cartItem.getQuantity()
                                        )
                                )
                );

                orderItemRepository.save(orderItem);
            }

            // Clear cart AFTER successful payment
            cartRepository.deleteAllCartItemsByUserId(userId);

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    /**
     * Save Order Items manually (optional utility)
     */
    @Transactional
    public void saveOrderItems(String orderId,
                               List<OrderItem> items) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        for (OrderItem item : items) {

            item.setOrder(order);

            orderItemRepository.save(item);
        }
    }
}
