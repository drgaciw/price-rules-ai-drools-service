package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for enhancing rule creation with documentation.
 * This class is used by client applications to request documentation-enhanced rule generation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentationEnhancementRequest {

    /**
     * The rule content or pattern to enhance with documentation.
     */
    @NotBlank(message = "Rule pattern cannot be empty")
    @Size(min = 5, max = 2000, message = "Rule pattern must be between 5 and 2000 characters")
    private String rulePattern;
    
    /**
     * Optional specific topic to retrieve documentation for.
     */
    private String topic;
    
    /**
     * Optional library name to use for documentation.
     */
    private String libraryName;
    
    /**
     * Optional flag to include code examples in the documentation.
     */
    private Boolean includeCodeExamples;
    
    /**
     * Optional flag to include best practices in the documentation.
     */
    private Boolean includeBestPractices;
    
    /**
     * Optional maximum number of tokens to retrieve.
     */
    private Integer maxTokens;
}