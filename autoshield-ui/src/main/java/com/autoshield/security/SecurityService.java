package com.autoshield.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Security service helper for Vaadin views
 */
@Service
@RequiredArgsConstructor
public class SecurityService {
    
    private final AuthenticationContext authenticationContext;
    
    public Optional<UserDetails> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class);
    }
    
    public String getCurrentUsername() {
        return getAuthenticatedUser()
                .map(UserDetails::getUsername)
                .orElse("anonymous");
    }
    
    public boolean hasRole(String role) {
        return getAuthenticatedUser()
                .map(user -> user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(auth -> auth.equals("ROLE_" + role)))
                .orElse(false);
    }
    
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    public void logout() {
        authenticationContext.logout();
    }
}
