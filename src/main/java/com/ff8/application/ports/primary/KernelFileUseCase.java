package com.ff8.application.ports.primary;

import com.ff8.domain.exceptions.BinaryParseException;

import java.io.IOException;
import java.util.Optional;

/**
 * Primary port for kernel.bin file operations.
 * Handles loading and saving of the kernel file.
 */
public interface KernelFileUseCase {

    /**
     * Load kernel.bin file from specified path
     */
    void loadKernelFile(String filePath) throws BinaryParseException, IOException;

    /**
     * Load magic binary file and add to existing data with isNewlyCreated flag
     */
    void loadMagicBinary(String filePath) throws BinaryParseException, IOException;

    /**
     * Save modifications back to kernel.bin file
     */
    void saveKernelFile(String filePath) throws BinaryParseException, IOException;

    /**
     * Check if a kernel file is currently loaded
     */
    boolean isFileLoaded();

    /**
     * Get the currently loaded file path
     */
    Optional<String> getCurrentFilePath();

    /**
     * Get file information
     */
    Optional<FileInfo> getFileInfo();

    /**
     * Close current file and clear data
     */
    void closeFile();

    /**
     * Check if file has been modified since last save
     */
    boolean isModified();

    /**
     * Create backup of current file
     */
    void createBackup(String backupPath) throws IOException;

    /**
     * Validate file integrity
     */
    ValidationResult validateFileIntegrity();

    /**
     * Record for file information
     */
    record FileInfo(
            String filePath,
            long fileSize,
            long lastModified,
            int magicCount,
            String checksumMD5
    ) {}

    /**
     * Record for validation results
     */
    record ValidationResult(
            boolean isValid,
            String message,
            java.util.List<String> issues
    ) {}
} 