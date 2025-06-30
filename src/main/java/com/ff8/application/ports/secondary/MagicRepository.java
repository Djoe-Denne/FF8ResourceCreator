package com.ff8.application.ports.secondary;

import com.ff8.domain.entities.MagicData;

import java.util.List;
import java.util.Optional;

/**
 * Secondary port for magic data persistence.
 * This interface will be implemented by infrastructure adapters.
 */
public interface MagicRepository {

    /**
     * Find magic data by kernel index (unique identifier)
     */
    Optional<MagicData> findByIndex(int index);

    /**
     * Save or update magic data
     */
    void save(MagicData magicData);

    /**
     * Save all magic data
     */
    void saveAll(List<MagicData> magicDataList);

    /**
     * Get all magic data
     */
    List<MagicData> findAll();

    /**
     * Delete magic data by kernel index (unique identifier)
     */
    void deleteByIndex(int index);

    /**
     * Delete magic data by ID (may not be unique - deletes first occurrence)
     * @deprecated Use deleteByIndex instead for unique identification
     */
    @Deprecated
    void deleteById(int magicId);

    /**
     * Check if magic data exists by kernel index
     */
    boolean existsByIndex(int index);

    /**
     * Check if magic data exists by ID
     * @deprecated Use existsByIndex instead for unique identification
     */
    @Deprecated
    boolean existsById(int magicId);

    /**
     * Get count of magic data
     */
    int count();

    /**
     * Find magic data by spell name (case-insensitive)
     */
    List<MagicData> findBySpellNameContaining(String nameFragment);

    /**
     * Clear all magic data
     */
    void clear();

    /**
     * Check if repository has been modified
     */
    boolean isModified();

    /**
     * Mark repository as clean (no modifications)
     */
    void markAsClean();

    /**
     * Get original (unmodified) magic data by kernel index
     */
    Optional<MagicData> getOriginalByIndex(int index);

    /**
     * Reset magic data to original state by kernel index
     */
    void resetToOriginalByIndex(int index);

    /**
     * Reset magic data to original state by ID (may not be unique)
     * @deprecated Use resetToOriginalByIndex instead for unique identification
     */
    @Deprecated
    void resetToOriginal(int magicId);

    /**
     * Get next available magic ID
     */
    int getNextAvailableId();

    /**
     * Get next available kernel index
     */
    int getNextAvailableIndex();

    /**
     * Remove all magic data that are NOT newly created (i.e., kernel data)
     */
    void removeKernelData();

    /**
     * Find all newly created magic data
     */
    List<MagicData> findNewlyCreated();

    /**
     * Find all kernel (non-newly created) magic data
     */
    List<MagicData> findKernelData();
} 