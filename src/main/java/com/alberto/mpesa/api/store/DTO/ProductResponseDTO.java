package com.alberto.mpesa.api.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String productName;
    private int quantity;
    private String category;
    private BigDecimal price;
    private LocalDate expiryDate;
    private String imageUrl;
}