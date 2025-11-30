package com.autoshield.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for scan request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    
    @NotBlank(message = "Target IP is required")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}(?:/[0-9]{1,2})?$", 
             message = "Invalid IP address or CIDR notation")
    private String targetIp;
    
    @NotBlank(message = "Scan type is required")
    @Pattern(regexp = "^(quick|full|vulnerability)$", 
             message = "Scan type must be: quick, full, or vulnerability")
    private String scanType;
    
    private String description;
}
