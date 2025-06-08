package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.service.RuleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for rule template operations.
 * Provides endpoints for generating rules from templates.
 */
@RestController
@RequestMapping("/api/v1/rule-templates")
@RequiredArgsConstructor
@Slf4j
public class RuleTemplateController {

    private final RuleTemplateService ruleTemplateService;
    
    /**
     * Creates a volume discount rule from a template.
     *
     * @param params parameters for the volume discount rule
     * @return ResponseEntity with the path to the generated rule file
     */
    @PostMapping("/volume-discount")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<String> createVolumeDiscountRule(@RequestBody Map<String, Object> params) {
        try {
            String ruleName = params.getOrDefault("ruleName", "Custom").toString();
            String outputPath = "/tmp/generated-rules/" + ruleName.replaceAll("\\s+", "-").toLowerCase() + "-volume-discount.drl";
            
            Path generatedPath = ruleTemplateService.generateVolumeDiscountRule(params, outputPath);
            ruleTemplateService.reloadRulesAfterGeneration(generatedPath);
            
            return ResponseEntity.ok("Rule generated successfully at: " + generatedPath);
        } catch (Exception e) {
            log.error("Error generating volume discount rule", e);
            return ResponseEntity.badRequest().body("Error generating rule: " + e.getMessage());
        }
    }
    
    /**
     * Creates a commitment rule from a template.
     *
     * @param params parameters for the commitment rule
     * @return ResponseEntity with the path to the generated rule file
     */
    @PostMapping("/commitment")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<String> createCommitmentRule(@RequestBody Map<String, Object> params) {
        try {
            String ruleName = params.getOrDefault("ruleName", "Custom").toString();
            String outputPath = "/tmp/generated-rules/" + ruleName.replaceAll("\\s+", "-").toLowerCase() + "-commitment.drl";
            
            Path generatedPath = ruleTemplateService.generateCommitmentRule(params, outputPath);
            ruleTemplateService.reloadRulesAfterGeneration(generatedPath);
            
            return ResponseEntity.ok("Rule generated successfully at: " + generatedPath);
        } catch (Exception e) {
            log.error("Error generating commitment rule", e);
            return ResponseEntity.badRequest().body("Error generating rule: " + e.getMessage());
        }
    }
    
    /**
     * Creates a risk adjustment rule from a template.
     *
     * @param params parameters for the risk adjustment rule
     * @return ResponseEntity with the path to the generated rule file
     */
    @PostMapping("/risk-adjustment")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<String> createRiskAdjustmentRule(@RequestBody Map<String, Object> params) {
        try {
            String ruleName = params.getOrDefault("ruleName", "Custom").toString();
            String outputPath = "/tmp/generated-rules/" + ruleName.replaceAll("\\s+", "-").toLowerCase() + "-risk-adjustment.drl";
            
            Path generatedPath = ruleTemplateService.generateRiskAdjustmentRule(params, outputPath);
            ruleTemplateService.reloadRulesAfterGeneration(generatedPath);
            
            return ResponseEntity.ok("Rule generated successfully at: " + generatedPath);
        } catch (Exception e) {
            log.error("Error generating risk adjustment rule", e);
            return ResponseEntity.badRequest().body("Error generating rule: " + e.getMessage());
        }
    }
    
    /**
     * Creates a contract length rule from a template.
     *
     * @param params parameters for the contract length rule
     * @return ResponseEntity with the path to the generated rule file
     */
    @PostMapping("/contract-length")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<String> createContractLengthRule(@RequestBody Map<String, Object> params) {
        try {
            String ruleName = params.getOrDefault("ruleName", "Custom").toString();
            String outputPath = "/tmp/generated-rules/" + ruleName.replaceAll("\\s+", "-").toLowerCase() + "-contract-length.drl";
            
            Path generatedPath = ruleTemplateService.generateContractLengthRule(params, outputPath);
            ruleTemplateService.reloadRulesAfterGeneration(generatedPath);
            
            return ResponseEntity.ok("Rule generated successfully at: " + generatedPath);
        } catch (Exception e) {
            log.error("Error generating contract length rule", e);
            return ResponseEntity.badRequest().body("Error generating rule: " + e.getMessage());
        }
    }
    
    /**
     * Gets example parameters for volume discount rules.
     *
     * @return Map of example parameters
     */
    @GetMapping("/examples/volume-discount")
    public ResponseEntity<Map<String, Object>> getVolumeDiscountExample() {
        Map<String, Object> example = new HashMap<>();
        example.put("metricField", "arr");
        example.put("threshold1", 10000);
        example.put("threshold2", 50000);
        example.put("threshold3", 100000);
        example.put("threshold4", 500000);
        example.put("discount1", 5.0);
        example.put("discount2", 10.0);
        example.put("discount3", 15.0);
        example.put("discount4", 20.0);
        example.put("ruleName", "ARR Volume");
        
        return ResponseEntity.ok(example);
    }
    
    /**
     * Gets example parameters for commitment rules.
     *
     * @return Map of example parameters
     */
    @GetMapping("/examples/commitment")
    public ResponseEntity<Map<String, Object>> getCommitmentExample() {
        Map<String, Object> example = new HashMap<>();
        example.put("metricField", "acv");
        example.put("tier1Min", 10000);
        example.put("tier2Min", 50000);
        example.put("tier3Min", 100000);
        example.put("tier4Min", 250000);
        example.put("tier5Min", 500000);
        example.put("tier1Name", "Basic");
        example.put("tier2Name", "Standard");
        example.put("tier3Name", "Premium");
        example.put("tier4Name", "Enterprise");
        example.put("tier5Name", "Strategic");
        example.put("tier1CommitmentPercent", 40.0);
        example.put("tier2CommitmentPercent", 35.0);
        example.put("tier3CommitmentPercent", 30.0);
        example.put("tier4CommitmentPercent", 25.0);
        example.put("tier5CommitmentPercent", 20.0);
        example.put("ruleName", "ACV Commitment");
        
        return ResponseEntity.ok(example);
    }
}