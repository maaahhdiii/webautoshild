package com.autoshield.controller;

import com.autoshield.dto.BlockIpRequest;
import com.autoshield.entity.FirewallRule;
import com.autoshield.service.FirewallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for firewall management
 */
@RestController
@RequestMapping("/api/v1/firewall")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Firewall", description = "Firewall rules management")
public class FirewallController {
    
    private final FirewallService firewallService;
    
    @PostMapping("/block")
    @Operation(summary = "Block an IP address")
    public ResponseEntity<FirewallRule> blockIp(
            @Valid @RequestBody BlockIpRequest request,
            Authentication authentication) {
        
        try {
            String username = authentication != null ? authentication.getName() : "system";
            FirewallRule rule = firewallService.blockIp(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(rule);
            
        } catch (RuntimeException e) {
            log.error("Error blocking IP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/unblock/{ip}")
    @Operation(summary = "Unblock an IP address")
    public ResponseEntity<Void> unblockIp(@PathVariable String ip) {
        try {
            firewallService.unblockIp(ip);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Error unblocking IP: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/rules")
    @Operation(summary = "Get all active firewall rules")
    public ResponseEntity<List<FirewallRule>> getAllRules() {
        return ResponseEntity.ok(firewallService.getAllActiveRules());
    }
    
    @GetMapping("/rules/all")
    @Operation(summary = "Get all firewall rules including inactive")
    public ResponseEntity<List<FirewallRule>> getAllRulesIncludingInactive() {
        return ResponseEntity.ok(firewallService.getAllRules());
    }
}
