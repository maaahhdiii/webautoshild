package com.autoshield.controller;

import com.autoshield.dto.ScanRequest;
import com.autoshield.dto.ScanResponse;
import com.autoshield.entity.ScanResult;
import com.autoshield.repository.ScanResultRepository;
import com.autoshield.service.PythonAiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for security scans
 */
@RestController
@RequestMapping("/api/v1/scan")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Scans", description = "Security scanning operations")
public class ScanController {
    
    private final PythonAiClient pythonAiClient;
    private final ScanResultRepository scanResultRepository;
    
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger a security scan")
    public ResponseEntity<ScanResponse> triggerScan(@Valid @RequestBody ScanRequest request) {
        log.info("Triggering {} scan for {}", request.getScanType(), request.getTargetIp());
        
        try {
            // Generate scan ID
            String scanId = UUID.randomUUID().toString();
            
            // Create initial scan result record
            ScanResult scanResult = ScanResult.builder()
                    .scanId(scanId)
                    .targetIp(request.getTargetIp())
                    .toolUsed("nmap_" + request.getScanType())
                    .status(ScanResult.ScanStatus.PENDING)
                    .timestamp(LocalDateTime.now())
                    .build();
            scanResultRepository.save(scanResult);
            
            // Call Python AI to execute scan (async)
            ScanResponse response = pythonAiClient.executeScan(request.getTargetIp(), request.getScanType());
            
            // Update scan ID if Python returns different one
            if (response.getScanId() != null) {
                scanResult.setScanId(response.getScanId());
                scanResult.setStatus(ScanResult.ScanStatus.IN_PROGRESS);
                scanResultRepository.save(scanResult);
            }
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error triggering scan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ScanResponse.builder()
                            .status("ERROR")
                            .message("Failed to trigger scan: " + e.getMessage())
                            .targetIp(request.getTargetIp())
                            .build());
        }
    }
    
    @GetMapping("/{scanId}")
    @Operation(summary = "Get scan result by scan ID")
    public ResponseEntity<ScanResult> getScanResult(@PathVariable String scanId) {
        return scanResultRepository.findByScanId(scanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/history")
    @Operation(summary = "Get scan history")
    public ResponseEntity<java.util.List<ScanResult>> getScanHistory(
            @RequestParam(required = false) String targetIp) {
        
        if (targetIp != null) {
            return ResponseEntity.ok(scanResultRepository.findByTargetIp(targetIp));
        }
        return ResponseEntity.ok(scanResultRepository.findAll());
    }
}
