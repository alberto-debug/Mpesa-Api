package com.alberto.mpesa.api.store.Repository;

import com.alberto.mpesa.api.store.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
