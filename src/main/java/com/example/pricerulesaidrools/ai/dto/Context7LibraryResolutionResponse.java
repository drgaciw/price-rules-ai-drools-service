package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Context7 library resolution.
 * This class contains information about resolved documentation libraries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context7LibraryResolutionResponse {

    /**
     * List of resolved libraries.
     */
    @JsonProperty("libraries")
    private List<Library> libraries;
    
    /**
     * Status of the resolution request.
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * Message providing additional information about the resolution.
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * Nested class representing a documentation library.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Library {
        
        /**
         * The unique identifier for the library.
         */
        @JsonProperty("id")
        private String id;
        
        /**
         * The name of the library.
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * The version of the library.
         */
        @JsonProperty("version")
        private String version;
        
        /**
         * Description of the library.
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * The relevance score of this library to the original query.
         */
        @JsonProperty("relevance")
        private Double relevance;
    }
}