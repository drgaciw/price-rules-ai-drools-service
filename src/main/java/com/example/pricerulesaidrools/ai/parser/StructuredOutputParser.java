package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;

/**
 * Interface for parsing LLM responses into structured, type-safe Java objects.
 * Provides methods for parsing with and without validation.
 */
public interface StructuredOutputParser {

    /**
     * Parses an LLM response string into a typed Java object.
     *
     * @param <T> The target type for parsing
     * @param llmResponse The raw LLM response string (expected to be JSON format)
     * @param targetClass The target class to parse into
     * @return The parsed object of type T
     * @throws IllegalArgumentException if parsing fails
     */
    <T> T parse(String llmResponse, Class<T> targetClass);

    /**
     * Parses an LLM response with validation of the resulting object.
     *
     * @param <T> The target type for parsing
     * @param llmResponse The raw LLM response string (expected to be JSON format)
     * @param targetClass The target class to parse into
     * @return The parsed and validated object of type T
     * @throws IllegalArgumentException if parsing or validation fails
     */
    <T> T parseWithValidation(String llmResponse, Class<T> targetClass);

    /**
     * Parses an LLM response into a structured response wrapper with metadata.
     *
     * @param <T> The target type for parsing
     * @param llmResponse The raw LLM response string
     * @param targetClass The target class for the data field
     * @param confidence The confidence score of the AI response (0.0 to 1.0)
     * @return AIStructuredResponse wrapper containing parsed data and metadata
     */
    <T> AIStructuredResponse<T> parseToStructuredResponse(String llmResponse, Class<T> targetClass, Double confidence);
}