package com.autoshield.service;

import com.autoshield.dto.HealthStatus;
import com.autoshield.dto.ScanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for communicating with Python AI backend
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PythonAiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${python.ai.url}")
    private String pythonUrl;
    
    /**
     * Trigger a security scan
     */
    public ScanResponse executeScan(String targetIp, String scanType) {
        try {
            String url = pythonUrl + "/api/v1/scan";
            
            Map<String, String> request = new HashMap<>();
            request.put("target_ip", targetIp);
            request.put("scan_type", scanType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // Accept both 200 OK and 202 Accepted status codes
            if ((response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) 
                && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return ScanResponse.builder()
                        .scanId((String) body.get("scan_id"))
                        .status((String) body.get("status"))
                        .message((String) body.get("message"))
                        .targetIp(targetIp)
                        .estimatedCompletionTime(String.valueOf(body.get("estimated_time")))
                        .build();
            }
            
            throw new RuntimeException("Failed to execute scan: " + response.getStatusCode());
            
        } catch (RestClientException e) {
            log.error("Error calling Python AI service: {}", e.getMessage());
            throw new RuntimeException("Python AI service unavailable", e);
        }
    }
    
    /**
     * Block an IP address via Python AI
     */
    public boolean blockIp(String ipAddress, String reason) {
        try {
            String url = pythonUrl + "/api/v1/firewall/block";
            
            Map<String, String> request = new HashMap<>();
            request.put("ip_address", ipAddress);
            request.put("reason", reason);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (RestClientException e) {
            log.error("Error blocking IP via Python AI: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Unblock an IP address
     */
    public boolean unblockIp(String ipAddress) {
        try {
            String url = pythonUrl + "/api/v1/firewall/unblock/" + ipAddress;
            
            restTemplate.delete(url);
            return true;
            
        } catch (RestClientException e) {
            log.error("Error unblocking IP via Python AI: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check health status of Python AI service
     */
    public HealthStatus.ServiceHealth checkHealth() {
        long startTime = System.currentTimeMillis();
        try {
            String url = pythonUrl + "/api/v1/health";
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return HealthStatus.ServiceHealth.builder()
                        .status("UP")
                        .message("Python AI service is healthy")
                        .responseTimeMs(responseTime)
                        .build();
            }
            
            return HealthStatus.ServiceHealth.builder()
                    .status("DOWN")
                    .message("Unexpected response from Python AI")
                    .responseTimeMs(responseTime)
                    .build();
                    
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthStatus.ServiceHealth.builder()
                    .status("DOWN")
                    .message("Python AI service unavailable: " + e.getMessage())
                    .responseTimeMs(responseTime)
                    .build();
        }
    }
}
