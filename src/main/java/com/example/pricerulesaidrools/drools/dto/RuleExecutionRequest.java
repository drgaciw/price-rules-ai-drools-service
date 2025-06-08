package com.example.pricerulesaidrools.drools.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionRequest {
    
    @NotBlank(message = "Rule set ID is required")
    private String ruleSetId;
    
    @NotNull(message = "Facts are required")
    @Builder.Default
    private Map<String, Object> facts = new HashMap<>();
}