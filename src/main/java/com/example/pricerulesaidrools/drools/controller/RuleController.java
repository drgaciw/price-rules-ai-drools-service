package com.example.pricerulesaidrools.drools.controller;

import com.example.pricerulesaidrools.drools.dto.*;
import com.example.pricerulesaidrools.drools.model.RuleSetMetadata;
import com.example.pricerulesaidrools.drools.service.DroolsIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/drools")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Rule Management API", description = "APIs for managing and executing Drools rules")
public class RuleController {

    private final DroolsIntegrationService droolsIntegrationService;

    @PostMapping("/rules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    @Operation(summary = "Deploy a new rule set", description = "Deploy a new Drools rule set to the engine")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule set deployed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rule content"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleDeploymentResult> deployRules(
            @Valid @RequestBody RuleRequest ruleRequest) {
        
        log.info("Deploying rule set: {}", ruleRequest.getName());
        RuleDeploymentResult result = droolsIntegrationService.deployRules(ruleRequest.getContent());
        
        if (result.isSuccessful()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/rules/{version}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    @Operation(summary = "Update an existing rule set", description = "Update an existing Drools rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule set updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rule content"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleDeploymentResult> updateRules(
            @Parameter(description = "Rule set version") @PathVariable String version,
            @Valid @RequestBody RuleRequest ruleRequest) {
        
        log.info("Updating rule set with version: {}", version);
        RuleDeploymentResult result = droolsIntegrationService.updateRules(ruleRequest.getContent(), version);
        
        if (result.isSuccessful()) {
            return ResponseEntity.ok(result);
        } else if (result.getMessage().contains("not found")) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/rules/{version}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    @Operation(summary = "Undeploy a rule set", description = "Undeploy a Drools rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rule set undeployed successfully"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> undeployRules(
            @Parameter(description = "Rule set version") @PathVariable String version) {
        
        log.info("Undeploying rule set with version: {}", version);
        droolsIntegrationService.undeployRules(version);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Validate rule content", description = "Validate Drools rule content without deploying")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleValidationResult> validateRules(
            @Valid @RequestBody RuleRequest ruleRequest) {
        
        log.info("Validating rule content");
        RuleValidationResult result = droolsIntegrationService.validateRules(ruleRequest.getContent());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/validate/{ruleSetId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Validate a deployed rule set", description = "Validate an existing deployed rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result returned"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleValidationResult> validateRuleSet(
            @Parameter(description = "Rule set ID") @PathVariable String ruleSetId) {
        
        log.info("Validating rule set with ID: {}", ruleSetId);
        RuleValidationResult result = droolsIntegrationService.validateRuleSet(ruleSetId);
        
        if (result.getErrors().stream().anyMatch(e -> 
                e.getCode().equals("RULE_SET_NOT_FOUND") || 
                e.getCode().equals("RULE_CONTENT_NOT_FOUND"))) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Execute rules", description = "Execute a deployed rule set with provided facts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules executed successfully"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleExecutionResponse> executeRules(
            @Valid @RequestBody RuleExecutionRequest request) {
        
        log.info("Executing rule set with ID: {}", request.getRuleSetId());
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = droolsIntegrationService.executeRules(request.getRuleSetId(), request.getFacts());
        long executionTime = System.currentTimeMillis() - startTime;
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        RuleExecutionResponse response = RuleExecutionResponse.builder()
                .results(result)
                .executionTime(executionTime)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-execute")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Execute rules in batch", description = "Execute a deployed rule set with multiple sets of facts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rules executed successfully"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Map<String, Object>>> executeBatchRules(
            @Parameter(description = "Rule set ID") @RequestParam String ruleSetId,
            @Valid @RequestBody List<Map<String, Object>> factsList) {
        
        log.info("Batch executing rule set with ID: {} for {} fact sets", ruleSetId, factsList.size());
        List<Map<String, Object>> results = droolsIntegrationService.executeBatchRules(ruleSetId, factsList);
        
        if (results.isEmpty() && !factsList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(results);
    }

    @GetMapping("/rules/{ruleSetId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "Get rule set metadata", description = "Get metadata for a deployed rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule set metadata returned"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleSetMetadata> getRuleSetMetadata(
            @Parameter(description = "Rule set ID") @PathVariable String ruleSetId) {
        
        log.info("Getting metadata for rule set with ID: {}", ruleSetId);
        RuleSetMetadata metadata = droolsIntegrationService.getRuleSetMetadata(ruleSetId);
        
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/rules")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "List all rule sets", description = "List metadata for all deployed rule sets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule set list returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RuleSetMetadata>> listRuleSets() {
        
        log.info("Listing all rule sets");
        List<RuleSetMetadata> ruleSetList = droolsIntegrationService.listRuleSets();
        return ResponseEntity.ok(ruleSetList);
    }

    @PostMapping("/rules/{ruleSetId}/reload")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    @Operation(summary = "Reload a rule set", description = "Reload a deployed rule set from storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rule set reloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> reloadRuleSet(
            @Parameter(description = "Rule set ID") @PathVariable String ruleSetId) {
        
        log.info("Reloading rule set with ID: {}", ruleSetId);
        
        if (droolsIntegrationService.getRuleSetMetadata(ruleSetId) == null) {
            return ResponseEntity.notFound().build();
        }
        
        droolsIntegrationService.reloadRuleSet(ruleSetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics/{ruleSetId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "Get rule execution metrics", description = "Get performance metrics for a deployed rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule execution metrics returned"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleExecutionMetrics> getRuleExecutionMetrics(
            @Parameter(description = "Rule set ID") @PathVariable String ruleSetId) {
        
        log.info("Getting execution metrics for rule set with ID: {}", ruleSetId);
        
        if (droolsIntegrationService.getRuleSetMetadata(ruleSetId) == null) {
            return ResponseEntity.notFound().build();
        }
        
        RuleExecutionMetrics metrics = droolsIntegrationService.getRuleExecutionMetrics(ruleSetId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/cache-metrics/{ruleSetId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "Get rule cache metrics", description = "Get cache performance metrics for a deployed rule set")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule cache metrics returned"),
            @ApiResponse(responseCode = "404", description = "Rule set not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCacheMetrics> getRuleCacheMetrics(
            @Parameter(description = "Rule set ID") @PathVariable String ruleSetId) {
        
        log.info("Getting cache metrics for rule set with ID: {}", ruleSetId);
        
        if (droolsIntegrationService.getRuleSetMetadata(ruleSetId) == null) {
            return ResponseEntity.notFound().build();
        }
        
        RuleCacheMetrics metrics = droolsIntegrationService.getRuleCacheMetrics(ruleSetId);
        return ResponseEntity.ok(metrics);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Error in rule controller", e);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}