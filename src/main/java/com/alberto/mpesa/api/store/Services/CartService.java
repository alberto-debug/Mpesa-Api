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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponseDTO addToCart(CartRequestDTO cartRequest, Long cartId) {
        Cart cart;
        if (cartId != null) {
            cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));
            if (cart.getStatus() != CartStatus.ACTIVE) {
                throw new IllegalStateException("Cannot add to inactive cart");
            }
        } else {
            cart = new Cart();
            cart.setStatus(CartStatus.ACTIVE);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setCartItems(new HashSet<>());
        }

        for (CartItemDTO item : cartRequest.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(cartItem -> cartItem.getProduct().getId().equals(item.getProductId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                int newQuantity = existingItem.get().getQuantity() + item.getQuantity();
                if (product.getStockQuantity() < newQuantity) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
                existingItem.get().setQuantity(newQuantity);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setProduct(product);
                cartItem.setQuantity(item.getQuantity());
                cartItem.setCart(cart);
                cart.getCartItems().add(cartItem);
            }
        }

        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponseDTO removeFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));

        cart.getCartItems().removeIf(cartItem -> cartItem.getProduct().getId().equals(productId));
        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponseDTO updateQuantity(Long cartId, Long productId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found with id: " + cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        Cart finalCart = cart;
        cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(cartItem -> {
                    if (quantity <= 0) {
                        finalCart.getCartItems().remove(cartItem);
                    } else {
                        cartItem.setQuantity(quantity);
                    }
                });

        cart.setTotal(calculateTotal(cart));
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    public CartResponseDTO getCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponseDTO clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        cart.getCartItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cart.setStatus(CartStatus.CANCELLED);
        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    public BigDecimal getCartTotal(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));
        return cart.getTotal();
    }

    private BigDecimal calculateTotal(Cart cart) {
        return cart.getCartItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponseDTO mapToResponse(Cart cart) {
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