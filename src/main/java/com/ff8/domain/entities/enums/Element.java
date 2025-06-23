package com.ff8.domain.entities.enums;

/**
 * Enumeration of elemental types in FF8.
 */
public enum Element {
    NONE(0, "None"),
    FIRE(1, "Fire"),
    ICE(2, "Ice"),
    THUNDER(3, "Thunder"),
    EARTH(4, "Earth"),
    POISON(5, "Poison"),
    WIND(6, "Wind"),
    WATER(7, "Water"),
    HOLY(8, "Holy");

    private final int value;
    private final String displayName;

    Element(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get Element by value using Java 21 pattern matching
     * Returns NONE for unknown values to handle unexpected data gracefully
     */
    public static Element fromValue(int value) {
        return switch (value) {
            case 0 -> NONE;
            case 1 -> FIRE;
            case 2 -> ICE;
            case 4 -> THUNDER;
            case 8 -> EARTH;
            case 16 -> POISON;
            case 32 -> WIND;
            case 64 -> WATER;
            case 128 -> HOLY;
            default -> {
                System.err.println("WARNING: Unknown element value " + value + ", using NONE");
                yield NONE;
            }
        };
    }
    
    /**
     * Get Element by value with strict validation (throws exception for unknown values)
     */
    public static Element fromValueStrict(int value) {
        return switch (value) {
            case 0 -> NONE;
            case 1 -> FIRE;
            case 2 -> ICE;
            case 4 -> THUNDER;
            case 8 -> EARTH;
            case 16 -> POISON;
            case 32 -> WIND;
            case 64 -> WATER;
            case 128 -> HOLY;
            default -> throw new IllegalArgumentException("Invalid element value: " + value);
        };
    }
} 