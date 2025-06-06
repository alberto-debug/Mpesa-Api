package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;



}
