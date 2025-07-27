package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Services.CartService;
import com.alberto.mpesa.api.store.Services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addTOCart(@RequestBody CartRequestDTO cartRequest){
        CartResponseDTO response = cartService.addToCart(cartRequest);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId){

        CartResponseDTO response = cartService.removeFromCart(cartId, productId);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }


    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateCart(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @PathVariable int quantity
    ){
        CartResponseDTO response = cartService.updateQuantity(cartId, productId, quantity);
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long cartId){

        CartResponseDTO response = cartService.getCart(cartId);

        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable Long cartId){

        CartResponseDTO response = cartService.clearCart(cartId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<BigDecimal> getTotal(@PathVariable Long cartId){

        BigDecimal response = cartService.getCartTotal(cartId);
        
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

}
