// ProductResponse.java
package com.stockpro.api.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id, String reference, String nom, String description,
        BigDecimal prix, Integer quantite, Integer seuilAlerte, boolean stockBas,
        Long categoryId, String categoryNom,
        Long supplierId, String supplierNom) {}