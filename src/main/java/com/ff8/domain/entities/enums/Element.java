package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of elemental types in FF8.
 */
@Getter
@RequiredArgsConstructor
public enum Element {
    NONE(0, "None"),
    FIRE(1, "Fire"),
    ICE(2, "Ice"),
    THUNDER(4, "Thunder"),
    EARTH(8, "Earth"),
    POISON(16, "Poison"),
    WIND(32, "Wind"),
    WATER(64, "Water"),
    HOLY(128, "Holy");

    private final int value;
    private final String displayName;

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