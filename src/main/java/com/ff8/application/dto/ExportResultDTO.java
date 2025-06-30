package com.ff8.application.dto;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Data transfer object for export operation results.
 * Contains information about the success/failure and details of the export operation.
 */
public record ExportResultDTO(
    boolean success,
    List<String> errors,
    List<String> warnings,
    ExportSummaryDTO summary,
    List<Path> createdFiles,
    LocalDateTime exportTime,
    long durationMillis
) {
    
    /**
     * Summary information about what was exported
     */
    public record ExportSummaryDTO(
        int totalSpells,
        int totalFiles,
        Set<String> languages,
        long totalFileSize,
        String baseFileName
    ) {
        public String getLanguagesDisplay() {
            return String.join(", ", languages);
        }
        
        public String getFormattedFileSize() {
            if (totalFileSize < 1024) {
                return totalFileSize + " bytes";
            } else if (totalFileSize < 1024 * 1024) {
                return String.format("%.1f KB", totalFileSize / 1024.0);
            } else {
                return String.format("%.1f MB", totalFileSize / (1024.0 * 1024.0));
            }
        }
    }
    
    /**
     * Create a successful export result
     */
    public static ExportResultDTO success(List<String> warnings, ExportSummaryDTO summary, 
                                        List<Path> createdFiles, long durationMillis) {
        return new ExportResultDTO(
            true, 
            List.of(), 
            warnings, 
            summary, 
            createdFiles, 
            LocalDateTime.now(), 
            durationMillis
        );
    }
    
    /**
     * Create a failed export result
     */
    public static ExportResultDTO failure(List<String> errors, List<String> warnings, long durationMillis) {
        return new ExportResultDTO(
            false, 
            errors, 
            warnings, 
            null, 
            List.of(), 
            LocalDateTime.now(), 
            durationMillis
        );
    }
    
    /**
     * Create a validation failure result
     */
    public static ExportResultDTO validationFailure(List<String> errors, List<String> warnings) {
        return new ExportResultDTO(
            false, 
            errors, 
            warnings, 
            null, 
            List.of(), 
            LocalDateTime.now(), 
            0
        );
    }
    
    /**
     * Check if the export had warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    /**
     * Check if the export had errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * Get formatted duration string
     */
    public String getFormattedDuration() {
        if (durationMillis < 1000) {
            return durationMillis + " ms";
        } else {
            return String.format("%.1f seconds", durationMillis / 1000.0);
        }
    }
    
    /**
     * Get a user-friendly result summary
     */
    public String getResultSummary() {
        if (!success) {
            return "Export failed with " + errors.size() + " error(s)";
        }
        
        StringBuilder sb = new StringBuilder("Export completed successfully");
        if (summary != null) {
            sb.append(" - ").append(summary.totalSpells()).append(" spell(s), ");
            sb.append(summary.totalFiles()).append(" file(s) created");
        }
        
        if (hasWarnings()) {
            sb.append(" (").append(warnings.size()).append(" warning(s))");
        }
        
        return sb.toString();
    }
    
    /**
     * Get detailed result information for logging
     */
    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Export Result:\n");
        sb.append("- Status: ").append(success ? "SUCCESS" : "FAILED").append("\n");
        sb.append("- Duration: ").append(getFormattedDuration()).append("\n");
        
        if (summary != null) {
            sb.append("- Spells exported: ").append(summary.totalSpells()).append("\n");
            sb.append("- Files created: ").append(summary.totalFiles()).append("\n");
            sb.append("- Languages: ").append(summary.getLanguagesDisplay()).append("\n");
            sb.append("- Total size: ").append(summary.getFormattedFileSize()).append("\n");
        }
        
        if (hasErrors()) {
            sb.append("- Errors: ").append(errors.size()).append("\n");
        }
        
        if (hasWarnings()) {
            sb.append("- Warnings: ").append(warnings.size()).append("\n");
        }
        
        if (!createdFiles.isEmpty()) {
            sb.append("- Created files:\n");
            for (Path file : createdFiles) {
                sb.append("  - ").append(file.getFileName()).append("\n");
            }
        }
        
        return sb.toString();
    }
} 