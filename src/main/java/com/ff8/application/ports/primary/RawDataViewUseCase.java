package com.ff8.application.ports.primary;

import com.ff8.application.dto.RawViewDTO;

import java.util.Optional;

/**
 * Primary port for raw data viewing operations.
 * This interface provides access to raw binary representation of magic data.
 */
public interface RawDataViewUseCase {
    
    /**
     * Get raw binary view of magic data by ID
     * 
     * @param magicId the magic spell ID
     * @return raw view DTO containing all binary fields with offsets and hex values
     */
    Optional<RawViewDTO> getRawView(int magicId);
    
    /**
     * Check if a magic spell exists for raw viewing
     * 
     * @param magicId the magic spell ID
     * @return true if magic exists
     */
    boolean magicExists(int magicId);
} 