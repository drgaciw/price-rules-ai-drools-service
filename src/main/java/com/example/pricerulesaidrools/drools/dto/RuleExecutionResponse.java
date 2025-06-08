package com.example.pricerulesaidrools.drools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResponse {
    
    @Builder.Default
    private Map<String, Object> results = new HashMap<>();
    
    @Builder.Default
    private List<String> rulesApplied = new ArrayList<>();
    
    private long executionTime;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}