package com.alberto.mpesa.api.store.domain.model;

import com.alberto.mpesa.api.store.domain.model.CartItem;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<CartItem> items;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Increased length to accommodate full JSON responses
    @Column(name = "checkout_request_id", length = 500)
    private String checkoutRequestId;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;
}