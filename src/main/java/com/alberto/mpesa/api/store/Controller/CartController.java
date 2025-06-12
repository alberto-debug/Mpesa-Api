package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.CartItemDTO;
import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {


    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ✅ Guest: Create cart with items (checkout simulation)
    @PostMapping
    public ResponseEntity<CartResponseDTO> createCart(@RequestBody CartRequestDTO cartRequestDTO) {
        return ResponseEntity.ok(cartService.createCart(cartRequestDTO));
    }

    // ✅ Guest: Add items to existing cart
    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDTO> addItemsToCart(@PathVariable Long cartId, @RequestBody List<CartItemDTO> items) {
        return ResponseEntity.ok(cartService.addItemsToCart(cartId, items));
    }

    // ✅ Guest: View their cart
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }
}
