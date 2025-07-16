package com.alberto.mpesa.api.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

//with product info
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDetailDTO {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;

}
