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
public class RuleDeploymentResult {
    
    private String id;
    private String ruleSetId;
    private boolean successful;
    private String message;
    
    @Builder.Default
    private List<ValidationError> validationErrors = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String code;
        private String message;
        private Severity severity;
        
        public enum Severity {
            ERROR, WARNING, INFO
        }
    }
}