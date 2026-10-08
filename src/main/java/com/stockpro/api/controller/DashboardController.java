package com.stockpro.api.controller;

import com.stockpro.api.dto.DashboardStats;
import com.stockpro.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/stats")
    public DashboardStats stats() {
        return service.stats();
    }
}