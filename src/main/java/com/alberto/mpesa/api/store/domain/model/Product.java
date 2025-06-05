package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;

@Entity
@Table(name = "products")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productid;

    private String productName;

    private int quantity;

    private byte price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private HashSet<CartItem> cartItems = new HashSet<>();


}
