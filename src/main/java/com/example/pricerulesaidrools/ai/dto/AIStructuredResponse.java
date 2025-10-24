package com.example.pricerulesaidrools.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic wrapper for AI responses with metadata and validation information.
 * Provides a structured container for parsed LLM outputs with additional context.
 *
 * @param <T> The type of the parsed data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIStructuredResponse<T> {

    /**
     * The parsed data from the LLM response
     */
    private T data;

    /**
     * Confidence score of the AI response (0.0 to 1.0)
     */
    private Double confidence;

    /**
     * Additional metadata about the response
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Timestamp when the response was parsed
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * List of validation errors, if any
     */
    @Builder.Default
    private List<String> validationErrors = new ArrayList<>();

    /**
     * The original raw response from the LLM
     */
    private String rawResponse;

    /**
     * The model that generated this response
     */
    private String modelId;

    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;

    /**
     * Whether the response passed all validations
     */
    @Builder.Default
    private boolean valid = true;

    /**
     * Add a validation error to the response
     */
    public void addValidationError(String error) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(error);
        this.valid = false;
    }

    /**
     * Add metadata to the response
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Check if the response has validation errors
     */
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    /**
     * Get validation errors as a formatted string
     */
    public String getFormattedValidationErrors() {
        if (!hasValidationErrors()) {
            return "No validation errors";
        }
        return String.join("; ", validationErrors);
    }
}