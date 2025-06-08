package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.service.RuleConflictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for rule conflict detection and resolution.
 * Provides endpoints for analyzing rules and detecting conflicts.
 */
@RestController
@RequestMapping("/api/v1/rule-conflicts")
@RequiredArgsConstructor
@Slf4j
public class RuleConflictController {

    private final RuleConflictService ruleConflictService;
    
    /**
     * Detects potential conflicts in the current rule base.
     *
     * @return list of detected rule conflicts
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> detectConflicts() {
        try {
            List<RuleConflictService.RuleConflict> conflicts = ruleConflictService.detectConflicts();
            
            List<Map<String, Object>> result = conflicts.stream()
                    .map(conflict -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("rule1", conflict.getRule1Name());
                        map.put("rule2", conflict.getRule2Name());
                        map.put("type", conflict.getConflictType());
                        map.put("description", conflict.getDescription());
                        map.put("severity", conflict.getSeverity());
                        map.put("resolutions", conflict.getResolutionOptions());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error detecting rule conflicts", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets suggestions for resolving a specific rule conflict.
     *
     * @param rule1 name of the first rule in the conflict
     * @param rule2 name of the second rule in the conflict
     * @return list of suggested resolutions
     */
    @GetMapping("/resolutions")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<List<String>> getSuggestedResolutions(
            @RequestParam String rule1, 
            @RequestParam String rule2) {
        try {
            List<RuleConflictService.RuleConflict> conflicts = ruleConflictService.detectConflicts();
            
            // Find the conflict between the specified rules
            for (RuleConflictService.RuleConflict conflict : conflicts) {
                if (conflict.getRule1Name().equals(rule1) && conflict.getRule2Name().equals(rule2)) {
                    return ResponseEntity.ok(conflict.getResolutionOptions());
                }
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting suggested resolutions", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Analyzes the current rule base and provides a summary of potential issues.
     *
     * @return a summary report of rule base health
     */
    @GetMapping("/analysis")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_MONITOR')")
    public ResponseEntity<Map<String, Object>> analyzeRuleBase() {
        try {
            Map<String, Object> report = ruleConflictService.analyzeRuleBase();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error analyzing rule base", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Gets health metrics for the rule base.
     *
     * @return health metrics for the rule base
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_MONITOR')")
    public ResponseEntity<Map<String, Object>> getRuleBaseHealth() {
        try {
            Map<String, Object> analysis = ruleConflictService.analyzeRuleBase();
            
            Map<String, Object> health = new HashMap<>();
            health.put("healthScore", analysis.get("healthScore"));
            health.put("totalRules", analysis.get("totalRules"));
            health.put("totalConflicts", analysis.get("totalConflicts"));
            
            // Add status based on health score
            int healthScore = (int) analysis.get("healthScore");
            if (healthScore >= 90) {
                health.put("status", "HEALTHY");
            } else if (healthScore >= 70) {
                health.put("status", "ACCEPTABLE");
            } else if (healthScore >= 50) {
                health.put("status", "WARNING");
            } else {
                health.put("status", "CRITICAL");
            }
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error getting rule base health", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}