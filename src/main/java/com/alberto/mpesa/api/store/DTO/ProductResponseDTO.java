package com.alberto.mpesa.api.store.DTO;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private String productName;
    private int quantity;
    private BigDecimal price;
}
