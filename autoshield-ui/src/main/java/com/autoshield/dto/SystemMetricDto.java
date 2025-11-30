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
public class SystemMetricDto {
    private Long id;
    private LocalDateTime timestamp;
    private String nodeId;
    private Double cpuPercent;
    private Double ramPercent;
    private Double diskPercent;
    private Long networkBytesIn;
    private Long networkBytesOut;
    private Integer activeThreats;
}
