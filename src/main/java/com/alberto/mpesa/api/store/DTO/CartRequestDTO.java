package com.alberto.mpesa.api.store.DTO;


import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.List;

//Incoming cart creation/update
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDTO {

    private CustomerDTO customer;
    private List<CartItemDTO> items;
}
