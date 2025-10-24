package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;
import com.example.pricerulesaidrools.model.PricingResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for StructuredOutputParser implementation.
 * Tests various scenarios including valid parsing, error handling, and validation.
 */
@ExtendWith(MockitoExtension.class)
class StructuredOutputParserTest {

    private StructuredOutputParser parser;
    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        parser = new StructuredOutputParserImpl(validator);
        objectMapper = new ObjectMapper();
    }

    // Test DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestDTO {
        private String name;
        private Integer age;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class ValidatedTestDTO {
        @NotNull(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @Min(value = 0, message = "Age must be non-negative")
        private Integer age;

        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class NestedTestDTO {
        private String id;
        private TestDTO nested;
        private List<String> tags;
    }

    @Test
    @DisplayName("Should parse valid JSON to simple DTO")
    void testParseValidJsonToSimpleDTO() {
        // Given
        String llmResponse = """
            {
                "name": "John Doe",
                "age": 30,
                "active": true
            }
            """;

        // When
        TestDTO result = parser.parse(llmResponse, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getAge()).isEqualTo(30);
        assertThat(result.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should parse JSON from code block")
    void testParseJsonFromCodeBlock() {
        // Given
        String llmResponse = """
            Here is the pricing result:

            ```json
            {
                "discount": 0.15,
                "finalPrice": 850.00,
                "calculationComplete": true,
                "priceMultiplier": 0.85,
                "discountDescription": "Volume discount applied",
                "minimumCommitment": 10000.0,
                "commitmentTier": "Gold",
                "appliedRules": ["VOLUME_DISCOUNT", "LOYALTY_BONUS"],
                "includedServices": ["Premium Support", "Advanced Analytics"]
            }
            ```

            This represents the calculated pricing.
            """;

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.15);
        assertThat(result.getFinalPrice()).isEqualTo(850.00);
        assertThat(result.isCalculationComplete()).isTrue();
        assertThat(result.getAppliedRules()).containsExactly("VOLUME_DISCOUNT", "LOYALTY_BONUS");
        assertThat(result.getIncludedServices()).containsExactly("Premium Support", "Advanced Analytics");
    }

    @Test
    @DisplayName("Should parse nested objects correctly")
    void testParseNestedObjects() {
        // Given
        String llmResponse = """
            {
                "id": "test-123",
                "nested": {
                    "name": "Nested Object",
                    "age": 25,
                    "active": false
                },
                "tags": ["tag1", "tag2", "tag3"]
            }
            """;

        // When
        NestedTestDTO result = parser.parse(llmResponse, NestedTestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-123");
        assertThat(result.getNested()).isNotNull();
        assertThat(result.getNested().getName()).isEqualTo("Nested Object");
        assertThat(result.getNested().getAge()).isEqualTo(25);
        assertThat(result.getNested().getActive()).isFalse();
        assertThat(result.getTags()).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    @DisplayName("Should handle malformed JSON with error")
    void testParseMalformedJson() {
        // Given
        String malformedJson = """
            {
                "name": "Test",
                "age": "not a number
            }
            """;

        // When/Then
        assertThatThrownBy(() -> parser.parse(malformedJson, TestDTO.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Failed to parse");
    }

    @Test
    @DisplayName("Should validate parsed object when using parseWithValidation")
    void testParseWithValidation() {
        // Given
        String llmResponse = """
            {
                "name": "A",
                "age": -5,
                "active": true
            }
            """;

        // When/Then
        assertThatThrownBy(() -> parser.parseWithValidation(llmResponse, ValidatedTestDTO.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validation failed")
            .hasMessageContaining("Name must be between 2 and 50 characters")
            .hasMessageContaining("Age must be non-negative");
    }

    @Test
    @DisplayName("Should successfully parse and validate valid data")
    void testParseWithValidationSuccess() {
        // Given
        String llmResponse = """
            {
                "name": "Valid Name",
                "age": 25,
                "active": true
            }
            """;

        // When
        ValidatedTestDTO result = parser.parseWithValidation(llmResponse, ValidatedTestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Valid Name");
        assertThat(result.getAge()).isEqualTo(25);
        assertThat(result.getActive()).isTrue();
    }

    @Test
    @DisplayName("Should create structured response with metadata")
    void testParseToStructuredResponse() {
        // Given
        String llmResponse = """
            {
                "name": "Test User",
                "age": 30,
                "active": true
            }
            """;
        Double confidence = 0.95;

        // When
        AIStructuredResponse<TestDTO> response = parser.parseToStructuredResponse(
            llmResponse, TestDTO.class, confidence
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getName()).isEqualTo("Test User");
        assertThat(response.getConfidence()).isEqualTo(0.95);
        assertThat(response.isValid()).isTrue();
        assertThat(response.getValidationErrors()).isEmpty();
        assertThat(response.getRawResponse()).isEqualTo(llmResponse);
        assertThat(response.getProcessingTimeMs()).isNotNull();
        assertThat(response.getMetadata()).containsKey("targetClass");
        assertThat(response.getMetadata()).containsKey("processingTimeMs");
    }

    @Test
    @DisplayName("Should handle partial JSON with validation errors")
    void testParsePartialJsonWithErrors() {
        // Given - JSON with missing required fields
        String partialJson = """
            {
                "age": 25
            }
            """;

        // When
        AIStructuredResponse<ValidatedTestDTO> response = parser.parseToStructuredResponse(
            partialJson, ValidatedTestDTO.class, 0.5
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.hasValidationErrors()).isTrue();
        assertThat(response.getValidationErrors()).isNotEmpty();
        assertThat(response.getValidationErrors().get(0)).contains("Validation failed");
    }

    @Test
    @DisplayName("Should handle null or empty input")
    void testParseNullOrEmptyInput() {
        // When/Then - null input
        assertThatThrownBy(() -> parser.parse(null, TestDTO.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");

        // When/Then - empty input
        assertThatThrownBy(() -> parser.parse("", TestDTO.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");

        // When/Then - whitespace only
        assertThatThrownBy(() -> parser.parse("   ", TestDTO.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    @DisplayName("Should parse arrays correctly")
    void testParseArrays() {
        // Given
        String llmResponse = """
            [
                {"name": "Item1", "age": 20, "active": true},
                {"name": "Item2", "age": 30, "active": false},
                {"name": "Item3", "age": 40, "active": true}
            ]
            """;

        // When
        TestDTO[] result = parser.parse(llmResponse, TestDTO[].class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result[0].getName()).isEqualTo("Item1");
        assertThat(result[1].getAge()).isEqualTo(30);
        assertThat(result[2].getActive()).isTrue();
    }

    @Test
    @DisplayName("Should handle LLM response with extra text around JSON")
    void testParseJsonWithSurroundingText() {
        // Given
        String llmResponse = """
            Based on the customer's profile and purchase history, here is the calculated pricing:

            {
                "discount": 0.20,
                "finalPrice": 800.00,
                "calculationComplete": true,
                "priceMultiplier": 0.80,
                "discountDescription": "Premium customer discount",
                "appliedRules": ["PREMIUM_CUSTOMER"]
            }

            This pricing reflects a 20% discount for premium customers.
            """;

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.20);
        assertThat(result.getFinalPrice()).isEqualTo(800.00);
        assertThat(result.getDiscountDescription()).isEqualTo("Premium customer discount");
    }

    @Test
    @DisplayName("Should extract JSON from markdown code block without json tag")
    void testParseFromMarkdownCodeBlock() {
        // Given
        String llmResponse = """
            Here's the response:

            ```
            {
                "name": "Test",
                "age": 25,
                "active": true
            }
            ```
            """;

        // When
        TestDTO result = parser.parse(llmResponse, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getAge()).isEqualTo(25);
    }

    @Test
    @DisplayName("Should handle complex PricingResult with all fields")
    void testParseComplexPricingResult() {
        // Given
        String llmResponse = """
            ```json
            {
                "discount": 0.25,
                "finalPrice": 7500.00,
                "calculationComplete": true,
                "priceMultiplier": 0.75,
                "discountDescription": "Enterprise volume discount with annual commitment",
                "minimumCommitment": 100000.0,
                "commitmentTier": "Enterprise",
                "appliedRules": [
                    "ENTERPRISE_DISCOUNT",
                    "ANNUAL_COMMITMENT",
                    "VOLUME_TIER_3"
                ],
                "includedServices": [
                    "24/7 Support",
                    "Dedicated Account Manager",
                    "Custom Integration",
                    "Training Sessions"
                ]
            }
            ```
            """;

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.25);
        assertThat(result.getFinalPrice()).isEqualTo(7500.00);
        assertThat(result.isCalculationComplete()).isTrue();
        assertThat(result.getPriceMultiplier()).isEqualTo(0.75);
        assertThat(result.getDiscountDescription()).isEqualTo("Enterprise volume discount with annual commitment");
        assertThat(result.getMinimumCommitment()).isEqualTo(100000.0);
        assertThat(result.getCommitmentTier()).isEqualTo("Enterprise");
        assertThat(result.getAppliedRules()).hasSize(3);
        assertThat(result.getIncludedServices()).hasSize(4);
    }

    @Test
    @DisplayName("Should capture processing time in structured response")
    void testProcessingTimeCapture() {
        // Given
        String llmResponse = """
            {
                "name": "Performance Test",
                "age": 25,
                "active": true
            }
            """;

        // When
        AIStructuredResponse<TestDTO> response = parser.parseToStructuredResponse(
            llmResponse, TestDTO.class, 0.9
        );

        // Then
        assertThat(response.getProcessingTimeMs()).isNotNull();
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(response.getMetadata().get("processingTimeMs")).isNotNull();
    }
}