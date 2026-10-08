package com.stockpro.api.dto;

public record AuthResponse(String token, String email, String role) {}