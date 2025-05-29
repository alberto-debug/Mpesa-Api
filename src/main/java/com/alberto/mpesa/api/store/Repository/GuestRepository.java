package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findBySessionId(String id);
}
