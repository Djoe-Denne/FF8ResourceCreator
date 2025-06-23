package com.ff8.application.dto;

import java.util.List;

/**
 * Data Transfer Object for raw binary view of magic data.
 * Represents the magic structure as raw hex values with offsets.
 */
public record RawViewDTO(
        List<RawFieldEntry> fields
) {
    /**
     * Represents a single field in the raw binary view.
     */
    public record RawFieldEntry(
            String offset,      // Hex offset like "00000000"
            String type,        // WORD, BYTE, DWORD
            String name,        // Field name like "offsetSpellName"
            String value        // Hex value like "0A1B"
    ) {}
    
    /**
     * Get field by name for easy lookup
     */
    public RawFieldEntry getFieldByName(String fieldName) {
        return fields.stream()
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get field by offset for easy lookup
     */
    public RawFieldEntry getFieldByOffset(String offset) {
        return fields.stream()
                .filter(field -> field.offset().equals(offset))
                .findFirst()
                .orElse(null);
    }
} 