package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response object from the Sequential Thinking API.
 * This class maps to the JSON structure returned by the Sequential Thinking service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequentialThinkingResponse {

    /**
     * The original thought that was processed.
     */
    @JsonProperty("originalThought")
    private String originalThought;
    
    /**
     * The thought number in the sequence.
     */
    @JsonProperty("thoughtNumber")
    private int thoughtNumber;
    
    /**
     * The next thought in the sequence.
     */
    @JsonProperty("nextThought")
    private String nextThought;
    
    /**
     * A suggested thought number for the next thought.
     */
    @JsonProperty("suggestedNextThoughtNumber")
    private Integer suggestedNextThoughtNumber;
    
    /**
     * Indicates if more thoughts are needed in this sequence.
     */
    @JsonProperty("needsMoreThoughts")
    private boolean needsMoreThoughts;
    
    /**
     * The reasoning behind the current thought.
     */
    @JsonProperty("reasoning")
    private String reasoning;
    
    /**
     * Extracted components from the thought, useful for rule creation.
     */
    @JsonProperty("components")
    private List<String> components;
    
    /**
     * Generated rule structure (for rule creation thoughts).
     */
    @JsonProperty("ruleStructure")
    private Map<String, Object> ruleStructure;
    
    /**
     * Generated rule name (for rule creation thoughts).
     */
    @JsonProperty("ruleName")
    private String ruleName;
    
    /**
     * Generated rule conditions (for rule creation thoughts).
     */
    @JsonProperty("conditions")
    private List<String> conditions;
    
    /**
     * Generated rule actions (for rule creation thoughts).
     */
    @JsonProperty("actions")
    private List<String> actions;
    
    /**
     * Complete Drools rule content (for final thoughts).
     */
    @JsonProperty("ruleContent")
    private String ruleContent;
    
    /**
     * Test cases for validating the rule (for testing thoughts).
     */
    @JsonProperty("testCases")
    private List<Map<String, Object>> testCases;
    
    /**
     * Updated rule content (for revision thoughts).
     */
    @JsonProperty("updatedRule")
    private String updatedRule;
}