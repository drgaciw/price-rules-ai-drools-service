package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.config.Context7Config;
import com.example.pricerulesaidrools.ai.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the Context7Service interface.
 * This class handles the actual communication with the Context7 API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Context7ServiceImpl implements Context7Service {

    private final Context7Config config;
    private final RestTemplate context7RestTemplate;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Context7LibraryResolutionResponse resolveLibraryId(String libraryName, String version) {
        log.debug("Resolving library ID for library: {}, version: {}", libraryName, version);
        
        try {
            HttpHeaders headers = createHeaders();
            
            Context7LibraryResolutionRequest request = Context7LibraryResolutionRequest.builder()
                    .libraryName(libraryName)
                    .version(version)
                    .build();
            
            HttpEntity<Context7LibraryResolutionRequest> entity = new HttpEntity<>(request, headers);
            
            return context7RestTemplate.postForObject(
                    config.getApiUrl() + "/resolve-library", 
                    entity, 
                    Context7LibraryResolutionResponse.class);
        } catch (RestClientException e) {
            log.error("Error resolving library ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve library ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public CompletableFuture<Context7LibraryResolutionResponse> resolveLibraryIdAsync(String libraryName, String version) {
        return CompletableFuture.completedFuture(resolveLibraryId(libraryName, version));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Context7DocumentationResponse getDocumentation(
            String libraryId,
            String topic,
            Integer tokens,
            Boolean includeCodeExamples,
            Boolean includeBestPractices) {
        
        log.debug("Retrieving documentation for topic: {}, from library: {}", topic, libraryId);
        
        try {
            HttpHeaders headers = createHeaders();
            
            Context7DocumentationRequest request = Context7DocumentationRequest.builder()
                    .libraryId(libraryId)
                    .topic(topic)
                    .tokens(tokens != null ? tokens : config.getDefaultTokenLimit())
                    .includeCodeExamples(includeCodeExamples)
                    .includeBestPractices(includeBestPractices)
                    .build();
            
            HttpEntity<Context7DocumentationRequest> entity = new HttpEntity<>(request, headers);
            
            return context7RestTemplate.postForObject(
                    config.getApiUrl() + "/get-documentation", 
                    entity, 
                    Context7DocumentationResponse.class);
        } catch (RestClientException e) {
            log.error("Error retrieving documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve documentation: " + e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public CompletableFuture<Context7DocumentationResponse> getDocumentationAsync(
            String libraryId,
            String topic,
            Integer tokens,
            Boolean includeCodeExamples,
            Boolean includeBestPractices) {
        
        return CompletableFuture.completedFuture(getDocumentation(
                libraryId, topic, tokens, includeCodeExamples, includeBestPractices));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Context7DocumentationResponse getDroolsDocumentation(String topic) {
        log.debug("Retrieving Drools documentation for topic: {}", topic);
        
        try {
            // Resolve library ID for Drools
            Context7LibraryResolutionResponse libraryResolution = resolveLibraryId(
                    config.getDefaultLibraryName(), null);
            
            if (libraryResolution == null || 
                libraryResolution.getLibraries() == null || 
                libraryResolution.getLibraries().isEmpty()) {
                
                log.error("No library found for name: {}", config.getDefaultLibraryName());
                throw new RuntimeException("Failed to resolve library ID for Drools");
            }
            
            // Extract the appropriate library ID
            String droolsLibraryId = libraryResolution.getLibraries().get(0).getId();
            
            // Retrieve documentation
            return getDocumentation(
                    droolsLibraryId,
                    topic,
                    config.getDefaultTokenLimit(),
                    true,
                    true);
        } catch (Exception e) {
            log.error("Error retrieving Drools documentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve Drools documentation: " + e.getMessage(), e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentationEnhancementResponse enhanceRuleWithDocumentation(DocumentationEnhancementRequest request) {
        log.info("Enhancing rule with documentation for pattern: {}", request.getRulePattern());
        
        try {
            // Determine the topic based on the rule pattern or specified topic
            String topic = request.getTopic() != null ? request.getTopic() : 
                    "drools rule for " + request.getRulePattern();
            
            // Get documentation
            Context7DocumentationResponse documentation = getDroolsDocumentation(topic);
            
            // Extract code examples and best practices
            List<Context7DocumentationResponse.CodeExample> codeExamples = 
                    documentation.getCodeExamples() != null ? documentation.getCodeExamples() : new ArrayList<>();
            
            List<Context7DocumentationResponse.BestPractice> bestPractices = 
                    documentation.getBestPractices() != null ? documentation.getBestPractices() : new ArrayList<>();
            
            // Generate improvement suggestions
            List<DocumentationEnhancementResponse.ImprovementSuggestion> suggestions = 
                    generateImprovementSuggestions(request.getRulePattern(), documentation.getContent(), 
                            codeExamples, bestPractices);
            
            // Build enhanced rule content (simplified version - in production would use more sophisticated analysis)
            String enhancedRuleContent = generateEnhancedRuleContent(
                    request.getRulePattern(), documentation.getContent(), codeExamples, bestPractices);
            
            // Build response
            return DocumentationEnhancementResponse.builder()
                    .originalRulePattern(request.getRulePattern())
                    .documentation(documentation.getContent())
                    .codeExamples(codeExamples)
                    .bestPractices(bestPractices)
                    .improvementSuggestions(suggestions)
                    .enhancedRuleContent(enhancedRuleContent)
                    .enhancementExplanation("Rule enhanced with " + codeExamples.size() + " code examples and " +
                            bestPractices.size() + " best practices from documentation.")
                    .enhancedWithDocumentation(true)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error enhancing rule with documentation: {}", e.getMessage(), e);
            
            // Return basic response with error information
            return DocumentationEnhancementResponse.builder()
                    .originalRulePattern(request.getRulePattern())
                    .enhancedWithDocumentation(false)
                    .enhancementExplanation("Error enhancing rule: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentationEnhancementResponse extractExamples(String documentationContent) {
        log.debug("Extracting code examples from documentation");
        
        try {
            // Extract code blocks using regex pattern
            List<Context7DocumentationResponse.CodeExample> codeExamples = new ArrayList<>();
            
            // Pattern to match code blocks in markdown: ```language\ncode\n```
            Pattern codePattern = Pattern.compile("```([a-zA-Z0-9]+)\\s*\\n([\\s\\S]*?)\\n```");
            Matcher matcher = codePattern.matcher(documentationContent);
            
            while (matcher.find()) {
                String language = matcher.group(1);
                String code = matcher.group(2);
                
                codeExamples.add(Context7DocumentationResponse.CodeExample.builder()
                        .language(language)
                        .code(code)
                        .description("Extracted code example in " + language)
                        .relevance(1.0)
                        .build());
            }
            
            return DocumentationEnhancementResponse.builder()
                    .codeExamples(codeExamples)
                    .enhancedWithDocumentation(true)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error extracting examples: {}", e.getMessage(), e);
            return DocumentationEnhancementResponse.builder()
                    .enhancedWithDocumentation(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentationEnhancementResponse generateSuggestions(String rulePattern, String documentation) {
        log.debug("Generating improvement suggestions for rule pattern");
        
        try {
            // Extract code examples first
            DocumentationEnhancementResponse examples = extractExamples(documentation);
            
            // Simple pattern matching for best practices
            List<Context7DocumentationResponse.BestPractice> bestPractices = new ArrayList<>();
            
            // Pattern to match sections that might contain best practices
            Pattern bestPracticePattern = Pattern.compile("(?i)(best practice|recommended|tip|advice|important)([^#]+)");
            Matcher matcher = bestPracticePattern.matcher(documentation);
            
            while (matcher.find()) {
                String title = matcher.group(1).trim();
                String description = matcher.group(2).trim();
                
                bestPractices.add(Context7DocumentationResponse.BestPractice.builder()
                        .title(title)
                        .description(description)
                        .relevance(1.0)
                        .build());
            }
            
            // Generate improvement suggestions based on extracted examples and best practices
            List<DocumentationEnhancementResponse.ImprovementSuggestion> suggestions = 
                    generateImprovementSuggestions(rulePattern, documentation, 
                            examples.getCodeExamples(), bestPractices);
            
            return DocumentationEnhancementResponse.builder()
                    .originalRulePattern(rulePattern)
                    .codeExamples(examples.getCodeExamples())
                    .bestPractices(bestPractices)
                    .improvementSuggestions(suggestions)
                    .enhancedWithDocumentation(true)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error generating suggestions: {}", e.getMessage(), e);
            return DocumentationEnhancementResponse.builder()
                    .originalRulePattern(rulePattern)
                    .enhancedWithDocumentation(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
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
    
    /**
     * Helper method to generate improvement suggestions.
     * 
     * @param rulePattern The rule pattern
     * @param documentation The documentation content
     * @param codeExamples List of code examples
     * @param bestPractices List of best practices
     * @return List of improvement suggestions
     */
    private List<DocumentationEnhancementResponse.ImprovementSuggestion> generateImprovementSuggestions(
            String rulePattern, 
            String documentation,
            List<Context7DocumentationResponse.CodeExample> codeExamples,
            List<Context7DocumentationResponse.BestPractice> bestPractices) {
        
        List<DocumentationEnhancementResponse.ImprovementSuggestion> suggestions = new ArrayList<>();
        
        // Add suggestions based on code examples
        if (codeExamples != null) {
            suggestions.addAll(codeExamples.stream()
                    .filter(example -> example.getCode() != null && !example.getCode().isEmpty())
                    .map(example -> DocumentationEnhancementResponse.ImprovementSuggestion.builder()
                            .title("Consider this code example")
                            .description("This example demonstrates a pattern that could be applied to your rule")
                            .exampleCode(example.getCode())
                            .relevance(example.getRelevance())
                            .build())
                    .collect(Collectors.toList()));
        }
        
        // Add suggestions based on best practices
        if (bestPractices != null) {
            suggestions.addAll(bestPractices.stream()
                    .filter(practice -> practice.getDescription() != null && !practice.getDescription().isEmpty())
                    .map(practice -> DocumentationEnhancementResponse.ImprovementSuggestion.builder()
                            .title(practice.getTitle() != null ? practice.getTitle() : "Best Practice")
                            .description(practice.getDescription())
                            .relevance(practice.getRelevance())
                            .build())
                    .collect(Collectors.toList()));
        }
        
        return suggestions;
    }
    
    /**
     * Helper method to generate enhanced rule content.
     * 
     * @param rulePattern The original rule pattern
     * @param documentation The documentation content
     * @param codeExamples List of code examples
     * @param bestPractices List of best practices
     * @return Enhanced rule content
     */
    private String generateEnhancedRuleContent(
            String rulePattern,
            String documentation,
            List<Context7DocumentationResponse.CodeExample> codeExamples,
            List<Context7DocumentationResponse.BestPractice> bestPractices) {
        
        // In a real implementation, this would use more sophisticated analysis to truly enhance the rule
        // For this example, we'll just add some documentation comments based on best practices
        
        StringBuilder enhancedRule = new StringBuilder();
        enhancedRule.append("/**\n");
        enhancedRule.append(" * Enhanced rule based on documentation\n");
        enhancedRule.append(" *\n");
        
        // Add best practices as documentation
        if (bestPractices != null && !bestPractices.isEmpty()) {
            enhancedRule.append(" * Best Practices:\n");
            for (int i = 0; i < Math.min(3, bestPractices.size()); i++) {
                Context7DocumentationResponse.BestPractice practice = bestPractices.get(i);
                String title = practice.getTitle() != null ? practice.getTitle() : "Best Practice";
                enhancedRule.append(" * - ").append(title).append(": ")
                        .append(truncate(practice.getDescription(), 100)).append("\n");
            }
            enhancedRule.append(" *\n");
        }
        
        enhancedRule.append(" */\n");
        
        // Add the original rule pattern
        enhancedRule.append(rulePattern);
        
        // If we have code examples, add the most relevant one as a reference
        if (codeExamples != null && !codeExamples.isEmpty()) {
            enhancedRule.append("\n\n/* Reference Example:\n");
            Context7DocumentationResponse.CodeExample example = codeExamples.get(0);
            enhancedRule.append(example.getCode());
            enhancedRule.append("\n*/");
        }
        
        return enhancedRule.toString();
    }
    
    /**
     * Helper method to truncate a string to a maximum length.
     * 
     * @param str The string to truncate
     * @param maxLength The maximum length
     * @return Truncated string
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}