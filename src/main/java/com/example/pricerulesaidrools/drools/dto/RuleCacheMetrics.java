package com.example.pricerulesaidrools.drools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCacheMetrics {
    
    private String ruleSetId;
    private int cacheSize;
    private int maxCacheSize;
    private double hitRate;
    private long cacheHits;
    private long cacheMisses;
    private long cacheEvictions;
}