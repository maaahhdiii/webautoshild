package com.autoshield.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for blocking IP address request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockIpRequest {
    
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", 
             message = "Invalid IP address format")
    private String ipAddress;
    
    @NotBlank(message = "Reason is required")
    @Size(min = 3, max = 500, message = "Reason must be between 3 and 500 characters")
    private String reason;
    
    private Integer durationMinutes;
    
    private Boolean permanent;
}
