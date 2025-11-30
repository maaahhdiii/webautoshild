package com.autoshield.service;

import com.autoshield.dto.BlockIpRequest;
import com.autoshield.entity.FirewallRule;
import com.autoshield.entity.FirewallRule.FirewallAction;
import com.autoshield.repository.FirewallRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing firewall rules
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FirewallService {
    
    private final FirewallRuleRepository firewallRuleRepository;
    private final PythonAiClient pythonAiClient;
    
    /**
     * Block an IP address
     */
    @Transactional
    public FirewallRule blockIp(BlockIpRequest request, String createdBy) {
        // Check if already blocked
        if (firewallRuleRepository.existsByIpAddressAndActive(request.getIpAddress(), true)) {
            throw new RuntimeException("IP address is already blocked");
        }
        
        // Call Python AI to apply the block
        boolean success = pythonAiClient.blockIp(request.getIpAddress(), request.getReason());
        if (!success) {
            throw new RuntimeException("Failed to block IP via Python AI service");
        }
        
        // Calculate expiration if temporary
        LocalDateTime expiresAt = null;
        if (request.getPermanent() == null || !request.getPermanent()) {
            int minutes = request.getDurationMinutes() != null ? request.getDurationMinutes() : 60;
            expiresAt = LocalDateTime.now().plusMinutes(minutes);
        }
        
        FirewallRule rule = FirewallRule.builder()
                .ipAddress(request.getIpAddress())
                .action(FirewallAction.BLOCK)
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .active(true)
                .createdBy(createdBy)
                .build();
        
        FirewallRule saved = firewallRuleRepository.save(rule);
        log.info("Blocked IP: {} - Reason: {}", request.getIpAddress(), request.getReason());
        return saved;
    }
    
    /**
     * Unblock an IP address
     */
    @Transactional
    public void unblockIp(String ipAddress) {
        FirewallRule rule = firewallRuleRepository.findByIpAddressAndActive(ipAddress, true)
                .orElseThrow(() -> new RuntimeException("No active rule found for IP: " + ipAddress));
        
        // Call Python AI to remove the block
        boolean success = pythonAiClient.unblockIp(ipAddress);
        if (!success) {
            log.warn("Failed to unblock IP via Python AI, marking inactive anyway");
        }
        
        rule.setActive(false);
        firewallRuleRepository.save(rule);
        log.info("Unblocked IP: {}", ipAddress);
    }
    
    /**
     * Get all active firewall rules
     */
    public List<FirewallRule> getAllActiveRules() {
        return firewallRuleRepository.findAllActiveNonExpired(LocalDateTime.now());
    }
    
    /**
     * Get all rules (including inactive)
     */
    public List<FirewallRule> getAllRules() {
        return firewallRuleRepository.findAll();
    }
    
    /**
     * Scheduled task to expire old rules
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void expireOldRules() {
        List<FirewallRule> expiredRules = firewallRuleRepository.findExpiredRules(LocalDateTime.now());
        
        for (FirewallRule rule : expiredRules) {
            log.info("Expiring rule for IP: {}", rule.getIpAddress());
            pythonAiClient.unblockIp(rule.getIpAddress());
            rule.setActive(false);
            firewallRuleRepository.save(rule);
        }
        
        if (!expiredRules.isEmpty()) {
            log.info("Expired {} firewall rules", expiredRules.size());
        }
    }
}
