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
    private double priceMultiplier = 1.0;
    private String discountDescription;
    private Double minimumCommitment;
    private String commitmentTier;
    
    @Builder.Default
    private List<String> appliedRules = new ArrayList<>();
    
    @Builder.Default
    private List<String> includedServices = new ArrayList<>();
    
    public void addAppliedRule(String ruleName) {
        if (appliedRules == null) {
            appliedRules = new ArrayList<>();
        }
        appliedRules.add(ruleName);
    }
    
    public void addIncludedService(String service) {
        if (includedServices == null) {
            includedServices = new ArrayList<>();
        }
        if (!includedServices.contains(service)) {
            includedServices.add(service);
        }
    }
}