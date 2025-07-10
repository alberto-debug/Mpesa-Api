package com.alberto.mpesa.api.store.DTO;


import com.alberto.mpesa.api.store.domain.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


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
