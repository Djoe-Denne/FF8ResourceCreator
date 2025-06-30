package com.ff8.domain.events;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event emitted when an export operation fails.
 * Contains error information and failure time.
 */
public record ExportFailedEvent(
    String baseFileName,
    List<String> errors,
    LocalDateTime failureTime
) {
    
    public String getEventType() {
        return "export_failed";
    }
    
    public String getDisplayMessage() {
        return String.format("Export failed: %s (%d error(s))", 
            baseFileName, errors.size());
    }
    
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }
    
    public String getFirstError() {
        return errors != null && !errors.isEmpty() ? errors.get(0) : "Unknown error";
    }
    
    public String getAllErrors() {
        return errors != null ? String.join("; ", errors) : "No error details available";
    }
} 