package com.autoshield.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    private Long id;
    private LocalDateTime timestamp;
    private String severity;
    private String type;
    private String sourceIp;
    private String status;
    private String actionTaken;
    private String details;
}
