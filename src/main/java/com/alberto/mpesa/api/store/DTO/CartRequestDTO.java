package com.alberto.mpesa.api.store.DTO;

import com.alberto.mpesa.api.store.domain.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartRequestDTO {

    private List<CartItemDTO> items;
}
