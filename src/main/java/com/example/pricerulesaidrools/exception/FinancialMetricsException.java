package com.example.pricerulesaidrools.exception;

/**
 * Exception thrown when financial metrics processing fails
 */
public class FinancialMetricsException extends BaseServiceException {
    
    public static final String METRICS_CALCULATION_FAILED = "METRICS_CALCULATION_FAILED";
    public static final String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
    public static final String INVALID_FINANCIAL_DATA = "INVALID_FINANCIAL_DATA";
    public static final String PRICING_STRATEGY_FAILED = "PRICING_STRATEGY_FAILED";
    
    public FinancialMetricsException(String message, String errorCode) {
        super(message, errorCode);
    }
    
    public FinancialMetricsException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
    
    public FinancialMetricsException(String message, String errorCode, Object... args) {
        super(message, errorCode, args);
    }
    
    public static FinancialMetricsException calculationFailed(String customerId, Throwable cause) {
        return new FinancialMetricsException(
                "Failed to calculate metrics for customer: " + customerId,
                METRICS_CALCULATION_FAILED,
                cause,
                customerId
        );
    }
    
    public static FinancialMetricsException customerNotFound(String customerId) {
        return new FinancialMetricsException(
                "Customer not found: " + customerId,
                CUSTOMER_NOT_FOUND,
                customerId
        );
    }
    
    public static FinancialMetricsException invalidData(String field, String value) {
        return new FinancialMetricsException(
                "Invalid financial data - " + field + ": " + value,
                INVALID_FINANCIAL_DATA,
                field,
                value
        );
    }
    
    public static FinancialMetricsException pricingStrategyFailed(String strategy, String quoteId, Throwable cause) {
        return new FinancialMetricsException(
                "Failed to apply pricing strategy '" + strategy + "' to quote: " + quoteId,
                PRICING_STRATEGY_FAILED,
                cause,
                strategy,
                quoteId
        );
    }
}