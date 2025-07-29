package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Services.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(
            @RequestBody CartRequestDTO cartRequest,
            @RequestParam(required = false) Long cartId) {
        CartResponseDTO response = cartService.addToCart(cartRequest, cartId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        CartResponseDTO response = cartService.removeFromCart(cartId, productId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{cartId}/items/{productId}/quantity")
    public ResponseEntity<CartResponseDTO> updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> body) {
        CartResponseDTO response = cartService.updateQuantity(cartId, productId, body.get("quantity"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long cartId) {
        CartResponseDTO response = cartService.getCart(cartId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable Long cartId) {
        CartResponseDTO response = cartService.clearCart(cartId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<BigDecimal> getTotal(@PathVariable Long cartId) {
        BigDecimal response = cartService.getCartTotal(cartId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<?> checkout(
            @PathVariable Long cartId,
            @RequestBody Map<String, String> body) {
        String phoneNumber = body.get("phoneNumber");
        String paymentMethod = body.get("paymentMethod");
        // Placeholder for M-Pesa payment initialization
        return ResponseEntity.ok(Map.of("message", "Payment initiated for " + paymentMethod +
                (phoneNumber != null ? " to " + phoneNumber : "")));
    }
}