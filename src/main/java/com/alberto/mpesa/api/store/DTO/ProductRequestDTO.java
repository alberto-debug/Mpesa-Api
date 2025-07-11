package com.alberto.mpesa.api.store.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDTO {

    private String productName;
    private int quantity;
    private BigDecimal price;
}
