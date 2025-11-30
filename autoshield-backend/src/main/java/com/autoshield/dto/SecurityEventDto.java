package com.autoshield.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for security events received from Python AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDto {
    
    private String eventType;
    private String severity;
    private String sourceIp;
    private String targetIp;
    private String description;
    private String actionTaken;
    private LocalDateTime timestamp;
    private String details;
}
