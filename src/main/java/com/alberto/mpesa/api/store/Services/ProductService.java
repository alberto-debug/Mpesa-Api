package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO dto){
        Product product = new Product();
        product.setName(dto.getProductName());
        product.setStockQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setExpiryDate(dto.getExpiryDate());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());

        Product saved = productRepository.save(product);
        return new ProductResponseDTO(saved.getId(), saved.getName(), saved.getPrice());
    }


    public List<ProductResponseDTO> listProducts(){
        return  productRepository.findAll()
                .stream()
                .map(product -> new ProductResponseDTO(product.getId(),product.getName(), product.getPrice()))
                .collect(Collectors.toList());
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Product not found"));

        product.setName(dto.getProductName());
        product.setStockQuantity(dto.getQuantity());
        product.setCategory(dto.getCategory());
        product.setExpiryDate(dto.getExpiryDate());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());

        Product updated = productRepository.save(product);
        return new ProductResponseDTO(product.getId(), product.getName(), product.getPrice());
    }

    public void deleteProduct(Long id){
        if (!productRepository.existsById(id)){
            throw new RuntimeException("Product not found with id : " + id);
        }

        productRepository.deleteById(id);

    }

}
