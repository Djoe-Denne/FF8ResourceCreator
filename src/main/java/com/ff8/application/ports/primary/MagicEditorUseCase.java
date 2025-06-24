package com.ff8.application.ports.primary;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.domain.exceptions.InvalidMagicDataException;

import java.util.List;
import java.util.Optional;

/**
 * Primary port for magic editing operations.
 * This is the main interface that UI controllers will use.
 */
public interface MagicEditorUseCase {

    /**
     * Get magic data by index for display
     */
    Optional<MagicDisplayDTO> getMagicData(int magicIndex);

    /**
     * Update magic data from UI input
     */
    void updateMagicData(int magicIndex, MagicDisplayDTO data) throws InvalidMagicDataException;
    
    /**
     * Update magic data with validation and return the updated DTO for UI synchronization.
     * This ensures that UI changes are properly validated and reflected back to the UI layer.
     * 
     * @param magicIndex the index of the magic to update
     * @param updatedData the updated magic data from the UI
     * @return the updated MagicDisplayDTO with any domain-level changes applied
     * @throws InvalidMagicDataException if the update fails validation
     */
    MagicDisplayDTO updateAndGetMagicData(int magicIndex, MagicDisplayDTO updatedData) throws InvalidMagicDataException;

    /**
     * Get all magic data for display
     */
    List<MagicDisplayDTO> getAllMagic();

    /**
     * Validate magic data without saving
     */
    ValidationResult validateMagicData(MagicDisplayDTO data);

    /**
     * Search magic by name or properties
     */
    List<MagicDisplayDTO> searchMagic(String query);

    /**
     * Get magic count
     */
    int getMagicCount();

    /**
     * Check if any magic data is modified
     */
    boolean hasUnsavedChanges();

    /**
     * Reset a magic spell to its original state
     */
    void resetMagicToOriginal(int magicIndex);

    /**
     * Create a new magic spell (if supported)
     */
    MagicDisplayDTO createNewMagic(String spellName) throws InvalidMagicDataException;

    /**
     * Duplicate an existing magic spell
     */
    MagicDisplayDTO duplicateMagic(int sourceMagicIndex, String newName) throws InvalidMagicDataException;

    /**
     * Get list of all magic IDs and names for UI dropdowns
     */
    List<String> getMagicIndexList();

    /**
     * Record for validation results
     */
    record ValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {}
} 