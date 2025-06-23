package com.alberto.mpesa.api.store.Services;


import com.alberto.mpesa.api.store.Repository.IOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private IOrderRepository iOrderRepository;

    @Autowired
    private CartService cartService;



}
