package com.autoshield.repository;

import com.autoshield.entity.Alert;
import com.autoshield.entity.Alert.AlertStatus;
import com.autoshield.entity.Alert.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Alert entity operations
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);
    
    Page<Alert> findBySeverity(Severity severity, Pageable pageable);
    
    Page<Alert> findByStatusAndSeverity(AlertStatus status, Severity severity, Pageable pageable);
    
    List<Alert> findTop10ByOrderByTimestampDesc();
    
    @Query("SELECT a FROM Alert a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = :status AND a.severity = :severity")
    long countByStatusAndSeverity(@Param("status") AlertStatus status, @Param("severity") Severity severity);
    
    long countByStatus(AlertStatus status);
}
