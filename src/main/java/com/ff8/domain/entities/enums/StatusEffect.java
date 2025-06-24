package com.ff8.domain.entities.enums;

/**
 * Enumeration of all possible status effects in FF8.
 * Uses Java 21 enum features for better type safety and functionality.
 */
public enum StatusEffect {
    // Bits 0-31 (DWORD)
    SLEEP(0, "Sleep"),
    HASTE(1, "Haste"),
    SLOW(2, "Slow"),
    STOP(3, "Stop"),
    REGEN(4, "Regen"),
    PROTECT(5, "Protect"),
    SHELL(6, "Shell"),
    REFLECT(7, "Reflect"),
    AURA(8, "Aura"),
    CURSE(9, "Curse"),
    DOOM(10, "Doom"),
    INVINCIBLE(11, "Invincible"),
    PETRIFYING(12, "Petrifying"),
    FLOAT(13, "Float"),
    CONFUSION(14, "Confusion"),
    DRAIN(15, "Drain"),
    EJECT(16, "Eject"),
    DOUBLE(17, "Double"),
    TRIPLE(18, "Triple"),
    UNKNOWN_19(19, "Unknown 19"),
    UNKNOWN_20(20, "Unknown 20"),
    DEFEND(21, "Defend"),
    CHARGED(22, "Charged"),
    BACK_ATTACK(23, "Back Attack"),
    VIT_0(24, "Vit 0"),
    ANGEL_WING(25, "Angel Wing"),
    UNKNOWN_26(26, "Unknown 26"),
    UNKNOWN_27(27, "Unknown 27"),
    UNKNOWN_28(28, "Unknown 28"),
    UNKNOWN_29(29, "Unknown 29"),
    HAS_MAGIC(30, "Has Magic"),
    SUMMON_GF(31, "Summon GF"),
    DEATH(32, "Death"),
    POISON(33, "Poison"),
    PETRIFY(34, "Petrify"),
    DARKNESS(35, "Darkness"),
    SILENCE(36, "Silence"),
    BERSERK(37, "Berserk"),
    ZOMBIE(38, "Zombie"),
    UNKNOWN_39(39, "Unknown 39");

    private final int bitIndex;
    private final String displayName;

    // Static array for fast lookup by bit index
    private static final StatusEffect[] BY_BIT_INDEX = new StatusEffect[40]; // 0-39
    
    static {
        for (StatusEffect effect : values()) {
            BY_BIT_INDEX[effect.bitIndex] = effect;
        }
    }

    StatusEffect(int bitIndex, String displayName) {
        this.bitIndex = bitIndex;
        this.displayName = displayName;
    }

    public int getBitIndex() {
        return bitIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get StatusEffect by bit index using array lookup for O(1) performance
     */
    public static StatusEffect fromBitIndex(int bitIndex) {
        if (bitIndex >= 0 && bitIndex < BY_BIT_INDEX.length && BY_BIT_INDEX[bitIndex] != null) {
            return BY_BIT_INDEX[bitIndex];
        }
        throw new IllegalArgumentException("Invalid bit index: " + bitIndex);
    }

    /**
     * Check if this is a known status effect (not unknown)
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 