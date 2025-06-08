package com.example.pricerulesaidrools.security.util;

import com.example.pricerulesaidrools.security.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    /**
     * Get the currently authenticated user
     * 
     * @return Optional containing the authenticated user, or empty if not authenticated
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        if (authentication.getPrincipal() instanceof User) {
            return Optional.of((User) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }
    
    /**
     * Get the currently authenticated username
     * 
     * @return Optional containing the authenticated username, or empty if not authenticated
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(User::getUsername);
    }
    
    /**
     * Check if the current user has a specific role
     * 
     * @param role the role to check
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    
    /**
     * Check if the current user is an admin
     * 
     * @return true if the user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    /**
     * Check if the current user is a rule manager
     * 
     * @return true if the user is a rule manager, false otherwise
     */
    public static boolean isRuleManager() {
        return hasRole("ROLE_RULE_MANAGER") || isAdmin();
    }
    
    /**
     * Check if the current user is a rule executor
     * 
     * @return true if the user is a rule executor, false otherwise
     */
    public static boolean isRuleExecutor() {
        return hasRole("ROLE_RULE_EXECUTOR") || isRuleManager() || isAdmin();
    }
}