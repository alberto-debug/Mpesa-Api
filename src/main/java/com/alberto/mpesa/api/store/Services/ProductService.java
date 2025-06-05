package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.DTO.ResponseDTO;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
@AllArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO dto){

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());

        Product saved = productRepository.save(product);
        return new ProductResponseDTO(saved.getProductName(), saved.getQuantity(),saved.getPrice());

    }
}
