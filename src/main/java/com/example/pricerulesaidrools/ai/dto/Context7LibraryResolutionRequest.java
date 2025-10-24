package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Context7 library resolution.
 * This class is used to resolve a documentation library name to a library ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Context7LibraryResolutionRequest {

    /**
     * The name of the library to resolve.
     */
    @JsonProperty("libraryName")
    private String libraryName;
    
    /**
     * Optional version specification for the library.
     */
    @JsonProperty("version")
    private String version;
}