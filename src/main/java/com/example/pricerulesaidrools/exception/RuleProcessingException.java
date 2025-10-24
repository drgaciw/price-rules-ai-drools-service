package com.example.pricerulesaidrools.exception;

/**
 * Exception thrown when rule processing fails
 */
public class RuleProcessingException extends BaseServiceException {
    
    public static final String RULE_COMPILATION_FAILED = "RULE_COMPILATION_FAILED";
    public static final String RULE_EXECUTION_FAILED = "RULE_EXECUTION_FAILED";
    public static final String RULE_VALIDATION_FAILED = "RULE_VALIDATION_FAILED";
    public static final String RULE_DEPLOYMENT_FAILED = "RULE_DEPLOYMENT_FAILED";
    
    public RuleProcessingException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public RuleProcessingException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public RuleProcessingException(String message, String errorCode, Object... args) {
        super(message, errorCode, args);
    }
    
    public static RuleProcessingException compilationFailed(String ruleContent, Throwable cause) {
        return new RuleProcessingException(
                "Failed to compile rule: " + cause.getMessage(),
                RULE_COMPILATION_FAILED,
                cause,
                ruleContent
        );
    }
    
    public static RuleProcessingException executionFailed(String ruleId, Throwable cause) {
        return new RuleProcessingException(
                "Failed to execute rule: " + ruleId,
                RULE_EXECUTION_FAILED,
                cause,
                ruleId
        );
    }
    
    public static RuleProcessingException validationFailed(String ruleContent, String validationError) {
        return new RuleProcessingException(
                "Rule validation failed: " + validationError,
                RULE_VALIDATION_FAILED,
                ruleContent,
                validationError
        );
    }
    
    public static RuleProcessingException deploymentFailed(String ruleId, String deploymentError) {
        return new RuleProcessingException(
                "Rule deployment failed: " + deploymentError,
                RULE_DEPLOYMENT_FAILED,
                ruleId,
                deploymentError
        );
    }
}