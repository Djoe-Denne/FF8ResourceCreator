package com.ff8.domain.entities;

/**
 * Interface for entities that can be serialized to/from binary data.
 * Provides a consistent contract for binary serialization operations.
 */
public interface BinarySerializable {
    
    /**
     * Convert this entity to binary data
     * @return byte array representing this entity
     */
    byte[] toBytes();
    
    /**
     * Get the expected size in bytes for this entity type
     * @return number of bytes this entity occupies in binary form
     */
    int getBinarySize();
    
    /**
     * Check if this entity has any meaningful data
     * (useful for empty/default instances)
     * @return true if entity contains non-default data
     */
    default boolean hasData() {
        return true;
    }
} 