package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of StructuredOutputParser using Spring AI's BeanOutputParser.
 * Provides type-safe parsing of LLM responses into Java objects with validation.
 */
@Slf4j
@Service
public class StructuredOutputParserImpl implements StructuredOutputParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    private static final Pattern JSON_PATTERN = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.MULTILINE);
    private static final Pattern INLINE_JSON_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}", Pattern.MULTILINE);

    @Autowired(required = false)
    public StructuredOutputParserImpl(Validator validator) {
        this.validator = validator;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public StructuredOutputParserImpl() {
        this(null);
    }

    @Override
    public <T> T parse(String llmResponse, Class<T> targetClass) {
        log.debug("Parsing LLM response to class: {}", targetClass.getName());

        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM response cannot be null or empty");
        }

        long startTime = System.currentTimeMillis();

        try {
            // Extract JSON from the response
            String jsonContent = extractJson(llmResponse);

            // Use Jackson ObjectMapper for parsing (simulating BeanOutputParser behavior)
            T result = objectMapper.readValue(jsonContent, targetClass);

            long processingTime = System.currentTimeMillis() - startTime;
            log.debug("Successfully parsed response to {} in {}ms", targetClass.getSimpleName(), processingTime);

            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM response to {}: {}", targetClass.getName(), e.getMessage());

            throw new IllegalArgumentException(
                String.format("Failed to parse LLM response to %s: %s",
                    targetClass.getSimpleName(),
                    e.getMessage()),
                e
            );
        }
    }

    @Override
    public <T> T parseWithValidation(String llmResponse, Class<T> targetClass) {
        log.debug("Parsing with validation for class: {}", targetClass.getName());

        // First, parse the response
        T result = parse(llmResponse, targetClass);

        // Then validate if validator is available
        if (validator != null) {
            Set<ConstraintViolation<T>> violations = validator.validate(result);

            if (!violations.isEmpty()) {
                List<String> errors = new ArrayList<>();
                for (ConstraintViolation<T> violation : violations) {
                    String error = String.format("%s: %s",
                        violation.getPropertyPath(),
                        violation.getMessage()
                    );
                    errors.add(error);
                    log.warn("Validation error: {}", error);
                }

                throw new IllegalArgumentException(
                    String.format("Validation failed with %d errors: %s",
                        errors.size(),
                        String.join("; ", errors))
                );
            }

            log.debug("Validation passed for parsed object");
        } else {
            log.debug("No validator available, skipping validation");
        }

        return result;
    }

    @Override
    public <T> AIStructuredResponse<T> parseToStructuredResponse(String llmResponse, Class<T> targetClass, Double confidence) {
        log.debug("Parsing to structured response for class: {}", targetClass.getName());

        long startTime = System.currentTimeMillis();
        AIStructuredResponse<T> response = new AIStructuredResponse<>();
        response.setRawResponse(llmResponse);
        response.setTimestamp(LocalDateTime.now());
        response.setConfidence(confidence != null ? confidence : 0.5);

        try {
            // Try parsing with validation
            T data = parseWithValidation(llmResponse, targetClass);
            response.setData(data);
            response.setValid(true);

            // Add parsing metadata
            response.addMetadata("targetClass", targetClass.getName());
            response.addMetadata("parsingMethod", "BeanOutputParser");

        } catch (IllegalArgumentException e) {
            log.warn("Parsing with validation failed, attempting without validation: {}", e.getMessage());

            try {
                // Try parsing without validation
                T data = parse(llmResponse, targetClass);
                response.setData(data);
                response.setValid(false);
                response.addValidationError("Validation failed: " + e.getMessage());
                response.addMetadata("parsingMethod", "BeanOutputParser-NoValidation");

            } catch (Exception parseException) {
                log.error("All parsing attempts failed: {}", parseException.getMessage());
                response.setValid(false);
                response.addValidationError("Parsing failed: " + parseException.getMessage());
                response.setData(null);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        response.setProcessingTimeMs(processingTime);
        response.addMetadata("processingTimeMs", processingTime);

        log.debug("Structured response created in {}ms, valid: {}", processingTime, response.isValid());

        return response;
    }

    /**
     * Extracts JSON content from an LLM response.
     * Handles various formats including code blocks and inline JSON.
     */
    private String extractJson(String llmResponse) {
        // First try to extract from code blocks
        Matcher codeMatcher = JSON_PATTERN.matcher(llmResponse);
        if (codeMatcher.find()) {
            String extracted = codeMatcher.group(1).trim();
            log.debug("Extracted JSON from code block");
            return extracted;
        }

        // Try to find inline JSON object
        Matcher inlineMatcher = INLINE_JSON_PATTERN.matcher(llmResponse);
        if (inlineMatcher.find()) {
            String extracted = inlineMatcher.group().trim();
            log.debug("Extracted inline JSON");
            return extracted;
        }

        // If no patterns match, assume the entire response is JSON
        log.debug("Using entire response as JSON");
        return llmResponse.trim();
    }

    /**
     * Validates if a string is valid JSON
     */
    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a sample format string for a given class to guide LLM responses
     */
    public <T> String getFormatInstructions(Class<T> targetClass) {
        try {
            // Create a sample instance with default values
            T instance = targetClass.getDeclaredConstructor().newInstance();
            String jsonSchema = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);

            return String.format(
                "Please provide your response in JSON format matching this schema:\n```json\n%s\n```\n" +
                "Ensure all required fields are populated with appropriate values.",
                jsonSchema
            );
        } catch (Exception e) {
            log.warn("Could not generate format instructions for {}: {}", targetClass.getName(), e.getMessage());
            return "Please provide your response in valid JSON format for " + targetClass.getSimpleName();
        }
    }
}