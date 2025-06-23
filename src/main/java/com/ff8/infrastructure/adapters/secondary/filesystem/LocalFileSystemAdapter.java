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

public class LocalFileSystemAdapter implements FileSystemPort {
    private static final Logger logger = Logger.getLogger(LocalFileSystemAdapter.class.getName());

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

    @Override
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    @Override
    public long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        return Files.size(path);
    }

    @Override
    public boolean isReadable(String filePath) {
        return Files.isReadable(Paths.get(filePath));
    }

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

    @Override
    public String resolvePath(String basePath, String relativePath) {
        Path base = Paths.get(basePath);
        Path relative = Paths.get(relativePath);
        return base.resolve(relative).toString();
    }

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

    @Override
    public String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }

    @Override
    public String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    @Override
    public java.util.List<String> listFiles(String directoryPath, String pattern) throws IOException {
        Path directory = Paths.get(directoryPath);
        
        if (!Files.exists(directory)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }
        
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directoryPath);
        }
        
        try {
            return Files.list(directory)
                .filter(Files::isRegularFile)
                .filter(path -> pattern == null || path.toString().toLowerCase().contains(pattern.toLowerCase()))
                .map(path -> path.getFileName().toString())
                .collect(java.util.stream.Collectors.toList());
        } catch (IOException e) {
            throw new IOException("Failed to list files in directory: " + directoryPath, e);
        }
    }

    @Override
    public Instant getLastModified(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        return Files.getLastModifiedTime(path).toInstant();
    }

    @Override
    public void createDirectories(String directoryPath) throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
    }

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

    @Override
    public String calculateMD5(String filePath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = readBinaryFile(filePath);
            byte[] hash = md.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 algorithm not available", e);
        }
    }

    @Override
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    @Override
    public String createTempFile(String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        return tempFile.toString();
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
} 