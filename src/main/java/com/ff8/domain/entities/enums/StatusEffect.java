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
    DEFEND(19, "Defend"),
    CHARGED(20, "Charged"),
    BACK_ATTACK(21, "Back Attack"),
    VIT_0(22, "Vit 0"),
    ANGEL_WING(23, "Angel Wing"),
    HAS_MAGIC(24, "Has Magic"),
    SUMMON_GF(25, "Summon GF"),
    DEATH(26, "Death"),
    POISON(27, "Poison"),
    PETRIFY(28, "Petrify"),
    DARKNESS(29, "Darkness"),
    SILENCE(30, "Silence"),
    BERSERK(31, "Berserk"),
    
    // Bits 32-47 (WORD)
    ZOMBIE(32, "Zombie"),
    UNKNOWN_33(33, "Unknown 33"),
    UNKNOWN_34(34, "Unknown 34"),
    UNKNOWN_35(35, "Unknown 35"),
    UNKNOWN_36(36, "Unknown 36"),
    UNKNOWN_37(37, "Unknown 37"),
    UNKNOWN_38(38, "Unknown 38"),
    UNKNOWN_39(39, "Unknown 39"),
    UNKNOWN_40(40, "Unknown 40"),
    UNKNOWN_41(41, "Unknown 41"),
    UNKNOWN_42(42, "Unknown 42"),
    UNKNOWN_43(43, "Unknown 43"),
    UNKNOWN_44(44, "Unknown 44"),
    UNKNOWN_45(45, "Unknown 45"),
    UNKNOWN_46(46, "Unknown 46"),
    UNKNOWN_47(47, "Unknown 47");

    private final int bitIndex;
    private final String displayName;

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
     * Get StatusEffect by bit index using Java 21 pattern matching
     */
    public static StatusEffect fromBitIndex(int bitIndex) {
        return switch (bitIndex) {
            case 0 -> SLEEP;
            case 1 -> HASTE;
            case 2 -> SLOW;
            case 3 -> STOP;
            case 4 -> REGEN;
            case 5 -> PROTECT;
            case 6 -> SHELL;
            case 7 -> REFLECT;
            case 8 -> AURA;
            case 9 -> CURSE;
            case 10 -> DOOM;
            case 11 -> INVINCIBLE;
            case 12 -> PETRIFYING;
            case 13 -> FLOAT;
            case 14 -> CONFUSION;
            case 15 -> DRAIN;
            case 16 -> EJECT;
            case 17 -> DOUBLE;
            case 18 -> TRIPLE;
            case 19 -> DEFEND;
            case 20 -> CHARGED;
            case 21 -> BACK_ATTACK;
            case 22 -> VIT_0;
            case 23 -> ANGEL_WING;
            case 24 -> HAS_MAGIC;
            case 25 -> SUMMON_GF;
            case 26 -> DEATH;
            case 27 -> POISON;
            case 28 -> PETRIFY;
            case 29 -> DARKNESS;
            case 30 -> SILENCE;
            case 31 -> BERSERK;
            case 32 -> ZOMBIE;
            case 33 -> UNKNOWN_33;
            case 34 -> UNKNOWN_34;
            case 35 -> UNKNOWN_35;
            case 36 -> UNKNOWN_36;
            case 37 -> UNKNOWN_37;
            case 38 -> UNKNOWN_38;
            case 39 -> UNKNOWN_39;
            case 40 -> UNKNOWN_40;
            case 41 -> UNKNOWN_41;
            case 42 -> UNKNOWN_42;
            case 43 -> UNKNOWN_43;
            case 44 -> UNKNOWN_44;
            case 45 -> UNKNOWN_45;
            case 46 -> UNKNOWN_46;
            case 47 -> UNKNOWN_47;
            default -> throw new IllegalArgumentException("Invalid bit index: " + bitIndex);
        };
    }

    /**
     * Check if this is a known status effect (not unknown)
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 