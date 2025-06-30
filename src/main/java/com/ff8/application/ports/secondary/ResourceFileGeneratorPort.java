package com.ff8.application.ports.secondary;

import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;
import com.ff8.domain.services.TextOffsetCalculationService;

import java.nio.file.Path;
import java.util.Map;

/**
 * Secondary port for generating resource files containing localized spell text.
 * Abstracts the file generation process to allow different implementations.
 */
public interface ResourceFileGeneratorPort {
    
    /**
     * Generate resource files for all required languages
     * 
     * @param spellTranslations Map of spell index to translations
     * @param textLayout Layout information for consistent text positioning
     * @param targetDirectory Directory where files should be created
     * @param baseFileName Base filename for resource files (language suffix will be added)
     * @return Result of the resource file generation
     */
    ResourceGenerationResult generateResourceFiles(
        Map<Integer, SpellTranslations> spellTranslations,
        TextOffsetCalculationService.TextLayoutResult textLayout,
        Path targetDirectory,
        String baseFileName
    );
    
    /**
     * Generate a single resource file for a specific language
     * 
     * @param spellTranslations Map of spell index to translations
     * @param language Language to generate file for
     * @param textLayout Layout information for text positioning
     * @param outputFile Target file path
     * @return Result of the single file generation
     */
    SingleFileResult generateSingleResourceFile(
        Map<Integer, SpellTranslations> spellTranslations,
        Language language,
        TextOffsetCalculationService.TextLayoutResult textLayout,
        Path outputFile
    );
    
    /**
     * Validate that resource files can be created in the target directory
     * 
     * @param targetDirectory Directory to validate
     * @param baseFileName Base filename to validate
     * @param requiredLanguages Languages that will need resource files
     * @return Validation result
     */
    ValidationResult validateResourceFileGeneration(
        Path targetDirectory,
        String baseFileName,
        Iterable<Language> requiredLanguages
    );
    
    /**
     * Result of generating multiple resource files
     */
    record ResourceGenerationResult(
        boolean success,
        Map<Language, Path> createdFiles,
        Map<Language, String> errors,
        long totalBytesWritten,
        long durationMillis
    ) {
        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
        
        public int getFileCount() {
            return createdFiles != null ? createdFiles.size() : 0;
        }
    }
    
    /**
     * Result of generating a single resource file
     */
    record SingleFileResult(
        boolean success,
        Path createdFile,
        String error,
        long bytesWritten
    ) {
        public boolean hasError() {
            return error != null && !error.isEmpty();
        }
    }
    
    /**
     * Validation result for resource file generation
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
    }
} 