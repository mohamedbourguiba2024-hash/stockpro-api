package com.stockpro.api.repository;

import com.stockpro.api.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByReference(String reference);
    boolean existsByReference(String reference);
    boolean existsByReferenceAndIdNot(String reference, Long id);

    @Query("select p from Product p where p.quantite <= p.seuilAlerte order by p.quantite asc")
    List<Product> findLowStock();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(p.prix * p.quantite), 0) from Product p")
    BigDecimal totalStockValue();

    @Query("select count(p) from Product p where p.quantite <= p.seuilAlerte")
    long countLowStock();
}