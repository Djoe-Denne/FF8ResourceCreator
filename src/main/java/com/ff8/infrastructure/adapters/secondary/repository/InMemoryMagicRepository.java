package com.ff8.infrastructure.adapters.secondary.repository;

import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.domain.entities.MagicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the MagicRepository interface.
 * 
 * <p>This class provides temporary storage for magic data during the editing session,
 * implementing the Repository pattern as a Secondary Adapter in the hexagonal architecture.
 * It uses a concurrent hash map to ensure thread-safe operations while providing
 * fast in-memory access to magic data.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Thread-safe concurrent access using ConcurrentHashMap</li>
 *   <li>Magic data indexed by kernel index for efficient retrieval</li>
 *   <li>Comprehensive logging for debugging and monitoring</li>
 *   <li>Support for both kernel data and newly created magic differentiation</li>
 *   <li>Search functionality by spell name with case-insensitive matching</li>
 * </ul>
 * 
 * <p>The repository maintains magic data in memory during the editing session and
 * provides change tracking capabilities. All modifications are kept in memory
 * until explicitly saved to the kernel file.</p>
 * 
 * <p>Note: This implementation uses the kernel index as the primary key for
 * magic data, which ensures unique identification and proper ordering within
 * the FF8 kernel.bin file structure.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class InMemoryMagicRepository implements MagicRepository {
    private final Map<Integer, MagicData> magicStore = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMagicRepository.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MagicData> findByIndex(int index) {
        return Optional.ofNullable(magicStore.get(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MagicData> findAll() {
        return new ArrayList<>(magicStore.values());
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Stores the magic data using the kernel index as the key, ensuring
     * unique identification and proper ordering. Logs the operation for
     * debugging purposes.</p>
     */
    @Override
    public void save(MagicData magic) {
        if (magic == null) {
            throw new IllegalArgumentException("Magic data cannot be null");
        }
        logger.info("Saving magic to repository: index=" + magic.getIndex() + 
                   ", magicID=" + magic.getMagicID() + ", name='" + magic.getExtractedSpellName() + "'");
        magicStore.put(magic.getIndex(), magic);  // Use index as key
        logger.info("Repository now contains " + magicStore.size() + " entries");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Saves all magic data entries in the provided list using individual
     * save operations. This ensures proper validation and logging for each entry.</p>
     */
    @Override
    public void saveAll(List<MagicData> magicList) {
        if (magicList == null) {
            throw new IllegalArgumentException("Magic list cannot be null");
        }
        logger.info("Saving " + magicList.size() + " magic entries to repository");
        for (MagicData magic : magicList) {
            save(magic);
        }
        logger.info("Finished saving all entries. Total in repository: " + magicStore.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByIndex(int index) {
        magicStore.remove(index);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated This method is deprecated as it uses magicID instead of the
     *             unique kernel index. Use {@link #deleteByIndex(int)} instead.
     */
    @Override
    @Deprecated
    public void deleteById(int magicId) {
        // Find and delete first magic with matching ID
        magicStore.values().stream()
            .filter(magic -> magic.getMagicID() == magicId)
            .findFirst()
            .ifPresent(magic -> magicStore.remove(magic.getIndex()));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Removes all magic data from the repository. This operation is
     * irreversible and will clear all loaded magic data.</p>
     */
    @Override
    public void clear() {
        magicStore.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return magicStore.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByIndex(int index) {
        return magicStore.containsKey(index);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated This method is deprecated as it uses magicID instead of the
     *             unique kernel index. Use {@link #existsByIndex(int)} instead.
     */
    @Override
    @Deprecated
    public boolean existsById(int magicId) {
        return magicStore.values().stream()
            .anyMatch(magic -> magic.getMagicID() == magicId);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Performs case-insensitive search on spell names. Returns all magic
     * data if the search fragment is null or empty. Results are sorted by
     * kernel index to maintain consistent ordering.</p>
     */
    @Override
    public List<MagicData> findBySpellNameContaining(String nameFragment) {
        if (nameFragment == null || nameFragment.trim().isEmpty()) {
            return findAll();
        }
        
        String lowercaseFragment = nameFragment.toLowerCase();
        return magicStore.values().stream()
            .filter(magic -> magic.getExtractedSpellName().toLowerCase().contains(lowercaseFragment))
            .sorted(Comparator.comparingInt(MagicData::getIndex))
            .toList();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Calculates the next available magic ID by finding the highest
     * existing ID and incrementing it by 1. Returns 0 if no magic data exists.</p>
     */
    @Override
    public int getNextAvailableId() {
        return magicStore.values().stream()
            .mapToInt(MagicData::getMagicID)
            .max()
            .orElse(-1) + 1;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Calculates the next available kernel index by finding the highest
     * existing index and incrementing it by 1. Returns 0 if no magic data exists.</p>
     */
    @Override
    public int getNextAvailableIndex() {
        return magicStore.keySet().stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(-1) + 1;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Simplified implementation that removes the magic data from the repository.
     * In a full implementation, this would restore the original data from a backup.</p>
     */
    @Override
    public void resetToOriginalByIndex(int index) {
        // For simplified repository, just delete the magic
        // In a real implementation, this would restore from backup
        deleteByIndex(index);
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated This method is deprecated as it uses magicID instead of the
     *             unique kernel index. Use {@link #resetToOriginalByIndex(int)} instead.
     */
    @Override
    @Deprecated
    public void resetToOriginal(int magicId) {
        // For simplified repository, just delete the magic
        // In a real implementation, this would restore from backup
        deleteById(magicId);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Simplified implementation that returns the current data. In a full
     * implementation, this would return the original unmodified data from a backup.</p>
     */
    @Override
    public Optional<MagicData> getOriginalByIndex(int index) {
        // For simplified repository, return current data (no original backup stored)
        return findByIndex(index);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Simplified implementation that performs no operation. In a full
     * implementation, this would clear dirty flags and change tracking.</p>
     */
    @Override
    public void markAsClean() {
        // For simplified repository, this is a no-op
        // In a real implementation, this would clear dirty flags
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Simplified implementation that always returns false. In a full
     * implementation, this would check dirty flags and change tracking.</p>
     */
    @Override
    public boolean isModified() {
        // For simplified repository, always return false
        // In a real implementation, this would check dirty flags
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Removes all magic data that originated from the kernel file,
     * keeping only newly created magic spells. This is useful when
     * preparing for export of only newly created magic.</p>
     */
    @Override
    public void removeKernelData() {
        logger.info("Removing kernel data (isNewlyCreated=false) from repository");
        int originalSize = magicStore.size();
        
        // Remove all entries where isNewlyCreated is false
        magicStore.entrySet().removeIf(entry -> !entry.getValue().isNewlyCreated());
        
        int newSize = magicStore.size();
        logger.info("Removed {} kernel data entries. Repository size: {} -> {}", 
                   (originalSize - newSize), originalSize, newSize);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns magic data that was created within the application,
     * sorted by kernel index for consistent ordering.</p>
     */
    @Override
    public List<MagicData> findNewlyCreated() {
        return magicStore.values().stream()
            .filter(MagicData::isNewlyCreated)
            .sorted(Comparator.comparingInt(MagicData::getIndex))
            .toList();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns magic data that was loaded from the kernel file,
     * sorted by kernel index for consistent ordering.</p>
     */
    @Override
    public List<MagicData> findKernelData() {
        return magicStore.values().stream()
            .filter(magic -> !magic.isNewlyCreated())
            .sorted(Comparator.comparingInt(MagicData::getIndex))
            .toList();
    }
} 