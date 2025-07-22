package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.Services.CartService;
import com.alberto.mpesa.api.store.Services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

}
