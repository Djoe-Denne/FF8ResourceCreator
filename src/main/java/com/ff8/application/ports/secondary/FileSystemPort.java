package com.ff8.application.ports.secondary;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Secondary port for file system operations.
 * This interface will be implemented by infrastructure adapters.
 */
public interface FileSystemPort {

    /**
     * Read binary file and return its contents
     */
    byte[] readBinaryFile(String filePath) throws IOException;

    /**
     * Read text file and return its contents as a list of lines
     */
    List<String> readTextFileLines(String filePath) throws IOException;

    /**
     * Write binary data to file
     */
    void writeBinaryFile(String filePath, byte[] data) throws IOException;

    /**
     * Check if file exists
     */
    boolean fileExists(String filePath);

    /**
     * Get file size in bytes
     */
    long getFileSize(String filePath) throws IOException;

    /**
     * Get file last modified time
     */
    Instant getLastModified(String filePath) throws IOException;

    /**
     * Create backup copy of file
     */
    void createBackup(String sourceFilePath, String backupFilePath) throws IOException;

    /**
     * Create directory if it doesn't exist
     */
    void createDirectories(String directoryPath) throws IOException;

    /**
     * Get file information
     */
    FileInfo getFileInfo(String filePath) throws IOException;

    /**
     * Calculate MD5 checksum of file
     */
    String calculateMD5(String filePath) throws IOException;

    /**
     * Check if file is readable
     */
    boolean isReadable(String filePath);

    /**
     * Check if file is writable
     */
    boolean isWritable(String filePath);

    /**
     * Get temporary directory path
     */
    String getTempDirectory();

    /**
     * Create temporary file
     */
    String createTempFile(String prefix, String suffix) throws IOException;

    /**
     * Delete file
     */
    void deleteFile(String filePath) throws IOException;

    /**
     * List files in directory with pattern
     */
    List<String> listFiles(String directoryPath, String pattern) throws IOException;

    /**
     * Get user's home directory
     */
    String getUserHomeDirectory();

    /**
     * Get current working directory
     */
    String getCurrentDirectory();

    /**
     * Resolve path relative to base path
     */
    String resolvePath(String basePath, String relativePath);

    /**
     * Get file extension
     */
    String getFileExtension(String filePath);

    /**
     * Get file name without extension
     */
    String getFileNameWithoutExtension(String filePath);

    /**
     * Record for file information
     */
    record FileInfo(
            String path,
            long size,
            Instant lastModified,
            Instant created,
            boolean isReadable,
            boolean isWritable,
            String extension
    ) {}
} 