package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
