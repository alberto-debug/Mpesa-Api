package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.Role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface roleRepository extends JpaRepository<Role, Long> {
}
