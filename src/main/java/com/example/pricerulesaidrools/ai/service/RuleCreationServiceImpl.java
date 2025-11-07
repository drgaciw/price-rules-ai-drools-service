package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementRequest;
import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementResponse;
import com.example.pricerulesaidrools.ai.dto.RuleCreationRequest;
import com.example.pricerulesaidrools.ai.dto.RuleCreationResponse;
import com.example.pricerulesaidrools.ai.dto.SequentialThinkingResponse;
import com.example.pricerulesaidrools.drools.service.DroolsIntegrationService;
import com.example.pricerulesaidrools.drools.dto.RuleDeploymentResult;
import com.example.pricerulesaidrools.drools.dto.RuleValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the RuleCreationService interface.
 * This class coordinates the use of Sequential Thinking for generating
 * pricing rules from business requirements and integrates with the Drools
 * engine.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleCreationServiceImpl implements RuleCreationService {

    private final SequentialThinkingService sequentialThinkingService;
    private final Context7Service context7Service;
    private final DroolsIntegrationService droolsIntegrationService;

    // Cache for storing created rules during the session
    private final Map<String, RuleCreationResponse> createdRules = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse createRule(RuleCreationRequest request) {
        log.info("Creating rule from business requirement");

        try {
            // Generate rule using Sequential Thinking
            SequentialThinkingResponse generatedRule = sequentialThinkingService.generateRule(
                    request.getBusinessRequirement(),
                    request.getRuleType());

            if (generatedRule == null || generatedRule.getRuleContent() == null) {
                return createErrorResponse("Failed to generate rule content");
            }

            // Deploy rule to Drools engine
            RuleDeploymentResult deploymentResult = droolsIntegrationService.deployRules(
                    generatedRule.getRuleContent());

            if (!deploymentResult.isSuccessful()) {
                return createErrorResponse("Failed to deploy rule: " +
                        (deploymentResult.getMessage() != null ? deploymentResult.getMessage() : "Unknown error"));
            }

            // Create response
            String ruleId = deploymentResult.getId();
            RuleCreationResponse response = RuleCreationResponse.builder()
                    .ruleId(ruleId)
                    .ruleName(request.getRuleName() != null ? request.getRuleName()
                            : (generatedRule.getRuleName() != null ? generatedRule.getRuleName() : "Rule_" + ruleId))
                    .ruleContent(generatedRule.getRuleContent())
                    .success(true)
                    .message("Rule created successfully")
                    .businessRequirement(request.getBusinessRequirement())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Add test cases if requested
            if (Boolean.TRUE.equals(request.getGenerateTestCases()) && generatedRule.getTestCases() != null) {
                response.setTestCases(generatedRule.getTestCases());

                // Execute test cases if provided
                if (request.getTestFacts() != null && !request.getTestFacts().isEmpty()) {
                    List<Map<String, Object>> testResults = executeTestCases(ruleId, request.getTestFacts());
                    response.setTestResults(testResults);
                }
            }

            // Add documentation if requested
            if (Boolean.TRUE.equals(request.getIncludeDocumentation()) && generatedRule.getReasoning() != null) {
                response.setDocumentation(generatedRule.getReasoning());
            }

            // Store the created rule
            createdRules.put(ruleId, response);

            return response;
        } catch (Exception e) {
            log.error("Error creating rule: {}", e.getMessage(), e);
            return createErrorResponse("Error creating rule: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public CompletableFuture<RuleCreationResponse> createRuleAsync(RuleCreationRequest request) {
        return CompletableFuture.completedFuture(createRule(request));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse validateRule(String ruleId) {
        log.info("Validating rule with ID: {}", ruleId);

        try {
            // Get the stored rule
            RuleCreationResponse storedRule = createdRules.get(ruleId);
            if (storedRule == null) {
                return createErrorResponse("Rule not found with ID: " + ruleId);
            }

            // Validate the rule using Drools
            RuleValidationResult validationResult = droolsIntegrationService.validateRuleSet(ruleId);

            // Create a copy of the stored rule with validation results
            RuleCreationResponse response = RuleCreationResponse.builder()
                    .ruleId(storedRule.getRuleId())
                    .ruleName(storedRule.getRuleName())
                    .ruleContent(storedRule.getRuleContent())
                    .success(validationResult.isValid())
                    .message(validationResult.isValid() ? "Rule validation successful" : "Rule validation failed")
                    .businessRequirement(storedRule.getBusinessRequirement())
                    .documentation(storedRule.getDocumentation())
                    .createdAt(storedRule.getCreatedAt())
                    .build();

            // Add validation messages
            if (validationResult.getErrors() != null && !validationResult.getErrors().isEmpty()) {
                List<RuleCreationResponse.ValidationMessage> validationMessages = new ArrayList<>();
                validationResult.getErrors().forEach(error -> {
                    validationMessages.add(RuleCreationResponse.ValidationMessage.builder()
                            .type(error.getSeverity().toString())
                            .message(error.getMessage())
                            .build());
                });
                response.setValidationMessages(validationMessages);
            }

            return response;
        } catch (Exception e) {
            log.error("Error validating rule: {}", e.getMessage(), e);
            return createErrorResponse("Error validating rule: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse analyzeRequirement(String requirement) {
        log.info("Analyzing business requirement");

        try {
            // Use Sequential Thinking to analyze the requirement
            SequentialThinkingResponse analysis = sequentialThinkingService.analyzeBusinessRequirement(requirement);

            // Create response
            return RuleCreationResponse.builder()
                    .ruleId(UUID.randomUUID().toString())
                    .success(true)
                    .message("Business requirement analysis completed")
                    .businessRequirement(requirement)
                    .documentation(analysis.getReasoning())
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error analyzing requirement: {}", e.getMessage(), e);
            return createErrorResponse("Error analyzing requirement: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse improveRule(String ruleId, String feedback) {
        log.info("Improving rule with ID: {} based on feedback", ruleId);

        try {
            // Get the stored rule
            RuleCreationResponse storedRule = createdRules.get(ruleId);
            if (storedRule == null) {
                return createErrorResponse("Rule not found with ID: " + ruleId);
            }

            // Create a thought request for improvement
            String improvementThought = "Let's improve this rule based on the feedback: " + feedback +
                    "\n\nOriginal business requirement: " + storedRule.getBusinessRequirement() +
                    "\n\nCurrent rule:\n" + storedRule.getRuleContent();

            SequentialThinkingResponse improvedRule = sequentialThinkingService.generateRule(
                    improvementThought,
                    "improvement");

            if (improvedRule == null || improvedRule.getUpdatedRule() == null) {
                return createErrorResponse("Failed to improve rule content");
            }

            // Deploy improved rule to Drools engine
            String improvedRuleContent = improvedRule.getUpdatedRule();
            RuleDeploymentResult deploymentResult = droolsIntegrationService.updateRules(
                    improvedRuleContent,
                    storedRule.getRuleId());

            if (!deploymentResult.isSuccessful()) {
                return createErrorResponse("Failed to deploy improved rule: " +
                        (deploymentResult.getMessage() != null ? deploymentResult.getMessage() : "Unknown error"));
            }

            // Create response
            RuleCreationResponse response = RuleCreationResponse.builder()
                    .ruleId(storedRule.getRuleId())
                    .ruleName(storedRule.getRuleName())
                    .ruleContent(improvedRuleContent)
                    .success(true)
                    .message("Rule improved successfully")
                    .businessRequirement(storedRule.getBusinessRequirement())
                    .documentation(improvedRule.getReasoning() != null ? improvedRule.getReasoning()
                            : storedRule.getDocumentation())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Update the stored rule
            createdRules.put(storedRule.getRuleId(), response);

            return response;
        } catch (Exception e) {
            log.error("Error improving rule: {}", e.getMessage(), e);
            return createErrorResponse("Error improving rule: " + e.getMessage());
        }
    }

    /**
     * Helper method to execute test cases for a rule.
     * 
     * @param ruleId    The ID of the rule to test
     * @param testFacts The test facts to use
     * @return List of test results
     */
    private List<Map<String, Object>> executeTestCases(String ruleId, List<Map<String, Object>> testFacts) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // Execute batch rules using Drools service
            List<Object> droolsResults = droolsIntegrationService.executeBatchRules(ruleId, testFacts);

            // Map results
            for (int i = 0; i < testFacts.size(); i++) {
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("testCase", testFacts.get(i));
                testResult.put("result", droolsResults.get(i));
                testResult.put("success", droolsResults.get(i) != null);
                results.add(testResult);
            }
        } catch (Exception e) {
            log.error("Error executing test cases: {}", e.getMessage(), e);
            // Create error result
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("success", false);
            results.add(errorResult);
        }

        return results;
    }

    /**
     * Helper method to create an error response.
     * 
     * @param errorMessage The error message
     * @return The error response
     */
    private RuleCreationResponse createErrorResponse(String errorMessage) {
        return RuleCreationResponse.builder()
                .ruleId(UUID.randomUUID().toString())
                .success(false)
                .message(errorMessage)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse createDocumentationEnhancedRule(RuleCreationRequest request) {
        log.info("Creating documentation-enhanced rule from business requirement");

        try {
            // Generate rule using Sequential Thinking
            SequentialThinkingResponse generatedRule = sequentialThinkingService.generateRule(
                    request.getBusinessRequirement(),
                    request.getRuleType());

            if (generatedRule == null || generatedRule.getRuleContent() == null) {
                return createErrorResponse("Failed to generate rule content");
            }

            // Enhance the rule with documentation insights
            DocumentationEnhancementRequest enhancementRequest = DocumentationEnhancementRequest.builder()
                    .rulePattern(generatedRule.getRuleContent())
                    .topic(request.getRuleType() != null ? request.getRuleType() + " pricing rules" : "pricing rules")
                    .includeCodeExamples(true)
                    .includeBestPractices(true)
                    .build();

            DocumentationEnhancementResponse enhancedRule = context7Service
                    .enhanceRuleWithDocumentation(enhancementRequest);

            if (!enhancedRule.isEnhancedWithDocumentation()) {
                log.warn("Rule could not be enhanced with documentation, using original rule");
            }

            // Use the enhanced rule if available, otherwise use the original
            String finalRuleContent = enhancedRule.isEnhancedWithDocumentation() &&
                    enhancedRule.getEnhancedRuleContent() != null ? enhancedRule.getEnhancedRuleContent()
                            : generatedRule.getRuleContent();

            // Deploy rule to Drools engine
            RuleDeploymentResult deploymentResult = droolsIntegrationService.deployRules(finalRuleContent);

            if (!deploymentResult.isSuccessful()) {
                return createErrorResponse("Failed to deploy rule: " +
                        (deploymentResult.getMessage() != null ? deploymentResult.getMessage() : "Unknown error"));
            }

            // Create response
            String ruleId = deploymentResult.getId();
            RuleCreationResponse response = RuleCreationResponse.builder()
                    .ruleId(ruleId)
                    .ruleName(request.getRuleName() != null ? request.getRuleName()
                            : (generatedRule.getRuleName() != null ? generatedRule.getRuleName() : "Rule_" + ruleId))
                    .ruleContent(finalRuleContent)
                    .success(true)
                    .message("Documentation-enhanced rule created successfully")
                    .businessRequirement(request.getBusinessRequirement())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Add documentation
            if (enhancedRule.isEnhancedWithDocumentation()) {
                StringBuilder docBuilder = new StringBuilder();

                if (enhancedRule.getDocumentation() != null) {
                    docBuilder.append("## Documentation\n\n")
                            .append(enhancedRule.getDocumentation())
                            .append("\n\n");
                }

                if (enhancedRule.getImprovementSuggestions() != null &&
                        !enhancedRule.getImprovementSuggestions().isEmpty()) {
                    docBuilder.append("## Improvement Suggestions\n\n");

                    for (DocumentationEnhancementResponse.ImprovementSuggestion suggestion : enhancedRule
                            .getImprovementSuggestions()) {
                        docBuilder.append("### ").append(suggestion.getTitle()).append("\n\n")
                                .append(suggestion.getDescription()).append("\n\n");

                        if (suggestion.getExampleCode() != null) {
                            docBuilder.append("```\n")
                                    .append(suggestion.getExampleCode())
                                    .append("\n```\n\n");
                        }
                    }
                }

                if (enhancedRule.getEnhancementExplanation() != null) {
                    docBuilder.append("## Enhancement Explanation\n\n")
                            .append(enhancedRule.getEnhancementExplanation());
                }

                response.setDocumentation(docBuilder.toString());
            }

            // Add test cases if requested
            if (Boolean.TRUE.equals(request.getGenerateTestCases()) && generatedRule.getTestCases() != null) {
                response.setTestCases(generatedRule.getTestCases());

                // Execute test cases if provided
                if (request.getTestFacts() != null && !request.getTestFacts().isEmpty()) {
                    List<Map<String, Object>> testResults = executeTestCases(ruleId, request.getTestFacts());
                    response.setTestResults(testResults);
                }
            }

            // Store the created rule
            createdRules.put(ruleId, response);

            return response;
        } catch (Exception e) {
            log.error("Error creating documentation-enhanced rule: {}", e.getMessage(), e);
            return createErrorResponse("Error creating documentation-enhanced rule: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleCreationResponse enhanceRuleWithDocumentation(String ruleId, String topic) {
        log.info("Enhancing rule with ID: {} with documentation", ruleId);

        try {
            // Get the stored rule
            RuleCreationResponse storedRule = createdRules.get(ruleId);
            if (storedRule == null) {
                return createErrorResponse("Rule not found with ID: " + ruleId);
            }

            // Enhance the rule with documentation
            DocumentationEnhancementRequest enhancementRequest = DocumentationEnhancementRequest.builder()
                    .rulePattern(storedRule.getRuleContent())
                    .topic(topic != null ? topic : "pricing rules")
                    .includeCodeExamples(true)
                    .includeBestPractices(true)
                    .build();

            DocumentationEnhancementResponse enhancedRule = context7Service
                    .enhanceRuleWithDocumentation(enhancementRequest);

            if (!enhancedRule.isEnhancedWithDocumentation()) {
                return createErrorResponse("Failed to enhance rule with documentation");
            }

            // Update the rule in Drools engine
            RuleDeploymentResult deploymentResult = droolsIntegrationService.updateRules(
                    enhancedRule.getEnhancedRuleContent(),
                    storedRule.getRuleId());

            if (!deploymentResult.isSuccessful()) {
                return createErrorResponse("Failed to update rule: " +
                        (deploymentResult.getMessage() != null ? deploymentResult.getMessage() : "Unknown error"));
            }

            // Create documentation string
            StringBuilder docBuilder = new StringBuilder();

            if (enhancedRule.getDocumentation() != null) {
                docBuilder.append("## Documentation\n\n")
                        .append(enhancedRule.getDocumentation())
                        .append("\n\n");
            }

            if (enhancedRule.getImprovementSuggestions() != null &&
                    !enhancedRule.getImprovementSuggestions().isEmpty()) {
                docBuilder.append("## Improvement Suggestions\n\n");

                for (DocumentationEnhancementResponse.ImprovementSuggestion suggestion : enhancedRule
                        .getImprovementSuggestions()) {
                    docBuilder.append("### ").append(suggestion.getTitle()).append("\n\n")
                            .append(suggestion.getDescription()).append("\n\n");

                    if (suggestion.getExampleCode() != null) {
                        docBuilder.append("```\n")
                                .append(suggestion.getExampleCode())
                                .append("\n```\n\n");
                    }
                }
            }

            if (enhancedRule.getEnhancementExplanation() != null) {
                docBuilder.append("## Enhancement Explanation\n\n")
                        .append(enhancedRule.getEnhancementExplanation());
            }

            // Create response
            RuleCreationResponse response = RuleCreationResponse.builder()
                    .ruleId(storedRule.getRuleId())
                    .ruleName(storedRule.getRuleName())
                    .ruleContent(enhancedRule.getEnhancedRuleContent())
                    .success(true)
                    .message("Rule enhanced with documentation successfully")
                    .businessRequirement(storedRule.getBusinessRequirement())
                    .documentation(docBuilder.toString())
                    .createdAt(LocalDateTime.now())
                    .build();

            // Update the stored rule
            createdRules.put(storedRule.getRuleId(), response);

            return response;
        } catch (Exception e) {
            log.error("Error enhancing rule with documentation: {}", e.getMessage(), e);
            return createErrorResponse("Error enhancing rule with documentation: " + e.getMessage());
        }
    }
}