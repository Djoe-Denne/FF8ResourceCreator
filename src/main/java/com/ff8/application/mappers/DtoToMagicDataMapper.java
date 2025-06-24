package com.ff8.application.mappers;

import com.ff8.application.dto.*;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.StatusEffect;

import java.util.List;

/**
 * Mapper class responsible for converting MagicDisplayDTO back to MagicData domain entities.
 * This mapper is part of the application layer and handles the conversion
 * from DTOs (coming from UI) back to domain objects for business logic processing.
 */
public class DtoToMagicDataMapper {
    
    /**
     * Convert MagicDisplayDTO to MagicData, preserving original binary fields.
     * This method takes an existing MagicData entity and updates it with values from the DTO,
     * preserving binary-specific fields that shouldn't be modified from the UI.
     */
    public MagicData toDomain(MagicDisplayDTO dto, MagicData originalMagic) {
        if (dto == null) {
            return null;
        }
        
        if (originalMagic == null) {
            throw new IllegalArgumentException("Original magic data cannot be null - binary fields must be preserved");
        }
        
        // Use builder pattern to create updated entity
        return originalMagic.toBuilder()
            // Update fields that can be modified from UI
            .spellPower(dto.spellPower())
            .element(dto.element())
            .attackType(dto.attackType())
            .drawResist(dto.drawResist())
            .hitCount(dto.hitCount())
            .statusAttackEnabler(dto.statusAttackEnabler())
            
            // Update complex objects
            .targetInfo(mapTargetInfo(dto.targetInfo(), originalMagic.getTargetInfo()))
            .attackFlags(mapAttackFlags(dto.attackInfo(), originalMagic.getAttackFlags()))
            .statusEffects(mapStatusEffects(dto.activeStatusEffects(), originalMagic.getStatusEffects()))
            .junctionStats(mapJunctionStats(dto.junctionStats()))
            .junctionElemental(mapJunctionElemental(dto.junctionElemental()))
            .junctionStatus(mapJunctionStatus(dto.junctionStatus()))
            .gfCompatibility(mapGFCompatibility(dto.gfCompatibility()))
            
            // Preserve binary fields that shouldn't be modified from UI
            .index(originalMagic.getIndex()) // Preserve original index
            .magicID(originalMagic.getMagicID()) // Preserve original magic ID
            .offsetSpellName(originalMagic.getOffsetSpellName()) // Preserve binary offsets
            .offsetSpellDescription(originalMagic.getOffsetSpellDescription())
            .extractedSpellName(originalMagic.getExtractedSpellName()) // Preserve extracted names
            .extractedSpellDescription(originalMagic.getExtractedSpellDescription())
            .animationTriggered(originalMagic.getAnimationTriggered()) // Preserve binary fields
            .unknown1(originalMagic.getUnknown1())
            .unknown2(originalMagic.getUnknown2())
            .unknown3(originalMagic.getUnknown3())
            .build();
    }
    
    /**
     * Create a completely new MagicData from DTO (used for creating new magic spells)
     */
    public MagicData createNewFromDto(MagicDisplayDTO dto, int index, int magicID) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        
        return MagicData.builder()
            // Set required identifiers
            .index(index)
            .magicID(magicID)
            
            // Set basic properties from DTO
            .spellPower(dto.spellPower())
            .element(dto.element())
            .attackType(dto.attackType())
            .drawResist(dto.drawResist())
            .hitCount(dto.hitCount())
            .statusAttackEnabler(dto.statusAttackEnabler())
            
            // Set spell names (for new spells, these would be the same)
            .extractedSpellName(dto.spellName())
            .extractedSpellDescription(dto.spellDescription())
            
            // Map complex objects
            .targetInfo(createTargetFlags(dto.targetInfo()))
            .attackFlags(createAttackFlags(dto.attackInfo()))
            .statusEffects(createStatusEffectSet(dto.activeStatusEffects()))
            .junctionStats(mapJunctionStats(dto.junctionStats()))
            .junctionElemental(mapJunctionElemental(dto.junctionElemental()))
            .junctionStatus(mapJunctionStatus(dto.junctionStatus()))
            .gfCompatibility(mapGFCompatibility(dto.gfCompatibility()))
            
            // Set default binary values for new spells
            .offsetSpellName(0) // Will be updated during serialization
            .offsetSpellDescription(0) // Will be updated during serialization
            .animationTriggered(0) // Default value
            .unknown1(0) // Default values for unknown fields
            .unknown2(0)
            .unknown3(0)
            .build();
    }
    
    /**
     * Map TargetInfo DTO back to TargetFlags, preserving original flags not represented in DTO
     */
    private TargetFlags mapTargetInfo(MagicDisplayDTO.TargetInfo targetInfo, TargetFlags originalFlags) {
        if (targetInfo == null) {
            return originalFlags != null ? originalFlags : new TargetFlags();
        }
        
        // Start with original flags to preserve unknown bits
        TargetFlags result = originalFlags != null ? new TargetFlags(originalFlags) : new TargetFlags();
        
        // Update known flags from DTO
        result.setDead(targetInfo.dead());
        result.setSingle(targetInfo.single());
        result.setEnemy(targetInfo.enemy());
        result.setSingleSide(targetInfo.singleSide());
        
        return result;
    }
    
    /**
     * Create new TargetFlags from TargetInfo DTO
     */
    private TargetFlags createTargetFlags(MagicDisplayDTO.TargetInfo targetInfo) {
        if (targetInfo == null) {
            return new TargetFlags();
        }
        
        TargetFlags result = new TargetFlags();
        result.setDead(targetInfo.dead());
        result.setSingle(targetInfo.single());
        result.setEnemy(targetInfo.enemy());
        result.setSingleSide(targetInfo.singleSide());
        
        return result;
    }
    
    /**
     * Map AttackInfo DTO back to AttackFlags, preserving original flags
     */
    private AttackFlags mapAttackFlags(MagicDisplayDTO.AttackInfo attackInfo, AttackFlags originalFlags) {
        if (attackInfo == null) {
            return originalFlags != null ? originalFlags : new AttackFlags();
        }
        
        // Start with original flags to preserve unknown bits
        AttackFlags result = originalFlags != null ? new AttackFlags(originalFlags) : new AttackFlags();
        
        // Update known flags from DTO
        result.setShelled(attackInfo.shelled());
        result.setReflected(attackInfo.reflected());
        result.setBreakDamageLimit(attackInfo.breakDamageLimit());
        result.setRevive(attackInfo.revive());
        
        return result;
    }
    
    /**
     * Create new AttackFlags from AttackInfo DTO
     */
    private AttackFlags createAttackFlags(MagicDisplayDTO.AttackInfo attackInfo) {
        if (attackInfo == null) {
            return new AttackFlags();
        }
        
        AttackFlags result = new AttackFlags();
        result.setShelled(attackInfo.shelled());
        result.setReflected(attackInfo.reflected());
        result.setBreakDamageLimit(attackInfo.breakDamageLimit());
        result.setRevive(attackInfo.revive());
        
        return result;
    }
    
    /**
     * Map status effects list back to StatusEffectSet
     */
    private StatusEffectSet mapStatusEffects(List<StatusEffect> statusEffects, StatusEffectSet originalSet) {
        if (statusEffects == null) {
            return originalSet != null ? originalSet : new StatusEffectSet();
        }
        
        StatusEffectSet result = new StatusEffectSet();
        for (StatusEffect effect : statusEffects) {
            result.setStatus(effect, true);
        }
        
        return result;
    }
    
    /**
     * Create new StatusEffectSet from status effects list
     */
    private StatusEffectSet createStatusEffectSet(List<StatusEffect> statusEffects) {
        StatusEffectSet result = new StatusEffectSet();
        if (statusEffects != null) {
            for (StatusEffect effect : statusEffects) {
                result.setStatus(effect, true);
            }
        }
        return result;
    }
    
    /**
     * Map JunctionStatsDTO back to JunctionStats
     */
    private JunctionStats mapJunctionStats(JunctionStatsDTO dto) {
        if (dto == null) {
            return JunctionStats.empty();
        }
        
        return new JunctionStats(
            dto.hp(),
            dto.str(),
            dto.vit(),
            dto.mag(),
            dto.spr(),
            dto.spd(),
            dto.eva(),
            dto.hit(),
            dto.luck()
        );
    }
    
    /**
     * Map JunctionElementalDTO back to JunctionElemental
     */
    private JunctionElemental mapJunctionElemental(JunctionElementalDTO dto) {
        if (dto == null) {
            return JunctionElemental.empty();
        }
        
        return new JunctionElemental(
            dto.attackElement(),
            dto.attackValue(),
            dto.defenseElements(),
            dto.defenseValue()
        );
    }
    
    /**
     * Map JunctionStatusDTO back to JunctionStatusEffects
     */
    private JunctionStatusEffects mapJunctionStatus(JunctionStatusDTO dto) {
        if (dto == null) {
            return JunctionStatusEffects.empty();
        }
        
        // Create status effect sets from the lists
        StatusEffectSet attackStatuses = createStatusEffectSet(dto.attackStatuses());
        StatusEffectSet defenseStatuses = createStatusEffectSet(dto.defenseStatuses());
        
        return new JunctionStatusEffects(
            attackStatuses,
            dto.attackValue(),
            defenseStatuses,
            dto.defenseValue()
        );
    }
    
    /**
     * Map GFCompatibilityDTO back to GFCompatibilitySet
     */
    private GFCompatibilitySet mapGFCompatibility(GFCompatibilityDTO dto) {
        if (dto == null) {
            return new GFCompatibilitySet();
        }
        
        // Use the constructor that takes a map
        return new GFCompatibilitySet(dto.compatibilities());
    }
} 