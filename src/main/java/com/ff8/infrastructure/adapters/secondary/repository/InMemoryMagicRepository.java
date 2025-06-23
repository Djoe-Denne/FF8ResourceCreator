package com.ff8.infrastructure.adapters.secondary.repository;

import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.domain.entities.MagicData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMagicRepository implements MagicRepository {
    private final Map<Integer, MagicData> magicStore = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMagicRepository.class);

    @Override
    public Optional<MagicData> findByIndex(int index) {
        return Optional.ofNullable(magicStore.get(index));
    }

    @Override
    @Deprecated
    public Optional<MagicData> findById(int magicId) {
        // Find first magic with matching ID (may not be unique)
        return magicStore.values().stream()
            .filter(magic -> magic.getMagicID() == magicId)
            .findFirst();
    }

    @Override
    public List<MagicData> findAll() {
        return new ArrayList<>(magicStore.values());
    }

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

    @Override
    public void deleteByIndex(int index) {
        magicStore.remove(index);
    }

    @Override
    @Deprecated
    public void deleteById(int magicId) {
        // Find and delete first magic with matching ID
        magicStore.values().stream()
            .filter(magic -> magic.getMagicID() == magicId)
            .findFirst()
            .ifPresent(magic -> magicStore.remove(magic.getIndex()));
    }

    @Override
    public void clear() {
        magicStore.clear();
    }

    @Override
    public int count() {
        return magicStore.size();
    }

    @Override
    public boolean existsByIndex(int index) {
        return magicStore.containsKey(index);
    }

    @Override
    @Deprecated
    public boolean existsById(int magicId) {
        return magicStore.values().stream()
            .anyMatch(magic -> magic.getMagicID() == magicId);
    }

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

    @Override
    public int getNextAvailableId() {
        return magicStore.values().stream()
            .mapToInt(MagicData::getMagicID)
            .max()
            .orElse(-1) + 1;
    }

    @Override
    public int getNextAvailableIndex() {
        return magicStore.keySet().stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(-1) + 1;
    }

    @Override
    public void resetToOriginalByIndex(int index) {
        // For simplified repository, just delete the magic
        // In a real implementation, this would restore from backup
        deleteByIndex(index);
    }

    @Override
    @Deprecated
    public void resetToOriginal(int magicId) {
        // For simplified repository, just delete the magic
        // In a real implementation, this would restore from backup
        deleteById(magicId);
    }

    @Override
    public Optional<MagicData> getOriginalByIndex(int index) {
        // For simplified repository, return current data (no original backup stored)
        return findByIndex(index);
    }

    @Override
    @Deprecated
    public Optional<MagicData> getOriginalById(int magicId) {
        // For simplified repository, return current data (no original backup stored)
        return findById(magicId);
    }

    @Override
    public void markAsClean() {
        // For simplified repository, this is a no-op
        // In a real implementation, this would clear dirty flags
    }

    @Override
    public boolean isModified() {
        // For simplified repository, always return false
        // In a real implementation, this would check dirty flags
        return false;
    }
} 