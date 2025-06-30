package com.ff8.domain.events;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Event emitted when an export operation completes successfully.
 * Contains information about created files and completion time.
 */
public record ExportCompletedEvent(
    String baseFileName,
    List<Path> createdFiles,
    LocalDateTime completionTime
) {
    
    public String getEventType() {
        return "export_completed";
    }
    
    public String getDisplayMessage() {
        return String.format("Export completed successfully: %s (%d files created)", 
            baseFileName, createdFiles.size());
    }
    
    public int getFileCount() {
        return createdFiles != null ? createdFiles.size() : 0;
    }
} 