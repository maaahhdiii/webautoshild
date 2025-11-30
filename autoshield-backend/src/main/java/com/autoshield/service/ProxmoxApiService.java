package com.autoshield.service;

import com.autoshield.dto.HealthStatus;
import com.autoshield.entity.SystemMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Service for communicating with Proxmox API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProxmoxApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${proxmox.api.url}")
    private String proxmoxUrl;
    
    @Value("${proxmox.api.token}")
    private String apiToken;
    
    /**
     * Fetch current system metrics from Proxmox
     */
    public SystemMetric fetchCurrentMetrics(String nodeId) {
        try {
            String url = proxmoxUrl + "/api2/json/nodes/" + nodeId + "/status";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PVEAPIToken=" + apiToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                
                return SystemMetric.builder()
                        .nodeId(nodeId)
                        .cpuPercent(extractDouble(data, "cpu") * 100)
                        .ramPercent(calculatePercentage(data, "memory", "maxmem"))
                        .diskPercent(calculatePercentage(data, "rootfs", "maxrootfs"))
                        .networkBytesIn(extractLong(data, "netin"))
                        .networkBytesOut(extractLong(data, "netout"))
                        .activeThreats(0)
                        .build();
            }
            
            log.warn("Failed to fetch Proxmox metrics");
            return createDefaultMetric(nodeId);
            
        } catch (RestClientException e) {
            log.error("Error fetching Proxmox metrics: {}", e.getMessage());
            return createDefaultMetric(nodeId);
        }
    }
    
    /**
     * Check health status of Proxmox API
     */
    public HealthStatus.ServiceHealth checkHealth() {
        long startTime = System.currentTimeMillis();
        try {
            String url = proxmoxUrl + "/api2/json/version";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PVEAPIToken=" + apiToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return HealthStatus.ServiceHealth.builder()
                        .status("UP")
                        .message("Proxmox API is accessible")
                        .responseTimeMs(responseTime)
                        .build();
            }
            
            return HealthStatus.ServiceHealth.builder()
                    .status("DOWN")
                    .message("Unexpected response from Proxmox")
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthStatus.ServiceHealth.builder()
                    .status("DOWN")
                    .message("Proxmox API unavailable: " + e.getMessage())
                    .responseTimeMs(responseTime)
                    .build();
        }
    }
    
    private Double extractDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    private Long extractLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
    
    private Double calculatePercentage(Map<String, Object> data, String usedKey, String maxKey) {
        double used = extractDouble(data, usedKey);
        double max = extractDouble(data, maxKey);
        if (max > 0) {
            return (used / max) * 100;
        }
        return 0.0;
    }
    
    private SystemMetric createDefaultMetric(String nodeId) {
        return SystemMetric.builder()
                .nodeId(nodeId)
                .cpuPercent(0.0)
                .ramPercent(0.0)
                .diskPercent(0.0)
                .networkBytesIn(0L)
                .networkBytesOut(0L)
                .activeThreats(0)
                .build();
    }
}
