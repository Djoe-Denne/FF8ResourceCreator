package com.ff8.domain.events;

import java.time.LocalDateTime;

/**
 * Event emitted when an export operation is started.
 * Notifies observers that a localized export process has begun.
 */
public record ExportStartedEvent(
    String baseFileName,
    LocalDateTime startTime
) {
    
    public String getEventType() {
        return "export_started";
    }
    
    public String getDisplayMessage() {
        return "Starting export of newly created magic to: " + baseFileName;
    }
} 