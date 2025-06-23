package com.ff8.application.services;

import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.KernelFileUseCase.ValidationResult;
import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.exceptions.BinaryParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class KernelFileService implements KernelFileUseCase {
    private static final Logger logger = Logger.getLogger(KernelFileService.class.getName());
    private static final int MAGIC_SECTION_OFFSET = 0x021C; // Standard offset for magic data section
    private static final int MAGIC_STRUCT_SIZE = 0x3C; // 60 bytes per magic entry
    private static final int MAGIC_COUNT = 56; // Number of magic spells in FF8
    
    private final BinaryParserPort binaryParser;
    private final FileSystemPort fileSystem;
    private final MagicRepository magicRepository;
    private boolean fileLoaded = false;
    private String currentFilePath;

    public KernelFileService(BinaryParserPort binaryParser, 
                           FileSystemPort fileSystem, 
                           MagicRepository magicRepository) {
        this.binaryParser = binaryParser;
        this.fileSystem = fileSystem;
        this.magicRepository = magicRepository;
    }

    @Override
    public void loadKernelFile(String filePath) throws BinaryParseException {
        try {
            logger.info("Loading kernel file: " + filePath);
            
            // Read the binary file
            byte[] kernelData = fileSystem.readBinaryFile(filePath);
            
            // Validate file size and structure
            if (kernelData.length < MAGIC_SECTION_OFFSET + (MAGIC_COUNT * MAGIC_STRUCT_SIZE)) {
                throw new BinaryParseException("Invalid kernel.bin file: insufficient size");
            }
            
            // Parse all magic data
            magicRepository.clear();
            
            for (int i = 0; i < MAGIC_COUNT; i++) {
                int offset = MAGIC_SECTION_OFFSET + (i * MAGIC_STRUCT_SIZE);
                MagicData magic = binaryParser.parseMagicData(kernelData, offset);
                // Set the index manually since the old interface doesn't support it
                magic = magic.toBuilder().index(i).build();
                magicRepository.save(magic);
                
                logger.fine("Parsed magic ID: " + i + " - " + magic.getExtractedSpellName());
            }
            
            this.currentFilePath = filePath;
            this.fileLoaded = true;
            
            logger.info("Successfully loaded " + MAGIC_COUNT + " magic spells from kernel file");
            
        } catch (IOException e) {
            throw new BinaryParseException("Failed to read kernel file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BinaryParseException("Failed to parse kernel file: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveKernelFile(String filePath) throws BinaryParseException {
        if (!fileLoaded) {
            throw new BinaryParseException("No kernel file loaded. Load a file first.");
        }
        
        try {
            logger.info("Saving kernel file: " + filePath);
            
            // Read the original kernel file to preserve non-magic data
            byte[] originalKernelData = fileSystem.readBinaryFile(currentFilePath != null ? currentFilePath : filePath);
            byte[] modifiedKernelData = originalKernelData.clone();
            
            // Get all magic data from repository
            List<MagicData> allMagic = magicRepository.findAll();
            
            if (allMagic.size() != MAGIC_COUNT) {
                throw new BinaryParseException("Invalid magic count: expected " + MAGIC_COUNT + ", got " + allMagic.size());
            }
            
            // Sort by magic ID to ensure correct order
            allMagic.sort((m1, m2) -> Integer.compare(m1.getMagicID(), m2.getMagicID()));
            
            // Serialize each magic data back to binary
            for (int i = 0; i < MAGIC_COUNT; i++) {
                MagicData magic = allMagic.get(i);
                if (magic.getMagicID() != i) {
                    throw new BinaryParseException("Magic ID mismatch at index " + i + ": expected " + i + ", got " + magic.getMagicID());
                }
                
                byte[] magicBinary = binaryParser.serializeMagicData(magic);
                if (magicBinary.length != MAGIC_STRUCT_SIZE) {
                    throw new BinaryParseException("Invalid serialized magic size: expected " + MAGIC_STRUCT_SIZE + ", got " + magicBinary.length);
                }
                
                // Write magic data to the correct offset in kernel
                int offset = MAGIC_SECTION_OFFSET + (i * MAGIC_STRUCT_SIZE);
                System.arraycopy(magicBinary, 0, modifiedKernelData, offset, MAGIC_STRUCT_SIZE);
                
                logger.fine("Serialized magic ID: " + i + " - " + magic.getExtractedSpellName());
            }
            
            // Write the modified kernel file
            fileSystem.writeBinaryFile(filePath, modifiedKernelData);
            
            // Update current file path if different
            if (!filePath.equals(currentFilePath)) {
                this.currentFilePath = filePath;
            }
            
            logger.info("Successfully saved kernel file with " + MAGIC_COUNT + " magic spells");
            
        } catch (IOException e) {
            throw new BinaryParseException("Failed to write kernel file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BinaryParseException("Failed to save kernel file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isFileLoaded() {
        return fileLoaded;
    }

    @Override
    public Optional<String> getCurrentFilePath() {
        return Optional.ofNullable(currentFilePath);
    }

    public void unloadFile() {
        this.fileLoaded = false;
        this.currentFilePath = null;
        this.magicRepository.clear();
        logger.info("Kernel file unloaded");
    }

    public int getMagicCount() {
        return MAGIC_COUNT;
    }

    public boolean hasUnsavedChanges() {
        // This would typically check if the repository has been modified
        // For now, we'll implement a simple version
        return fileLoaded && magicRepository.findAll().size() == MAGIC_COUNT;
    }

    @Override
    public boolean isModified() {
        return hasUnsavedChanges();
    }

    @Override
    public void createBackup(String backupPath) throws BinaryParseException {
        if (currentFilePath == null) {
            throw new BinaryParseException("No file is currently loaded");
        }
        try {
            fileSystem.createBackup(currentFilePath, backupPath);
        } catch (IOException e) {
            throw new BinaryParseException("Failed to create backup: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<FileInfo> getFileInfo() {
        if (currentFilePath == null) {
            return Optional.empty();
        }
        
        try {
            long fileSize = fileSystem.getFileSize(currentFilePath);
            // For lastModified, we would need to add this to FileSystemPort
            // For now, use current time as placeholder
            long lastModified = System.currentTimeMillis();
            int magicCount = magicRepository.count();
            String checksumMD5 = "TBD"; // Would need MD5 calculation
            
            return Optional.of(new FileInfo(currentFilePath, fileSize, lastModified, magicCount, checksumMD5));
        } catch (Exception e) {
            logger.warning("Failed to get file info: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public void closeFile() {
        unloadFile();
    }

    @Override
    public ValidationResult validateFileIntegrity() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!isFileLoaded()) {
            errors.add("No file is currently loaded");
            return new ValidationResult(false, "No file loaded", errors);
        }
        
        try {
            // Check if file still exists
            if (!fileSystem.fileExists(currentFilePath)) {
                errors.add("Current file no longer exists: " + currentFilePath);
            }
            
            // Validate magic data count
            List<MagicData> magicList = magicRepository.findAll();
            if (magicList.size() != MAGIC_COUNT) {
                warnings.add("Expected " + MAGIC_COUNT + " magic entries, found " + magicList.size());
            }
            
            // Check for duplicate magic IDs
            long uniqueIds = magicList.stream()
                .mapToInt(MagicData::getMagicID)
                .distinct()
                .count();
            
            if (uniqueIds != magicList.size()) {
                errors.add("Duplicate magic IDs detected");
            }
            
        } catch (Exception e) {
            errors.add("File integrity check failed: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors.isEmpty() ? "File integrity check passed" : "File integrity issues found", errors);
    }
} 