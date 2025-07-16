package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.CartItemDTO;
import com.alberto.mpesa.api.store.DTO.CartItemDetailDTO;
import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.Enums.CartStatus;
import com.alberto.mpesa.api.store.domain.model.Cart;
import com.alberto.mpesa.api.store.domain.model.CartItem;
import com.alberto.mpesa.api.store.domain.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartResponseDTO createProduct(CartRequestDTO cartRequest) {

        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setCartItems(new HashSet<>());

        CartItem cartItem = null;
        for (CartItemDTO item : cartRequest.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product Not found: " + item.getProductId()));

            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(item.getQuantity());
            cartItem.setCart(cart);
            cart.getCartItems().add(cartItem);

        }
        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);

        return mapToResponse(cart);

    }
    
    private BigDecimal calculateTotal(Cart cart){
        return cart.getCartItems().stream()
                .map(i-> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponseDTO mapToResponse(Cart cart){
        List<CartItemDetailDTO> items = cart.getCartItems().stream()
                .map(item -> new CartItemDetailDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
        return new CartResponseDTO(cart.getId(), items, cart.getTotal());
    }
}
