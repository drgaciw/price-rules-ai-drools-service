package com.example.pricerulesaidrools.pricing;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;

/**
 * Interface defining different pricing strategies
 * Different implementations can provide various pricing approaches
 */
public interface PricingStrategy {
    
    /**
     * Apply pricing strategy to a quote based on financial metrics
     * 
     * @param quote The quote to apply pricing to
     * @param metrics The financial metrics for calculation
     * @return The quote with pricing applied
     */
    Quote applyStrategy(Quote quote, FinancialMetrics metrics);
    
    /**
     * Get the strategy type
     * 
     * @return The strategy type
     */
    StrategyType getType();
    
    /**
     * Types of pricing strategies
     */
    enum StrategyType {
        VOLUME,     // Based on volume/quantity discounts
        VALUE,      // Based on customer value
        RISK_ADJUSTED, // Based on risk assessment
        CUSTOM      // Custom strategy
    }
}