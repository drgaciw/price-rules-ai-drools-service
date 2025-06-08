package com.example.pricerulesaidrools.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for financial metrics calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetricsRequest {
    
    @NotBlank(message = "Quote ID is required")
    private String quoteId;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Monthly price is required")
    @Positive(message = "Monthly price must be positive")
    private BigDecimal monthlyPrice;
    
    @NotNull(message = "Duration in months is required")
    @Positive(message = "Duration in months must be positive")
    private Integer durationInMonths;
    
    private Integer expectedDuration;
    
    private String customerType;
    
    private String subscriptionType;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
    
    private Integer customerTenureMonths;
    
    private String productId;
}