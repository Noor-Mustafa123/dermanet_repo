package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.AnalyticsResponse;
import io.github.nomus.dermanet.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        AnalyticsResponse response = analyticsService.getAnalyticsData();
        return ResponseEntity.ok(response);
    }
}
