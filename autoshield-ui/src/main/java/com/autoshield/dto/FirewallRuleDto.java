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
public class FirewallRuleDto {
    private Long id;
    private String ipAddress;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean active;
    private String createdBy;
}
