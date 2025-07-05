package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

   @ManyToOne
   @JoinColumn(name = "product_id")
    private Product product;

   private int quantity;


}
