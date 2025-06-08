package com.example.pricerulesaidrools.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for creating pricing strategy instances
 */
@Component
@RequiredArgsConstructor
public class PricingStrategyFactory {
    
    private final List<PricingStrategy> pricingStrategies;
    private final Map<PricingStrategy.StrategyType, PricingStrategy> strategyMap;
    
    /**
     * Constructor that initializes the strategy map
     */
    public PricingStrategyFactory(List<PricingStrategy> pricingStrategies) {
        this.pricingStrategies = pricingStrategies;
        this.strategyMap = pricingStrategies.stream()
                .collect(Collectors.toMap(PricingStrategy::getType, Function.identity()));
    }
    
    /**
     * Get a pricing strategy by type
     * 
     * @param type The strategy type
     * @return The pricing strategy implementation
     */
    public PricingStrategy getStrategy(PricingStrategy.StrategyType type) {
        PricingStrategy strategy = strategyMap.get(type);
        
        if (strategy == null) {
            throw new IllegalArgumentException("Pricing strategy not found for type: " + type);
        }
        
        return strategy;
    }
    
    /**
     * Get a pricing strategy by name
     * 
     * @param name The strategy name (case-insensitive)
     * @return The pricing strategy implementation
     */
    public PricingStrategy getStrategy(String name) {
        try {
            PricingStrategy.StrategyType type = PricingStrategy.StrategyType.valueOf(name.toUpperCase());
            return getStrategy(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pricing strategy not found: " + name, e);
        }
    }
    
    /**
     * Get all available pricing strategies
     * 
     * @return List of all pricing strategies
     */
    public List<PricingStrategy> getAllStrategies() {
        return pricingStrategies;
    }
}