package com.alberto.mpesa.api.store.DTO;

import com.alberto.mpesa.api.store.domain.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductRequestDTO {

    private String productName;
    private int quantity;
    private Category category;
    private BigDecimal price;
    private LocalDate expiryDate;
    private String imageUrl;
}
