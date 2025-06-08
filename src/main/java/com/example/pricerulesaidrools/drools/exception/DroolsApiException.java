package com.example.pricerulesaidrools.drools.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DroolsApiException extends RuntimeException {
    
    private final DroolsErrorCode errorCode;
    private final String ruleSetId;
    private final Map<String, Object> context;
    
    public DroolsApiException(DroolsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.ruleSetId = null;
        this.context = new HashMap<>();
    }
    
    public DroolsApiException(DroolsErrorCode errorCode, String message, String ruleSetId) {
        super(message);
        this.errorCode = errorCode;
        this.ruleSetId = ruleSetId;
        this.context = new HashMap<>();
    }
    
    public DroolsApiException(DroolsErrorCode errorCode, String message, String ruleSetId, Map<String, Object> context) {
        super(message);
        this.errorCode = errorCode;
        this.ruleSetId = ruleSetId;
        this.context = context;
    }
    
    public DroolsApiException(DroolsErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.ruleSetId = null;
        this.context = new HashMap<>();
    }
    
    public DroolsApiException(DroolsErrorCode errorCode, String message, String ruleSetId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.ruleSetId = ruleSetId;
        this.context = new HashMap<>();
    }
}