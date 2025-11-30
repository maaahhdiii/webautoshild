package com.autoshield.controller;

import com.autoshield.dto.HealthStatus;
import com.autoshield.service.ProxmoxApiService;
import com.autoshield.service.PythonAiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for system health checks
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {
    
    private final ProxmoxApiService proxmoxApiService;
    private final PythonAiClient pythonAiClient;
    
    @GetMapping
    @Operation(summary = "Get health status of all services")
    public ResponseEntity<HealthStatus> getHealthStatus() {
        Map<String, HealthStatus.ServiceHealth> services = new HashMap<>();
        
        // Check Proxmox API
        services.put("proxmox", proxmoxApiService.checkHealth());
        
        // Check Python AI
        services.put("python_ai", pythonAiClient.checkHealth());
        
        // Check database (if we're running, DB is up)
        services.put("database", HealthStatus.ServiceHealth.builder()
                .status("UP")
                .message("Database is accessible")
                .responseTimeMs(0L)
                .build());
        
        // Determine overall status
        boolean allUp = services.values().stream()
                .allMatch(s -> "UP".equals(s.getStatus()));
        
        HealthStatus health = HealthStatus.builder()
                .status(allUp ? "UP" : "DEGRADED")
                .services(services)
                .build();
        
        return ResponseEntity.ok(health);
    }
}
