package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response object for rule creation using AI.
 * This DTO contains the results of the rule creation process including the generated rule and test results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleCreationResponse {

    /**
     * Unique identifier for the created rule.
     */
    private String ruleId;
    
    /**
     * Name of the created rule.
     */
    private String ruleName;
    
    /**
     * The complete Drools rule content in DRL format.
     */
    private String ruleContent;
    
    /**
     * Flag indicating if rule creation was successful.
     */
    private boolean success;
    
    /**
     * Message describing the result of rule creation.
     */
    private String message;
    
    /**
     * List of test cases used to validate the rule.
     */
    private List<Map<String, Object>> testCases;
    
    /**
     * Results of running the test cases.
     */
    private List<Map<String, Object>> testResults;
    
    /**
     * Business requirement that was used to create the rule.
     */
    private String businessRequirement;
    
    /**
     * Documentation and explanation of the rule.
     */
    private String documentation;
    
    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;
    
    /**
     * Possible validation errors or warnings.
     */
    private List<ValidationMessage> validationMessages;
    
    /**
     * Nested class for validation messages.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationMessage {
        /**
         * Type of validation message (ERROR, WARNING, INFO).
         */
        private String type;
        
        /**
         * The validation message.
         */
        private String message;
    }
}