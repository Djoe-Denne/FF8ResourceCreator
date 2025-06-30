package com.ff8.application.ports.secondary;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.services.TextOffsetCalculationService;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Secondary port for generating binary files containing newly created magic data.
 * Abstracts the binary file generation process for different implementations.
 */
public interface BinaryExportPort {
    
    /**
     * Generate binary file containing newly created magic spells
     * 
     * @param newlyCreatedMagic Collection of newly created magic spells to export
     * @param textLayout Layout information with calculated text offsets
     * @param outputFile Target file path for the binary file
     * @return Result of the binary file generation
     */
    BinaryGenerationResult generateBinaryFile(
        Collection<MagicData> newlyCreatedMagic,
        TextOffsetCalculationService.TextLayoutResult textLayout,
        Path outputFile
    );
    
    /**
     * Update magic data with calculated text offsets before serialization
     * 
     * @param magicData Original magic data
     * @param textLayout Layout information with calculated offsets
     * @return Updated magic data with correct text offsets
     */
    MagicData updateMagicWithTextOffsets(
        MagicData magicData,
        TextOffsetCalculationService.TextLayoutResult textLayout
    );
    
    /**
     * Calculate the expected binary file size for given magic data
     * 
     * @param magicData Collection of magic data to calculate size for
     * @return Expected file size in bytes
     */
    long calculateBinaryFileSize(Collection<MagicData> magicData);
    
    /**
     * Validate that binary file can be created at the target location
     * 
     * @param outputFile Target file path
     * @return Validation result
     */
    ValidationResult validateBinaryFileGeneration(Path outputFile);
    
    /**
     * Result of binary file generation
     */
    record BinaryGenerationResult(
        boolean success,
        Path createdFile,
        String error,
        long bytesWritten,
        int spellsWritten,
        long durationMillis
    ) {
        public boolean hasError() {
            return error != null && !error.isEmpty();
        }
        
        public String getFormattedSize() {
            if (bytesWritten < 1024) {
                return bytesWritten + " bytes";
            } else if (bytesWritten < 1024 * 1024) {
                return String.format("%.1f KB", bytesWritten / 1024.0);
            } else {
                return String.format("%.1f MB", bytesWritten / (1024.0 * 1024.0));
            }
        }
        
        public String getSummary() {
            if (!success) {
                return "Binary generation failed: " + error;
            }
            return String.format("Generated binary file: %d spells, %s", 
                spellsWritten, getFormattedSize());
        }
    }
    
    /**
     * Validation result for binary file generation
     */
    record ValidationResult(
        boolean canGenerate,
        String issue
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult failure(String issue) {
            return new ValidationResult(false, issue);
        }
        
        public boolean isValid() {
            return canGenerate;
        }
    }
} 