// MovementRequest.java
package com.stockpro.api.dto;

import com.stockpro.api.entity.MovementType;
import jakarta.validation.constraints.*;

public record MovementRequest(
        @NotNull(message = "Le produit est obligatoire") Long productId,
        @NotNull(message = "Le type est obligatoire") MovementType type,
        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être supérieure à 0") Integer quantite,
        @Size(max = 500, message = "500 caractères maximum") String commentaire) {}