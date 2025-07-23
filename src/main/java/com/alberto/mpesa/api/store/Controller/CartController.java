package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Services.CartService;
import com.alberto.mpesa.api.store.Services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<CartResponseDTO> addTOCart(@RequestBody CartRequestDTO cartRequest){
        CartResponseDTO response = cartService.addToCart(cartRequest);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId){

        CartResponseDTO response = cartService.removeFromCart(cartId, productId);

        return ResponseEntity.ok(response);
    }

}
