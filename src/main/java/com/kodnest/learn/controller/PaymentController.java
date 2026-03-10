package com.kodnest.learn.controller;

import com.kodnest.learn.entity.User;
import com.kodnest.learn.service.PaymentService;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /*
    ------------------------------------
    CREATE ORDER
    ------------------------------------
    */
    @PostMapping("/create")
    public ResponseEntity<String> createPaymentOrder(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {

            User user = (User) request.getAttribute("authenticatedUser");

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            BigDecimal totalAmount =
                    new BigDecimal(requestBody.get("totalAmount").toString());

            String orderId =
                    paymentService.createOrder(user.getUserId(), totalAmount);

            return ResponseEntity.ok(orderId);

        } catch (RazorpayException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request");

        }
    }

    /*
    ------------------------------------
    VERIFY PAYMENT
    ------------------------------------
    */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {

            User user = (User) request.getAttribute("authenticatedUser");

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated");
            }

            int userId = user.getUserId();

            String razorpayOrderId =
                    (String) requestBody.get("razorpayOrderId");

            String razorpayPaymentId =
                    (String) requestBody.get("razorpayPaymentId");

            String razorpaySignature =
                    (String) requestBody.get("razorpaySignature");

            boolean verified =
                    paymentService.verifyPayment(
                            razorpayOrderId,
                            razorpayPaymentId,
                            razorpaySignature,
                            userId
                    );

            if (verified) {

                return ResponseEntity.ok("Payment successful");

            } else {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Payment verification failed");
            }

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying payment");
        }
    }
}
