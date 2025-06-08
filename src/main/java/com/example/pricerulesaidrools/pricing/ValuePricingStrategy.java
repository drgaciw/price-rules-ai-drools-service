package com.example.pricerulesaidrools.pricing;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Value-based pricing strategy implementation
 * Adjusts pricing based on customer lifetime value (CLV)
 */
@Component
@Slf4j
public class ValuePricingStrategy implements PricingStrategy {
    
    // Price multiplier tiers based on CLV
    private final List<ValueTier> valueTiers;
    
    public ValuePricingStrategy() {
        this.valueTiers = new ArrayList<>();
        
        // Initialize default value tiers
        valueTiers.add(new ValueTier(BigDecimal.valueOf(1000000), BigDecimal.valueOf(0.85))); // $1M+ CLV: 15% discount
        valueTiers.add(new ValueTier(BigDecimal.valueOf(500000), BigDecimal.valueOf(0.90)));  // $500K+ CLV: 10% discount
        valueTiers.add(new ValueTier(BigDecimal.valueOf(100000), BigDecimal.valueOf(1.0)));   // $100K+ CLV: no change
        valueTiers.add(new ValueTier(BigDecimal.valueOf(0), BigDecimal.valueOf(1.05)));       // Below $100K: 5% premium
    }
    
    @Override
    public Quote applyStrategy(Quote quote, FinancialMetrics metrics) {
        log.info("Applying value-based pricing strategy for quote ID: {}", quote.getQuoteId());
        
        BigDecimal clv = metrics.getClv();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal priceMultiplier = BigDecimal.ONE; // Default: no change
        
        // Find applicable value tier
        for (ValueTier tier : valueTiers) {
            if (clv.compareTo(tier.getThreshold()) >= 0) {
                priceMultiplier = tier.getPriceMultiplier();
                log.info("Applied value tier: threshold={}, multiplier={}", 
                        tier.getThreshold(), priceMultiplier);
                break;
            }
        }
        
        // Apply price adjustment
        BigDecimal finalPrice = basePrice.multiply(priceMultiplier).setScale(2, RoundingMode.HALF_UP);
        
        // Update and return quote
        quote.setFinalPrice(finalPrice);
        return quote;
    }
    
    @Override
    public StrategyType getType() {
        return StrategyType.VALUE;
    }
    
    /**
     * Inner class to represent value tiers
     */
    private static class ValueTier {
        private final BigDecimal threshold;
        private final BigDecimal priceMultiplier;
        
        public ValueTier(BigDecimal threshold, BigDecimal priceMultiplier) {
            this.threshold = threshold;
            this.priceMultiplier = priceMultiplier;
        }
        
        public BigDecimal getThreshold() {
            return threshold;
        }
        
        public BigDecimal getPriceMultiplier() {
            return priceMultiplier;
        }
    }
}