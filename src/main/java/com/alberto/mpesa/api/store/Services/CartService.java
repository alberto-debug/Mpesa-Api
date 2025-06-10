package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.*;
import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Cart;
import com.alberto.mpesa.api.store.domain.model.CartItem;
import com.alberto.mpesa.api.store.domain.model.Product;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // 📌 Create a new cart with optional items
    @Transactional
    public CartResponseDTO createCart(CartRequestDTO cartRequestDTO){

        Cart cart =  new Cart();

        List<CartItem>  cartItems = cartRequestDTO.getItems().stream().map(dto ->{
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + dto.getProductId()));

            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setCart(cart);
            return  item;

        }).collect(Collectors.toList());

        cart.getCartItems().addAll(cartItems);
        Cart savedCart = cartRepository.save(cart);
        return null;
    }


    // ✅ Add items to an existing cart
    @Transactional
    public CartResponseDTO addItemsToCart(Long cartId, List<CartItemDTO> itemsToAdd){
        Cart cart  = cartRepository.findById(cartId)
                .orElseThrow(()-> new IllegalArgumentException("Cart not found"));

        for (CartItemDTO dto : itemsToAdd){
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(()-> new IllegalArgumentException("Product not found with id:" + dto.getProductId()));

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            cart.getCartItems().add(item);
        }

        Cart updateCart = cartRepository.save(cart);
        return  null;
    }

    // ✅ View cart details
    public CartResponseDTO viewCartDetails(Long cartId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new IllegalArgumentException("Cart not found with id: " + cartId));
        return null;
    }

    // 🔁Convert Cart to CartResponseDTO
    private CartResponseDTO mapToCArtResponse(Cart cart){
        List<CartItemDetailDTO> items = cart.getCartItems().stream().map(item -> {
            Product product = item.getProduct();
            return new CartItemDetailDTO(
                    product.getProductid(),
                    product.getProductName(),
                    product.getQuantity(),
                    product.getPrice()
            );
        }).collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDTO(cart.getId(), items, total);
    }


}
