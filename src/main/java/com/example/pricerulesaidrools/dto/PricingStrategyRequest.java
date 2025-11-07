package com.example.pricerulesaidrools.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for applying pricing strategies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingStrategyRequest {

    @NotBlank(message = "Quote ID is required")
    private String quoteId;

    @NotBlank(message = "Strategy is required")
    private String strategy;

    private Parameters parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameters {
        private BigDecimal minCommitment;

        private List<DiscountTier> discountTiers;

        private List<PremiumTier> premiumTiers;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DiscountTier {
            private BigDecimal threshold;
            private BigDecimal discount;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PremiumTier {
            private BigDecimal threshold;
            private BigDecimal multiplier;
        }
    }
}