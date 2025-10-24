package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.config.SequentialThinkingConfig;
import com.example.pricerulesaidrools.ai.dto.SequentialThinkingRequest;
import com.example.pricerulesaidrools.ai.dto.SequentialThinkingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the SequentialThinkingService interface.
 * This class handles the actual communication with the Sequential Thinking API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SequentialThinkingServiceImpl implements SequentialThinkingService {

    private final SequentialThinkingConfig config;
    private final RestTemplate sequentialThinkingRestTemplate;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SequentialThinkingResponse processThought(SequentialThinkingRequest request) {
        log.debug("Processing thought: {}", request.getThought());
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<SequentialThinkingRequest> entity = new HttpEntity<>(request, headers);
            
            // If model is not specified in the request, use the default from config
            if (request.getModel() == null) {
                request.setModel(config.getModel());
            }
            
            return sequentialThinkingRestTemplate.postForObject(
                    config.getApiUrl(), 
                    entity, 
                    SequentialThinkingResponse.class);
        } catch (RestClientException e) {
            log.error("Error processing thought: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process thought: " + e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public CompletableFuture<SequentialThinkingResponse> processThoughtAsync(SequentialThinkingRequest request) {
        return CompletableFuture.completedFuture(processThought(request));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<SequentialThinkingResponse> executeThoughtSequence(
            String initialThought, 
            int totalThoughts, 
            String context) {
        
        log.info("Executing thought sequence. Initial thought: {}", initialThought);
        List<SequentialThinkingResponse> thoughtChain = new ArrayList<>();
        
        // Create the first thought request
        SequentialThinkingRequest request = SequentialThinkingRequest.builder()
                .totalThoughts(totalThoughts)
                .thought(initialThought)
                .thoughtNumber(1)
                .nextThoughtNeeded(true)
                .context(context)
                .model(config.getModel())
                .build();
        
        // Process the first thought
        SequentialThinkingResponse response = processThought(request);
        thoughtChain.add(response);
        
        // Continue the thought chain until completion or max thoughts reached
        int currentThoughtNumber = 2;
        while (response.isNeedsMoreThoughts() && currentThoughtNumber <= totalThoughts) {
            // Use the next thought from the previous response
            request = SequentialThinkingRequest.builder()
                    .totalThoughts(totalThoughts)
                    .thought(response.getNextThought())
                    .thoughtNumber(currentThoughtNumber)
                    .nextThoughtNeeded(currentThoughtNumber < totalThoughts)
                    .context(context)
                    .model(config.getModel())
                    .build();
            
            response = processThought(request);
            thoughtChain.add(response);
            currentThoughtNumber++;
        }
        
        log.info("Thought sequence completed. Total thoughts: {}", thoughtChain.size());
        return thoughtChain;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SequentialThinkingResponse analyzeBusinessRequirement(String businessRequirement) {
        log.info("Analyzing business requirement");
        
        // Create a thought request focused on requirement analysis
        SequentialThinkingRequest request = SequentialThinkingRequest.builder()
                .totalThoughts(1)
                .thought("Let's break down this pricing rule requirement into its core components: " + businessRequirement)
                .thoughtNumber(1)
                .nextThoughtNeeded(false)
                .model(config.getModel())
                .build();
        
        return processThought(request);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SequentialThinkingResponse generateRule(String businessRequirement, String ruleType) {
        log.info("Generating rule from business requirement. Rule type: {}", ruleType);
        
        // Determine the total number of thoughts based on complexity
        int totalThoughts = config.getMaxThoughts();
        
        // Initial thought focused on rule generation
        String initialThought = "Let's break down this " + 
                (ruleType != null ? ruleType + " " : "") + 
                "pricing rule requirement into components: " + businessRequirement;
        
        // Execute the complete thought sequence
        List<SequentialThinkingResponse> thoughtChain = executeThoughtSequence(
                initialThought, 
                totalThoughts, 
                businessRequirement);
        
        // Return the final thought which should contain the complete rule
        return thoughtChain.get(thoughtChain.size() - 1);
    }
    
    /**
     * Creates HTTP headers for API requests.
     * 
     * @return HttpHeaders with proper content type and authorization
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());
        return headers;
    }
}