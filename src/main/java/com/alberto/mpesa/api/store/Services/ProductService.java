package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public class ProductService {

    @Autowired
    private ProductRepository productRepository;


    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO productRequestDTO){


    }
}
