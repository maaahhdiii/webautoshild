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
            
            log.debug("Fetching metrics from Proxmox: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PVEAPIToken=" + apiToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                
                if (data == null) {
                    log.error("Proxmox API returned null data - check node name '{}'", nodeId);
                    return createDefaultMetric(nodeId);
                }
                
                // Extract RAM usage from memory object
                Double ramPercent = 0.0;
                if (data.get("memory") instanceof Map) {
                    Map<String, Object> memory = (Map<String, Object>) data.get("memory");
                    double used = extractDouble(memory, "used");
                    double total = extractDouble(memory, "total");
                    if (total > 0) {
                        ramPercent = (used / total) * 100;
                    }
                }
                
                // Extract disk usage from rootfs object
                Double diskPercent = 0.0;
                if (data.get("rootfs") instanceof Map) {
                    Map<String, Object> rootfs = (Map<String, Object>) data.get("rootfs");
                    double used = extractDouble(rootfs, "used");
                    double total = extractDouble(rootfs, "total");
                    if (total > 0) {
                        diskPercent = (used / total) * 100;
                    }
                }
                
                double cpuPercent = extractDouble(data, "cpu") * 100;
                
                log.debug("Proxmox metrics parsed: CPU={}%, RAM={}%, Disk={}%", cpuPercent, ramPercent, diskPercent);
                
                return SystemMetric.builder()
                        .nodeId(nodeId)
                        .cpuPercent(cpuPercent)
                        .ramPercent(ramPercent)
                        .diskPercent(diskPercent)
                        .networkBytesIn(extractLong(data, "netin"))
                        .networkBytesOut(extractLong(data, "netout"))
                        .activeThreats(0)
                        .build();
            }
            
            log.warn("Failed to fetch Proxmox metrics - status code: {}", response.getStatusCode());
            return createDefaultMetric(nodeId);
            
        } catch (RestClientException e) {
            log.error("⚠ Proxmox API connection failed: {} - Check URL: {} and API token", e.getMessage(), proxmoxUrl);
            return createDefaultMetric(nodeId);
        } catch (Exception e) {
            log.error("⚠ Unexpected error fetching Proxmox metrics: {}", e.getMessage(), e);
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
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
    
    private Long extractLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
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
