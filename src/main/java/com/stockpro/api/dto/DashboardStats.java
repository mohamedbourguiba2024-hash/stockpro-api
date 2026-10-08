// DashboardStats.java
package com.stockpro.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(
        BigDecimal valeurTotaleStock,
        long nombreProduits,
        long produitsSousSeuil,
        long entrees30Jours,
        long sorties30Jours,
        List<DailyMovement> mouvementsParJour,
        List<CategoryStock> stockParCategorie) {

    public record DailyMovement(String date, long entrees, long sorties) {}
    public record CategoryStock(String categorie, long quantite) {}
}