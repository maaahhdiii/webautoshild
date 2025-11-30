package com.autoshield.repository;

import com.autoshield.entity.SystemMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SystemMetric entity operations
 */
@Repository
public interface SystemMetricRepository extends JpaRepository<SystemMetric, Long> {
    
    Optional<SystemMetric> findFirstByNodeIdOrderByTimestampDesc(String nodeId);
    
    @Query("SELECT m FROM SystemMetric m WHERE m.nodeId = :nodeId AND m.timestamp >= :since ORDER BY m.timestamp ASC")
    List<SystemMetric> findMetricHistory(@Param("nodeId") String nodeId, @Param("since") LocalDateTime since);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SystemMetric m WHERE m.timestamp < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT AVG(m.cpuPercent) FROM SystemMetric m WHERE m.nodeId = :nodeId AND m.timestamp >= :since")
    Double getAverageCpu(@Param("nodeId") String nodeId, @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(m.ramPercent) FROM SystemMetric m WHERE m.nodeId = :nodeId AND m.timestamp >= :since")
    Double getAverageRam(@Param("nodeId") String nodeId, @Param("since") LocalDateTime since);
}
