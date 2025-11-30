package com.autoshield.service;

import com.autoshield.entity.SystemMetric;
import com.autoshield.repository.AlertRepository;
import com.autoshield.repository.SystemMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for collecting and managing system metrics
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetricsCollectionService {
    
    private final ProxmoxApiService proxmoxApiService;
    private final SystemMetricRepository metricRepository;
    private final AlertRepository alertRepository;
    
    @Value("${autoshield.metrics.retention-days:7}")
    private int retentionDays;
    
    /**
     * Scheduled metrics collection every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void collectMetrics() {
        try {
            SystemMetric metric = proxmoxApiService.fetchCurrentMetrics("impaandaa");
            
            // Add active threats count
            long activeThreats = alertRepository.countByStatus(com.autoshield.entity.Alert.AlertStatus.ACTIVE);
            metric.setActiveThreats((int) activeThreats);
            
            metricRepository.save(metric);
            log.debug("Collected metrics: CPU={}%, RAM={}%, Active Threats={}", 
                    metric.getCpuPercent(), metric.getRamPercent(), activeThreats);
            
        } catch (Exception e) {
            log.error("Error collecting metrics: {}", e.getMessage());
        }
    }
    
    /**
     * Get current metrics
     */
    public SystemMetric getCurrentMetrics() {
        return metricRepository.findFirstByNodeIdOrderByTimestampDesc("impaandaa")
                .orElse(createDefaultMetric());
    }
    
    /**
     * Get metrics history
     */
    public List<SystemMetric> getMetricHistory(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return metricRepository.findMetricHistory("impaandaa", since);
    }
    
    /**
     * Scheduled cleanup of old metrics - runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = metricRepository.deleteOlderThan(cutoff);
        log.info("Cleaned up {} old metrics (older than {} days)", deleted, retentionDays);
    }
    
    /**
     * Get average metrics over a period
     */
    public AverageMetrics getAverageMetrics(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        Double avgCpu = metricRepository.getAverageCpu("impaandaa", since);
        Double avgRam = metricRepository.getAverageRam("impaandaa", since);
        
        return new AverageMetrics(
                avgCpu != null ? avgCpu : 0.0,
                avgRam != null ? avgRam : 0.0
        );
    }
    
    private SystemMetric createDefaultMetric() {
        return SystemMetric.builder()
                .nodeId("impaandaa")
                .cpuPercent(0.0)
                .ramPercent(0.0)
                .diskPercent(0.0)
                .networkBytesIn(0L)
                .networkBytesOut(0L)
                .activeThreats(0)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public record AverageMetrics(Double avgCpu, Double avgRam) {}
}
