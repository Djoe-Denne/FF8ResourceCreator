package com.ff8.domain.entities.enums;

/**
 * Enumeration of Guardian Forces (GFs) in FF8.
 */
public enum GF {
    QUEZACOLT(0, "Quezacolt"),
    SHIVA(1, "Shiva"),
    IFRIT(2, "Ifrit"),
    SIREN(3, "Siren"),
    BROTHERS(4, "Brothers"),
    DIABLOS(5, "Diablos"),
    CARBUNCLE(6, "Carbuncle"),
    LEVIATHAN(7, "Leviathan"),
    PANDEMONA(8, "Pandemona"),
    CERBERUS(9, "Cerberus"),
    ALEXANDER(10, "Alexander"),
    DOOMTRAIN(11, "Doomtrain"),
    BAHAMUT(12, "Bahamut"),
    CACTUAR(13, "Cactuar"),
    TONBERRY(14, "Tonberry"),
    EDEN(15, "Eden");

    private final int index;
    private final String displayName;

    // Static array for fast lookup by index
    private static final GF[] BY_INDEX = new GF[16]; // 0-15
    
    static {
        for (GF gf : values()) {
            BY_INDEX[gf.index] = gf;
        }
    }

    GF(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get GF by index using array lookup for O(1) performance
     */
    public static GF fromIndex(int index) {
        if (index >= 0 && index < BY_INDEX.length && BY_INDEX[index] != null) {
            return BY_INDEX[index];
        }
        throw new IllegalArgumentException("Invalid GF index: " + index);
    }
} 