package com.autoshield.services;

import com.autoshield.dto.AlertDto;
import com.autoshield.dto.FirewallRuleDto;
import com.autoshield.dto.SystemMetricDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for communicating with AutoShield Backend API
 */
@Service
@Slf4j
public class BackendService {
    
    @Value("${autoshield.backend.url}")
    private String backendUrl;
    
    @Value("${autoshield.backend.username}")
    private String username;
    
    @Value("${autoshield.backend.password}")
    private String password;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    /**
     * Get recent alerts
     */
    public List<AlertDto> getRecentAlerts(int hours) {
        try {
            String url = backendUrl + "/api/v1/alerts/recent?hours=" + hours;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<List<AlertDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<AlertDto>>() {});
            
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error fetching alerts: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Get all alerts with pagination
     */
    public Map<String, Object> getAlerts(int page, int size) {
        try {
            String url = backendUrl + "/api/v1/alerts?page=" + page + "&size=" + size;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (RestClientException e) {
            log.error("Error fetching paginated alerts: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
    
    /**
     * Get current system metrics
     */
    public SystemMetricDto getCurrentMetrics() {
        try {
            String url = backendUrl + "/api/v1/metrics/current";
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<SystemMetricDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, SystemMetricDto.class);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching current metrics: {}", e.getMessage());
            return createDefaultMetric();
        }
    }
    
    /**
     * Get metrics history
     */
    public List<SystemMetricDto> getMetricHistory(int hours) {
        try {
            String url = backendUrl + "/api/v1/metrics/history?hours=" + hours;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<List<SystemMetricDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<SystemMetricDto>>() {});
            
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error fetching metric history: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Trigger a security scan
     */
    public Map<String, Object> triggerScan(String targetIp, String scanType) {
        try {
            String url = backendUrl + "/api/v1/scan/trigger";
            
            Map<String, String> request = new HashMap<>();
            request.put("targetIp", targetIp);
            request.put("scanType", scanType);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, createHeaders());
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (RestClientException e) {
            log.error("Error triggering scan: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return error;
        }
    }
    
    /**
     * Block an IP address
     */
    public boolean blockIp(String ipAddress, String reason, Integer durationMinutes, Boolean permanent) {
        try {
            String url = backendUrl + "/api/v1/firewall/block";
            
            Map<String, Object> request = new HashMap<>();
            request.put("ipAddress", ipAddress);
            request.put("reason", reason);
            if (durationMinutes != null) request.put("durationMinutes", durationMinutes);
            if (permanent != null) request.put("permanent", permanent);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
            
            ResponseEntity<FirewallRuleDto> response = restTemplate.postForEntity(
                    url, entity, FirewallRuleDto.class);
            
            return response.getStatusCode() == HttpStatus.CREATED;
        } catch (RestClientException e) {
            log.error("Error blocking IP: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Unblock an IP address
     */
    public boolean unblockIp(String ipAddress) {
        try {
            String url = backendUrl + "/api/v1/firewall/unblock/" + ipAddress;
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return true;
        } catch (RestClientException e) {
            log.error("Error unblocking IP: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all firewall rules
     */
    public List<FirewallRuleDto> getFirewallRules() {
        try {
            String url = backendUrl + "/api/v1/firewall/rules";
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<List<FirewallRuleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<FirewallRuleDto>>() {});
            
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Error fetching firewall rules: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Get system health status
     */
    public Map<String, Object> getHealthStatus() {
        try {
            String url = backendUrl + "/api/v1/health";
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);
            
            return response.getBody() != null ? response.getBody() : Collections.emptyMap();
        } catch (RestClientException e) {
            log.error("Error fetching health status: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("message", e.getMessage());
            return error;
        }
    }
    
    /**
     * Test backend connection
     */
    public boolean testConnection() {
        try {
            String url = backendUrl + "/api/v1/health";
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
    
    private SystemMetricDto createDefaultMetric() {
        return SystemMetricDto.builder()
                .cpuPercent(0.0)
                .ramPercent(0.0)
                .diskPercent(0.0)
                .activeThreats(0)
                .build();
    }
}
