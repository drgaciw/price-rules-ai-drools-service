package com.example.pricerulesaidrools.drools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleSetMetadata {
    
    private String id;
    private String name;
    private String version;
    private String description;
    private RuleStatus status;
    private String author;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdated;
    private long executionCount;
    
    public enum RuleStatus {
        ACTIVE, INACTIVE, DELETED
    }
}