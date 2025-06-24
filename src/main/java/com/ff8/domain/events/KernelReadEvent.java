package com.ff8.domain.events;

import com.ff8.application.dto.MagicDisplayDTO;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Event fired when a kernel.bin file has been successfully read and parsed.
 * Contains the list of magic data that was loaded from the file.
 */
public class KernelReadEvent {
    private final String filePath;
    private final List<MagicDisplayDTO> magicData;
    private final int magicCount;
    private final Instant timestamp;
    private final long fileSizeBytes;
    
    public KernelReadEvent(String filePath, List<MagicDisplayDTO> magicData, long fileSizeBytes) {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.magicData = Objects.requireNonNull(magicData, "Magic data cannot be null");
        this.magicCount = magicData.size();
        this.fileSizeBytes = fileSizeBytes;
        this.timestamp = Instant.now();
    }
    
    /**
     * The file path that was loaded
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * The magic data that was parsed from the kernel file
     */
    public List<MagicDisplayDTO> getMagicData() {
        return magicData;
    }
    
    /**
     * The number of magic spells that were loaded
     */
    public int getMagicCount() {
        return magicCount;
    }
    
    /**
     * When this event was created
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * The size of the kernel file in bytes
     */
    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    @Override
    public String toString() {
        return "KernelReadEvent{" +
               "filePath='" + filePath + '\'' +
               ", magicCount=" + magicCount +
               ", timestamp=" + timestamp +
               ", fileSizeBytes=" + fileSizeBytes +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KernelReadEvent that = (KernelReadEvent) o;
        return magicCount == that.magicCount &&
               fileSizeBytes == that.fileSizeBytes &&
               Objects.equals(filePath, that.filePath) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filePath, magicCount, timestamp, fileSizeBytes);
    }
} 