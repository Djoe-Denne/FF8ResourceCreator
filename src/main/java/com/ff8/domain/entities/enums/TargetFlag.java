package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of target flags in FF8.
 */
@Getter
@RequiredArgsConstructor
public enum TargetFlag {
    DEAD(0, "Dead"),
    UNKNOWN_1(1, "Unknown 1"),
    UNKNOWN_2(2, "Unknown 2"),
    SINGLE_SIDE(3, "Single Side"),
    SINGLE(4, "Single"),
    UNKNOWN_5(5, "Unknown 5"),
    ENEMY(6, "Enemy"),
    UNKNOWN_7(7, "Unknown 7");

    private final int bitIndex;
    private final String displayName;

    // Static array for fast lookup by bit index
    private static final TargetFlag[] BY_BIT_INDEX = new TargetFlag[8]; // 0-7
    
    static {
        for (TargetFlag flag : values()) {
            BY_BIT_INDEX[flag.bitIndex] = flag;
        }
    }

    /**
     * Get TargetFlag by bit index using array lookup for O(1) performance
     */
    public static TargetFlag fromBitIndex(int bitIndex) {
        if (bitIndex >= 0 && bitIndex < BY_BIT_INDEX.length && BY_BIT_INDEX[bitIndex] != null) {
            return BY_BIT_INDEX[bitIndex];
        }
        throw new IllegalArgumentException("Invalid bit index: " + bitIndex);
    }

    /**
     * Check if this is a known target flag (not unknown)
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 