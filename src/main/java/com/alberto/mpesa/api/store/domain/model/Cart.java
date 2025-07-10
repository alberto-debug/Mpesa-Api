package com.alberto.mpesa.api.store.domain.model;

import com.alberto.mpesa.api.store.domain.Enums.CartStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //One cart can contain many cart items. get list of all cart items
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> cartItems = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private CartStatus status; // ACTIVE, CHECKED_OUT, CANCELLED

    private BigDecimal total;

    private LocalDateTime createdAt = LocalDateTime.now();

}