package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<CartItem> items;

    private BigDecimal price;

    private LocalDateTime createdAt;

    private String paymentMethod;

    private String phoneNumber;
}