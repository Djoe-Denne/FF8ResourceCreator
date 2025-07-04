package com.ff8.domain.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the different sections within the FF8 kernel.bin file.
 * Each section contains specific game data that requires specialized parsing logic.
 */
@Getter
@AllArgsConstructor
public enum SectionType {
    MAGIC("Magic", 0x021C, 0x3C, 56, "Contains spell/magic data including stats, effects, and junction information");
    
    private final String displayName;
    private final int standardOffset;
    private final int structSize;
    private final int expectedCount;
    private final String description;
    
    /**
     * Check if this section type has been implemented for parsing
     */
    public boolean isImplemented() {
        return this == MAGIC; // Only magic is currently implemented
    }
} 