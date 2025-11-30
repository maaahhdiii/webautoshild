package com.autoshield.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a security scan result
 */
@Entity
@Table(name = "scan_results", indexes = {
    @Index(name = "idx_scan_id", columnList = "scanId"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String scanId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String targetIp;
    
    @Column(nullable = false)
    private String toolUsed;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;
    
    @Column(length = 5000)
    private String findings;
    
    @Column(length = 10000)
    private String rawOutput;
    
    private Integer threatScore;
    
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status == null) {
            status = ScanStatus.PENDING;
        }
    }
    
    public enum ScanStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
