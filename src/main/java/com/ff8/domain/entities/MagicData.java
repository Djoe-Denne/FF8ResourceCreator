package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import java.util.Objects;

/**
 * Main aggregate root for magic data in the FF8 Magic Editor.
 * 
 * <p>This class represents a complete magic spell with all its properties, following the
 * FF8 kernel.bin binary format while providing a modern object-oriented interface.
 * The class maintains exact binary format compatibility for round-trip serialization
 * while offering rich domain behavior and validation.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Immutable value object with builder pattern support</li>
 *   <li>Binary preservation fields for exact kernel.bin compatibility</li>
 *   <li>Modern object-oriented properties for status effects and junction data</li>
 *   <li>Internationalization support through SpellTranslations</li>
 *   <li>Validation and business logic methods</li>
 * </ul>
 * 
 * <p>The class follows the hexagonal architecture principles by containing only
 * domain logic and having no dependencies on external frameworks or infrastructure.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@Value
@Builder(toBuilder = true)
@With
public class MagicData {
    
    /**
     * Unique identifier representing the position in the kernel file.
     * This serves as the primary key for magic data and determines the spell's
     * position in the game's magic list.
     */
    @Builder.Default
    int index = 0;
    
    // === EXACT BINARY FORMAT FIELDS ===
    // These fields maintain exact binary compatibility with FF8's kernel.bin format
    
    /**
     * Offset pointer to the spell name in the kernel file's text section.
     * Used for binary serialization and text extraction.
     */
    @Builder.Default
    int offsetSpellName = 0;
    
    /**
     * Offset pointer to the spell description in the kernel file's text section.
     * Used for binary serialization and text extraction.
     */
    @Builder.Default
    int offsetSpellDescription = 0;
    
    /**
     * Internal magic ID used by the game's spell system.
     * This is distinct from the index and may not be sequential.
     */
    @Builder.Default
    int magicID = 0;
    
    /**
     * Animation trigger ID for spell visual effects.
     * Determines which animation sequence plays when the spell is cast.
     */
    @Builder.Default
    int animationTriggered = 0;
    
    /**
     * The type of attack this spell performs.
     * Determines damage calculation, targeting behavior, and AI usage patterns.
     */
    @Builder.Default
    AttackType attackType = AttackType.NONE;
    
    /**
     * Base power of the spell for damage or healing calculations.
     * Valid range is 0-255. Higher values increase effectiveness.
     */
    @Builder.Default
    int spellPower = 0;
    
    /**
     * Unknown binary field preserved for round-trip compatibility.
     * This field's purpose is not yet understood but must be preserved.
     */
    @Builder.Default
    int unknown1 = 0;
    
    /**
     * Resistance value for drawing this spell from enemies.
     * Higher values make the spell harder to draw. Range: 0-255.
     */
    @Builder.Default
    int drawResist = 0;
    
    /**
     * Number of hits this spell performs when cast.
     * Multi-hit spells deal damage multiple times. Range: 1-255.
     */
    @Builder.Default
    int hitCount = 1;
    
    /**
     * Elemental affinity of the spell.
     * Determines elemental damage type and interaction with resistances.
     */
    @Builder.Default
    Element element = Element.NONE;
    
    /**
     * Unknown binary field preserved for round-trip compatibility.
     * This field's purpose is not yet understood but must be preserved.
     */
    @Builder.Default
    int unknown2 = 0;
    
    /**
     * Enabler flag for status attack effects.
     * Controls whether the spell's status effects are applied to targets.
     */
    @Builder.Default
    int statusAttackEnabler = 0;
    
    /**
     * Unknown binary field preserved for round-trip compatibility.
     * This field's purpose is not yet understood but must be preserved.
     */
    @Builder.Default
    int unknown3 = 0;

    // === MODERN OBJECT-ORIENTED PROPERTIES ===
    // These provide rich domain behavior while maintaining binary compatibility
    
    /**
     * Target selection flags defining who can be targeted by this spell.
     * Includes flags for targeting enemies, allies, dead characters, etc.
     */
    @Builder.Default
    TargetFlags targetInfo = new TargetFlags();
    
    /**
     * Attack behavior flags controlling spell mechanics.
     * Includes flags for reflection, shell interaction, damage limits, etc.
     */
    @Builder.Default
    AttackFlags attackFlags = new AttackFlags();
    
    /**
     * Set of status effects that this spell can inflict on targets.
     * Manages the complex 48-bit status effect system from FF8.
     */
    @Builder.Default
    StatusEffectSet statusEffects = new StatusEffectSet();
    
    /**
     * Junction stat bonuses provided when this spell is equipped.
     * Includes bonuses to HP, STR, VIT, MAG, SPR, SPD, EVA, HIT, and LUCK.
     */
    @Builder.Default
    JunctionStats junctionStats = JunctionStats.empty();
    
    /**
     * Elemental junction properties for attack and defense.
     * Defines elemental damage addition and resistance when junctioned.
     */
    @Builder.Default
    JunctionElemental junctionElemental = JunctionElemental.empty();
    
    /**
     * Status effect junction properties for attack and defense.
     * Defines status effect application and resistance when junctioned.
     */
    @Builder.Default
    JunctionStatusEffects junctionStatus = JunctionStatusEffects.empty();
    
    /**
     * Guardian Force compatibility matrix.
     * Defines which GFs work well with this spell for AP gain bonuses.
     */
    @Builder.Default
    GFCompatibilitySet gfCompatibility = new GFCompatibilitySet();

    // === EXTRACTED RUNTIME DATA ===
    // These fields are populated from binary data during parsing
    
    /**
     * Spell name extracted from the kernel file's text section.
     * This is the English name as it appears in the original game.
     */
    @Builder.Default
    String extractedSpellName = "";
    
    /**
     * Spell description extracted from the kernel file's text section.
     * This is the English description as it appears in the original game.
     */
    @Builder.Default
    String extractedSpellDescription = "";

    // === INTERNATIONALIZATION SUPPORT ===
    
    /**
     * Multi-language translations for spell name and description.
     * Supports localization for various languages beyond English.
     */
    @Builder.Default
    SpellTranslations translations = null;

    // === CREATION METADATA ===
    
    /**
     * Flag indicating if this magic was newly created by the user.
     * Newly created spells are highlighted in the UI and included in exports.
     */
    @Builder.Default
    boolean isNewlyCreated = false;

    /**
     * Determines if this spell provides healing rather than damage.
     * 
     * @return true if this is a curative spell (healing or beneficial effect)
     */
    public boolean isCurative() {
        return attackType == AttackType.CURATIVE_MAGIC || attackType == AttackType.CURATIVE_ITEM;
    }

    /**
     * Checks if this spell can inflict any status effects on targets.
     * 
     * @return true if the spell has any status effects configured
     */
    public boolean hasStatusEffects() {
        return statusEffects.hasAnyStatus();
    }

    /**
     * Checks if this spell provides any junction bonuses when equipped.
     * 
     * <p>Junction bonuses include:</p>
     * <ul>
     *   <li>Stat bonuses (HP, STR, VIT, MAG, SPR, SPD, EVA, HIT, LUCK)</li>
     *   <li>Elemental attack or defense properties</li>
     *   <li>Status effect attack or defense properties</li>
     *   <li>Guardian Force compatibility bonuses</li>
     * </ul>
     * 
     * @return true if the spell provides any junction bonuses
     */
    public boolean hasJunctionBonuses() {
        return junctionStats.hasAnyBonuses() ||
               junctionElemental.hasElementalAttack() ||
               junctionElemental.hasElementalDefense() ||
               junctionStatus.hasStatusAttack() ||
               junctionStatus.hasStatusDefense() ||
               gfCompatibility.hasAnyGoodCompatibilities();
    }

    /**
     * Gets the current display name for this spell.
     * 
     * <p>Prioritizes translated names over extracted names:</p>
     * <ol>
     *   <li>If translations exist, returns the English translation</li>
     *   <li>Otherwise, returns the extracted name from the kernel file</li>
     *   <li>Falls back to empty string if neither is available</li>
     * </ol>
     * 
     * @return the spell's display name, never null
     */
    public String getSpellName() {
        if (translations != null) {
            return translations.getEnglishName();
        }
        return extractedSpellName != null ? extractedSpellName : "";
    }

    /**
     * Gets the current display description for this spell.
     * 
     * <p>Prioritizes translated descriptions over extracted descriptions:</p>
     * <ol>
     *   <li>If translations exist, returns the English translation</li>
     *   <li>Otherwise, returns the extracted description from the kernel file</li>
     *   <li>Falls back to empty string if neither is available</li>
     * </ol>
     * 
     * @return the spell's display description, never null
     */
    public String getSpellDescription() {
        if (translations != null) {
            return translations.getEnglishDescription();
        }
        return extractedSpellDescription != null ? extractedSpellDescription : "";
    }

    /**
     * Gets the spell translations, creating default ones if needed.
     * 
     * <p>If no translations exist, creates a default SpellTranslations object
     * using the extracted name and description. This ensures consistent
     * access to translation data throughout the application.</p>
     * 
     * @return the spell translations, never null
     */
    public SpellTranslations getTranslations() {
        if (translations != null) {
            return translations;
        }
        // Create default translations from extracted data
        return new SpellTranslations(
            extractedSpellName != null ? extractedSpellName : "",
            extractedSpellDescription != null ? extractedSpellDescription : ""
        );
    }

    /**
     * Creates a new instance with updated translations.
     * 
     * <p>This method follows the immutable pattern by returning a new instance
     * rather than modifying the current one.</p>
     * 
     * @param newTranslations the new translations to apply
     * @return a new MagicData instance with updated translations
     */
    public MagicData withTranslations(SpellTranslations newTranslations) {
        return this.toBuilder().translations(newTranslations).build();
    }

    /**
     * Updates both the spell name and description for the English language.
     * 
     * <p>This is a convenience method that maintains existing translations
     * for other languages while updating the English translation.</p>
     * 
     * @param name the new English spell name
     * @param description the new English spell description
     * @return a new MagicData instance with updated English translations
     */
    public MagicData withSpellNameAndDescription(String name, String description) {
        SpellTranslations currentTranslations = getTranslations();
        SpellTranslations updatedTranslations = currentTranslations.withEnglishTranslation(name, description);
        return withTranslations(updatedTranslations);
    }

    /**
     * Checks if this spell has custom translations beyond the basic English text.
     * 
     * <p>This method helps identify spells that have been localized for
     * multiple languages versus those that only have the basic English text.</p>
     * 
     * @return true if the spell has translations for languages other than English
     */
    public boolean hasCustomTranslations() {
        return translations != null && !translations.hasOnlyEnglish();
    }

    /**
     * Determines equality based on the spell's index (position in kernel file).
     * 
     * <p>The index serves as the primary key for magic data, as it represents
     * the spell's unique position in the kernel file structure.</p>
     * 
     * @param o the object to compare with
     * @return true if both objects are MagicData with the same index
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MagicData magicData = (MagicData) o;
        return index == magicData.index;  // Index is the primary key (unique position in kernel file)
    }

    /**
     * Generates hash code based on the spell's index.
     * 
     * <p>Consistent with the equals method, the hash code is based solely
     * on the index to ensure proper behavior in hash-based collections.</p>
     * 
     * @return hash code based on the spell's index
     */
    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    /**
     * Provides a comprehensive string representation of the magic data.
     * 
     * <p>Includes key identifying information and computed properties
     * for debugging and logging purposes.</p>
     * 
     * @return a string representation of the magic data
     */
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