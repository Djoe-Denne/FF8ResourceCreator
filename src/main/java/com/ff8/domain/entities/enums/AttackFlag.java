package com.ff8.domain.entities.enums;

/**
 * Enumeration of attack flags in FF8.
 */
public enum AttackFlag {
    SHELLED(0, "Shelled"),
    UNKNOWN_1(1, "Unknown 1"),
    UNKNOWN_2(2, "Unknown 2"),
    BREAK_DAMAGE_LIMIT(3, "Break Damage Limit"),
    REFLECTED(4, "Reflected"),
    UNKNOWN_5(5, "Unknown 5"),
    UNKNOWN_6(6, "Unknown 6"),
    REVIVE(7, "Revive");

    private final int bitIndex;
    private final String displayName;

    // Static array for fast lookup by bit index
    private static final AttackFlag[] BY_BIT_INDEX = new AttackFlag[8]; // 0-7
    
    static {
        for (AttackFlag flag : values()) {
            BY_BIT_INDEX[flag.bitIndex] = flag;
        }
    }

    AttackFlag(int bitIndex, String displayName) {
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
     * Get AttackFlag by bit index using array lookup for O(1) performance
     */
    public static AttackFlag fromBitIndex(int bitIndex) {
        if (bitIndex >= 0 && bitIndex < BY_BIT_INDEX.length && BY_BIT_INDEX[bitIndex] != null) {
            return BY_BIT_INDEX[bitIndex];
        }
        throw new IllegalArgumentException("Invalid bit index: " + bitIndex);
    }

    /**
     * Check if this is a known attack flag (not unknown)
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 