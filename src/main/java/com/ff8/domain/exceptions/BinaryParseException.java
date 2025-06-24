package com.ff8.domain.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when binary parsing fails.
 * Used for errors during kernel.bin file parsing.
 */
@StandardException
public class BinaryParseException extends RuntimeException {
    
    public BinaryParseException(String operation, int offset, String reason) {
        super("Binary parse error during " + operation + " at offset 0x" + 
              Integer.toHexString(offset).toUpperCase() + ": " + reason);
    }
} 