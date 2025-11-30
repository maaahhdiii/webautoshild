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
     * Create new alert
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
        
        Alert saved = alertRepository.save(alert);
        log.info("Created alert: {} - {} from {}", type, severity, sourceIp);
        return saved;
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
