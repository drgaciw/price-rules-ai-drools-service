package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.pricing.PricingStrategy;
import com.example.pricerulesaidrools.pricing.PricingStrategyFactory;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing pricing operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {
    
    private final PricingStrategyFactory strategyFactory;
    private final FinancialMetricsCalculator metricsCalculator;
    private final QuoteRepository quoteRepository;
    
    /**
     * Apply pricing strategy to a quote
     * 
     * @param quote The quote to apply pricing to
     * @param strategyName The pricing strategy name
     * @return The quote with pricing applied
     */
    @Transactional
    public Quote applyPricingStrategy(Quote quote, String strategyName) {
        log.info("Applying pricing strategy {} to quote {}", strategyName, quote.getQuoteId());
        
        // Calculate or retrieve financial metrics
        FinancialMetrics metrics = metricsCalculator.calculateMetrics(quote);
        
        // Get the appropriate pricing strategy
        PricingStrategy strategy = strategyFactory.getStrategy(strategyName);
        
        // Apply the strategy
        Quote pricedQuote = strategy.applyStrategy(quote, metrics);
        
        // Save and return the updated quote
        return quoteRepository.save(pricedQuote);
    }
    
    /**
     * Apply pricing strategy to an existing quote by ID
     * 
     * @param quoteId The quote ID
     * @param strategyName The pricing strategy name
     * @return The quote with pricing applied
     */
    @Transactional
    public Quote applyPricingStrategy(String quoteId, String strategyName) {
        Quote quote = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
        
        return applyPricingStrategy(quote, strategyName);
    }
}