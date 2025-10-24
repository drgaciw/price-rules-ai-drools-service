package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.repository.FinancialMetricsRepository;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialMetricsServiceImpl implements FinancialMetricsService {
    
    private final FinancialMetricsRepository metricsRepository;
    private final QuoteRepository quoteRepository;
    private final FinancialMetricsCalculator calculator;
    
    @Override
    @Transactional
    public FinancialMetrics calculateMetrics(Quote quote) {
        log.info("Calculating financial metrics for quote ID: {}", quote.getQuoteId());
        return calculator.calculateMetrics(quote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getARR(String quoteId) {
        log.info("Getting ARR for quote ID: {}", quoteId);
        Quote quote = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
        
        return calculator.calculateARR(quote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTCV(String quoteId) {
        log.info("Getting TCV for quote ID: {}", quoteId);
        Quote quote = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
        
        return calculator.calculateTCV(quote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getACV(String quoteId) {
        log.info("Getting ACV for quote ID: {}", quoteId);
        Quote quote = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
        
        return calculator.calculateACV(quote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCLV(String quoteId) {
        log.info("Getting CLV for quote ID: {}", quoteId);
        Quote quote = quoteRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
        
        return calculator.calculateCLV(quote);
    }
    
    @Override
    @Transactional
    public Quote applyPricingStrategy(Quote quote, String strategy) {
        log.info("Applying pricing strategy {} to quote ID: {}", strategy, quote.getQuoteId());
        
        // Calculate metrics first
        FinancialMetrics metrics = calculateMetrics(quote);
        
        // Apply pricing strategy based on the specified strategy
        switch (strategy.toUpperCase()) {
            case "VOLUME":
                return applyVolumePricingStrategy(quote, metrics);
            case "VALUE":
                return applyValuePricingStrategy(quote, metrics);
            case "RISK_ADJUSTED":
                return applyRiskAdjustedPricingStrategy(quote, metrics);
            default:
                throw new IllegalArgumentException("Unsupported pricing strategy: " + strategy);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<FinancialMetrics> getHistoricalMetrics(String customerId, Duration period) {
        log.info("Getting historical metrics for customer ID: {}", customerId);
        return calculator.getHistoricalMetrics(customerId, period)
                .stream()
                .map(this::convertSnapshotToMetrics)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private FinancialMetrics convertSnapshotToMetrics(com.example.pricerulesaidrools.model.FinancialMetricsSnapshot snapshot) {
        return FinancialMetrics.builder()
                .customerId(snapshot.getCustomerId())
                .arr(snapshot.getArr())
                .tcv(snapshot.getTcv())
                .acv(snapshot.getAcv())
                .clv(snapshot.getClv())
                .churnRiskScore(snapshot.getChurnRiskScore())
                .churnTrend("STABLE") // Default value since snapshot doesn't have this field
                .growthRate(snapshot.getGrowthRate())
                .createdAt(snapshot.getCreatedAt())
                .updatedAt(snapshot.getCreatedAt())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer calculateChurnRisk(String customerId) {
        log.info("Calculating churn risk for customer ID: {}", customerId);
        BigDecimal churnRiskScore = calculator.calculateChurnRiskScore(customerId);
        
        // Convert to integer percentage (0-100)
        return churnRiskScore.multiply(BigDecimal.valueOf(100)).intValue();
    }
    
    /**
     * Apply volume-based pricing strategy
     * Provides discounts based on the ARR value
     * 
     * @param quote The quote
     * @param metrics The financial metrics
     * @return The updated quote with pricing applied
     */
    private Quote applyVolumePricingStrategy(Quote quote, FinancialMetrics metrics) {
        BigDecimal arr = metrics.getArr();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal discountRate = BigDecimal.ZERO;
        
        // Apply discount tiers based on ARR
        if (arr.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            // Over $1M ARR: 20% discount
            discountRate = BigDecimal.valueOf(0.20);
        } else if (arr.compareTo(BigDecimal.valueOf(500000)) > 0) {
            // $500K-$1M ARR: 15% discount
            discountRate = BigDecimal.valueOf(0.15);
        } else if (arr.compareTo(BigDecimal.valueOf(250000)) > 0) {
            // $250K-$500K ARR: 10% discount
            discountRate = BigDecimal.valueOf(0.10);
        } else if (arr.compareTo(BigDecimal.valueOf(100000)) > 0) {
            // $100K-$250K ARR: 5% discount
            discountRate = BigDecimal.valueOf(0.05);
        }
        
        // Calculate final price
        BigDecimal discount = basePrice.multiply(discountRate);
        BigDecimal finalPrice = basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        
        // Update quote
        quote.setFinalPrice(finalPrice);
        return quoteRepository.save(quote);
    }
    
    /**
     * Apply value-based pricing strategy
     * Focuses on customer lifetime value for pricing decisions
     * 
     * @param quote The quote
     * @param metrics The financial metrics
     * @return The updated quote with pricing applied
     */
    private Quote applyValuePricingStrategy(Quote quote, FinancialMetrics metrics) {
        BigDecimal clv = metrics.getClv();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal priceMultiplier = BigDecimal.ONE; // Default: no change
        
        // Apply price adjustments based on CLV
        if (clv.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            // Strategic customer: offer discount
            priceMultiplier = BigDecimal.valueOf(0.85); // 15% discount
        } else if (clv.compareTo(BigDecimal.valueOf(500000)) > 0) {
            // High-value customer: offer smaller discount
            priceMultiplier = BigDecimal.valueOf(0.90); // 10% discount
        } else if (clv.compareTo(BigDecimal.valueOf(100000)) > 0) {
            // Medium-value customer: no change
            priceMultiplier = BigDecimal.ONE;
        } else {
            // Low-value customer: increase price
            priceMultiplier = BigDecimal.valueOf(1.05); // 5% premium
        }
        
        // Calculate final price
        BigDecimal finalPrice = basePrice.multiply(priceMultiplier).setScale(2, RoundingMode.HALF_UP);
        
        // Update quote
        quote.setFinalPrice(finalPrice);
        return quoteRepository.save(quote);
    }
    
    /**
     * Apply risk-adjusted pricing strategy
     * Adjusts pricing based on customer churn risk
     * 
     * @param quote The quote
     * @param metrics The financial metrics
     * @return The updated quote with pricing applied
     */
    private Quote applyRiskAdjustedPricingStrategy(Quote quote, FinancialMetrics metrics) {
        BigDecimal churnRiskScore = metrics.getChurnRiskScore();
        BigDecimal basePrice = quote.getBasePrice();
        BigDecimal priceMultiplier;
        
        // Apply price adjustments based on churn risk
        if (churnRiskScore.compareTo(BigDecimal.valueOf(0.8)) > 0) {
            // Very high churn risk: significant discount to retain
            priceMultiplier = BigDecimal.valueOf(0.80); // 20% discount
        } else if (churnRiskScore.compareTo(BigDecimal.valueOf(0.6)) > 0) {
            // High churn risk: moderate discount
            priceMultiplier = BigDecimal.valueOf(0.85); // 15% discount
        } else if (churnRiskScore.compareTo(BigDecimal.valueOf(0.4)) > 0) {
            // Medium churn risk: small discount
            priceMultiplier = BigDecimal.valueOf(0.95); // 5% discount
        } else if (churnRiskScore.compareTo(BigDecimal.valueOf(0.2)) > 0) {
            // Low churn risk: no change
            priceMultiplier = BigDecimal.ONE;
        } else {
            // Very low churn risk: premium pricing
            priceMultiplier = BigDecimal.valueOf(1.05); // 5% premium
        }
        
        // Calculate final price
        BigDecimal finalPrice = basePrice.multiply(priceMultiplier).setScale(2, RoundingMode.HALF_UP);
        
        // Update quote
        quote.setFinalPrice(finalPrice);
        return quoteRepository.save(quote);
    }
}