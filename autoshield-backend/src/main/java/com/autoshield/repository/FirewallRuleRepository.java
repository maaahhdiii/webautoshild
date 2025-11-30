package com.autoshield.repository;

import com.autoshield.entity.FirewallRule;
import com.autoshield.entity.FirewallRule.FirewallAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FirewallRule entity operations
 */
@Repository
public interface FirewallRuleRepository extends JpaRepository<FirewallRule, Long> {
    
    Optional<FirewallRule> findByIpAddressAndActive(String ipAddress, Boolean active);
    
    List<FirewallRule> findByActive(Boolean active);
    
    List<FirewallRule> findByActionAndActive(FirewallAction action, Boolean active);
    
    @Query("SELECT r FROM FirewallRule r WHERE r.active = true AND (r.expiresAt IS NULL OR r.expiresAt > :now)")
    List<FirewallRule> findAllActiveNonExpired(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM FirewallRule r WHERE r.active = true AND r.expiresAt IS NOT NULL AND r.expiresAt <= :now")
    List<FirewallRule> findExpiredRules(@Param("now") LocalDateTime now);
    
    boolean existsByIpAddressAndActive(String ipAddress, Boolean active);
}
