package com.stockpro.api.repository;

import com.stockpro.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByReference(String reference);
    boolean existsByReference(String reference);
    boolean existsByReferenceAndIdNot(String reference, Long id);

    @Query("select p from Product p where p.quantite <= p.seuilAlerte order by p.quantite asc")
    List<Product> findLowStock();
}