package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of attack types in Final Fantasy VIII.
 * 
 * <p>This enum represents the different types of attacks that magic spells can perform
 * in FF8. The attack type determines how damage is calculated, which stats are used,
 * and how the spell interacts with various defensive mechanics.</p>
 * 
 * <p>Attack types affect:</p>
 * <ul>
 *   <li>Damage calculation formulas</li>
 *   <li>Stat dependencies (STR vs MAG, VIT vs SPR)</li>
 *   <li>Interaction with defensive abilities (Shell, Protect)</li>
 *   <li>AI behavior and targeting patterns</li>
 *   <li>Animation and visual effects</li>
 * </ul>
 * 
 * <p>The enum provides both value-based lookup for binary serialization and
 * descriptive names for user interface display.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum AttackType {
    /** No attack type - typically used for utility spells */
    NONE(0, "None"),
    
    /** Physical attack using STR stat, affected by Protect */
    PHYSICAL_ATTACK(1, "Physical Attack"),
    
    /** Magic attack using MAG stat, affected by Shell */
    MAGIC_ATTACK(2, "Magic Attack"),
    
    /** Curative magic that heals HP/MP or removes negative status effects */
    CURATIVE_MAGIC(3, "Curative Magic"),
    
    /** Curative item effect similar to curative magic */
    CURATIVE_ITEM(4, "Curative Item"),
    
    /** Revive spell that brings back KO'd characters */
    REVIVE(5, "Revive"),
    
    /** Revive spell that brings back KO'd characters at full HP */
    REVIVE_AT_FULL_HP(6, "Revive At Full HP"),
    
    /** Percentage-based physical damage */
    PERCENT_PHYSICAL_DAMAGE(7, "% Physical Damage"),
    
    /** Percentage-based magic damage */
    PERCENT_MAGIC_DAMAGE(8, "% Magic Damage"),
    
    /** Special attack type for Renzokuken finisher moves */
    RENZOKUKEN_FINISHER(9, "Renzokuken Finisher"),
    
    /** Special attack type for Squall's gunblade attacks */
    SQUALL_GUNBLADE_ATTACK(10, "Squall Gunblade Attack"),
    
    /** Guardian Force summon attack */
    GF(11, "GF"),
    
    /** Scan-type spell that reveals enemy information */
    SCAN(12, "Scan"),
    
    /** Level reduction spell */
    LV_DOWN(13, "LV Down"),
    
    /** Summon item effect (rarely used) */
    SUMMON_ITEM(14, "Summon Item?"),
    
    /** Guardian Force attack that ignores target's SPR stat */
    GF_IGNORE_TARGET_SPR(15, "GF (Ignore Target SPR)"),
    
    /** Level increase spell */
    LV_UP(16, "LV Up"),
    
    /** Card spell that turns enemies into cards */
    CARD(17, "Card"),
    
    /** Kamikaze attack that sacrifices the caster */
    KAMIKAZE(18, "Kamikaze"),
    
    /** Devour attack that consumes the target */
    DEVOUR(19, "Devour"),
    
    /** Percentage-based Guardian Force damage */
    PERCENT_GF_DAMAGE(20, "% GF Damage"),
    
    /** Unknown attack type (purpose unclear) */
    UNKNOWN_1(21, "Unknown 1"),
    
    /** Magic attack that ignores target's SPR stat */
    MAGIC_ATTACK_IGNORE_TARGET_SPR(22, "Magic Attack (Ignore Target SPR)"),
    
    /** Angelo Search ability */
    ANGELO_SEARCH(23, "Angelo Search"),
    
    /** Moogle Dance ability */
    MOOGLE_DANCE(24, "Moogle Dance"),
    
    /** White Wind ability (Quistis Blue Magic) */
    WHITE_WIND_QUISTIS(25, "White Wind (Quistis)"),
    
    /** Level-based attack with variable damage */
    LV_QUESTION_ATTACK(26, "LV? Attack"),
    
    /** Fixed damage that ignores stats and defenses */
    FIXED_DAMAGE(27, "Fixed Damage"),
    
    /** Attack that reduces target HP to 1 */
    TARGET_CURRENT_HP_MINUS_1(28, "Target Current HP - 1"),
    
    /** Fixed magic damage based on Guardian Force level */
    FIXED_MAGIC_DAMAGE_BASED_ON_GF_LEVEL(29, "Fixed Magic Damage Based on GF Level"),
    
    /** Unknown attack type (purpose unclear) */
    UNKNOWN_2(30, "Unknown 2"),
    
    /** Unknown attack type (purpose unclear) */
    UNKNOWN_3(31, "Unknown 3"),
    
    /** Gives percentage of HP to target */
    GIVE_PERCENTAGE_HP(32, "Give Percentage HP"),
    
    /** Unknown attack type (purpose unclear) */
    UNKNOWN_4(33, "Unknown 4"),
    
    /** Everyone's Grudge ability */
    EVERYONES_GRUDGE(34, "Everyone's Grudge"),
    
    /** Fixed 1 HP damage */
    ONE_HP_DAMAGE(35, "1 HP Damage"),
    
    /** Physical attack that ignores target's VIT stat */
    PHYSICAL_ATTACK_IGNORE_TARGET_VIT(36, "Physical Attack (Ignore Target VIT)");

    /** The numeric value used in binary serialization */
    private final int value;
    
    /** The display name for user interfaces */
    private final String displayName;

    // Static array for fast lookup by value
    private static final AttackType[] BY_VALUE = new AttackType[37]; // 0-36
    
    static {
        for (AttackType type : values()) {
            BY_VALUE[type.value] = type;
        }
    }

    /**
     * Gets an AttackType by its numeric value using fast array lookup.
     * 
     * <p>This method provides O(1) lookup performance and gracefully handles
     * unknown values by returning NONE instead of throwing an exception.
     * This is useful when parsing binary data that might contain unexpected values.</p>
     * 
     * @param value the numeric value to look up
     * @return the corresponding AttackType, or NONE if the value is unknown
     */
    public static AttackType fromValue(int value) {
        if (value >= 0 && value < BY_VALUE.length && BY_VALUE[value] != null) {
            return BY_VALUE[value];
        }
        System.err.println("WARNING: Unknown attack type value " + value + ", using NONE");
        return NONE;
    }
    
    /**
     * Gets an AttackType by its numeric value with strict validation.
     * 
     * <p>This method provides O(1) lookup performance and throws an exception
     * for unknown values. Use this method when you need to ensure the value
     * is valid and want to handle errors explicitly.</p>
     * 
     * @param value the numeric value to look up
     * @return the corresponding AttackType
     * @throws IllegalArgumentException if the value is not valid
     */
    public static AttackType fromValueStrict(int value) {
        if (value >= 0 && value < BY_VALUE.length && BY_VALUE[value] != null) {
            return BY_VALUE[value];
        }
        throw new IllegalArgumentException("Invalid attack type value: " + value);
    }
} 