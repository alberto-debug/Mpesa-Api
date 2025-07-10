package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = "phone_number"))
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "customer")
    private List<Cart> carts;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
}
