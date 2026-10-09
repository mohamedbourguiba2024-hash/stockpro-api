package com.stockpro.api.dto;

import com.stockpro.api.entity.Role;
import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "150 caractères maximum") String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide") String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Entre 8 et 100 caractères") String password,

        @NotNull(message = "Le rôle est obligatoire") Role role) {}