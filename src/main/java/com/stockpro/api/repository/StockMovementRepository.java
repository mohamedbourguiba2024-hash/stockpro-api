package com.stockpro.api.repository;

import com.stockpro.api.entity.MovementType;
import com.stockpro.api.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {
    @Query("select coalesce(sum(m.quantite), 0) from StockMovement m " +
            "where m.type = :type and m.date >= :since")
    long sumQuantiteSince(@Param("type") MovementType type, @Param("since") LocalDateTime since);

    @Query("select m from StockMovement m where m.date >= :since")
    List<StockMovement> findSince(@Param("since") LocalDateTime since);
}