package com.ff8.application.services;

import com.ff8.application.dto.*;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.*;
import com.ff8.domain.exceptions.InvalidMagicDataException;
import com.ff8.domain.services.MagicValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MagicEditorService implements MagicEditorUseCase {
    private static final Logger logger = LoggerFactory.getLogger(MagicEditorService.class);
    
    private final MagicRepository magicRepository;
    private final MagicValidationService validationService;
    private final FileSystemPort fileSystemPort;

    public MagicEditorService(MagicRepository magicRepository, MagicValidationService validationService, FileSystemPort fileSystemPort) {
        this.magicRepository = magicRepository;
        this.validationService = validationService;
        this.fileSystemPort = fileSystemPort;
    }

    @Override
    public Optional<MagicDisplayDTO> getMagicData(int magicId) {
        return magicRepository.findById(magicId)
            .map(this::mapToDisplayDTO);
    }

    @Override
    public void updateMagicData(int magicId, MagicDisplayDTO data) {
        MagicData originalMagic = magicRepository.findById(magicId)
            .orElseThrow(() -> new IllegalArgumentException("Magic with ID " + magicId + " not found"));
        
        // Validate the data before updating
        ValidationResult validation = validateMagicData(data);
        if (!validation.isValid()) {
            throw new InvalidMagicDataException("Validation failed: " + String.join(", ", validation.errors()));
        }
        
        // Create updated magic data using builder pattern
        MagicData updatedMagic = updateMagicFromDTO(originalMagic, data);
        
        // Save the updated magic
        magicRepository.save(updatedMagic);
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
        
        List<MagicDisplayDTO> result = allMagicData.stream()
            .map(this::mapToDisplayDTO)
            .collect(Collectors.toList());
        
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
        return mapToDisplayDTO(newMagic);
    }

    @Override
    public MagicDisplayDTO duplicateMagic(int sourceId, String newName) {
        MagicData source = magicRepository.findById(sourceId)
            .orElseThrow(() -> new IllegalArgumentException("Source magic with ID " + sourceId + " not found"));
        
        // Create a new magic data with a new ID and index using Lombok builder pattern
        int newId = magicRepository.getNextAvailableId();
        int newIndex = magicRepository.getNextAvailableIndex();
        // Create new instance with toBuilder from existing source
        MagicData newMagic = source.toBuilder()
            .index(newIndex)
            .magicID(newId)
            .extractedSpellName(newName)
            .build();
        
        magicRepository.save(newMagic);
        return mapToDisplayDTO(newMagic);
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

    private MagicDisplayDTO mapToDisplayDTO(MagicData magic) {
        return new MagicDisplayDTO(
            magic.getIndex(),
            magic.getMagicID(),
            magic.getExtractedSpellName(),
            magic.getExtractedSpellDescription(),
            magic.getSpellPower(),
            magic.getElement(),
            magic.getAttackType(),
            magic.getDrawResist(),
            magic.getHitCount(),
            magic.getStatusAttackEnabler(),
            // TargetInfo
            new MagicDisplayDTO.TargetInfo(
                magic.getTargetInfo().isDead(),
                magic.getTargetInfo().isSingle(),
                magic.getTargetInfo().isEnemy(),
                magic.getTargetInfo().isSingleSide(),
                magic.getTargetInfo().getActiveTargets()
            ),
            // AttackInfo
            new MagicDisplayDTO.AttackInfo(
                magic.getAttackFlags().isShelled(),
                magic.getAttackFlags().isReflected(),
                magic.getAttackFlags().isBreakDamageLimit(),
                magic.getAttackFlags().isRevive(),
                magic.getAttackFlags().getActiveFlags()
            ),
            // Active status effects
            magic.getStatusEffects().getActiveStatuses(),
            // Junction DTOs
            mapJunctionStats(magic.getJunctionStats()),
            mapJunctionElemental(magic.getJunctionElemental()),
            mapJunctionStatus(magic.getJunctionStatus()),
            mapGFCompatibility(magic.getGfCompatibility()),
            // Metadata
            magic.hasStatusEffects(),
            magic.hasJunctionBonuses(),
            magic.isCurative(),
            false // not modified initially
        );
    }

    private JunctionStatsDTO mapJunctionStats(JunctionStats stats) {
        if (stats == null) return null;
        return new JunctionStatsDTO(
            stats.getHp(),
            stats.getStr(),
            stats.getVit(),
            stats.getMag(),
            stats.getSpr(),
            stats.getSpd(),
            stats.getEva(),
            stats.getHit(),
            stats.getLuck()
        );
    }

    private JunctionElementalDTO mapJunctionElemental(JunctionElemental elemental) {
        if (elemental == null) return null;
        return new JunctionElementalDTO(
            elemental.getAttackElement(),
            elemental.getAttackValue(),
            elemental.getDefenseElements(),
            elemental.getDefenseValue()
        );
    }

    private JunctionStatusDTO mapJunctionStatus(JunctionStatusEffects status) {
        if (status == null) return null;
        return new JunctionStatusDTO(
            status.getAttackStatuses().getActiveStatuses(),
            status.getAttackValue(),
            status.getDefenseStatuses().getActiveStatuses(),
            status.getDefenseValue()
        );
    }

    private GFCompatibilityDTO mapGFCompatibility(GFCompatibilitySet compatibility) {
        if (compatibility == null) return null;
        return new GFCompatibilityDTO(compatibility.getAllCompatibilities());
    }

    private MagicData updateMagicFromDTO(MagicData original, MagicDisplayDTO dto) {
        // Use Lombok builder pattern to create immutable updates
        // Create new instance with toBuilder from original
        return original.toBuilder()
            .spellPower(dto.spellPower())
            .hitCount(dto.hitCount())
            .drawResist(dto.drawResist())
            .element(dto.element())
            .attackType(dto.attackType())
            .junctionStats(dto.junctionStats() != null ? 
                new JunctionStats(
                    dto.junctionStats().hp(),
                    dto.junctionStats().str(),
                    dto.junctionStats().vit(),
                    dto.junctionStats().mag(),
                    dto.junctionStats().spr(),
                    dto.junctionStats().spd(),
                    dto.junctionStats().eva(),
                    dto.junctionStats().hit(),
                    dto.junctionStats().luck()
                ) : original.getJunctionStats()
            )
            .build();
    }

    @Override
    public List<MagicDisplayDTO> searchMagic(String query) {
        return magicRepository.findBySpellNameContaining(query).stream()
            .map(this::mapToDisplayDTO)
            .collect(Collectors.toList());
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
    public void resetMagicToOriginal(int magicId) {
        // For simplified repository, we'll just delete and reload from kernel
        magicRepository.deleteById(magicId);
    }

    @Override
    public List<String> getMagicIdList() {
        // Get magic data from repository in kernel order (sorted by ID)
        List<MagicData> allMagic = magicRepository.findAll();
        
        if (allMagic.isEmpty()) {
            throw new IllegalStateException("No magic data loaded in repository");
        }
        
        // Sort by magic ID to ensure kernel order and format as "ID - Name"
        return allMagic.stream()
            .sorted((m1, m2) -> Integer.compare(m1.getMagicID(), m2.getMagicID()))
            .map(magic -> String.format("%03d - %s", magic.getMagicID(), magic.getExtractedSpellName()))
            .toList();
    }
} 