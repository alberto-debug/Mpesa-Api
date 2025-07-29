package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.Services.OrderService;
import com.alberto.mpesa.api.store.domain.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);

            // Create a response object matching what the frontend expects
            Map<String, Object> response = Map.of(
                    "orderId", order.getId(),
                    "paymentStatus", order.getPaymentStatus(),
                    "checkoutRequestId", order.getCheckoutRequestId() != null ? order.getCheckoutRequestId() : "",
                    "total", order.getPrice(),
                    "paymentMethod", order.getPaymentMethod(),
                    "phoneNumber", order.getPhoneNumber() != null ? order.getPhoneNumber() : "",
                    "createdAt", order.getCreatedAt()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error fetching order: " + e.getMessage());
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Order not found"));
        } catch (Exception e) {
            System.err.println("Unexpected error fetching order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PostMapping("/payment-callback")
    public ResponseEntity<String> paymentCallback(@RequestBody Map<String, Object> callbackData) {
        System.out.println("Payment callback received: " + callbackData);
        try {
            orderService.processPaymentCallback(callbackData);
            return ResponseEntity.ok("Callback processed successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid callback data: " + e.getMessage());
            return ResponseEntity.status(400).body("Invalid callback data: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Order not found for callback: " + e.getMessage());
            return ResponseEntity.status(404).body("Order not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Callback processing failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Callback processing failed: " + e.getMessage());
        }
    }
}