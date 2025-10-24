package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;
import com.example.pricerulesaidrools.model.PricingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test demonstrating the StructuredOutputParser functionality
 * with sample LLM responses for pricing scenarios.
 */
public class StructuredOutputParserIntegrationTest {

    private StructuredOutputParser parser;

    @BeforeEach
    void setUp() {
        parser = new StructuredOutputParserImpl();
    }

    @Test
    @DisplayName("Demonstrate parsing of standard pricing response from LLM")
    void testParseLLMPricingResponse() {
        // Given - A typical LLM response for a pricing calculation
        String llmResponse = """
            Based on the customer profile and order details, here's the calculated pricing:

            ```json
            {
                "discount": 0.15,
                "finalPrice": 8500.00,
                "calculationComplete": true,
                "priceMultiplier": 0.85,
                "discountDescription": "Volume discount (15%) applied for order quantity > 100 units",
                "minimumCommitment": 50000.0,
                "commitmentTier": "Gold",
                "appliedRules": [
                    "VOLUME_DISCOUNT_TIER_2",
                    "GOLD_CUSTOMER_BENEFIT"
                ],
                "includedServices": [
                    "Priority Support",
                    "Free Shipping",
                    "Extended Warranty"
                ]
            }
            ```

            The customer qualifies for Gold tier benefits with this order.
            """;

        // When
        PricingResult result = parser.parse(llmResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.15);
        assertThat(result.getFinalPrice()).isEqualTo(8500.00);
        assertThat(result.isCalculationComplete()).isTrue();
        assertThat(result.getPriceMultiplier()).isEqualTo(0.85);
        assertThat(result.getDiscountDescription()).isEqualTo("Volume discount (15%) applied for order quantity > 100 units");
        assertThat(result.getMinimumCommitment()).isEqualTo(50000.0);
        assertThat(result.getCommitmentTier()).isEqualTo("Gold");
        assertThat(result.getAppliedRules()).containsExactly(
            "VOLUME_DISCOUNT_TIER_2",
            "GOLD_CUSTOMER_BENEFIT"
        );
        assertThat(result.getIncludedServices()).containsExactly(
            "Priority Support",
            "Free Shipping",
            "Extended Warranty"
        );
    }

    @Test
    @DisplayName("Demonstrate structured response with metadata")
    void testParseToStructuredResponseWithMetadata() {
        // Given
        String llmResponse = """
            {
                "discount": 0.20,
                "finalPrice": 4000.00,
                "calculationComplete": true,
                "priceMultiplier": 0.80,
                "discountDescription": "Enterprise discount",
                "appliedRules": ["ENTERPRISE_TIER"]
            }
            """;
        Double confidence = 0.95;

        // When
        AIStructuredResponse<PricingResult> response = parser.parseToStructuredResponse(
            llmResponse, PricingResult.class, confidence
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getConfidence()).isEqualTo(0.95);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getDiscount()).isEqualTo(0.20);
        assertThat(response.getData().getFinalPrice()).isEqualTo(4000.00);
        assertThat(response.hasValidationErrors()).isFalse();
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
        assertThat(response.getMetadata()).containsKey("targetClass");
        assertThat(response.getMetadata().get("targetClass")).isEqualTo("com.example.pricerulesaidrools.model.PricingResult");
    }

    @Test
    @DisplayName("Demonstrate error handling for malformed JSON")
    void testHandleMalformedJson() {
        // Given
        String malformedResponse = """
            {
                "discount": 0.10,
                "finalPrice": "not_a_number",
                "calculationComplete": true
            }
            """;

        // When
        AIStructuredResponse<PricingResult> response = parser.parseToStructuredResponse(
            malformedResponse, PricingResult.class, 0.5
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.hasValidationErrors()).isTrue();
        assertThat(response.getValidationErrors()).isNotEmpty();
        assertThat(response.getValidationErrors().get(0)).contains("Parsing failed");
    }

    @Test
    @DisplayName("Demonstrate complex pricing scenario parsing")
    void testParseComplexPricingScenario() {
        // Given - Complex LLM response with multiple discount tiers
        String complexResponse = """
            After analyzing the customer's purchase history and current order, I've calculated the following pricing structure:

            ```json
            {
                "discount": 0.30,
                "finalPrice": 70000.00,
                "calculationComplete": true,
                "priceMultiplier": 0.70,
                "discountDescription": "Multi-tier discount: Enterprise (20%) + Volume (10%) + Loyalty (5%) - Maximum 30% applied",
                "minimumCommitment": 100000.0,
                "commitmentTier": "Enterprise",
                "appliedRules": [
                    "ENTERPRISE_BASE_DISCOUNT",
                    "VOLUME_TIER_3",
                    "LOYALTY_5_YEAR",
                    "MAX_DISCOUNT_CAP"
                ],
                "includedServices": [
                    "24/7 Dedicated Support",
                    "Custom Integration Services",
                    "Training and Onboarding",
                    "Quarterly Business Reviews",
                    "SLA Guarantee 99.99%"
                ]
            }
            ```

            Note: The maximum discount cap of 30% has been applied per company policy.
            """;

        // When
        PricingResult result = parser.parse(complexResponse, PricingResult.class);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDiscount()).isEqualTo(0.30);
        assertThat(result.getFinalPrice()).isEqualTo(70000.00);
        assertThat(result.getCommitmentTier()).isEqualTo("Enterprise");
        assertThat(result.getAppliedRules()).hasSize(4);
        assertThat(result.getIncludedServices()).hasSize(5);
        assertThat(result.getDiscountDescription()).contains("Multi-tier discount");
    }

    @Test
    @DisplayName("Show example usage for Drools integration")
    void demonstrateDroolsIntegrationExample() {
        // Given - An LLM-generated pricing recommendation
        String aiGeneratedPricing = """
            {
                "discount": 0.25,
                "finalPrice": 7500.00,
                "calculationComplete": false,
                "priceMultiplier": 0.75,
                "discountDescription": "AI-recommended discount pending Drools validation",
                "appliedRules": ["AI_RECOMMENDATION"]
            }
            """;

        // When - Parse the AI response
        PricingResult aiResult = parser.parse(aiGeneratedPricing, PricingResult.class);

        // Then - Result can be passed to Drools for validation
        assertThat(aiResult).isNotNull();
        assertThat(aiResult.isCalculationComplete()).isFalse(); // Indicates Drools validation needed
        assertThat(aiResult.getDiscountDescription()).contains("pending Drools validation");

        // This PricingResult can now be:
        // 1. Passed to Drools rules for validation
        // 2. Enhanced with additional business rules
        // 3. Logged for audit purposes

        System.out.println("AI-generated pricing ready for Drools validation:");
        System.out.println("  Discount: " + (aiResult.getDiscount() * 100) + "%");
        System.out.println("  Final Price: $" + aiResult.getFinalPrice());
        System.out.println("  Status: " + (aiResult.isCalculationComplete() ? "Complete" : "Pending Validation"));
    }
}