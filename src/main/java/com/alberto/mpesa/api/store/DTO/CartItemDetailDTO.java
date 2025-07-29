package com.alberto.mpesa.api.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDetailDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private int quantity;
}