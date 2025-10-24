package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for retrieving documentation from Context7.
 * This class is used to query for specific documentation on a topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Context7DocumentationRequest {

    /**
     * The library ID to retrieve documentation from.
     */
    @JsonProperty("context7CompatibleLibraryID")
    private String libraryId;
    
    /**
     * The topic to retrieve documentation for.
     */
    @JsonProperty("topic")
    private String topic;
    
    /**
     * The maximum number of tokens to retrieve.
     */
    @JsonProperty("tokens")
    private Integer tokens;
    
    /**
     * Optional flag to indicate whether to include code examples.
     */
    @JsonProperty("includeCodeExamples")
    private Boolean includeCodeExamples;
    
    /**
     * Optional flag to indicate whether to include best practices.
     */
    @JsonProperty("includeBestPractices")
    private Boolean includeBestPractices;
}