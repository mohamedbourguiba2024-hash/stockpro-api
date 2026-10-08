package com.stockpro.api.repository;

import com.stockpro.api.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement,Long> {
}
