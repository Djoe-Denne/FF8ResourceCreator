package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of attack types in FF8.
 */
@Getter
@RequiredArgsConstructor
public enum AttackType {
    NONE(0, "None"),
    PHYSICAL_ATTACK(1, "Physical Attack"),
    MAGIC_ATTACK(2, "Magic Attack"),
    CURATIVE_MAGIC(3, "Curative Magic"),
    CURATIVE_ITEM(4, "Curative Item"),
    REVIVE(5, "Revive"),
    REVIVE_AT_FULL_HP(6, "Revive At Full HP"),
    PERCENT_PHYSICAL_DAMAGE(7, "% Physical Damage"),
    PERCENT_MAGIC_DAMAGE(8, "% Magic Damage"),
    RENZOKUKEN_FINISHER(9, "Renzokuken Finisher"),
    SQUALL_GUNBLADE_ATTACK(10, "Squall Gunblade Attack"),
    GF(11, "GF"),
    SCAN(12, "Scan"),
    LV_DOWN(13, "LV Down"),
    SUMMON_ITEM(14, "Summon Item?"),
    GF_IGNORE_TARGET_SPR(15, "GF (Ignore Target SPR)"),
    LV_UP(16, "LV Up"),
    CARD(17, "Card"),
    KAMIKAZE(18, "Kamikaze"),
    DEVOUR(19, "Devour"),
    PERCENT_GF_DAMAGE(20, "% GF Damage"),
    UNKNOWN_1(21, "Unknown 1"),
    MAGIC_ATTACK_IGNORE_TARGET_SPR(22, "Magic Attack (Ignore Target SPR)"),
    ANGELO_SEARCH(23, "Angelo Search"),
    MOOGLE_DANCE(24, "Moogle Dance"),
    WHITE_WIND_QUISTIS(25, "White Wind (Quistis)"),
    LV_QUESTION_ATTACK(26, "LV? Attack"),
    FIXED_DAMAGE(27, "Fixed Damage"),
    TARGET_CURRENT_HP_MINUS_1(28, "Target Current HP - 1"),
    FIXED_MAGIC_DAMAGE_BASED_ON_GF_LEVEL(29, "Fixed Magic Damage Based on GF Level"),
    UNKNOWN_2(30, "Unknown 2"),
    UNKNOWN_3(31, "Unknown 3"),
    GIVE_PERCENTAGE_HP(32, "Give Percentage HP"),
    UNKNOWN_4(33, "Unknown 4"),
    EVERYONES_GRUDGE(34, "Everyone's Grudge"),
    ONE_HP_DAMAGE(35, "1 HP Damage"),
    PHYSICAL_ATTACK_IGNORE_TARGET_VIT(36, "Physical Attack (Ignore Target VIT)");

    private final int value;
    private final String displayName;

    // Static array for fast lookup by value
    private static final AttackType[] BY_VALUE = new AttackType[37]; // 0-36
    
    static {
        for (AttackType type : values()) {
            BY_VALUE[type.value] = type;
        }
    }

    /**
     * Get AttackType by value using array lookup for O(1) performance.
     * Returns NONE for unknown values to handle unexpected data gracefully.
     */
    public static AttackType fromValue(int value) {
        if (value >= 0 && value < BY_VALUE.length && BY_VALUE[value] != null) {
            return BY_VALUE[value];
        }
        System.err.println("WARNING: Unknown attack type value " + value + ", using NONE");
        return NONE;
    }
    
    /**
     * Get AttackType by value with strict validation (throws exception for unknown values)
     */
    public static AttackType fromValueStrict(int value) {
        if (value >= 0 && value < BY_VALUE.length && BY_VALUE[value] != null) {
            return BY_VALUE[value];
        }
        throw new IllegalArgumentException("Invalid attack type value: " + value);
    }
} 