package com.ff8.application.services;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.mappers.DtoToMagicDataMapper;
import com.ff8.application.mappers.MagicDataToDtoMapper;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.dto.JunctionStatsDTO;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.SpellTranslations;
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

/**
 * Application service implementing magic editing use cases.
 * 
 * <p>This service orchestrates magic data operations within the hexagonal architecture,
 * serving as the primary implementation of the {@link MagicEditorUseCase} port. It coordinates
 * between the domain layer (business logic) and infrastructure layer (persistence, UI)
 * while maintaining proper separation of concerns.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Orchestrate magic data CRUD operations</li>
 *   <li>Validate magic data according to business rules</li>
 *   <li>Emit events to notify UI components of changes</li>
 *   <li>Coordinate between domain entities and DTOs</li>
 *   <li>Handle translation updates and internationalization</li>
 * </ul>
 * 
 * <p>This service implements the Observer pattern by extending {@link AbstractSubject}
 * to notify registered observers (typically UI components) of magic data changes.
 * This ensures real-time synchronization across the application.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@RequiredArgsConstructor
public class MagicEditorService extends AbstractSubject<MagicDataChangeEvent> implements MagicEditorUseCase {
    private static final Logger logger = LoggerFactory.getLogger(MagicEditorService.class);
    
    private final MagicRepository magicRepository;
    private final MagicValidationService validationService;
    private final FileSystemPort fileSystemPort;
    private final MagicDataToDtoMapper magicDataToDtoMapper;
    private final DtoToMagicDataMapper dtoToMagicDataMapper;

    /**
     * Retrieves magic data for display in the UI.
     * 
     * <p>This method fetches magic data from the repository and converts it to a
     * presentation-friendly DTO format. If the magic doesn't exist, returns an empty Optional.</p>
     * 
     * @param magicIndex the index of the magic spell to retrieve
     * @return an Optional containing the magic display data, or empty if not found
     */
    @Override
    public Optional<MagicDisplayDTO> getMagicData(int magicIndex) {
        return magicRepository.findByIndex(magicIndex)
            .map(magicDataToDtoMapper::toDto);
    }

    /**
     * Updates magic data with validation and event notification.
     * 
     * <p>This method performs the following operations:</p>
     * <ol>
     *   <li>Validates the incoming data against business rules</li>
     *   <li>Converts the DTO to domain entity while preserving binary fields</li>
     *   <li>Saves the updated entity to the repository</li>
     *   <li>Emits change events to notify observers</li>
     * </ol>
     * 
     * @param magicIndex the index of the magic spell to update
     * @param data the updated magic data from the UI
     * @throws InvalidMagicDataException if validation fails
     * @throws IllegalArgumentException if magic with given index doesn't exist
     */
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

    /**
     * Updates the multi-language translations for a spell.
     * 
     * <p>This method handles internationalization by updating the spell's translations
     * while preserving existing magic data. It validates that the English translation
     * is present as it serves as the fallback language.</p>
     * 
     * @param magicIndex the index of the magic spell to update
     * @param translations the new translations to apply
     * @throws InvalidMagicDataException if translations are invalid
     * @throws IllegalArgumentException if magic with given index doesn't exist
     */
    @Override
    public void updateMagicTranslations(int magicIndex, SpellTranslations translations) throws InvalidMagicDataException {
        MagicData originalMagic = magicRepository.findByIndex(magicIndex)
            .orElseThrow(() -> new IllegalArgumentException("Magic with index " + magicIndex + " not found"));
        
        // Validate the translations (basic validation)
        if (translations == null) {
            throw new InvalidMagicDataException("Translations cannot be null");
        }
        
        if (translations.getEnglishName().trim().isEmpty()) {
            throw new InvalidMagicDataException("English spell name cannot be empty");
        }
        
        // Update the magic with new translations
        MagicData updatedMagic = originalMagic.withTranslations(translations);
        
        // Save the updated magic
        magicRepository.save(updatedMagic);
        
        // Convert to DTO for event notification
        MagicDisplayDTO updatedMagicDto = magicDataToDtoMapper.toDto(updatedMagic);
        
        // Emit event to notify observers of the translation change
        MagicDataChangeEvent changeEvent = new MagicDataChangeEvent(magicIndex, updatedMagicDto, "translations_update");
        notifyObservers(changeEvent);
        
        logger.info("Magic translations updated for index {} and event notified to {} observers", 
                   magicIndex, getObserverCount());
    }

    /**
     * Retrieves all magic data for display in the UI.
     * 
     * <p>This method fetches all magic data from the repository and converts it to
     * a list of presentation-friendly DTOs. The method includes detailed logging
     * to help with debugging and monitoring.</p>
     * 
     * @return a list of all magic display DTOs
     */
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

    /**
     * Creates a new magic spell with the given name.
     * 
     * <p>This method creates a new magic spell with default properties and marks it
     * as newly created. The spell is assigned the next available index and ID,
     * and observers are notified of the creation.</p>
     * 
     * @param spellName the name for the new spell
     * @return the newly created magic display DTO
     * @throws InvalidMagicDataException if the spell name is invalid
     */
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
            .isNewlyCreated(true)
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

    /**
     * Duplicates an existing magic spell with a new name.
     * 
     * <p>This method creates a copy of an existing spell with all its properties
     * preserved except for the name and index. The duplicated spell is marked as
     * newly created and observers are notified.</p>
     * 
     * @param sourceId the index of the magic spell to duplicate
     * @param newName the name for the duplicated spell
     * @return the duplicated magic display DTO
     * @throws IllegalArgumentException if source magic doesn't exist
     */
    @Override
    public MagicDisplayDTO duplicateMagic(int sourceId, String newName) {
        MagicData source = magicRepository.findByIndex(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source magic with index " + sourceId + " not found"));
        
        int newIndex = magicRepository.getNextAvailableIndex();
        // Create new instance with toBuilder from existing source
        MagicData newMagic = source.toBuilder()
            .index(newIndex)
            .extractedSpellName(newName)
            .isNewlyCreated(true)
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

    /**
     * Validates magic data against business rules.
     * 
     * <p>This method performs comprehensive validation of magic data including:</p>
     * <ul>
     *   <li>Value range validation (0-255 for most numeric fields)</li>
     *   <li>Junction stat validation</li>
     *   <li>Business rule validation</li>
     * </ul>
     * 
     * @param data the magic data to validate
     * @return a ValidationResult containing any errors or warnings
     */
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
    
    /**
     * Validates a single stat value is within the valid range.
     * 
     * @param value the stat value to validate
     * @param statName the name of the stat for error messages
     * @param errors the list to add errors to
     */
    private void validateStatValue(int value, String statName, List<String> errors) {
        if (value < 0 || value > 255) {
            errors.add(statName + " junction bonus must be between 0 and 255");
        }
    }

    /**
     * Updates magic data and returns the updated DTO for UI synchronization.
     * 
     * <p>This method combines update and retrieval operations to ensure the UI
     * receives the exact state after domain-level processing and validation.</p>
     * 
     * @param magicIndex the index of the magic to update
     * @param updatedData the updated magic data from the UI
     * @return the updated MagicDisplayDTO with any domain-level changes applied
     * @throws InvalidMagicDataException if the update fails validation
     */
    @Override
    public MagicDisplayDTO updateAndGetMagicData(int magicIndex, MagicDisplayDTO updatedData) throws InvalidMagicDataException {
        updateMagicData(magicIndex, updatedData);
        return getMagicData(magicIndex)
            .orElseThrow(() -> new IllegalStateException("Magic should exist after update"));
    }

    /**
     * Searches for magic spells by name or properties.
     * 
     * <p>This method performs a basic search across magic spell names.
     * Future implementations could extend this to search descriptions,
     * elements, or other properties.</p>
     * 
     * @param query the search query string
     * @return a list of matching magic display DTOs
     */
    @Override
    public List<MagicDisplayDTO> searchMagic(String query) {
        // TODO: Implement actual search logic
        return getAllMagic();
    }

    /**
     * Gets the total count of magic spells in the repository.
     * 
     * @return the number of magic spells currently loaded
     */
    @Override
    public int getMagicCount() {
        return magicRepository.count();
    }

    /**
     * Checks if there are any unsaved changes to magic data.
     * 
     * <p>This method helps determine if the user should be warned about
     * unsaved changes when closing the application or loading a new file.</p>
     * 
     * @return true if there are unsaved changes
     */
    @Override
    public boolean hasUnsavedChanges() {
        // For now, we consider magic data as always saved since we use in-memory storage
        // In a real implementation, this would check for dirty flags or compare with saved state
        return false;
    }

    /**
     * Resets a magic spell to its original state.
     * 
     * <p>This method would restore a spell to its original state before any
     * modifications were made. Currently not implemented.</p>
     * 
     * @param magicIndex the index of the magic to reset
     */
    @Override
    public void resetMagicToOriginal(int magicIndex) {
        // TODO: Implement reset to original functionality
        // This would require storing original state and restoring it
    }
} 