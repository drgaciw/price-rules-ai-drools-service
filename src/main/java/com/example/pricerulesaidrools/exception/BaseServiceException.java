package com.example.pricerulesaidrools.exception;

/**
 * Base exception class for all service-related exceptions
 */
public abstract class BaseServiceException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;
    
    protected BaseServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    protected BaseServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    protected BaseServiceException(String message, String errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args != null ? args : new Object[0];
    }
    
    protected BaseServiceException(String message, String errorCode, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args != null ? args : new Object[0];
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArgs() {
        return args;
    }
}