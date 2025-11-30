package com.autoshield.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for scan response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    
    private String scanId;
    private String status;
    private String message;
    private String targetIp;
    private String estimatedCompletionTime;
}
