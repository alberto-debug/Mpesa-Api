package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.Role.Role;
import com.alberto.mpesa.api.store.domain.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin findByName(String name);
}
