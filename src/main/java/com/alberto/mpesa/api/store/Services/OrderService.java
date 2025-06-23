package com.alberto.mpesa.api.store.Services;


import com.alberto.mpesa.api.store.Repository.IOrderRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService {

    private final IOrderRepository iOrderRepository;
    private final CartService cartService;

    

}
