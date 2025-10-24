package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request object for the Sequential Thinking API.
 * This class maps to the expected JSON structure for creating or continuing a sequential thought process.
 */
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SequentialThinkingRequest {

    /**
     * The total number of thoughts in the sequence.
     */
    @JsonProperty("totalThoughts")
    private int totalThoughts;

    /**
     * The current thought content.
     */
    @JsonProperty("thought")
    private String thought;
    
    /**
     * The number/position of the current thought in the sequence.
     */
    @JsonProperty("thoughtNumber")
    private int thoughtNumber;
    
    /**
     * Indicates if the next thought in the sequence is needed.
     */
    @JsonProperty("nextThoughtNeeded")
    private boolean nextThoughtNeeded;
    
    /**
     * The model to use for processing this thought.
     */
    @JsonProperty("model")
    private String model;
    
    /**
     * Indicates if this thought is a revision of a previous thought.
     */
    @JsonProperty("isRevision")
    private Boolean isRevision;
    
    /**
     * If this is a revision, indicates which thought is being revised.
     */
    @JsonProperty("revisesThought")
    private Integer revisesThought;
    
    /**
     * Indicates if more thoughts are needed beyond the current sequence.
     */
    @JsonProperty("needsMoreThoughts")
    private Boolean needsMoreThoughts;
    
    /**
     * If this thought branches from another thought, indicates the thought number it branches from.
     */
    @JsonProperty("branchFromThought")
    private Integer branchFromThought;
    
    /**
     * Additional context for the thinking process.
     */
    @JsonProperty("context")
    private String context;
}