package com.example.pricerulesaidrools.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request object for creating a pricing rule using AI.
 * This DTO is used by client applications to request rule generation based on business requirements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleCreationRequest {

    /**
     * Business requirement description in natural language.
     */
    @NotBlank(message = "Business requirement cannot be empty")
    @Size(min = 10, max = 4000, message = "Business requirement must be between 10 and 4000 characters")
    private String businessRequirement;
    
    /**
     * Optional rule type to guide the AI in rule creation.
     */
    private String ruleType;
    
    /**
     * Optional name for the rule.
     */
    private String ruleName;
    
    /**
     * Optional list of facts to be used in rule testing.
     */
    private List<Map<String, Object>> testFacts;
    
    /**
     * Optional flag to indicate if documentation should be included.
     */
    private Boolean includeDocumentation;
    
    /**
     * Optional flag to indicate if test cases should be generated.
     */
    private Boolean generateTestCases;
    
    /**
     * Optional tags for the rule.
     */
    private List<String> tags;
}