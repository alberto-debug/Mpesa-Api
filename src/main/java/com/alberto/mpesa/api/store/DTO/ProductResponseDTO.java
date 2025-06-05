package com.alberto.mpesa.api.store.DTO;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String productName;
    private BigDecimal price;
}
