package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.LinkOption;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}
