// ProductRequest.java
package com.stockpro.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "La référence est obligatoire")
        @Size(max = 50, message = "50 caractères maximum")
        String reference,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "150 caractères maximum")
        String nom,

        @Size(max = 1000, message = "1000 caractères maximum")
        String description,

        @NotNull(message = "Le prix est obligatoire")
        @PositiveOrZero(message = "Le prix doit être positif")
        BigDecimal prix,

        @NotNull(message = "La quantité est obligatoire")
        @PositiveOrZero(message = "La quantité doit être positive")
        Integer quantite,

        @NotNull(message = "Le seuil d'alerte est obligatoire")
        @PositiveOrZero(message = "Le seuil doit être positif")
        Integer seuilAlerte,

        Long categoryId,
        Long supplierId) {}