package com.ff8.infrastructure.adapters.secondary.filesystem;

import com.ff8.application.ports.secondary.FileSystemPort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * Local file system adapter providing file I/O operations for the FF8 Magic Creator.
 * 
 * <p>This class implements the {@link FileSystemPort} interface as a Secondary Adapter
 * in the hexagonal architecture, providing concrete file system operations using
 * Java NIO.2 APIs. It handles all file system interactions including reading,
 * writing, validation, and backup operations.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Robust file operations with comprehensive error handling</li>
 *   <li>Automatic backup creation before file modifications</li>
 *   <li>Path validation and security checks</li>
 *   <li>MD5 checksum calculation for file integrity</li>
 *   <li>Cross-platform file system operations</li>
 *   <li>Comprehensive logging for debugging and monitoring</li>
 * </ul>
 * 
 * <p>Security considerations:</p>
 * <ul>
 *   <li>Validates file existence and accessibility before operations</li>
 *   <li>Checks read/write permissions before attempting operations</li>
 *   <li>Creates parent directories safely when needed</li>
 *   <li>Handles file locking and concurrent access scenarios</li>
 * </ul>
 * 
 * <p>The adapter provides full support for kernel.bin file operations including:</p>
 * <ul>
 *   <li>Binary file reading and writing for kernel data</li>
 *   <li>Text file operations for configuration and preferences</li>
 *   <li>Backup management for safe file modifications</li>
 *   <li>File integrity verification through checksums</li>
 * </ul>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class LocalFileSystemAdapter implements FileSystemPort {
    private static final Logger logger = Logger.getLogger(LocalFileSystemAdapter.class.getName());

    /**
     * {@inheritDoc}
     * 
     * <p>Reads a binary file from the local file system with comprehensive validation.
     * This method performs the following operations:</p>
     * <ul>
     *   <li>Validates file existence and accessibility</li>
     *   <li>Checks that the path points to a regular file</li>
     *   <li>Verifies read permissions</li>
     *   <li>Reads the entire file content into a byte array</li>
     *   <li>Logs the operation for debugging purposes</li>
     * </ul>
     * 
     * @param filePath The path to the binary file to read
     * @return The complete file content as a byte array
     * @throws IOException if the file doesn't exist, is not readable, or an I/O error occurs
     */
    @Override
    public byte[] readBinaryFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }
        
        logger.info("Reading binary file: " + filePath);
        byte[] data = Files.readAllBytes(path);
        logger.fine("Read " + data.length + " bytes from " + filePath);
        
        return data;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Reads a text file from the local file system and returns its content as lines.
     * This method performs validation similar to binary file reading and uses the
     * default character encoding of the platform.</p>
     * 
     * @param filePath The path to the text file to read
     * @return List of strings, each representing a line in the file
     * @throws IOException if the file doesn't exist, is not readable, or an I/O error occurs
     */
    @Override
    public java.util.List<String> readTextFileLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }
        
        logger.info("Reading text file: " + filePath);
        java.util.List<String> lines = Files.readAllLines(path);
        logger.fine("Read " + lines.size() + " lines from " + filePath);
        
        return lines;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Writes binary data to a file with automatic backup creation and directory management.
     * This method performs the following operations:</p>
     * <ul>
     *   <li>Validates the data parameter is not null</li>
     *   <li>Creates parent directories if they don't exist</li>
     *   <li>Creates a backup of the existing file if it exists</li>
     *   <li>Writes the data to the specified file path</li>
     *   <li>Logs the operation for debugging purposes</li>
     * </ul>
     * 
     * @param filePath The path where the binary data should be written
     * @param data The binary data to write to the file
     * @throws IOException if the write operation fails or data validation fails
     * @throws IllegalArgumentException if data is null
     */
    @Override
    public void writeBinaryFile(String filePath, byte[] data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        
        // Create parent directories if they don't exist
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        // Create backup if file already exists
        if (Files.exists(path)) {
            createBackup(path);
        }
        
        logger.info("Writing binary file: " + filePath);
        Files.write(path, data);
        logger.fine("Wrote " + data.length + " bytes to " + filePath);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Checks if a file exists at the specified path using Java NIO.2 APIs.</p>
     * 
     * @param filePath The path to check for file existence
     * @return true if the file exists, false otherwise
     */
    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Gets the size of a file in bytes. This method validates file existence
     * before attempting to retrieve the size.</p>
     * 
     * @param filePath The path to the file to measure
     * @return The size of the file in bytes
     * @throws IOException if the file doesn't exist or an I/O error occurs
     */
    @Override
    public long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        return Files.size(path);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Checks if a file is readable by the current process.</p>
     * 
     * @param filePath The path to the file to check
     * @return true if the file is readable, false otherwise
     */
    @Override
    public boolean isReadable(String filePath) {
        return Files.isReadable(Paths.get(filePath));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Checks if a file is writable by the current process. For non-existing files,
     * this method checks if the parent directory is writable.</p>
     * 
     * @param filePath The path to the file to check
     * @return true if the file is writable or can be created, false otherwise
     */
    @Override
    public boolean isWritable(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return Files.isWritable(path);
        } else {
            // Check if parent directory is writable
            Path parent = path.getParent();
            return parent != null && Files.isWritable(parent);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Creates a backup copy of a file at the specified backup location.
     * If the backup file already exists, numbered backups are created to
     * avoid overwriting existing backups.</p>
     * 
     * @param sourceFilePath The path to the source file to backup
     * @param backupFilePath The path where the backup should be created
     * @throws IOException if the source file doesn't exist or backup creation fails
     */
    @Override
    public void createBackup(String sourceFilePath, String backupFilePath) throws IOException {
        Path originalPath = Paths.get(sourceFilePath);
        if (!Files.exists(originalPath)) {
            throw new IOException("Cannot create backup: file does not exist: " + sourceFilePath);
        }
        
        Path backup = Paths.get(backupFilePath);
        
        // If backup already exists, create numbered backup
        int counter = 1;
        while (Files.exists(backup)) {
            String numberedBackupPath = backupFilePath + "." + counter;
            backup = Paths.get(numberedBackupPath);
            counter++;
        }
        
        logger.info("Creating backup: " + backup.toString());
        Files.copy(originalPath, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * Creates a backup of an existing file using automatic naming.
     * 
     * <p>This private method creates a backup with a .backup extension,
     * and if that exists, creates numbered backups (.backup.1, .backup.2, etc.)
     * to avoid overwriting existing backups.</p>
     * 
     * @param originalPath The path to the original file to backup
     * @throws IOException if the backup creation fails
     */
    private void createBackup(Path originalPath) throws IOException {
        String backupPath = originalPath.toString() + ".backup";
        Path backup = Paths.get(backupPath);
        
        // If backup already exists, create numbered backup
        int counter = 1;
        while (Files.exists(backup)) {
            backupPath = originalPath.toString() + ".backup." + counter;
            backup = Paths.get(backupPath);
            counter++;
        }
        
        logger.fine("Creating backup: " + backupPath);
        Files.copy(originalPath, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Extracts the file extension from a file path. Returns an empty string
     * if no extension is found or if the path is invalid.</p>
     * 
     * @param filePath The file path to extract the extension from
     * @return The file extension without the dot, or empty string if no extension
     */
    @Override
    public String getFileExtension(String filePath) {
        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();
        String fileNameStr = fileName != null ? fileName.toString() : "";
        
        if (fileNameStr.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileNameStr.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileNameStr.length() - 1) {
            return fileNameStr.substring(lastDotIndex + 1);
        }
        
        return ""; // No extension found
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Resolves a relative path against a base path to create an absolute path.
     * This method uses Java NIO.2 path resolution which handles cross-platform
     * path separators correctly.</p>
     * 
     * @param basePath The base path to resolve against
     * @param relativePath The relative path to resolve
     * @return The resolved absolute path as a string
     */
    @Override
    public String resolvePath(String basePath, String relativePath) {
        Path base = Paths.get(basePath);
        Path relative = Paths.get(relativePath);
        return base.resolve(relative).toString();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Extracts the file name without its extension from a file path.
     * This method handles edge cases like hidden files and files without extensions.</p>
     * 
     * @param filePath The file path to extract the name from
     * @return The file name without extension, or empty string if invalid path
     */
    @Override
    public String getFileNameWithoutExtension(String filePath) {
        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();
        String fileNameStr = fileName != null ? fileName.toString() : "";
        
        if (fileNameStr.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileNameStr.lastIndexOf('.');
        if (lastDotIndex > 0) { // > 0 to avoid hidden files like ".gitignore"
            return fileNameStr.substring(0, lastDotIndex);
        }
        
        return fileNameStr; // No extension found
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns the current working directory of the application.</p>
     * 
     * @return The current working directory as a string
     */
    @Override
    public String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns the user's home directory path.</p>
     * 
     * @return The user's home directory as a string
     */
    @Override
    public String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Lists files in a directory that match the specified pattern.
     * The pattern can use glob syntax for flexible file matching.</p>
     * 
     * @param directoryPath The directory to list files from
     * @param pattern The glob pattern to match files (e.g., "*.bin")
     * @return List of file paths that match the pattern
     * @throws IOException if the directory doesn't exist or cannot be read
     */
    @Override
    public java.util.List<String> listFiles(String directoryPath, String pattern) throws IOException {
        Path dir = Paths.get(directoryPath);
        
        if (!Files.exists(dir)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }
        
        if (!Files.isDirectory(dir)) {
            throw new IOException("Path is not a directory: " + directoryPath);
        }
        
        try (java.util.stream.Stream<Path> stream = Files.find(dir, 1, 
                (path, attrs) -> attrs.isRegularFile() && path.getFileName().toString().matches(pattern.replace("*", ".*")))) {
            return stream.map(Path::toString).collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Gets the last modified time of a file as an Instant.</p>
     * 
     * @param filePath The path to the file to check
     * @return The last modified time as an Instant
     * @throws IOException if the file doesn't exist or attributes cannot be read
     */
    @Override
    public Instant getLastModified(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.lastModifiedTime().toInstant();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Creates directories including any necessary parent directories.
     * This method is idempotent - it won't fail if the directories already exist.</p>
     * 
     * @param directoryPath The directory path to create
     * @throws IOException if the directory creation fails
     */
    @Override
    public void createDirectories(String directoryPath) throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Retrieves comprehensive file information including size, modification time,
     * and file type. This method provides all the basic file attributes needed
     * for file management operations.</p>
     * 
     * @param filePath The path to the file to get information about
     * @return FileInfo object containing file metadata
     * @throws IOException if the file doesn't exist or attributes cannot be read
     */
    @Override
    public FileSystemPort.FileInfo getFileInfo(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        
        return new FileSystemPort.FileInfo(
            filePath,
            attrs.size(),
            attrs.lastModifiedTime().toInstant(),
            attrs.creationTime().toInstant(),
            Files.isReadable(path),
            Files.isWritable(path),
            getFileExtension(filePath)
        );
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Calculates the MD5 checksum of a file for integrity verification.
     * This method is useful for verifying file integrity and detecting corruption.</p>
     * 
     * @param filePath The path to the file to calculate checksum for
     * @return The MD5 checksum as a hexadecimal string
     * @throws IOException if the file cannot be read or doesn't exist
     */
    @Override
    public String calculateMD5(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = Files.readAllBytes(path);
            byte[] digest = md5.digest(data);
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 algorithm not available", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns the system's temporary directory path.</p>
     * 
     * @return The temporary directory path as a string
     */
    @Override
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Creates a temporary file with the specified prefix and suffix.
     * The file is created in the system's temporary directory.</p>
     * 
     * @param prefix The prefix for the temporary file name
     * @param suffix The suffix for the temporary file name
     * @return The path to the created temporary file
     * @throws IOException if the temporary file cannot be created
     */
    @Override
    public String createTempFile(String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        return tempFile.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Deletes a file from the file system. This method will fail if the file
     * is in use or if there are insufficient permissions.</p>
     * 
     * @param filePath The path to the file to delete
     * @throws IOException if the file cannot be deleted or doesn't exist
     */
    @Override
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.delete(path);
    }
} 