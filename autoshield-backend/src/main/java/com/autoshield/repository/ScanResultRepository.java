package com.autoshield.repository;

import com.autoshield.entity.ScanResult;
import com.autoshield.entity.ScanResult.ScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ScanResult entity operations
 */
@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
    
    Optional<ScanResult> findByScanId(String scanId);
    
    List<ScanResult> findByTargetIp(String targetIp);
    
    List<ScanResult> findByStatus(ScanStatus status);
    
    @Query("SELECT s FROM ScanResult s WHERE s.timestamp >= :since ORDER BY s.timestamp DESC")
    List<ScanResult> findRecentScans(@Param("since") LocalDateTime since);
    
    @Query("SELECT s FROM ScanResult s WHERE s.threatScore >= :minScore ORDER BY s.threatScore DESC")
    List<ScanResult> findHighThreatScans(@Param("minScore") Integer minScore);
}
