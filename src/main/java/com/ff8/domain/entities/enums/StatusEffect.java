package com.ff8.domain.entities.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of all possible status effects in FF8.
 * Uses Java 21 enum features for better type safety and functionality.
 */
@Getter
@RequiredArgsConstructor
public enum StatusEffect {
    // Bits 0-31 (DWORD)
    SLEEP(1, "Sleep"),
    HASTE(2, "Haste"),
    SLOW(4, "Slow"),
    STOP(8, "Stop"),
    REGEN(16, "Regen"),
    PROTECT(32, "Protect"),
    SHELL(64, "Shell"),
    REFLECT(128, "Reflect"),
    AURA(256, "Aura"),
    CURSE(512, "Curse"),
    DOOM(1024, "Doom"),
    INVINCIBLE(2048, "Invincible"),
    PETRIFYING(4096, "Petrifying"),
    FLOAT(8192, "Float"),
    CONFUSION(16384, "Confusion"),
    DRAIN(32768, "Drain"),
    EJECT(65536, "Eject"),
    DOUBLE(131072, "Double"),
    TRIPLE(262144, "Triple"),
    UNKNOWN_19(524288, "Unknown 19"),
    UNKNOWN_20(1048576, "Unknown 20"),
    DEFEND(2097152, "Defend"),
    CHARGED(4194304, "Charged"),
    BACK_ATTACK(8388608, "Back Attack"),
    VIT_0(16777216, "Vit 0"),
    ANGEL_WING(33554432, "Angel Wing"),
    UNKNOWN_26(67108864, "Unknown 26"),
    UNKNOWN_27(134217728, "Unknown 27"),
    UNKNOWN_28(268435456, "Unknown 28"),
    UNKNOWN_29(536870912, "Unknown 29"),
    HAS_MAGIC(1073741824, "Has Magic"),
    SUMMON_GF(2147483648L, "Summon GF"),
    DEATH(4294967296L, "Death"),
    POISON(8589934592L, "Poison"),
    PETRIFY(17179869184L, "Petrify"),
    DARKNESS(34359738368L, "Darkness"),
    SILENCE(68719476736L, "Silence"),
    BERSERK(137438953472L, "Berserk"),
    ZOMBIE(274877906944L, "Zombie"),
    UNKNOWN_39(549755813888L, "Unknown 39");

    private final long bitValue;
    private final String displayName;

    // Static array for fast lookup by bit index
    private static final Map<Long, StatusEffect> BY_BIT_VALUE = new HashMap<>(); // 0-39
    
    static {
        for (StatusEffect effect : values()) {
            BY_BIT_VALUE.put(effect.bitValue, effect);
        }
    }

    /**
     * Get StatusEffect by bit index using array lookup for O(1) performance
     */
    public static StatusEffect fromBit(long bit) {
        if (bit >= 0 && bit < UNKNOWN_39.bitValue && BY_BIT_VALUE.get(bit) != null) {
            return BY_BIT_VALUE.get(bit);
        }
        throw new IllegalArgumentException("Invalid bit index: " + bit);
    }

    /**
     * Check if this is a known status effect (not unknown)
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 