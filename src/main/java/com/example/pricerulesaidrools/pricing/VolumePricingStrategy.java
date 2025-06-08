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
 * Volume-based pricing strategy implementation
 * Provides discounts based on volume (ARR tiers)
 */
@Component
@Slf4j
public class VolumePricingStrategy implements PricingStrategy {
    
    // Discount tier thresholds
    private final List<DiscountTier> discountTiers;
    
    public VolumePricingStrategy() {
        this.discountTiers = new ArrayList<>();
        
        // Initialize default discount tiers
        discountTiers.add(new DiscountTier(BigDecimal.valueOf(1000000), BigDecimal.valueOf(0.20))); // $1M+ ARR: 20% discount
        discountTiers.add(new DiscountTier(BigDecimal.valueOf(500000), BigDecimal.valueOf(0.15)));  // $500K+ ARR: 15% discount
        discountTiers.add(new DiscountTier(BigDecimal.valueOf(250000), BigDecimal.valueOf(0.10)));  // $250K+ ARR: 10% discount
        discountTiers.add(new DiscountTier(BigDecimal.valueOf(100000), BigDecimal.valueOf(0.05)));  // $100K+ ARR: 5% discount
        discountTiers.add(new DiscountTier(BigDecimal.valueOf(0), BigDecimal.valueOf(0.0)));        // Below $100K: no discount
    }
    
    @Override
    public Quote applyStrategy(Quote quote, FinancialMetrics metrics) {
        log.info("Applying volume-based pricing strategy for quote ID: {}", quote.getQuoteId());
        
        BigDecimal arr = metrics.getArr();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal discountRate = BigDecimal.ZERO;
        
        // Find applicable discount tier
        for (DiscountTier tier : discountTiers) {
            if (arr.compareTo(tier.getThreshold()) >= 0) {
                discountRate = tier.getDiscountRate();
                log.info("Applied discount tier: threshold={}, discount={}%", 
                        tier.getThreshold(), discountRate.multiply(BigDecimal.valueOf(100)));
                break;
            }
        }
        
        // Enforce minimum commitment if applicable
        BigDecimal minimumCommitment = getMinimumCommitment(arr);
        
        // Apply volume discount
        BigDecimal discount = basePrice.multiply(discountRate);
        BigDecimal finalPrice = basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        
        // Ensure price is not below minimum commitment
        if (finalPrice.compareTo(minimumCommitment) < 0) {
            log.info("Final price ${} is below minimum commitment ${}. Adjusting to minimum.", 
                    finalPrice, minimumCommitment);
            finalPrice = minimumCommitment;
        }
        
        // Update and return quote
        quote.setFinalPrice(finalPrice);
        return quote;
    }
    
    @Override
    public StrategyType getType() {
        return StrategyType.VOLUME;
    }
    
    /**
     * Calculate minimum commitment based on ARR
     * 
     * @param arr Annual Recurring Revenue
     * @return Minimum commitment amount
     */
    private BigDecimal getMinimumCommitment(BigDecimal arr) {
        // Default implementation: 10% of ARR as minimum commitment
        return arr.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Inner class to represent discount tiers
     */
    private static class DiscountTier {
        private final BigDecimal threshold;
        private final BigDecimal discountRate;
        
        public DiscountTier(BigDecimal threshold, BigDecimal discountRate) {
            this.threshold = threshold;
            this.discountRate = discountRate;
        }
        
        public BigDecimal getThreshold() {
            return threshold;
        }
        
        public BigDecimal getDiscountRate() {
            return discountRate;
        }
    }
}