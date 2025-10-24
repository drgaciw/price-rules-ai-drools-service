package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Context7 documentation retrieval.
 * This class contains the documentation content and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context7DocumentationResponse {

    /**
     * The documentation content.
     */
    @JsonProperty("content")
    private String content;
    
    /**
     * The topic the documentation relates to.
     */
    @JsonProperty("topic")
    private String topic;
    
    /**
     * The library ID the documentation was retrieved from.
     */
    @JsonProperty("libraryId")
    private String libraryId;
    
    /**
     * The library name the documentation was retrieved from.
     */
    @JsonProperty("libraryName")
    private String libraryName;
    
    /**
     * The version of the documentation.
     */
    @JsonProperty("version")
    private String version;
    
    /**
     * List of code examples extracted from the documentation.
     */
    @JsonProperty("codeExamples")
    private List<CodeExample> codeExamples;
    
    /**
     * List of best practices extracted from the documentation.
     */
    @JsonProperty("bestPractices")
    private List<BestPractice> bestPractices;
    
    /**
     * Status of the documentation retrieval request.
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Message providing additional information about the retrieval.
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Nested class representing a code example from the documentation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeExample {
        
        /**
         * The language of the code example.
         */
        @JsonProperty("language")
        private String language;
        
        /**
         * The code content.
         */
        @JsonProperty("code")
        private String code;
        
        /**
         * Description or purpose of the code example.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The relevance score of this example to the original query.
         */
        @JsonProperty("relevance")
        private Double relevance;
    }
    
    /**
     * Nested class representing a best practice from the documentation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BestPractice {
        
        /**
         * The best practice title or summary.
         */
        @JsonProperty("title")
        private String title;
        
        /**
         * Detailed description of the best practice.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The relevance score of this best practice to the original query.
         */
        @JsonProperty("relevance")
        private Double relevance;
    }
}