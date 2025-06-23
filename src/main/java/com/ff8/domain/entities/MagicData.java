package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import java.util.Objects;

/**
 * Main aggregate root for magic data.
 * Represents a complete magic spell with all its properties.
 */
@Value
@Builder(toBuilder = true)
@With
public class MagicData {
    // Unique identifier - position in kernel file
    @Builder.Default
    int index = 0;  // Position in kernel file (unique identifier)
    
    // EXACT binary format fields (for binary preservation)
    @Builder.Default
    int offsetSpellName = 0;
    @Builder.Default
    int offsetSpellDescription = 0;
    @Builder.Default
    int magicID = 0;
    @Builder.Default
    int animationTriggered = 0;
    @Builder.Default
    AttackType attackType = AttackType.NONE;
    @Builder.Default
    int spellPower = 0;
    @Builder.Default
    int unknown1 = 0;
    @Builder.Default
    int drawResist = 0;
    @Builder.Default
    int hitCount = 1;
    @Builder.Default
    Element element = Element.NONE;
    @Builder.Default
    int unknown2 = 0;
    @Builder.Default
    int statusAttackEnabler = 0;
    @Builder.Default
    int unknown3 = 0;

    // Modern object-oriented properties
    @Builder.Default
    TargetFlags targetInfo = new TargetFlags();
    @Builder.Default
    AttackFlags attackFlags = new AttackFlags();
    @Builder.Default
    StatusEffectSet statusEffects = new StatusEffectSet();
    @Builder.Default
    JunctionStats junctionStats = JunctionStats.empty();
    @Builder.Default
    JunctionElemental junctionElemental = JunctionElemental.empty();
    @Builder.Default
    JunctionStatusEffects junctionStatus = JunctionStatusEffects.empty();
    @Builder.Default
    GFCompatibilitySet gfCompatibility = new GFCompatibilitySet();

    // Extracted from binary data at runtime (exclude from serialization)
    @Builder.Default
    String extractedSpellName = "";
    
    @Builder.Default
    String extractedSpellDescription = "";

    /**
     * Check if this is a curative spell
     */
    public boolean isCurative() {
        return attackType == AttackType.CURATIVE_MAGIC || attackType == AttackType.CURATIVE_ITEM;
    }

    /**
     * Check if this spell has status effects
     */
    public boolean hasStatusEffects() {
        return statusEffects.hasAnyStatus();
    }

    /**
     * Check if this spell has junction bonuses
     */
    public boolean hasJunctionBonuses() {
        return junctionStats.hasAnyBonuses() ||
               junctionElemental.hasElementalAttack() ||
               junctionElemental.hasElementalDefense() ||
               junctionStatus.hasStatusAttack() ||
               junctionStatus.hasStatusDefense() ||
               gfCompatibility.hasAnyGoodCompatibilities();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagicData magicData = (MagicData) o;
        return index == magicData.index;  // Index is the primary key (unique position in kernel file)
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        return "MagicData{" +
                "index=" + index +
                ", magicID=" + magicID +
                ", extractedSpellName='" + extractedSpellName + '\'' +
                ", spellPower=" + spellPower +
                ", element=" + element +
                ", attackType=" + attackType +
                ", hasStatusEffects=" + hasStatusEffects() +
                ", hasJunctionBonuses=" + hasJunctionBonuses() +
                '}';
    }
} 