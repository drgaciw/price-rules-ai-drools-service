package com.example.pricerulesaidrools.ai.controller;

import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementRequest;
import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementResponse;
import com.example.pricerulesaidrools.ai.dto.RuleCreationRequest;
import com.example.pricerulesaidrools.ai.dto.RuleCreationResponse;
import com.example.pricerulesaidrools.ai.service.Context7Service;
import com.example.pricerulesaidrools.ai.service.RuleCreationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for AI-powered rule creation and management.
 * This controller provides endpoints for creating, validating, and improving rules using AI.
 */
@RestController
@RequestMapping("/ai/rules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Rule Creation", description = "API for AI-powered rule creation and management")
public class AIRuleController {

    private final RuleCreationService ruleCreationService;
    private final Context7Service context7Service;

    /**
     * Creates a new rule from a business requirement using AI.
     *
     * @param request The rule creation request
     * @return The rule creation response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Create a new rule using AI",
            description = "Creates a new pricing rule from a business requirement using sequential thinking AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule created successfully",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> createRule(@Valid @RequestBody RuleCreationRequest request) {
        log.info("Received request to create rule using AI");
        RuleCreationResponse response = ruleCreationService.createRule(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new rule asynchronously from a business requirement using AI.
     *
     * @param request The rule creation request
     * @return CompletableFuture with the rule creation response
     */
    @PostMapping("/async")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Create a new rule asynchronously using AI",
            description = "Creates a new pricing rule asynchronously from a business requirement using sequential thinking AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule creation initiated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public CompletableFuture<ResponseEntity<RuleCreationResponse>> createRuleAsync(
            @Valid @RequestBody RuleCreationRequest request) {
        log.info("Received request to create rule asynchronously using AI");
        return ruleCreationService.createRuleAsync(request)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Validates an existing rule.
     *
     * @param ruleId The ID of the rule to validate
     * @return The validation response
     */
    @GetMapping("/{ruleId}/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR') or hasRole('RULE_VALIDATOR')")
    @Operation(summary = "Validate an existing rule",
            description = "Validates an existing rule against test cases and rule syntax")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule validation completed",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> validateRule(
            @Parameter(description = "ID of the rule to validate", required = true)
            @PathVariable @NotBlank String ruleId) {
        log.info("Received request to validate rule: {}", ruleId);
        RuleCreationResponse response = ruleCreationService.validateRule(ruleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Analyzes a business requirement without creating a rule.
     *
     * @param requirement The business requirement to analyze
     * @return The analysis response
     */
    @PostMapping("/analyze")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR') or hasRole('RULE_ANALYST')")
    @Operation(summary = "Analyze a business requirement",
            description = "Analyzes a business requirement without creating a rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis completed",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> analyzeRequirement(
            @Parameter(description = "Business requirement to analyze", required = true)
            @RequestBody @NotBlank String requirement) {
        log.info("Received request to analyze business requirement");
        RuleCreationResponse response = ruleCreationService.analyzeRequirement(requirement);
        return ResponseEntity.ok(response);
    }

    /**
     * Improves an existing rule based on feedback.
     *
     * @param ruleId   The ID of the rule to improve
     * @param feedback The feedback to incorporate
     * @return The improved rule response
     */
    @PutMapping("/{ruleId}/improve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Improve an existing rule",
            description = "Improves an existing rule based on feedback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule improved successfully",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> improveRule(
            @Parameter(description = "ID of the rule to improve", required = true)
            @PathVariable @NotBlank String ruleId,
            @Parameter(description = "Feedback to incorporate", required = true)
            @RequestBody @NotBlank String feedback) {
        log.info("Received request to improve rule: {}", ruleId);
        RuleCreationResponse response = ruleCreationService.improveRule(ruleId, feedback);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Creates a new rule with documentation enhancement.
     *
     * @param request The rule creation request
     * @return The rule creation response with documentation enhancement
     */
    @PostMapping("/documentation-enhanced")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Create a new rule with documentation enhancement",
            description = "Creates a new pricing rule enhanced with documentation insights and best practices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentation-enhanced rule created successfully",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> createDocumentationEnhancedRule(
            @Valid @RequestBody RuleCreationRequest request) {
        log.info("Received request to create documentation-enhanced rule");
        RuleCreationResponse response = ruleCreationService.createDocumentationEnhancedRule(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enhances an existing rule with documentation.
     *
     * @param ruleId The ID of the rule to enhance
     * @param topic Optional topic for documentation retrieval
     * @return The rule creation response with documentation enhancement
     */
    @PutMapping("/{ruleId}/documentation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Enhance an existing rule with documentation",
            description = "Enhances an existing rule with documentation insights and best practices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule enhanced with documentation successfully",
                    content = @Content(schema = @Schema(implementation = RuleCreationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RuleCreationResponse> enhanceRuleWithDocumentation(
            @Parameter(description = "ID of the rule to enhance", required = true)
            @PathVariable @NotBlank String ruleId,
            @Parameter(description = "Topic for documentation retrieval")
            @RequestParam(required = false) String topic) {
        log.info("Received request to enhance rule: {} with documentation", ruleId);
        RuleCreationResponse response = ruleCreationService.enhanceRuleWithDocumentation(ruleId, topic);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves documentation for a specific topic.
     *
     * @param topic The topic to retrieve documentation for
     * @return The documentation response
     */
    @GetMapping("/documentation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR') or hasRole('RULE_VALIDATOR')")
    @Operation(summary = "Retrieve documentation for a topic",
            description = "Retrieves documentation from the Context7 service for a specific topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DocumentationEnhancementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DocumentationEnhancementResponse> getDocumentation(
            @Parameter(description = "Topic to retrieve documentation for", required = true)
            @RequestParam @NotBlank String topic) {
        log.info("Received request to retrieve documentation for topic: {}", topic);
        DocumentationEnhancementRequest request = DocumentationEnhancementRequest.builder()
                .topic(topic)
                .rulePattern("") // Empty pattern since we're just retrieving documentation
                .includeCodeExamples(true)
                .includeBestPractices(true)
                .build();
        DocumentationEnhancementResponse response = context7Service.enhanceRuleWithDocumentation(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enhances a rule pattern with documentation insights.
     *
     * @param request The documentation enhancement request
     * @return The documentation enhancement response
     */
    @PostMapping("/documentation-enhancement")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RULE_CREATOR')")
    @Operation(summary = "Enhance a rule pattern with documentation",
            description = "Enhances a rule pattern with documentation insights and best practices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule pattern enhanced successfully",
                    content = @Content(schema = @Schema(implementation = DocumentationEnhancementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DocumentationEnhancementResponse> enhanceWithDocumentation(
            @Valid @RequestBody DocumentationEnhancementRequest request) {
        log.info("Received request to enhance rule pattern with documentation");
        DocumentationEnhancementResponse response = context7Service.enhanceRuleWithDocumentation(request);
        return ResponseEntity.ok(response);
    }
}