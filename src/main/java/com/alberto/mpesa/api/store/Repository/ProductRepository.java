package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
