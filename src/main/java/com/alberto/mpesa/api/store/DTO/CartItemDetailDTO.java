package com.alberto.mpesa.api.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDetailDTO {

    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
