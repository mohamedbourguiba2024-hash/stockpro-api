// CategoryRequest.java
package com.stockpro.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "100 caractères maximum")
        String nom) {}