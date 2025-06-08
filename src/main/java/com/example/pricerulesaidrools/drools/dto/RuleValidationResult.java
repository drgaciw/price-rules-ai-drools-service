package com.example.pricerulesaidrools.drools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleValidationResult {
    
    private boolean isValid;
    
    @Builder.Default
    private List<RuleDeploymentResult.ValidationError> errors = new ArrayList<>();
    
    private long validationTimeMs;
}