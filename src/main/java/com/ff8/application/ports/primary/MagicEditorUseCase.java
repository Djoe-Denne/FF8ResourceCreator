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
     * Get magic data by ID for display
     */
    Optional<MagicDisplayDTO> getMagicData(int magicId);

    /**
     * Update magic data from UI input
     */
    void updateMagicData(int magicId, MagicDisplayDTO data) throws InvalidMagicDataException;

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
    void resetMagicToOriginal(int magicId);

    /**
     * Create a new magic spell (if supported)
     */
    MagicDisplayDTO createNewMagic(String spellName) throws InvalidMagicDataException;

    /**
     * Duplicate an existing magic spell
     */
    MagicDisplayDTO duplicateMagic(int sourceMagicId, String newName) throws InvalidMagicDataException;

    /**
     * Get list of all magic IDs and names for UI dropdowns
     */
    List<String> getMagicIdList();

    /**
     * Record for validation results
     */
    record ValidationResult(
            boolean isValid,
            List<String> errors,
            List<String> warnings
    ) {}
} 