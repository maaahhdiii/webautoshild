package com.autoshield.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing system metrics collected from Proxmox
 */
@Entity
@Table(name = "system_metrics", indexes = {
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_node_id", columnList = "nodeId")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String nodeId;
    
    private Double cpuPercent;
    
    private Double ramPercent;
    
    private Double diskPercent;
    
    private Long networkBytesIn;
    
    private Long networkBytesOut;
    
    private Integer activeThreats;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
