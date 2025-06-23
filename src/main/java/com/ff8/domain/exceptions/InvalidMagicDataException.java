package com.ff8.domain.exceptions;

/**
 * Exception thrown when magic data validation fails.
 * Used for domain-level validation errors.
 */
public class InvalidMagicDataException extends RuntimeException {
    
    public InvalidMagicDataException(String message) {
        super(message);
    }
    
    public InvalidMagicDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidMagicDataException(String field, Object value, String reason) {
        super("Invalid value for field '" + field + "': " + value + " - " + reason);
    }
} 