package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.dto.Context7DocumentationResponse;
import com.example.pricerulesaidrools.ai.dto.Context7LibraryResolutionResponse;
import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementRequest;
import com.example.pricerulesaidrools.ai.dto.DocumentationEnhancementResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for interactions with the Context7 API.
 * This service handles communication with the external Context7 documentation service
 * and provides methods for various documentation operations.
 */
public interface Context7Service {

    /**
     * Resolves a library name to a library ID.
     * 
     * @param libraryName The name of the library to resolve
     * @param version Optional version specification
     * @return The library resolution response
     */
    Context7LibraryResolutionResponse resolveLibraryId(String libraryName, String version);
    
    /**
     * Resolves a library name to a library ID asynchronously.
     * 
     * @param libraryName The name of the library to resolve
     * @param version Optional version specification
     * @return A CompletableFuture that will contain the library resolution response when complete
     */
    CompletableFuture<Context7LibraryResolutionResponse> resolveLibraryIdAsync(String libraryName, String version);
    
    /**
     * Retrieves documentation on a specific topic.
     * 
     * @param libraryId The ID of the library to retrieve documentation from
     * @param topic The topic to retrieve documentation for
     * @param tokens The maximum number of tokens to retrieve
     * @param includeCodeExamples Flag to include code examples
     * @param includeBestPractices Flag to include best practices
     * @return The documentation response
     */
    Context7DocumentationResponse getDocumentation(
            String libraryId,
            String topic,
            Integer tokens,
            Boolean includeCodeExamples,
            Boolean includeBestPractices);
    
    /**
     * Retrieves documentation on a specific topic asynchronously.
     * 
     * @param libraryId The ID of the library to retrieve documentation from
     * @param topic The topic to retrieve documentation for
     * @param tokens The maximum number of tokens to retrieve
     * @param includeCodeExamples Flag to include code examples
     * @param includeBestPractices Flag to include best practices
     * @return A CompletableFuture that will contain the documentation response when complete
     */
    CompletableFuture<Context7DocumentationResponse> getDocumentationAsync(
            String libraryId,
            String topic,
            Integer tokens,
            Boolean includeCodeExamples,
            Boolean includeBestPractices);
    
    /**
     * Convenience method to retrieve documentation using a default library.
     * 
     * @param topic The topic to retrieve documentation for
     * @return The documentation response
     */
    Context7DocumentationResponse getDroolsDocumentation(String topic);
    
    /**
     * Enhances a rule pattern with documentation insights.
     * 
     * @param request The documentation enhancement request
     * @return The documentation enhancement response
     */
    DocumentationEnhancementResponse enhanceRuleWithDocumentation(DocumentationEnhancementRequest request);
    
    /**
     * Extracts code examples from documentation content.
     * 
     * @param documentationContent The documentation content to extract examples from
     * @return The documentation enhancement response with extracted examples
     */
    DocumentationEnhancementResponse extractExamples(String documentationContent);
    
    /**
     * Generates improvement suggestions for a rule based on documentation.
     * 
     * @param rulePattern The rule pattern to generate suggestions for
     * @param documentation The documentation to use for generating suggestions
     * @return The documentation enhancement response with improvement suggestions
     */
    DocumentationEnhancementResponse generateSuggestions(String rulePattern, String documentation);
}