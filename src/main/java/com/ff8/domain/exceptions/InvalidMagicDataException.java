package com.ff8.domain.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when magic data validation fails.
 * Used for domain-level validation errors.
 */
@StandardException
public class InvalidMagicDataException extends RuntimeException {
    
    public InvalidMagicDataException(String field, Object value, String reason) {
        super("Invalid value for field '" + field + "': " + value + " - " + reason);
    }
} 