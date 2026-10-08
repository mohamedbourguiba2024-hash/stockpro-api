package com.stockpro.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "150 caractères maximum")
        String nom,

        @Size(max = 30, message = "30 caractères maximum")
        String telephone,

        @Email(message = "Email invalide")
        String email,

        @Size(max = 255, message = "255 caractères maximum")
        String adresse) {}