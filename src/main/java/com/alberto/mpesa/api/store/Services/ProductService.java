package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.Repository.CategoryRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Category;
import com.alberto.mpesa.api.store.domain.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = new Product();
        Category category = findOrCreateCategory(dto.getCategory());

        product.setName(dto.getProductName());
        product.setStockQuantity(dto.getQuantity());
        product.setCategory(category);
        product.setExpiryDate(dto.getExpiryDate());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        Category category = findOrCreateCategory(dto.getCategory());

        product.setName(dto.getProductName());
        product.setStockQuantity(dto.getQuantity());
        product.setCategory(category);
        product.setExpiryDate(dto.getExpiryDate());
        product.setImageUrl(dto.getImageUrl());
        product.setPrice(dto.getPrice());

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private Category findOrCreateCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        return categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    return categoryRepository.save(newCategory);
                });
    }

    private ProductResponseDTO mapToResponse(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getPrice(),
                product.getExpiryDate(),
                product.getImageUrl()
        );
    }
}