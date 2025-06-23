package com.ff8.domain.entities;

import java.util.BitSet;

/**
 * Utility class for common binary serialization operations.
 * Provides reusable methods for converting between Java objects and binary data.
 */
public final class BinarySerializationUtils {
    
    private BinarySerializationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validate that a value is within byte range (0-255)
     */
    public static void validateByteValue(int value, String fieldName) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 255, got " + value);
        }
    }

    /**
     * Convert unsigned byte to int
     */
    public static int toUnsignedInt(byte value) {
        return Byte.toUnsignedInt(value);
    }

    /**
     * Convert BitSet to byte for 8-bit flags
     */
    public static int bitSetToByte(BitSet bitSet, int totalBits) {
        if (totalBits > 8) {
            throw new IllegalArgumentException("Total bits must be <= 8 for byte conversion");
        }
        
        int result = 0;
        for (int i = 0; i < totalBits; i++) {
            if (bitSet.get(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }

    /**
     * Convert byte to BitSet for 8-bit flags
     */
    public static BitSet byteTobitSet(int flagByte, int totalBits) {
        if (totalBits > 8) {
            throw new IllegalArgumentException("Total bits must be <= 8 for byte conversion");
        }
        
        BitSet bitSet = new BitSet(totalBits);
        for (int i = 0; i < totalBits; i++) {
            if ((flagByte & (1 << i)) != 0) {
                bitSet.set(i, true);
            }
        }
        return bitSet;
    }

    /**
     * Convert BitSet to 32-bit integer (DWORD)
     */
    public static int bitSetToDword(BitSet bitSet) {
        int result = 0;
        for (int i = 0; i < 32; i++) {
            if (bitSet.get(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }

    /**
     * Convert BitSet to 16-bit integer (WORD)
     */
    public static int bitSetToWord(BitSet bitSet, int startBit) {
        int result = 0;
        for (int i = 0; i < 16; i++) {
            if (bitSet.get(startBit + i)) {
                result |= (1 << i);
            }
        }
        return result;
    }

    /**
     * Set bits in BitSet from 32-bit DWORD
     */
    public static void dwordToBitSet(int dword, BitSet bitSet, int startBit) {
        for (int i = 0; i < 32; i++) {
            if ((dword & (1 << i)) != 0) {
                bitSet.set(startBit + i, true);
            }
        }
    }

    /**
     * Set bits in BitSet from 16-bit WORD
     */
    public static void wordToBitSet(int word, BitSet bitSet, int startBit) {
        for (int i = 0; i < 16; i++) {
            if ((word & (1 << i)) != 0) {
                bitSet.set(startBit + i, true);
            }
        }
    }

    /**
     * Read little-endian short from byte array
     */
    public static short readShortLE(byte[] bytes, int offset) {
        if (bytes.length < offset + 2) {
            throw new IllegalArgumentException("Not enough bytes to read short at offset " + offset);
        }
        return (short) (toUnsignedInt(bytes[offset]) | 
                       (toUnsignedInt(bytes[offset + 1]) << 8));
    }

    /**
     * Write little-endian short to byte array
     */
    public static void writeShortLE(byte[] bytes, int offset, int value) {
        if (bytes.length < offset + 2) {
            throw new IllegalArgumentException("Not enough bytes to write short at offset " + offset);
        }
        bytes[offset] = (byte) (value & 0xFF);
        bytes[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    /**
     * Check if enough bytes are available for reading
     */
    public static void validateBytesAvailable(byte[] bytes, int offset, int required, String operation) {
        if (bytes.length < offset + required) {
            throw new IllegalArgumentException(
                String.format("Not enough bytes for %s: need %d bytes at offset %d, but only %d available", 
                             operation, required, offset, bytes.length - offset));
        }
    }
} 