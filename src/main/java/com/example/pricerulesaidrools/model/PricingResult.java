package com.example.pricerulesaidrools.model;

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
public class PricingResult {
    private double discount;
    private double finalPrice;
    private boolean calculationComplete;
    
    @Builder.Default
    private List<String> appliedRules = new ArrayList<>();
    
    public void addAppliedRule(String ruleName) {
        if (appliedRules == null) {
            appliedRules = new ArrayList<>();
        }
        appliedRules.add(ruleName);
    }
}