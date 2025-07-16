package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Each/many cart item belongs to one cart
    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    //Each cart item references a product.
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

}