package com.ff8.infrastructure.adapters.secondary.export;

import com.ff8.application.ports.secondary.ResourceFileGeneratorPort;
import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;
import com.ff8.domain.services.TextEncodingService;
import com.ff8.domain.services.TextOffsetCalculationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Infrastructure adapter for generating resource files containing localized spell text.
 * Implements contiguous text layout with Caesar cipher encoding as required by FF8 format.
 */
public class ResourceFileGenerator implements ResourceFileGeneratorPort {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceFileGenerator.class);
    
    private final TextEncodingService textEncodingService;
    
    public ResourceFileGenerator(TextEncodingService textEncodingService) {
        this.textEncodingService = textEncodingService;
    }
    
    @Override
    public ResourceGenerationResult generateResourceFiles(
            Map<Integer, SpellTranslations> spellTranslations,
            TextOffsetCalculationService.TextLayoutResult textLayout,
            Path targetDirectory,
            String baseFileName) {
        
        long startTime = System.currentTimeMillis();
        logger.info("Generating resource files for {} languages in directory: {}", 
            textLayout.getRequiredLanguages().size(), targetDirectory);
        
        Map<Language, Path> createdFiles = new LinkedHashMap<>();
        Map<Language, String> errors = new LinkedHashMap<>();
        long totalBytesWritten = 0;
        
        try {
            // Ensure target directory exists
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
                logger.info("Created target directory: {}", targetDirectory);
            }
            
            // Generate file for each required language
            for (Language language : textLayout.getRequiredLanguages()) {
                Path outputFile = getResourceFilePath(targetDirectory, baseFileName, language);
                
                try {
                    SingleFileResult result = generateSingleResourceFile(
                        spellTranslations, language, textLayout, outputFile);
                    
                    if (result.success()) {
                        createdFiles.put(language, result.createdFile());
                        totalBytesWritten += result.bytesWritten();
                        logger.info("Generated {} resource file: {} bytes", 
                            language.getDisplayName(), result.bytesWritten());
                    } else {
                        errors.put(language, result.error());
                        logger.error("Failed to generate {} resource file: {}", 
                            language.getDisplayName(), result.error());
                    }
                    
                } catch (Exception e) {
                    String error = "Unexpected error generating " + language.getDisplayName() + " file: " + e.getMessage();
                    errors.put(language, error);
                    logger.error(error, e);
                }
            }
            
        } catch (IOException e) {
            logger.error("Error creating target directory", e);
            return new ResourceGenerationResult(false, createdFiles, errors, totalBytesWritten, 
                System.currentTimeMillis() - startTime);
        }
        
        boolean success = errors.isEmpty();
        long duration = System.currentTimeMillis() - startTime;
        
        if (success) {
            logger.info("Successfully generated {} resource files in {} ms", 
                createdFiles.size(), duration);
        } else {
            logger.error("Resource generation completed with {} errors", errors.size());
        }
        
        return new ResourceGenerationResult(success, createdFiles, errors, totalBytesWritten, duration);
    }
    
    @Override
    public SingleFileResult generateSingleResourceFile(
            Map<Integer, SpellTranslations> spellTranslations,
            Language language,
            TextOffsetCalculationService.TextLayoutResult textLayout,
            Path outputFile) {
        
        logger.debug("Generating resource file for {}: {}", language.getDisplayName(), outputFile);
        
        try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
            
            // Sort spell indices for consistent ordering
            List<Integer> sortedSpellIndices = new ArrayList<>(spellTranslations.keySet());
            Collections.sort(sortedSpellIndices);
            
            long totalBytesWritten = 0;
            
            for (int spellIndex : sortedSpellIndices) {
                SpellTranslations translations = spellTranslations.get(spellIndex);
                TextOffsetCalculationService.SpellTextLayout spellLayout = 
                    textLayout.getLayoutForSpell(spellIndex);
                
                if (spellLayout == null) {
                    logger.warn("No layout found for spell index {}", spellIndex);
                    continue;
                }
                
                // Get translation for this language (with English fallback)
                SpellTranslations.Translation translation = translations.getTranslation(language.getDisplayName())
                    .orElse(translations.getTranslation("English")
                        .orElse(new SpellTranslations.Translation("", "")));
                
                // Encode the text using Caesar cipher
                String encodedName = textEncodingService.encipherCaesarCode(translation.getName());
                String encodedDescription = textEncodingService.encipherCaesarCode(translation.getDescription());
                
                // Write spell name with padding
                totalBytesWritten += writeTextWithPadding(fos, encodedName, spellLayout.getMaxNameLength());
                
                // Write spell description with padding
                totalBytesWritten += writeTextWithPadding(fos, encodedDescription, spellLayout.getMaxDescriptionLength());
            }
            
            logger.debug("Wrote {} bytes to {}", totalBytesWritten, outputFile.getFileName());
            return new SingleFileResult(true, outputFile, null, totalBytesWritten);
            
        } catch (IOException e) {
            String error = "Failed to write resource file: " + e.getMessage();
            logger.error(error, e);
            return new SingleFileResult(false, outputFile, error, 0);
        } catch (Exception e) {
            String error = "Unexpected error during file generation: " + e.getMessage();
            logger.error(error, e);
            return new SingleFileResult(false, outputFile, error, 0);
        }
    }
    
    @Override
    public ValidationResult validateResourceFileGeneration(
            Path targetDirectory,
            String baseFileName,
            Iterable<Language> requiredLanguages) {
        
        // Check target directory
        if (targetDirectory == null) {
            return ValidationResult.failure("Target directory cannot be null");
        }
        
        if (!Files.exists(targetDirectory)) {
            try {
                Files.createDirectories(targetDirectory);
            } catch (IOException e) {
                return ValidationResult.failure("Cannot create target directory: " + e.getMessage());
            }
        }
        
        if (!Files.isDirectory(targetDirectory)) {
            return ValidationResult.failure("Target path is not a directory");
        }
        
        if (!Files.isWritable(targetDirectory)) {
            return ValidationResult.failure("Target directory is not writable");
        }
        
        // Check base filename
        if (baseFileName == null || baseFileName.trim().isEmpty()) {
            return ValidationResult.failure("Base filename cannot be null or empty");
        }
        
        if (baseFileName.contains("/") || baseFileName.contains("\\") || baseFileName.contains(":")) {
            return ValidationResult.failure("Base filename contains invalid characters");
        }
        
        // Check for file conflicts
        for (Language language : requiredLanguages) {
            Path resourceFile = getResourceFilePath(targetDirectory, baseFileName, language);
            if (Files.exists(resourceFile) && !Files.isWritable(resourceFile)) {
                return ValidationResult.failure("Resource file exists and is not writable: " + 
                    resourceFile.getFileName());
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Write text with null termination and padding to reach target length
     */
    private long writeTextWithPadding(FileOutputStream fos, String text, int targetLength) throws IOException {
        // Convert text to bytes
        byte[] textBytes = text.getBytes("ISO-8859-1"); // Use ISO-8859-1 for compatibility
        
        // Write the text
        fos.write(textBytes);
        long bytesWritten = textBytes.length;
        
        // Write null terminator
        fos.write(0);
        bytesWritten++;
        
        // Write padding to reach target length
        int paddingNeeded = targetLength - (int)bytesWritten;
        if (paddingNeeded > 0) {
            byte[] padding = new byte[paddingNeeded];
            Arrays.fill(padding, (byte) 0);
            fos.write(padding);
            bytesWritten += paddingNeeded;
        }
        
        return bytesWritten;
    }
    
    /**
     * Get the resource file path for a specific language
     */
    private Path getResourceFilePath(Path targetDirectory, String baseFileName, Language language) {
        String filename = baseFileName + "_" + language.getDisplayName().toLowerCase() + ".resources.bin";
        return targetDirectory.resolve(filename);
    }
} 