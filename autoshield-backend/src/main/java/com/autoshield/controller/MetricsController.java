package com.autoshield.controller;

import com.autoshield.entity.SystemMetric;
import com.autoshield.service.MetricsCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for system metrics
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "System metrics and monitoring")
public class MetricsController {
    
    private final MetricsCollectionService metricsService;
    
    @GetMapping("/current")
    @Operation(summary = "Get current system metrics")
    public ResponseEntity<SystemMetric> getCurrentMetrics() {
        return ResponseEntity.ok(metricsService.getCurrentMetrics());
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get metrics history")
    public ResponseEntity<List<SystemMetric>> getMetricHistory(
            @RequestParam(defaultValue = "24") int hours) {
        
        return ResponseEntity.ok(metricsService.getMetricHistory(hours));
    }
    
    @GetMapping("/average")
    @Operation(summary = "Get average metrics over a period")
    public ResponseEntity<MetricsCollectionService.AverageMetrics> getAverageMetrics(
            @RequestParam(defaultValue = "24") int hours) {
        
        return ResponseEntity.ok(metricsService.getAverageMetrics(hours));
    }
}
