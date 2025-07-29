package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCheckoutRequestId(String checkoutRequestId);
}