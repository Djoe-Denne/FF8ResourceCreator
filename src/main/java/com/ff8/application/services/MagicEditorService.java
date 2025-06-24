package com.ff8.application.services;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.mappers.DtoToMagicDataMapper;
import com.ff8.application.mappers.MagicDataToDtoMapper;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.dto.JunctionStatsDTO;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.events.MagicDataChangeEvent;
import com.ff8.domain.exceptions.InvalidMagicDataException;
import com.ff8.domain.observers.AbstractSubject;
import com.ff8.domain.services.MagicValidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MagicEditorService extends AbstractSubject<MagicDataChangeEvent> implements MagicEditorUseCase {
    private static final Logger logger = LoggerFactory.getLogger(MagicEditorService.class);
    
    private final MagicRepository magicRepository;
    private final MagicValidationService validationService;
    private final FileSystemPort fileSystemPort;
    private final MagicDataToDtoMapper magicDataToDtoMapper;
    private final DtoToMagicDataMapper dtoToMagicDataMapper;

    @Override
    public Optional<MagicDisplayDTO> getMagicData(int magicIndex) {
        return magicRepository.findByIndex(magicIndex)
            .map(magicDataToDtoMapper::toDto);
    }

    @Override
    public void updateMagicData(int magicIndex, MagicDisplayDTO data) {
        MagicData originalMagic = magicRepository.findByIndex(data.index())
            .orElseThrow(() -> new IllegalArgumentException("Magic with index " + data.index() + " not found"));
        
        // Validate the data before updating
        ValidationResult validation = validateMagicData(data);
        if (!validation.isValid()) {
            throw new InvalidMagicDataException("Validation failed: " + String.join(", ", validation.errors()));
        }
        
        // Convert DTO back to domain using mapper (preserving binary fields)
        MagicData updatedMagic = dtoToMagicDataMapper.toDomain(data, originalMagic);
        
        // Save the updated magic
        magicRepository.save(updatedMagic);
        
        // Emit event to notify observers of the change
        MagicDataChangeEvent changeEvent = new MagicDataChangeEvent(magicIndex, data, "update");
        notifyObservers(changeEvent);
        
        logger.info("Magic data updated for index {} and event notified to {} observers", 
                   magicIndex, getObserverCount());
    }

    @Override
    public List<MagicDisplayDTO> getAllMagic() {
        List<MagicData> allMagicData = magicRepository.findAll();
        logger.info("MagicEditorService.getAllMagic(): Repository contains {} magic entries", allMagicData.size());
        
        for (int i = 0; i < Math.min(3, allMagicData.size()); i++) {
            MagicData magic = allMagicData.get(i);
            logger.info("Magic {}: index={}, magicID={}, name='{}'", 
                       i, magic.getIndex(), magic.getMagicID(), magic.getExtractedSpellName());
        }
        
        List<MagicDisplayDTO> result = magicDataToDtoMapper.toDtoList(allMagicData);
        
        logger.info("MagicEditorService.getAllMagic(): Returning {} display DTOs", result.size());
        return result;
    }

    @Override
    public MagicDisplayDTO createNewMagic(String spellName) throws InvalidMagicDataException {
        int newId = magicRepository.getNextAvailableId();
        int newIndex = magicRepository.getNextAvailableIndex();
            
        MagicData newMagic = MagicData.builder()
            .index(newIndex)
            .magicID(newId)
            .extractedSpellName(spellName)
            .spellPower(1)
            .element(com.ff8.domain.entities.enums.Element.NONE)
            .attackType(com.ff8.domain.entities.enums.AttackType.MAGIC_ATTACK)
            .drawResist(0)
            .hitCount(1)
            .build();
        
        magicRepository.save(newMagic);
        MagicDisplayDTO newMagicDto = magicDataToDtoMapper.toDto(newMagic);
        
        // Emit event to notify observers of the new magic creation
        MagicDataChangeEvent changeEvent = new MagicDataChangeEvent(newIndex, newMagicDto, "create");
        notifyObservers(changeEvent);
        
        logger.info("New magic created at index {} and event notified to {} observers", 
                   newIndex, getObserverCount());
        
        return newMagicDto;
    }

    @Override
    public MagicDisplayDTO duplicateMagic(int sourceId, String newName) {
        MagicData source = magicRepository.findByIndex(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source magic with index " + sourceId + " not found"));
        
        int newIndex = magicRepository.getNextAvailableIndex();
        // Create new instance with toBuilder from existing source
        MagicData newMagic = source.toBuilder()
            .index(newIndex)
            .extractedSpellName(newName)
            .build();
        
        magicRepository.save(newMagic);
        MagicDisplayDTO duplicatedMagicDto = magicDataToDtoMapper.toDto(newMagic);
        
        // Emit event to notify observers of the magic duplication
        MagicDataChangeEvent changeEvent = new MagicDataChangeEvent(newIndex, duplicatedMagicDto, "duplicate");
        notifyObservers(changeEvent);
        
        logger.info("Magic duplicated from index {} to index {} and event notified to {} observers", 
                   sourceId, newIndex, getObserverCount());
        
        return duplicatedMagicDto;
    }

    @Override
    public ValidationResult validateMagicData(MagicDisplayDTO data) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (data.spellPower() < 0 || data.spellPower() > 255) {
            errors.add("Spell power must be between 0 and 255");
        }
        
        if (data.hitCount() < 1 || data.hitCount() > 255) {
            errors.add("Hit count must be between 1 and 255");
        }
        
        if (data.drawResist() < 0 || data.drawResist() > 255) {
            errors.add("Draw resist must be between 0 and 255");
        }
        
        // Validate junction stats
        JunctionStatsDTO junctionStats = data.junctionStats();
        if (junctionStats != null) {
            validateStatValue(junctionStats.hp(), "HP", errors);
            validateStatValue(junctionStats.str(), "STR", errors);
            validateStatValue(junctionStats.vit(), "VIT", errors);
            validateStatValue(junctionStats.mag(), "MAG", errors);
            validateStatValue(junctionStats.spr(), "SPR", errors);
            validateStatValue(junctionStats.spd(), "SPD", errors);
            validateStatValue(junctionStats.eva(), "EVA", errors);
            validateStatValue(junctionStats.hit(), "HIT", errors);
            validateStatValue(junctionStats.luck(), "LUCK", errors);
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    private void validateStatValue(int value, String statName, List<String> errors) {
        if (value < 0 || value > 255) {
            errors.add(statName + " must be between 0 and 255");
        }
    }

    @Override
    public MagicDisplayDTO updateAndGetMagicData(int magicIndex, MagicDisplayDTO updatedData) throws InvalidMagicDataException {
        logger.info("MagicEditorService.updateAndGetMagicData(): Updating magic data for ID {}", magicIndex);
        // Update the magic data (this will automatically emit the event via updateMagicData)
        updateMagicData(magicIndex, updatedData);
        
        return getMagicData(magicIndex)
            .orElseThrow(() -> new IllegalStateException("Magic data was just updated but not found: " + magicIndex));
    }

    @Override
    public List<MagicDisplayDTO> searchMagic(String query) {
        return magicDataToDtoMapper.toDtoList(magicRepository.findBySpellNameContaining(query));
    }

    @Override
    public int getMagicCount() {
        return magicRepository.count();
    }

    @Override
    public boolean hasUnsavedChanges() {
        // In a real implementation, this would track modifications
        return false;
    }

    @Override
    public void resetMagicToOriginal(int magicIndex) {
        // For simplified repository, we'll just delete and reload from kernel
        magicRepository.deleteByIndex(magicIndex);
    }

    @Override
    public List<String> getMagicIndexList() {
        // Get magic data from repository in kernel order (sorted by ID)
        List<MagicData> allMagic = magicRepository.findAll();
        
        // Sort by magic ID to ensure kernel order and format as "ID - Name"
        return allMagic.stream()
            .sorted((m1, m2) -> Integer.compare(m1.getIndex(), m2.getIndex()))
            .map(magic -> String.format("%03d - %s", magic.getIndex(), magic.getExtractedSpellName()))
            .toList();
    }
} 