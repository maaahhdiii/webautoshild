package com.autoshield.controller;

import com.autoshield.dto.SecurityEventDto;
import com.autoshield.entity.Alert.Severity;
import com.autoshield.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving webhooks from external systems
 */
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook endpoints for external integrations")
public class WebhookController {
    
    private final AlertService alertService;
    
    @PostMapping("/python")
    @Operation(summary = "Receive security events from Python AI service")
    public ResponseEntity<Void> receivePythonEvent(@RequestBody SecurityEventDto event) {
        log.info("Received security event: {} - {} from {}", 
                event.getEventType(), event.getSeverity(), event.getSourceIp());
        
        try {
            // Convert severity string to enum
            Severity severity = parseSeverity(event.getSeverity());
            
            // Create alert
            alertService.createAlert(
                    event.getEventType(),
                    severity,
                    event.getSourceIp(),
                    event.getDetails() != null ? event.getDetails() : event.getDescription()
            );
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/test")
    @Operation(summary = "Test webhook endpoint")
    public ResponseEntity<String> testWebhook(@RequestBody(required = false) String payload) {
        log.info("Test webhook called with payload: {}", payload);
        return ResponseEntity.ok("Webhook is working");
    }
    
    private Severity parseSeverity(String severity) {
        if (severity == null) return Severity.MEDIUM;
        
        return switch (severity.toUpperCase()) {
            case "CRITICAL", "CRIT" -> Severity.CRITICAL;
            case "HIGH" -> Severity.HIGH;
            case "MEDIUM", "MED" -> Severity.MEDIUM;
            case "LOW" -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }
}
