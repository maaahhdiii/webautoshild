package com.autoshield.controller;

import com.autoshield.dto.UpdateStatusRequest;
import com.autoshield.entity.Alert;
import com.autoshield.entity.Alert.AlertStatus;
import com.autoshield.entity.Alert.Severity;
import com.autoshield.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for security alerts
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Security alerts management")
public class AlertController {
    
    private final AlertService alertService;
    
    @GetMapping
    @Operation(summary = "Get all alerts with filtering and pagination")
    public Page<Alert> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return alertService.getAlerts(severity, status, pageable);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        Alert alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update alert status")
    public ResponseEntity<Alert> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        Alert updated = alertService.updateStatus(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent alerts for dashboard")
    public ResponseEntity<java.util.List<Alert>> getRecentAlerts(
            @RequestParam(defaultValue = "24") int hours) {
        
        return ResponseEntity.ok(alertService.getRecentAlerts(hours));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get alert statistics")
    public ResponseEntity<java.util.Map<String, Long>> getAlertStats() {
        return ResponseEntity.ok(java.util.Map.of(
            "active", alertService.getActiveAlertsCount(),
            "critical", alertService.getActiveCriticalCount()
        ));
    }
}
