package com.autoshield.service;

import com.autoshield.dto.UpdateStatusRequest;
import com.autoshield.entity.Alert;
import com.autoshield.entity.Alert.AlertStatus;
import com.autoshield.entity.Alert.Severity;
import com.autoshield.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing security alerts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {
    
    private final AlertRepository alertRepository;
    private final FirewallService firewallService;
    
    /**
     * Get alerts with filtering
     */
    public Page<Alert> getAlerts(Severity severity, AlertStatus status, Pageable pageable) {
        if (severity != null && status != null) {
            return alertRepository.findByStatusAndSeverity(status, severity, pageable);
        } else if (severity != null) {
            return alertRepository.findBySeverity(severity, pageable);
        } else if (status != null) {
            return alertRepository.findByStatus(status, pageable);
        }
        return alertRepository.findAll(pageable);
    }
    
    /**
     * Get alert by ID
     */
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
    }
    
    /**
     * Create new alert with automated threat response
     */
    @Transactional
    public Alert createAlert(String type, Severity severity, String sourceIp, String details) {
        Alert alert = Alert.builder()
                .type(type)
                .severity(severity)
                .sourceIp(sourceIp)
                .details(details)
                .status(AlertStatus.ACTIVE)
                .timestamp(LocalDateTime.now())
                .build();
        
        // AUTOMATED THREAT RESPONSE
        String action = takeAutomatedAction(type, severity, sourceIp);
        if (action != null) {
            alert.setActionTaken(action);
        }
        
        Alert saved = alertRepository.save(alert);
        log.info("🚨 Alert: {} - {} from {} | Action: {}", type, severity, sourceIp, action != null ? action : "None");
        return saved;
    }
    
    /**
     * Take automated action based on threat
     */
    private String takeAutomatedAction(String type, Severity severity, String sourceIp) {
        if (sourceIp == null || sourceIp.isBlank()) {
            return null;
        }
        
        try {
            // CRITICAL threats = Auto-block 24h
            if (severity == Severity.CRITICAL) {
                com.autoshield.dto.BlockIpRequest blockRequest = com.autoshield.dto.BlockIpRequest.builder()
                        .ipAddress(sourceIp)
                        .reason("AUTO-BLOCK: " + type + " (CRITICAL)")
                        .durationMinutes(1440) // 24 hours
                        .permanent(false)
                        .build();
                
                firewallService.blockIp(blockRequest, "AutoShield-AI");
                log.warn("🔴 CRITICAL THREAT BLOCKED: {} - Type: {}", sourceIp, type);
                return "IP BLOCKED for 24h (CRITICAL threat)";
            }
            
            // HIGH severity brute force/exploits = Auto-block 4h
            if (severity == Severity.HIGH && (
                type.toUpperCase().contains("BRUTE") || 
                type.toUpperCase().contains("EXPLOIT") ||
                type.toUpperCase().contains("ATTACK"))) {
                
                com.autoshield.dto.BlockIpRequest blockRequest = com.autoshield.dto.BlockIpRequest.builder()
                        .ipAddress(sourceIp)
                        .reason("AUTO-BLOCK: " + type + " (HIGH)")
                        .durationMinutes(240) // 4 hours
                        .permanent(false)
                        .build();
                
                firewallService.blockIp(blockRequest, "AutoShield-AI");
                log.warn("🟠 HIGH THREAT BLOCKED: {} - Type: {}", sourceIp, type);
                return "IP BLOCKED for 4h (HIGH severity attack)";
            }
            
            // MEDIUM threats = Warning only
            if (severity == Severity.MEDIUM) {
                log.info("⚠️ Medium threat detected from {} - Monitoring", sourceIp);
                return "Threat logged - Under monitoring";
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to take automated action for {}: {}", sourceIp, e.getMessage());
            return "Auto-block failed: " + e.getMessage();
        }
        
        return null;
    }
    
    /**
     * Update alert status
     */
    @Transactional
    public Alert updateStatus(Long id, UpdateStatusRequest request) {
        Alert alert = getAlertById(id);
        alert.setStatus(request.getStatus());
        
        if (request.getNotes() != null) {
            String updatedDetails = alert.getDetails() + "\n[Updated] " + request.getNotes();
            alert.setDetails(updatedDetails);
            
            // Extract action from notes if it contains "AI Automated Response:"
            if (request.getNotes().contains("AI Automated Response:")) {
                String action = request.getNotes().replace("AI Automated Response: ", "");
                alert.setActionTaken(action);
            }
        }
        
        Alert updated = alertRepository.save(alert);
        log.info("Updated alert {} status to {}", id, request.getStatus());
        return updated;
    }
    
    /**
     * Get recent alerts for dashboard
     */
    public List<Alert> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertRepository.findRecentAlerts(since);
    }
    
    /**
     * Get active critical alerts count
     */
    public long getActiveCriticalCount() {
        return alertRepository.countByStatusAndSeverity(AlertStatus.ACTIVE, Severity.CRITICAL);
    }
    
    /**
     * Get active alerts count
     */
    public long getActiveAlertsCount() {
        return alertRepository.countByStatus(AlertStatus.ACTIVE);
    }
}
