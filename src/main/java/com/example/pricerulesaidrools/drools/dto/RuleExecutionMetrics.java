package com.example.pricerulesaidrools.drools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionMetrics {
    
    private String ruleSetId;
    private long totalExecutions;
    private double averageExecutionTimeMs;
    private double cacheHitRate;
    private double errorRate;
    private long lastExecutionTimeMs;
    private long peakExecutionTimeMs;
}