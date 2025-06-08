package com.example.pricerulesaidrools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for financial metrics calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetricsResponse {
    
    private String quoteId;
    private String customerId;
    
    private Metrics metrics;
    
    private Pricing pricing;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private BigDecimal arr;        // Annual Recurring Revenue
        private BigDecimal tcv;        // Total Contract Value
        private BigDecimal acv;        // Annual Contract Value
        private BigDecimal clv;        // Customer Lifetime Value
        private BigDecimal churnScore; // Churn Risk Score (0-1)
        private BigDecimal discountRate;
        private BigDecimal priceMultiplier;
        private BigDecimal minCommitment;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pricing {
        private BigDecimal basePrice;
        private BigDecimal finalPrice;
        private String currency;
        private String[] appliedRules;
    }
}