package com.ff8.domain.exceptions;

/**
 * Exception thrown when binary parsing fails.
 * Used for errors during kernel.bin file parsing.
 */
public class BinaryParseException extends RuntimeException {
    
    public BinaryParseException(String message) {
        super(message);
    }
    
    public BinaryParseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BinaryParseException(String operation, int offset, String reason) {
        super("Binary parse error during " + operation + " at offset 0x" + 
              Integer.toHexString(offset).toUpperCase() + ": " + reason);
    }
} 