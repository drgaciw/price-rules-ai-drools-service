package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Service interface for financial metrics operations
 */
public interface FinancialMetricsService {
    
    /**
     * Calculate all financial metrics for a quote
     * 
     * @param quote The quote to calculate metrics for
     * @return The calculated financial metrics
     */
    FinancialMetrics calculateMetrics(Quote quote);
    
    /**
     * Get Annual Recurring Revenue (ARR) for a quote
     * 
     * @param quoteId The quote ID
     * @return The ARR value
     */
    BigDecimal getARR(String quoteId);
    
    /**
     * Get Total Contract Value (TCV) for a quote
     * 
     * @param quoteId The quote ID
     * @return The TCV value
     */
    BigDecimal getTCV(String quoteId);
    
    /**
     * Get Annual Contract Value (ACV) for a quote
     * 
     * @param quoteId The quote ID
     * @return The ACV value
     */
    BigDecimal getACV(String quoteId);
    
    /**
     * Get Customer Lifetime Value (CLV) for a quote
     * 
     * @param quoteId The quote ID
     * @return The CLV value
     */
    BigDecimal getCLV(String quoteId);
    
    /**
     * Apply a pricing strategy to a quote
     * 
     * @param quote The quote to apply strategy to
     * @param strategy The pricing strategy to apply
     * @return The pricing result
     */
    Quote applyPricingStrategy(Quote quote, String strategy);
    
    /**
     * Get historical metrics for a customer over a period
     * 
     * @param customerId The customer ID
     * @param period The time period to look back
     * @return List of historical financial metrics
     */
    List<FinancialMetrics> getHistoricalMetrics(String customerId, Duration period);
    
    /**
     * Calculate churn risk for a customer
     * 
     * @param customerId The customer ID
     * @return The churn risk score (0-100)
     */
    Integer calculateChurnRisk(String customerId);
}