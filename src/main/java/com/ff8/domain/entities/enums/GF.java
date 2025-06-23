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
     * Get GF by index using Java 21 pattern matching
     */
    public static GF fromIndex(int index) {
        return switch (index) {
            case 0 -> QUEZACOLT;
            case 1 -> SHIVA;
            case 2 -> IFRIT;
            case 3 -> SIREN;
            case 4 -> BROTHERS;
            case 5 -> DIABLOS;
            case 6 -> CARBUNCLE;
            case 7 -> LEVIATHAN;
            case 8 -> PANDEMONA;
            case 9 -> CERBERUS;
            case 10 -> ALEXANDER;
            case 11 -> DOOMTRAIN;
            case 12 -> BAHAMUT;
            case 13 -> CACTUAR;
            case 14 -> TONBERRY;
            case 15 -> EDEN;
            default -> throw new IllegalArgumentException("Invalid GF index: " + index);
        };
    }
} 