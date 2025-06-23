package com.alberto.mpesa.api.store.Services;


import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.IOrderRepository;
import com.alberto.mpesa.api.store.domain.model.Cart;
import com.alberto.mpesa.api.store.domain.model.CartItem;
import com.alberto.mpesa.api.store.domain.model.Order;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class OrderService {

    private final IOrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    public Order checkout(Long cartId){

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new RuntimeException("cart not found with id: " + cartId));

        Set<CartItem> cartItems = cart.getCartItems();
        Order order = new Order();
        order.setItems((List<CartItem>) cartItems);
        order.setPrice(calculateTotal((List<CartItem>) cartItems));

        // Clear cart after successful order creation
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderRepository.save(order);

    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
