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
 * Risk-adjusted pricing strategy implementation
 * Adjusts pricing based on customer churn risk
 */
@Component
@Slf4j
public class RiskAdjustedPricingStrategy implements PricingStrategy {
    
    // Risk tiers for price adjustment
    private final List<RiskTier> riskTiers;
    
    public RiskAdjustedPricingStrategy() {
        this.riskTiers = new ArrayList<>();
        
        // Initialize default risk tiers
        riskTiers.add(new RiskTier(BigDecimal.valueOf(0.8), BigDecimal.valueOf(0.80))); // Very high risk: 20% discount
        riskTiers.add(new RiskTier(BigDecimal.valueOf(0.6), BigDecimal.valueOf(0.85))); // High risk: 15% discount
        riskTiers.add(new RiskTier(BigDecimal.valueOf(0.4), BigDecimal.valueOf(0.95))); // Medium risk: 5% discount
        riskTiers.add(new RiskTier(BigDecimal.valueOf(0.2), BigDecimal.valueOf(1.0)));  // Low risk: no change
        riskTiers.add(new RiskTier(BigDecimal.valueOf(0.0), BigDecimal.valueOf(1.05))); // Very low risk: 5% premium
    }
    
    @Override
    public Quote applyStrategy(Quote quote, FinancialMetrics metrics) {
        log.info("Applying risk-adjusted pricing strategy for quote ID: {}", quote.getQuoteId());
        
        BigDecimal churnRiskScore = metrics.getChurnRiskScore();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal priceMultiplier = BigDecimal.ONE; // Default: no change
        
        // Find applicable risk tier
        for (RiskTier tier : riskTiers) {
            if (churnRiskScore.compareTo(tier.getRiskThreshold()) >= 0) {
                priceMultiplier = tier.getPriceMultiplier();
                log.info("Applied risk tier: threshold={}, multiplier={}", 
                        tier.getRiskThreshold(), priceMultiplier);
                break;
            }
        }
        
        // Apply price adjustment
        BigDecimal finalPrice = basePrice.multiply(priceMultiplier).setScale(2, RoundingMode.HALF_UP);
        
        // Apply minimum commitment based on risk
        BigDecimal minimumCommitment = calculateMinimumCommitment(churnRiskScore, metrics);
        if (finalPrice.compareTo(minimumCommitment) < 0) {
            log.info("Final price ${} is below risk-adjusted minimum commitment ${}. Adjusting to minimum.", 
                    finalPrice, minimumCommitment);
            finalPrice = minimumCommitment;
        }
        
        // Update and return quote
        quote.setFinalPrice(finalPrice);
        return quote;
    }
    
    @Override
    public StrategyType getType() {
        return StrategyType.RISK_ADJUSTED;
    }
    
    /**
     * Calculate minimum commitment based on risk
     * Higher risk customers may have lower minimum commitments
     * 
     * @param churnRiskScore The churn risk score
     * @param metrics The financial metrics
     * @return The minimum commitment amount
     */
    private BigDecimal calculateMinimumCommitment(BigDecimal churnRiskScore, FinancialMetrics metrics) {
        // Base minimum commitment is 5% of ARR
        BigDecimal baseCommitment = metrics.getArr().multiply(BigDecimal.valueOf(0.05));
        
        // Adjust based on risk - higher risk means lower commitment
        BigDecimal riskMultiplier = BigDecimal.ONE.subtract(churnRiskScore.multiply(BigDecimal.valueOf(0.5)));
        
        return baseCommitment.multiply(riskMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Inner class to represent risk tiers
     */
    private static class RiskTier {
        private final BigDecimal riskThreshold;
        private final BigDecimal priceMultiplier;
        
        public RiskTier(BigDecimal riskThreshold, BigDecimal priceMultiplier) {
            this.riskThreshold = riskThreshold;
            this.priceMultiplier = priceMultiplier;
        }
        
        public BigDecimal getRiskThreshold() {
            return riskThreshold;
        }
        
        public BigDecimal getPriceMultiplier() {
            return priceMultiplier;
        }
    }
}