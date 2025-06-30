package com.ff8.infrastructure.adapters.secondary.export;

import com.ff8.application.ports.secondary.BinaryExportPort;
import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.services.TextOffsetCalculationService;
import com.ff8.domain.exceptions.BinaryParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Infrastructure adapter for generating binary files containing newly created magic data.
 * Uses the existing binary parser to serialize magic data with calculated text offsets.
 */
public class BinaryExportAdapter implements BinaryExportPort {
    
    private static final Logger logger = LoggerFactory.getLogger(BinaryExportAdapter.class);
    private static final int MAGIC_STRUCT_SIZE = 60; // 60 bytes per magic structure
    
    private final BinaryParserPort binaryParser;
    
    public BinaryExportAdapter(BinaryParserPort binaryParser) {
        this.binaryParser = binaryParser;
    }
    
    @Override
    public BinaryGenerationResult generateBinaryFile(
            Collection<MagicData> newlyCreatedMagic,
            TextOffsetCalculationService.TextLayoutResult textLayout,
            Path outputFile) {
        
        long startTime = System.currentTimeMillis();
        logger.info("Generating binary file: {} with {} spells", outputFile.getFileName(), newlyCreatedMagic.size());
        
        try {
            // Ensure parent directory exists
            Path parentDir = outputFile.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                logger.debug("Created parent directory: {}", parentDir);
            }
            
            // Update magic data with calculated text offsets
            List<MagicData> updatedMagicList = new ArrayList<>();
            for (MagicData magic : newlyCreatedMagic) {
                MagicData updatedMagic = updateMagicWithTextOffsets(magic, textLayout);
                updatedMagicList.add(updatedMagic);
            }
            
            // Generate binary content
            long totalBytesWritten = 0;
            try (FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                
                for (MagicData magic : updatedMagicList) {
                    byte[] magicBytes = binaryParser.serializeMagicData(magic);
                    fos.write(magicBytes);
                    totalBytesWritten += magicBytes.length;
                    
                    logger.debug("Serialized magic {} (index {}): {} bytes", 
                        magic.getSpellName(), magic.getIndex(), magicBytes.length);
                }
                
                fos.flush();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Successfully generated binary file: {} bytes, {} spells in {} ms", 
                totalBytesWritten, updatedMagicList.size(), duration);
            
            return new BinaryGenerationResult(
                true, outputFile, null, totalBytesWritten, updatedMagicList.size(), duration);
            
        } catch (BinaryParseException e) {
            String error = "Binary serialization error: " + e.getMessage();
            logger.error(error, e);
            long duration = System.currentTimeMillis() - startTime;
            return new BinaryGenerationResult(false, outputFile, error, 0, 0, duration);
            
        } catch (IOException e) {
            String error = "File I/O error: " + e.getMessage();
            logger.error(error, e);
            long duration = System.currentTimeMillis() - startTime;
            return new BinaryGenerationResult(false, outputFile, error, 0, 0, duration);
            
        } catch (Exception e) {
            String error = "Unexpected error during binary generation: " + e.getMessage();
            logger.error(error, e);
            long duration = System.currentTimeMillis() - startTime;
            return new BinaryGenerationResult(false, outputFile, error, 0, 0, duration);
        }
    }
    
    @Override
    public MagicData updateMagicWithTextOffsets(
            MagicData magicData,
            TextOffsetCalculationService.TextLayoutResult textLayout) {
        
        TextOffsetCalculationService.SpellTextLayout spellLayout = 
            textLayout.getLayoutForSpell(magicData.getIndex());
        
        if (spellLayout == null) {
            logger.warn("No text layout found for spell index {}, using original offsets", magicData.getIndex());
            return magicData;
        }
        
        // Calculate binary offsets (with 511-byte adjustment)
        int binaryNameOffset = spellLayout.getBinaryNameOffset();
        int binaryDescOffset = spellLayout.getBinaryDescriptionOffset();
        
        logger.debug("Updating text offsets for spell {}: name=0x{} ({}), desc=0x{} ({})", 
            magicData.getIndex(), 
            Integer.toHexString(binaryNameOffset), binaryNameOffset,
            Integer.toHexString(binaryDescOffset), binaryDescOffset);
        
        // Return updated magic data with new text offsets
        return magicData.toBuilder()
            .offsetSpellName(binaryNameOffset)
            .offsetSpellDescription(binaryDescOffset)
            .build();
    }
    
    @Override
    public long calculateBinaryFileSize(Collection<MagicData> magicData) {
        return (long) magicData.size() * MAGIC_STRUCT_SIZE;
    }
    
    @Override
    public ValidationResult validateBinaryFileGeneration(Path outputFile) {
        if (outputFile == null) {
            return ValidationResult.failure("Output file path cannot be null");
        }
        
        // Check parent directory
        Path parentDir = outputFile.getParent();
        if (parentDir != null) {
            if (!Files.exists(parentDir)) {
                try {
                    Files.createDirectories(parentDir);
                } catch (IOException e) {
                    return ValidationResult.failure("Cannot create parent directory: " + e.getMessage());
                }
            }
            
            if (!Files.isDirectory(parentDir)) {
                return ValidationResult.failure("Parent path is not a directory");
            }
            
            if (!Files.isWritable(parentDir)) {
                return ValidationResult.failure("Parent directory is not writable");
            }
        }
        
        // Check if file already exists and is writable
        if (Files.exists(outputFile)) {
            if (Files.isDirectory(outputFile)) {
                return ValidationResult.failure("Output path is a directory, not a file");
            }
            
            if (!Files.isWritable(outputFile)) {
                return ValidationResult.failure("Output file exists and is not writable");
            }
        }
        
        // Check filename validity
        String filename = outputFile.getFileName().toString();
        if (filename.contains("/") || filename.contains("\\") || filename.contains(":")) {
            return ValidationResult.failure("Filename contains invalid characters");
        }
        
        return ValidationResult.success();
    }
} 