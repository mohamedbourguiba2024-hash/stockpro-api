package com.stockpro.api.repository;

import com.stockpro.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByReference(String reference);
    boolean existsByReference(String reference);
}