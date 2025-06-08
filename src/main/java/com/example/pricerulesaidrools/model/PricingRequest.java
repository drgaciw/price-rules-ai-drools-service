package com.example.pricerulesaidrools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRequest {
    private String customerId;
    private double basePrice;
    private int quantity;
    private String productId;
    private String pricingStrategy;
    private int customerTenureMonths;
    private int contractLengthMonths;
    private FinancialMetrics financialMetrics;
}