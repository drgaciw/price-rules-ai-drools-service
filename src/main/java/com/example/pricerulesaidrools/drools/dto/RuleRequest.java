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
public class RuleRequest {
    
    @NotBlank(message = "Rule name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Rule content is required")
    private String content;
    
    private String version;
    
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}