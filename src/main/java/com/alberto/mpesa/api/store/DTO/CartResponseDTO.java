package com.alberto.mpesa.api.store.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {

    private Long cartId;
    private List<CartItemDetailDTO> list;
    private BigDecimal total;

}
