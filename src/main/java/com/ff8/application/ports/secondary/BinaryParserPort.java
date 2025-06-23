package com.ff8.application.ports.secondary;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.exceptions.BinaryParseException;

import java.util.List;

/**
 * Secondary port for binary parsing and serialization operations.
 * This interface will be implemented by infrastructure adapters.
 */
public interface BinaryParserPort {

    /**
     * Parse a single magic data entry from binary data at specified offset
     */
    MagicData parseMagicData(byte[] binaryData, int offset) throws BinaryParseException;

    /**
     * Parse all magic data from kernel.bin binary data
     */
    List<MagicData> parseAllMagicData(byte[] kernelData) throws BinaryParseException;

    /**
     * Serialize a single magic data entry to binary format
     */
    byte[] serializeMagicData(MagicData magicData) throws BinaryParseException;

    /**
     * Serialize all magic data and update kernel.bin data
     */
    byte[] serializeAllMagicData(List<MagicData> magicDataList, byte[] originalKernelData) throws BinaryParseException;

    /**
     * Find the magic section offset in kernel.bin
     */
    int findMagicSectionOffset(byte[] kernelData) throws BinaryParseException;

    /**
     * Get the size of a single magic data structure in bytes
     */
    int getMagicStructSize();

    /**
     * Get the expected magic count in kernel.bin
     */
    int getExpectedMagicCount();

    /**
     * Validate kernel.bin file structure
     */
    ValidationResult validateKernelStructure(byte[] kernelData);

    /**
     * Extract spell names from kernel.bin text section
     */
    List<String> extractSpellNames(byte[] kernelData) throws BinaryParseException;

    /**
     * Calculate checksum for magic section
     */
    String calculateMagicSectionChecksum(byte[] kernelData);

    /**
     * Check if binary data represents a valid kernel.bin file
     */
    boolean isValidKernelFile(byte[] data);

    /**
     * Record for validation results
     */
    record ValidationResult(
            boolean isValid,
            String message,
            List<String> issues,
            int magicSectionOffset,
            int magicCount
    ) {}
} 