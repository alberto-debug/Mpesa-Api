package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.Repository.CartRepository;
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


    // ✅ Create product
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());

        Product saved = productRepository.save(product);
        return new ProductResponseDTO(saved.getProductid(), saved.getProductName(), saved.getPrice());
    }

    // ✅ List all products
    public List<ProductResponseDTO> findAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(p -> new ProductResponseDTO(p.getProductid(), p.getProductName(), p.getPrice()))
                .collect(Collectors.toList());
    }

    // ✅ Update product by ID
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        product.setProductName(dto.getProductName());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());

        Product updated = productRepository.save(product);
        return new ProductResponseDTO(updated.getProductid(), updated.getProductName(), updated.getPrice());
    }

    // ✅ Delete product by ID
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);
    }

}
