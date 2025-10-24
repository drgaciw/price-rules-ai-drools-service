package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.dto.RuleCreationRequest;
import com.example.pricerulesaidrools.ai.dto.RuleCreationResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for AI-powered rule creation.
 * This service coordinates the use of Sequential Thinking and Context7
 * for generating pricing rules from business requirements.
 */
public interface RuleCreationService {

    /**
     * Creates a new rule from a business requirement using AI.
     *
     * @param request The rule creation request containing business requirements
     * @return The rule creation response with the generated rule and test results
     */
    RuleCreationResponse createRule(RuleCreationRequest request);
    
    /**
     * Creates a new rule asynchronously from a business requirement using AI.
     *
     * @param request The rule creation request containing business requirements
     * @return A CompletableFuture that will contain the rule creation response when complete
     */
    CompletableFuture<RuleCreationResponse> createRuleAsync(RuleCreationRequest request);
    
    /**
     * Validates a generated rule against test cases.
     *
     * @param ruleId The ID of the rule to validate
     * @return The rule creation response with validation results
     */
    RuleCreationResponse validateRule(String ruleId);
    
    /**
     * Analyzes a business requirement without creating a rule.
     *
     * @param requirement The business requirement to analyze
     * @return The rule creation response with analysis results
     */
    RuleCreationResponse analyzeRequirement(String requirement);
    
    /**
     * Improves an existing rule based on performance feedback.
     *
     * @param ruleId The ID of the rule to improve
     * @param feedback The feedback to incorporate
     * @return The rule creation response with the improved rule
     */
    RuleCreationResponse improveRule(String ruleId, String feedback);
    
    /**
     * Creates a documentation-enhanced rule from a business requirement.
     * This method uses both Sequential Thinking and Context7 to generate a rule with
     * documentation insights and best practices.
     *
     * @param request The rule creation request
     * @return The rule creation response with documentation-enhanced rule
     */
    RuleCreationResponse createDocumentationEnhancedRule(RuleCreationRequest request);
    
    /**
     * Enhances an existing rule with documentation insights.
     *
     * @param ruleId The ID of the rule to enhance
     * @param topic Optional specific topic for documentation retrieval
     * @return The rule creation response with the enhanced rule
     */
    RuleCreationResponse enhanceRuleWithDocumentation(String ruleId, String topic);
}