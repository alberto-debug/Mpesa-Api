package com.alberto.mpesa.api.store.DTO;

import lombok.*;

import java.math.BigDecimal;

//CartItemDTO (for display)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {

    private Long productId;
    private int quantity;

}
