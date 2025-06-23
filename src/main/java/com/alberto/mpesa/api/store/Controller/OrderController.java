package com.alberto.mpesa.api.store.Controller;


import com.alberto.mpesa.api.store.Services.OrderService;
import com.alberto.mpesa.api.store.domain.model.Order;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam Long cartId){
        Order order = orderService.checkout(cartId);

        return ResponseEntity.ok(order);
    }

}
