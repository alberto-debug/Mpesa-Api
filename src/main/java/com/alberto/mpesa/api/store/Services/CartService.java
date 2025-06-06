package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.CartItemDTO;
import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.DTO.ResponseDTO;
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



}
