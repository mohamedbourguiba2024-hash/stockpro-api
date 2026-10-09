package com.stockpro.api.dto;

import com.stockpro.api.entity.Role;

public record UserResponse(Long id, String nom, String email, Role role) {}