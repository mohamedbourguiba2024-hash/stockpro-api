package com.stockpro.api.service;

import com.stockpro.api.dto.DashboardStats;
import com.stockpro.api.dto.DashboardStats.CategoryStock;
import com.stockpro.api.dto.DashboardStats.DailyMovement;
import com.stockpro.api.entity.MovementType;
import com.stockpro.api.entity.Product;
import com.stockpro.api.entity.StockMovement;
import com.stockpro.api.repository.ProductRepository;
import com.stockpro.api.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(29).atStartOfDay();

        // Un jour = une ligne, y compris les jours sans mouvement
        Map<LocalDate, long[]> perDay = new TreeMap<>();
        for (int i = 0; i < 30; i++) {
            perDay.put(today.minusDays(29 - i), new long[]{0, 0});
        }
        for (StockMovement m : movementRepository.findSince(since)) {
            long[] counters = perDay.get(m.getDate().toLocalDate());
            if (counters == null) continue;
            if (m.getType() == MovementType.ENTREE) counters[0] += m.getQuantite();
            else counters[1] += m.getQuantite();
        }
        List<DailyMovement> daily = perDay.entrySet().stream()
                .map(e -> new DailyMovement(e.getKey().toString(), e.getValue()[0], e.getValue()[1]))
                .toList();

        Map<String, Long> byCategory = new TreeMap<>();
        for (Product p : productRepository.findAll()) {
            String cat = p.getCategory() != null ? p.getCategory().getNom() : "Sans catégorie";
            byCategory.merge(cat, (long) p.getQuantite(), Long::sum);
        }
        List<CategoryStock> categories = byCategory.entrySet().stream()
                .map(e -> new CategoryStock(e.getKey(), e.getValue())).toList();

        return new DashboardStats(
                productRepository.totalStockValue(),
                productRepository.count(),
                productRepository.countLowStock(),
                movementRepository.sumQuantiteSince(MovementType.ENTREE, since),
                movementRepository.sumQuantiteSince(MovementType.SORTIE, since),
                daily,
                categories);
    }
}