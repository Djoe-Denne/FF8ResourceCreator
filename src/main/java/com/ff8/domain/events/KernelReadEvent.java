package com.ff8.domain.events;

import com.ff8.application.dto.MagicDisplayDTO;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Event fired when a kernel.bin file has been successfully read and parsed.
 * Contains the list of magic data that was loaded from the file.
 */
@Getter
@EqualsAndHashCode(of = {"filePath", "magicCount", "fileSizeBytes"})
@ToString(exclude = "magicData") // Exclude large list from toString for performance
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
} 