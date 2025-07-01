package com.ff8.application.dto;

import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;

import lombok.Builder;
import lombok.With;

import java.util.List;

/**
 * Data Transfer Object for magic data display in UI.
 * Uses Java 21 records for immutability and conciseness.
 */
@With
@Builder(toBuilder = true)
public record MagicDisplayDTO(
        // Index properties - unique identifier
        int index,  // Position in kernel file (unique identifier)
        
        // Basic properties
        int magicID,
        String spellName,  // This is the extracted spell name from binary
        String spellDescription,  // This is the extracted spell description from binary
        SpellTranslations translations,  // Internationalization support
        int spellPower,
        Element element,
        AttackType attackType,
        int drawResist,
        int hitCount,
        
        // Status attack enabler
        int statusAttackEnabler,
        
        // Target and attack properties
        TargetInfo targetInfo,
        AttackInfo attackInfo,
        
        // Status effects
        List<StatusEffect> activeStatusEffects,
        
        // Junction bonuses
        JunctionStatsDTO junctionStats,
        JunctionElementalDTO junctionElemental,
        JunctionStatusDTO junctionStatus,
        GFCompatibilityDTO gfCompatibility,
        
        // Metadata
        boolean hasStatusEffects,
        boolean hasJunctionBonuses,
        boolean isCurative,
        boolean isModified,
        boolean isNewlyCreated
) {
    /**
     * Record for target information
     */
    @With
    @Builder(toBuilder = true)
    public record TargetInfo(
            boolean dead,
            boolean single,
            boolean enemy,
            boolean singleSide,
            List<Integer> activeBits
    ) {}

    /**
     * Record for attack information
     */
    @With
    @Builder(toBuilder = true)
    public record AttackInfo(
            boolean shelled,
            boolean reflected,
            boolean breakDamageLimit,
            boolean revive,
            List<Integer> activeBits
    ) {}

    /**
     * Validation method for the DTO
     */
    public List<String> validate() {
        var errors = new java.util.ArrayList<String>();
        
        if (index < 0) {
            errors.add("Index must be non-negative");
        }
        
        if (magicID < 0 || magicID > 345) {
            errors.add("Magic ID must be between 0 and 345");
        }
        
        if (spellPower < 0 || spellPower > 255) {
            errors.add("Spell power must be between 0 and 255");
        }
        
        if (hitCount < 0 || hitCount > 255) {
            errors.add("Hit count must be between 0 and 255");
        }
        
        if (drawResist < 0 || drawResist > 255) {
            errors.add("Draw resist must be between 0 and 255");
        }
        
        if (spellName == null || spellName.trim().isEmpty()) {
            errors.add("Spell name cannot be empty");
        }
        
        if (element == null) {
            errors.add("Element cannot be null");
        }
        
        if (attackType == null) {
            errors.add("Attack type cannot be null");
        }
        
        return errors;
    }

    /**
     * Check if this DTO is valid
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    /**
     * Get display summary for UI
     */
    public String getDisplaySummary() {
        var summary = new StringBuilder();
        summary.append(spellName).append(" (Index: ").append(index).append(", ID: ").append(magicID).append(")");
        
        if (spellPower > 0) {
            summary.append(" - Power: ").append(spellPower);
        }
        
        if (element != Element.NONE) {
            summary.append(" - ").append(element.getDisplayName());
        }
        
        if (hasStatusEffects) {
            summary.append(" - Status Effects: ").append(activeStatusEffects.size());
        }
        
        if (hasJunctionBonuses) {
            summary.append(" - Junction Bonuses");
        }
        
        return summary.toString();
    }
} 