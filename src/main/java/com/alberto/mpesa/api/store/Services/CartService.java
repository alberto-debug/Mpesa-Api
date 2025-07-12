package com.alberto.mpesa.api.store.Services;


import com.alberto.mpesa.api.store.DTO.CartRequestDTO;
import com.alberto.mpesa.api.store.DTO.CartResponseDTO;
import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartResponseDTO createProduct(CartRequestDTO cartRequestDTO){

        return  null;

    }

}
