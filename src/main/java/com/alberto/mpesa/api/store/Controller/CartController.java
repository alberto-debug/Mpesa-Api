package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Services.CartService;
import com.alberto.mpesa.api.store.Services.OrderService;
import com.alberto.mpesa.api.store.domain.model.Order;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
@CrossOrigin(origins = "*") // Add this if you need CORS support
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(
            @RequestBody CartRequestDTO cartRequest,
            @RequestParam(required = false) Long cartId) {
        try {
            CartResponseDTO response = cartService.addToCart(cartRequest, cartId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            System.err.println("State error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        try {
            CartResponseDTO response = cartService.removeFromCart(cartId, productId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Cart or product not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error removing from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{cartId}/items/{productId}/quantity")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");
            if (quantity == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            CartResponseDTO response = cartService.updateQuantity(cartId, productId, quantity);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error updating quantity: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("Error updating quantity: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long cartId) {
        try {
            CartResponseDTO response = cartService.getCart(cartId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Cart not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error getting cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable Long cartId) {
        try {
            CartResponseDTO response = cartService.clearCart(cartId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Cart not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<BigDecimal> getTotal(@PathVariable Long cartId) {
        try {
            BigDecimal response = cartService.getCartTotal(cartId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Cart not found for total: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error getting cart total: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<?> checkout(
            @PathVariable Long cartId,
            @RequestBody Map<String, String> body) {
        try {
            String phoneNumber = body.get("phoneNumber");
            String paymentMethod = body.get("paymentMethod");

            // Log the checkout request for debugging
            System.out.println("Checkout request - CartId: " + cartId +
                    ", PaymentMethod: " + paymentMethod +
                    ", PhoneNumber: " + phoneNumber);

            Order order = orderService.checkout(cartId, phoneNumber, paymentMethod);

            // Create a comprehensive response object
            Map<String, Object> response = Map.of(
                    "orderId", order.getId(),
                    "cartId", cartId,
                    "paymentStatus", order.getPaymentStatus(),
                    "checkoutRequestId", order.getCheckoutRequestId() != null ? order.getCheckoutRequestId() : "",
                    "total", order.getPrice(),
                    "paymentMethod", order.getPaymentMethod(),
                    "phoneNumber", order.getPhoneNumber() != null ? order.getPhoneNumber() : "",
                    "message", getCheckoutMessage(order)
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.err.println("Validation error during checkout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "errorType", "VALIDATION_ERROR"
                    ));
        } catch (IllegalStateException e) {
            System.err.println("State error during checkout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "errorType", "STATE_ERROR"
                    ));
        } catch (RuntimeException e) {
            System.err.println("Payment error during checkout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of(
                            "error", "Payment processing failed: " + e.getMessage(),
                            "errorType", "PAYMENT_ERROR"
                    ));
        } catch (Exception e) {
            System.err.println("Unexpected error during checkout: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "An unexpected error occurred during checkout",
                            "errorType", "SYSTEM_ERROR"
                    ));
        }
    }

    private String getCheckoutMessage(Order order) {
        if ("cash".equals(order.getPaymentMethod())) {
            return "Cash payment completed successfully";
        } else if ("mpesa".equals(order.getPaymentMethod())) {
            if ("PENDING".equals(order.getPaymentStatus())) {
                return "M-Pesa payment initiated. Please complete the payment on your phone.";
            } else if ("COMPLETED".equals(order.getPaymentStatus())) {
                return "M-Pesa payment completed successfully";
            } else if ("FAILED".equals(order.getPaymentStatus())) {
                return "M-Pesa payment failed. Please try again.";
            }
        }
        return "Order processed";
    }
}