package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderRepository extends JpaRepository<Order, Long> {
}