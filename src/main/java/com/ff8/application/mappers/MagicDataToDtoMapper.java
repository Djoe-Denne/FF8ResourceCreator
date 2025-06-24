package com.ff8.application.mappers;

import com.ff8.application.dto.*;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.StatusEffect;

import java.util.List;

/**
 * Mapper class responsible for converting MagicData domain entities to MagicDisplayDTO.
 * This mapper is part of the application layer and handles the conversion
 * from domain objects to DTOs for UI consumption.
 */
public class MagicDataToDtoMapper {
    
    /**
     * Convert a single MagicData entity to MagicDisplayDTO
     */
    public MagicDisplayDTO toDto(MagicData magic) {
        if (magic == null) {
            return null;
        }
        
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
            mapTargetInfo(magic.getTargetInfo()),
            // AttackInfo
            mapAttackInfo(magic.getAttackFlags()),
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
    
    /**
     * Convert a list of MagicData entities to MagicDisplayDTOs
     */
    public List<MagicDisplayDTO> toDtoList(List<MagicData> magicDataList) {
        if (magicDataList == null) {
            return List.of();
        }
        
        return magicDataList.stream()
            .map(this::toDto)
            .toList();
    }
    
    /**
     * Map TargetFlags to TargetInfo DTO
     */
    private MagicDisplayDTO.TargetInfo mapTargetInfo(TargetFlags targetFlags) {
        if (targetFlags == null) {
            return new MagicDisplayDTO.TargetInfo(false, false, false, false, List.of());
        }
        
        return new MagicDisplayDTO.TargetInfo(
            targetFlags.isDead(),
            targetFlags.isSingle(),
            targetFlags.isEnemy(),
            targetFlags.isSingleSide(),
            targetFlags.getActiveTargets()
        );
    }
    
    /**
     * Map AttackFlags to AttackInfo DTO
     */
    private MagicDisplayDTO.AttackInfo mapAttackInfo(AttackFlags attackFlags) {
        if (attackFlags == null) {
            return new MagicDisplayDTO.AttackInfo(false, false, false, false, List.of());
        }
        
        return new MagicDisplayDTO.AttackInfo(
            attackFlags.isShelled(),
            attackFlags.isReflected(),
            attackFlags.isBreakDamageLimit(),
            attackFlags.isRevive(),
            attackFlags.getActiveFlags()
        );
    }
    
    /**
     * Map JunctionStats to JunctionStatsDTO
     */
    private JunctionStatsDTO mapJunctionStats(JunctionStats stats) {
        if (stats == null) {
            return JunctionStatsDTO.empty();
        }
        
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
    
    /**
     * Map JunctionElemental to JunctionElementalDTO
     */
    private JunctionElementalDTO mapJunctionElemental(JunctionElemental elemental) {
        if (elemental == null) {
            return JunctionElementalDTO.empty();
        }
        
        return new JunctionElementalDTO(
            elemental.getAttackElement(),
            elemental.getAttackValue(),
            elemental.getDefenseElements(),
            elemental.getDefenseValue()
        );
    }
    
    /**
     * Map JunctionStatusEffects to JunctionStatusDTO
     */
    private JunctionStatusDTO mapJunctionStatus(JunctionStatusEffects status) {
        if (status == null) {
            return JunctionStatusDTO.empty();
        }
        
        return new JunctionStatusDTO(
            status.getAttackStatuses().getActiveStatuses(),
            status.getAttackValue(),
            status.getDefenseStatuses().getActiveStatuses(),
            status.getDefenseValue()
        );
    }
    
    /**
     * Map GFCompatibilitySet to GFCompatibilityDTO
     */
    private GFCompatibilityDTO mapGFCompatibility(GFCompatibilitySet compatibility) {
        if (compatibility == null) {
            return GFCompatibilityDTO.createDefault();
        }
        
        return new GFCompatibilityDTO(compatibility.getAllCompatibilities());
    }
} 