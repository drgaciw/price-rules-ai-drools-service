package com.example.pricerulesaidrools.ai.parser;

import com.example.pricerulesaidrools.ai.dto.AIStructuredResponse;
import com.example.pricerulesaidrools.model.PricingResult;

/**
 * Demo application to showcase the StructuredOutputParser functionality
 * for parsing LLM responses into typed DTOs for Drools consumption.
 */
public class StructuredOutputParserDemo {

    public static void main(String[] args) {
        System.out.println("=== BeanOutputParser Integration Demo ===\n");

        // Initialize the parser
        StructuredOutputParser parser = new StructuredOutputParserImpl();

        // Demo 1: Parse standard pricing response
        demo1StandardPricing(parser);

        // Demo 2: Parse with validation and metadata
        demo2WithValidation(parser);

        // Demo 3: Handle malformed response
        demo3ErrorHandling(parser);

        // Demo 4: Complex enterprise pricing
        demo4ComplexPricing(parser);

        System.out.println("\n=== Demo Complete ===");
    }

    private static void demo1StandardPricing(StructuredOutputParser parser) {
        System.out.println("Demo 1: Standard Pricing Response Parsing");
        System.out.println("-----------------------------------------");

        String llmResponse = """
            Based on the customer analysis, here's the pricing:

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
            """;

        try {
            PricingResult result = parser.parse(llmResponse, PricingResult.class);
            System.out.println("✓ Successfully parsed pricing result:");
            System.out.println("  - Discount: " + (result.getDiscount() * 100) + "%");
            System.out.println("  - Final Price: $" + result.getFinalPrice());
            System.out.println("  - Tier: " + result.getCommitmentTier());
            System.out.println("  - Applied Rules: " + result.getAppliedRules());
            System.out.println("  - Services: " + result.getIncludedServices());
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void demo2WithValidation(StructuredOutputParser parser) {
        System.out.println("Demo 2: Structured Response with Metadata");
        System.out.println("------------------------------------------");

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

        AIStructuredResponse<PricingResult> response = parser.parseToStructuredResponse(
            llmResponse, PricingResult.class, 0.95
        );

        System.out.println("✓ Structured response created:");
        System.out.println("  - Valid: " + response.isValid());
        System.out.println("  - Confidence: " + (response.getConfidence() * 100) + "%");
        System.out.println("  - Processing Time: " + response.getProcessingTimeMs() + "ms");
        if (response.getData() != null) {
            System.out.println("  - Final Price: $" + response.getData().getFinalPrice());
            System.out.println("  - Discount: " + (response.getData().getDiscount() * 100) + "%");
        }
        System.out.println("  - Metadata: " + response.getMetadata());
        System.out.println();
    }

    private static void demo3ErrorHandling(StructuredOutputParser parser) {
        System.out.println("Demo 3: Error Handling for Malformed JSON");
        System.out.println("------------------------------------------");

        String malformedResponse = """
            {
                "discount": 0.10,
                "finalPrice": "not_a_number",
                "calculationComplete": true
            }
            """;

        AIStructuredResponse<PricingResult> response = parser.parseToStructuredResponse(
            malformedResponse, PricingResult.class, 0.5
        );

        System.out.println("✓ Error handling demonstrated:");
        System.out.println("  - Valid: " + response.isValid());
        System.out.println("  - Has Errors: " + response.hasValidationErrors());
        if (response.hasValidationErrors()) {
            System.out.println("  - Errors: " + response.getFormattedValidationErrors());
        }
        System.out.println();
    }

    private static void demo4ComplexPricing(StructuredOutputParser parser) {
        System.out.println("Demo 4: Complex Enterprise Pricing");
        System.out.println("-----------------------------------");

        String complexResponse = """
            After analyzing the customer profile:

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
                    "Custom Integration",
                    "Training",
                    "Quarterly Reviews",
                    "SLA 99.99%"
                ]
            }
            ```

            Maximum discount cap applied.
            """;

        try {
            PricingResult result = parser.parse(complexResponse, PricingResult.class);
            System.out.println("✓ Complex pricing parsed successfully:");
            System.out.println("  - Discount: " + (result.getDiscount() * 100) + "%");
            System.out.println("  - Final Price: $" + String.format("%.2f", result.getFinalPrice()));
            System.out.println("  - Commitment: $" + String.format("%.2f", result.getMinimumCommitment()));
            System.out.println("  - Tier: " + result.getCommitmentTier());
            System.out.println("  - Description: " + result.getDiscountDescription());
            System.out.println("  - Applied Rules (" + result.getAppliedRules().size() + "): " + result.getAppliedRules());
            System.out.println("  - Services (" + result.getIncludedServices().size() + "): " + result.getIncludedServices());

            // Show Drools integration readiness
            System.out.println("\n  → Ready for Drools Integration:");
            System.out.println("    - Can be passed to KieSession for rule execution");
            System.out.println("    - Type-safe DTO ensures compatibility");
            System.out.println("    - Validation state: " + (result.isCalculationComplete() ? "Complete" : "Pending"));
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
        System.out.println();
    }
}