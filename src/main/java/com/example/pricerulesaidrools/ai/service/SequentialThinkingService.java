package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.dto.SequentialThinkingRequest;
import com.example.pricerulesaidrools.ai.dto.SequentialThinkingResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for interactions with the Sequential Thinking API.
 * This service handles communication with the external Sequential Thinking service
 * and provides methods for various thinking operations.
 */
public interface SequentialThinkingService {

    /**
     * Processes a single sequential thinking step.
     * 
     * @param request The request containing thought details
     * @return The response with the processed thought
     */
    SequentialThinkingResponse processThought(SequentialThinkingRequest request);
    
    /**
     * Processes a single sequential thinking step asynchronously.
     * 
     * @param request The request containing thought details
     * @return A CompletableFuture that will contain the response when complete
     */
    CompletableFuture<SequentialThinkingResponse> processThoughtAsync(SequentialThinkingRequest request);
    
    /**
     * Executes a complete sequence of thoughts based on initial prompt.
     * 
     * @param initialThought The starting thought content
     * @param totalThoughts The total number of thoughts to process
     * @param context Additional context for the thinking process (optional)
     * @return List of sequential thinking responses representing the thought chain
     */
    List<SequentialThinkingResponse> executeThoughtSequence(
            String initialThought, 
            int totalThoughts, 
            String context);
    
    /**
     * Analyzes a business requirement and breaks it down into components.
     * 
     * @param businessRequirement The business requirement to analyze
     * @return The response containing the analysis and component breakdown
     */
    SequentialThinkingResponse analyzeBusinessRequirement(String businessRequirement);
    
    /**
     * Generates a rule based on a business requirement using sequential thinking.
     * 
     * @param businessRequirement The business requirement to transform into a rule
     * @param ruleType Optional rule type to guide the generation process
     * @return The final response containing the generated rule
     */
    SequentialThinkingResponse generateRule(String businessRequirement, String ruleType);
}