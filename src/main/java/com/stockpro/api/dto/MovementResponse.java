// MovementResponse.java
package com.stockpro.api.dto;

import com.stockpro.api.entity.MovementType;
import java.time.LocalDateTime;

public record MovementResponse(
        Long id, Long productId, String productReference, String productNom,
        MovementType type, Integer quantite, LocalDateTime date,
        String commentaire, String userEmail, Integer stockApres) {}