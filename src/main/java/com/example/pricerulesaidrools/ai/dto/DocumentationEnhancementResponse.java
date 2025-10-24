package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for documentation-enhanced rule content.
 * This class contains the documentation and rule enhancement results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentationEnhancementResponse {

    /**
     * The original rule pattern or content.
     */
    private String originalRulePattern;
    
    /**
     * The documentation content retrieved for the rule pattern.
     */
    private String documentation;
    
    /**
     * List of code examples extracted from the documentation.
     */
    private List<Context7DocumentationResponse.CodeExample> codeExamples;
    
    /**
     * List of best practices extracted from the documentation.
     */
    private List<Context7DocumentationResponse.BestPractice> bestPractices;
    
    /**
     * List of improvement suggestions based on the documentation.
     */
    private List<ImprovementSuggestion> improvementSuggestions;
    
    /**
     * Enhanced rule content with documentation insights applied.
     */
    private String enhancedRuleContent;
    
    /**
     * Explanation of how the documentation was applied to enhance the rule.
     */
    private String enhancementExplanation;
    
    /**
     * Flag indicating if the rule was successfully enhanced.
     */
    private boolean enhancedWithDocumentation;
    
    /**
     * Timestamp of when the enhancement was performed.
     */
    private LocalDateTime timestamp;
    
    /**
     * Nested class representing a suggestion for rule improvement.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImprovementSuggestion {
        
        /**
         * The title or summary of the suggestion.
         */
        private String title;
        
        /**
         * Detailed description of the suggestion.
         */
        private String description;
        
        /**
         * Example code demonstrating the suggestion.
         */
        private String exampleCode;
        
        /**
         * The relevance or importance score of this suggestion.
         */
        private Double relevance;
    }
}