package com.ff8.application.dto;

import java.nio.file.Path;
import java.util.Set;

/**
 * Data transfer object for export requests.
 * Contains all information needed to perform a localized export operation.
 */
public record ExportRequestDTO(
    String baseFileName,
    Path targetDirectory,
    Set<String> requiredLanguages,
    boolean includeWarningsInResult,
    boolean forceOverwrite
) {
    
    /**
     * Create a simple export request with just base filename and directory
     */
    public static ExportRequestDTO simple(String baseFileName, Path targetDirectory) {
        return new ExportRequestDTO(baseFileName, targetDirectory, Set.of("English"), false, false);
    }
    
    /**
     * Create an export request with specific language requirements
     */
    public static ExportRequestDTO withLanguages(String baseFileName, Path targetDirectory, Set<String> languages) {
        return new ExportRequestDTO(baseFileName, targetDirectory, languages, false, false);
    }
    
    /**
     * Get the binary file path that will be created
     */
    public Path getBinaryFilePath() {
        return targetDirectory.resolve(baseFileName + ".bin");
    }
    
    /**
     * Get the resource file path for a specific language
     */
    public Path getResourceFilePath(String language) {
        return targetDirectory.resolve(baseFileName + "_" + language.toLowerCase() + ".resources.bin");
    }
    
    /**
     * Check if the target directory exists and is writable
     */
    public boolean isTargetDirectoryValid() {
        return targetDirectory != null && 
               targetDirectory.toFile().exists() && 
               targetDirectory.toFile().isDirectory() &&
               targetDirectory.toFile().canWrite();
    }
    
    /**
     * Validate the export request for basic correctness
     */
    public boolean isValid() {
        return baseFileName != null && !baseFileName.trim().isEmpty() &&
               isTargetDirectoryValid() &&
               requiredLanguages != null && !requiredLanguages.isEmpty();
    }
} 