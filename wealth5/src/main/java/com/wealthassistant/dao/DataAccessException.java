package com.wealthassistant.dao;

public class DataAccessException extends Exception {
    
    public DataAccessException(String message) {
        super(message);
    }
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataAccessException(Throwable cause) {
        super(cause);
    }
} 