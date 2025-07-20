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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;


    //add Product to Cart
    public CartResponseDTO addToCart(CartRequestDTO cartRequest) {

        Cart cart = new Cart();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setCartItems(new HashSet<>());


        for (CartItemDTO item : cartRequest.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product Not found: " + item.getProductId()));

            CartItem cartItem= new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(item.getQuantity());
            cartItem.setCart(cart);
            //Add this cartItem to the cart’s list of items.
            cart.getCartItems().add(cartItem);

        }
        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);

        return mapToResponse(cart);

    }

    //Remove From Cart
    @Transactional
    public CartResponseDTO removeFromCart(Long cartId, Long productId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new IllegalArgumentException("Cart not found with id: " + cartId));

        cart.getCartItems().removeIf(cartItem -> cartItem.getId().equals(productId));
        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }
    
    @Transactional
    public CartResponseDTO updateQuantity(Long cartId, Long productId, int quantity){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new IllegalArgumentException("Cart Not found with id: " + cartId));

        cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getId().equals(productId))
                .findFirst()
                .ifPresent(cartItem -> cartItem.setQuantity(quantity));

        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    //get and clear Cart methods to add

    private BigDecimal calculateTotal(Cart cart){
        return cart.getCartItems().stream()
                .map(i-> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //Converts each CartItem into a CartItemDetailDTO (for the frontend).
    //Packages all into CartResponseDTO
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
