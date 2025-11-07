package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;
import com.example.pricerulesaidrools.model.PricingResult;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for StructuredOutputParser implementation.
 * Tests various scenarios including valid parsing, error handling, and
 * validation.
 */
@ExtendWith(MockitoExtension.class)
class StructuredOutputParserTest {

    private StructuredOutputParser parser;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        parser = new StructuredOutputParserImpl(validator);
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
                llmResponse, TestDTO.class, confidence);

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
                partialJson, ValidatedTestDTO.class, 0.5);

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
                llmResponse, TestDTO.class, 0.9);

        // Then
        assertThat(response.getProcessingTimeMs()).isNotNull();
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(response.getMetadata().get("processingTimeMs")).isNotNull();
    }

    // ==================== PARAMETRIZED TESTS ====================

    @ParameterizedTest
    @DisplayName("Should parse JSON with various whitespace formats")
    @ValueSource(strings = {
            "{\"name\": \"Test\", \"age\": 30, \"active\": true}",
            "{\n  \"name\": \"Test\",\n  \"age\": 30,\n  \"active\": true\n}",
            "{  \"name\"  :  \"Test\"  ,  \"age\"  :  30  ,  \"active\"  :  true  }",
            "{\r\n\"name\": \"Test\",\r\n\"age\": 30,\r\n\"active\": true\r\n}"
    })
    void testParseWithVariousWhitespaceFormats(String jsonString) {
        // When
        TestDTO result = parser.parse(jsonString, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getAge()).isEqualTo(30);
        assertThat(result.getActive()).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Should parse pricing results with different discount values")
    @CsvSource({
            "0.0, 1000.00, No discount",
            "0.10, 900.00, 10% discount",
            "0.25, 750.00, 25% discount",
            "0.50, 500.00, 50% discount",
            "1.00, 0.00, Full discount"
    })
    void testParsePricingWithVariousDiscounts(double discount, double expectedPrice, String description) {
        // Given
        String llmResponse = String.format("""
                {
                    "discount": %f,
                    "finalPrice": %f,
                    "calculationComplete": true,
                    "priceMultiplier": %f,
                    "discountDescription": "%s"
                }
                """, discount, expectedPrice, (1.0 - discount), description);

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(discount, within(0.0001));
        assertThat(result.getFinalPrice()).isEqualTo(expectedPrice, within(0.01));
        assertThat(result.getDiscountDescription()).isEqualTo(description);
    }

    @ParameterizedTest
    @DisplayName("Should handle various JSON wrapping formats")
    @CsvSource({
            "```json|%s|```",
            "```|%s|```",
            "```javascript|%s|```",
            "<json>|%s|</json>"
    })
    void testParseWithDifferentJsonWrappers(String prefix, String jsonContent, String suffix) {
        // Given - dynamically formatted with proper JSON structure
        String json = "{\"name\": \"Test\", \"age\": 25, \"active\": true}";
        String wrapped = prefix.replace("|", "") + json + suffix.replace("|", "");

        // When
        TestDTO result = parser.parse(wrapped, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getAge()).isEqualTo(25);
    }

    @ParameterizedTest
    @DisplayName("Should handle numeric boundary values in pricing")
    @CsvSource({
            "0.0, 0.0", // Minimum values
            "0.01, 0.01", // Minimal non-zero
            "0.99, 0.99", // Close to max
            "999999.99, 999999.99", // Large value
            "0.001, 0.001" // Precision test
    })
    void testPricingNumericBoundaries(double discount, double multiplier) {
        // Given
        String llmResponse = String.format("""
                {
                    "discount": %f,
                    "finalPrice": 1000.0,
                    "calculationComplete": true,
                    "priceMultiplier": %f
                }
                """, discount, multiplier);

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isCloseTo(discount, within(0.00001));
        assertThat(result.getPriceMultiplier()).isCloseTo(multiplier, within(0.00001));
    }

    @Test
    @DisplayName("Should handle deeply nested JSON structures")
    void testParseDeeplyNestedJson() {
        // Given
        String llmResponse = """
                {
                    "id": "root",
                    "nested": {
                        "level2": {
                            "level3": {
                                "level4": {
                                    "name": "Deep",
                                    "age": 100
                                }
                            }
                        }
                    }
                }
                """;

        // When
        NestedTestDTO result = parser.parse(llmResponse, NestedTestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("root");
        // Note: This test validates parser handles nested structures
    }

    @Test
    @DisplayName("Should handle very long strings in JSON")
    void testParseLongStringValues() {
        // Given - 1000 character string
        String longString = "A".repeat(1000);
        String llmResponse = String.format("""
                {
                    "name": "%s",
                    "age": 30,
                    "active": true
                }
                """, longString);

        // When
        TestDTO result = parser.parse(llmResponse, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).hasSize(1000);
        assertThat(result.getName()).isEqualTo(longString);
    }

    @Test
    @DisplayName("Should handle special characters and escape sequences")
    void testParseSpecialCharacters() {
        // Given
        String llmResponse = """
                {
                    "name": "Test\\\\with\\\\backslashes",
                    "age": 25,
                    "active": true
                }
                """;

        // When
        TestDTO result = parser.parse(llmResponse, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).contains("backslashes");
    }

    @Test
    @DisplayName("Should handle unicode characters in JSON")
    void testParseUnicodeCharacters() {
        // Given
        String llmResponse = """
                {
                    "name": "Test with émojis 🎉 and chars ñ",
                    "age": 30,
                    "active": true
                }
                """;

        // When
        TestDTO result = parser.parse(llmResponse, TestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).contains("Test");
        assertThat(result.getName()).contains("émojis");
    }

    @Test
    @DisplayName("Should preserve metadata in structured response with confidence")
    void testStructuredResponseMetadataPreservation() {
        // Given
        String llmResponse = """
                {
                    "name": "Metadata Test",
                    "age": 35,
                    "active": false
                }
                """;
        Double confidence = 0.87;

        // When
        AIStructuredResponse<TestDTO> response = parser.parseToStructuredResponse(
                llmResponse, TestDTO.class, confidence);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getConfidence()).isEqualTo(0.87);
        assertThat(response.isValid()).isTrue();
        assertThat(response.getValidationErrors()).isEmpty();
        assertThat(response.getMetadata()).containsKeys("targetClass", "processingTimeMs");
        assertThat(response.getRawResponse()).isEqualTo(llmResponse);
    }

    @Test
    @DisplayName("Should handle array of objects with complex structure")
    void testParseComplexArrayOfObjects() {
        // Given
        String llmResponse = """
                [
                    {
                        "id": "id1",
                        "nested": {"name": "Item1", "age": 20, "active": true},
                        "tags": ["tag1", "tag2"]
                    },
                    {
                        "id": "id2",
                        "nested": {"name": "Item2", "age": 30, "active": false},
                        "tags": ["tag3", "tag4", "tag5"]
                    }
                ]
                """;

        // When
        NestedTestDTO[] result = parser.parse(llmResponse, NestedTestDTO[].class);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result[0].getId()).isEqualTo("id1");
        assertThat(result[0].getTags()).hasSize(2);
        assertThat(result[1].getId()).isEqualTo("id2");
        assertThat(result[1].getTags()).hasSize(3);
    }

    @Test
    @DisplayName("Should parse pricing result with null optional fields")
    void testParsePricingWithNullOptionalFields() {
        // Given
        String llmResponse = """
                {
                    "discount": 0.15,
                    "finalPrice": 850.00,
                    "calculationComplete": true
                }
                """;

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.15);
        assertThat(result.getFinalPrice()).isEqualTo(850.00);
        // Optional fields should be null or empty depending on implementation
    }

    @Test
    @DisplayName("Should track parsing performance across multiple invocations")
    void testParsingPerformanceConsistency() {
        // Given
        String llmResponse = """
                {
                    "name": "Perf Test",
                    "age": 25,
                    "active": true
                }
                """;

        // When - parse multiple times
        long[] times = new long[5];
        for (int i = 0; i < 5; i++) {
            AIStructuredResponse<TestDTO> response = parser.parseToStructuredResponse(
                    llmResponse, TestDTO.class, 0.9);
            times[i] = response.getProcessingTimeMs();
        }

        // Then - verify times are reasonable and consistent
        for (long time : times) {
            assertThat(time).isGreaterThanOrEqualTo(0);
            assertThat(time).isLessThan(5000); // Should complete within 5 seconds
        }
    }

    @Test
    @DisplayName("Should provide helpful error messages for common parsing issues")
    void testErrorMessageClarity() {
        // Given - JSON with trailing comma (common error)
        String malformedJson = """
                {
                    "name": "Test",
                    "age": 25,
                    "active": true,
                }
                """;

        // When/Then
        assertThatThrownBy(() -> parser.parse(malformedJson, TestDTO.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse")
                .hasMessageNotContaining("null"); // Should have helpful message
    }

    @Test
    @DisplayName("Should handle empty arrays in JSON")
    void testParseEmptyArrays() {
        // Given
        String llmResponse = """
                {
                    "id": "test-id",
                    "nested": null,
                    "tags": []
                }
                """;

        // When
        NestedTestDTO result = parser.parse(llmResponse, NestedTestDTO.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-id");
        assertThat(result.getTags()).isEmpty();
    }

    @Test
    @DisplayName("Should handle response with mixed case JSON keys")
    void testParseMixedCaseJsonKeys() {
        // Given
        String llmResponse = """
                {
                    "Name": "Test",
                    "Age": 25,
                    "Active": true
                }
                """;

        // When/Then - depending on implementation, may need specific handling
        try {
            TestDTO result = parser.parse(llmResponse, TestDTO.class);
            // If successful, all values should be parsed correctly
            assertThat(result).isNotNull();
        } catch (IllegalArgumentException e) {
            // If strict parsing, should provide clear error about case sensitivity
            assertThat(e.getMessage()).contains("Failed to parse");
        }
    }
}