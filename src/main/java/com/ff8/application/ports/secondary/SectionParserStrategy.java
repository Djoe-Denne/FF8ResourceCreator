package com.ff8.application.ports.secondary;

import com.ff8.application.ports.secondary.BinaryParserPort.ValidationResult;
import com.ff8.domain.entities.enums.SectionType;
import com.ff8.domain.exceptions.BinaryParseException;

import java.util.List;

/**
 * Strategy interface for parsing specific sections of the FF8 kernel.bin file.
 * Each implementation handles parsing and serialization for a particular section type.
 */
public interface SectionParserStrategy<T> {
    
    /**
     * Get the section type this strategy handles
     */
    SectionType getSectionType();
    
    /**
     * Parse a single item from binary data at the specified offset
     * @param binaryData The complete kernel binary data
     * @param offset The offset where the item starts
     * @param index The index of the item within the section (for context)
     * @return The parsed item
     * @throws BinaryParseException if parsing fails
     */
    T parseItem(byte[] binaryData, int offset, int index) throws BinaryParseException;
    
    /**
     * Serialize a single item to binary data
     * @param item The item to serialize
     * @return The serialized binary data
     * @throws BinaryParseException if serialization fails
     */
    byte[] serializeItem(T item) throws BinaryParseException;
    
    /**
     * Parse all items from this section
     * @param kernelData The complete kernel binary data
     * @return List of all parsed items from this section
     * @throws BinaryParseException if parsing fails
     */
    List<T> parseAllItems(byte[] kernelData) throws BinaryParseException;
    
    /**
     * Serialize all items and update the kernel data
     * @param items List of items to serialize
     * @param originalKernelData The original kernel data to update
     * @return Updated kernel data with serialized items
     * @throws BinaryParseException if serialization fails
     */
    byte[] serializeAllItems(List<T> items, byte[] originalKernelData) throws BinaryParseException;
    
    /**
     * Find the offset where this section starts in the kernel data
     * @param kernelData The kernel binary data
     * @return The offset where the section starts
     * @throws BinaryParseException if the section cannot be found
     */
    int findSectionOffset(byte[] kernelData) throws BinaryParseException;
    
    /**
     * Get the size of each item structure in bytes
     */
    int getItemStructSize();
    
    /**
     * Get the expected number of items in this section
     */
    int getExpectedItemCount();
    
    /**
     * Validate the structure of this section in the kernel data
     * @param kernelData The kernel binary data
     * @return Validation result with details
     */
    ValidationResult validateSectionStructure(byte[] kernelData);
    
    /**
     * Calculate a checksum for this section
     * @param kernelData The kernel binary data
     * @return Checksum string
     */
    String calculateSectionChecksum(byte[] kernelData);
    
    /**
     * Extract display names/titles for items in this section
     * @param kernelData The kernel binary data
     * @return List of display names
     * @throws BinaryParseException if extraction fails
     */
    List<String> extractItemNames(byte[] kernelData) throws BinaryParseException;
} 