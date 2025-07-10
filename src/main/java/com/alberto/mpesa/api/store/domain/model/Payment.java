package com.alberto.mpesa.api.store.domain.model;

import com.alberto.mpesa.api.store.domain.Enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // MPESA, CASH, CARD

    private BigDecimal amount;

    private LocalDateTime paidAt = LocalDateTime.now();

    private String transactionId; // for M-Pesa or card
}
