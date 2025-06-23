package com.ff8.application.dto;

import com.ff8.domain.entities.enums.GF;
import java.util.Map;
import java.util.EnumMap;

/**
 * Data Transfer Object for GF compatibility values.
 * Uses Java 21 record for immutability.
 */
public record GFCompatibilityDTO(
        Map<GF, Integer> compatibilities
) {
    /**
     * Validation constructor
     */
    public GFCompatibilityDTO {
        if (compatibilities == null) {
            throw new IllegalArgumentException("Compatibilities cannot be null");
        }
        
        // Validate all values are in range and create defensive copy
        var validatedMap = new EnumMap<GF, Integer>(GF.class);
        for (GF gf : GF.values()) {
            var value = compatibilities.getOrDefault(gf, 100); // Default to 100 (no effect)
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Compatibility for " + gf + " must be 0-255, got " + value);
            }
            validatedMap.put(gf, value);
        }
        compatibilities = Map.copyOf(validatedMap);
    }

    /**
     * Create default GF compatibility (all at 100 = no effect)
     */
    public static GFCompatibilityDTO createDefault() {
        var defaultCompatibilities = new EnumMap<GF, Integer>(GF.class);
        for (GF gf : GF.values()) {
            defaultCompatibilities.put(gf, 100);
        }
        return new GFCompatibilityDTO(defaultCompatibilities);
    }

    /**
     * Get compatibility value for a GF (0-255)
     */
    public int getCompatibilityRaw(GF gf) {
        return compatibilities.get(gf);
    }
    
    /**
     * Get compatibility value for a GF as double (for UI spinners)
     */
    public double getCompatibility(GF gf) {
        return getDisplayValue(gf) / 10.0; // Convert to -10.0 to +10.0 range
    }

    /**
     * Get display value for UI (typically (100 - value) / 5)
     * Higher raw values = worse compatibility, so we invert for display
     */
    public int getDisplayValue(GF gf) {
        var rawValue = getCompatibilityRaw(gf);
        return Math.max(0, (100 - rawValue) / 5);
    }

    /**
     * Create a copy with modified compatibility for one GF
     */
    public GFCompatibilityDTO withCompatibility(GF gf, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Compatibility must be 0-255, got " + value);
        }
        var newCompatibilities = new EnumMap<>(compatibilities);
        newCompatibilities.put(gf, value);
        return new GFCompatibilityDTO(newCompatibilities);
    }

    /**
     * Create a copy with modified display value for one GF
     */
    public GFCompatibilityDTO withDisplayValue(GF gf, int displayValue) {
        if (displayValue < 0 || displayValue > 20) {
            throw new IllegalArgumentException("Display value must be 0-20, got " + displayValue);
        }
        var rawValue = 100 - (displayValue * 5);
        return withCompatibility(gf, rawValue);
    }

    /**
     * Get GFs with good compatibility (display value > 0)
     */
    public Map<GF, Integer> getGoodCompatibilities() {
        return compatibilities.entrySet().stream()
                .filter(entry -> getDisplayValue(entry.getKey()) > 0)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        () -> new EnumMap<>(GF.class)
                ))
                .entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    /**
     * Check if has any good compatibilities
     */
    public boolean hasAnyGoodCompatibilities() {
        return compatibilities.entrySet().stream()
                .anyMatch(entry -> getDisplayValue(entry.getKey()) > 0);
    }

    /**
     * Get count of GFs with good compatibility
     */
    public int getGoodCompatibilityCount() {
        return (int) compatibilities.entrySet().stream()
                .filter(entry -> getDisplayValue(entry.getKey()) > 0)
                .count();
    }

    /**
     * Get best compatible GF
     */
    public GF getBestCompatibleGF() {
        return compatibilities.entrySet().stream()
                .min(Map.Entry.comparingByValue()) // Lower raw value = better compatibility
                .map(Map.Entry::getKey)
                .orElse(GF.QUEZACOLT);
    }

    /**
     * Get worst compatible GF
     */
    public GF getWorstCompatibleGF() {
        return compatibilities.entrySet().stream()
                .max(Map.Entry.comparingByValue()) // Higher raw value = worse compatibility
                .map(Map.Entry::getKey)
                .orElse(GF.QUEZACOLT);
    }

    /**
     * Get average compatibility display value
     */
    public double getAverageDisplayValue() {
        return compatibilities.values().stream()
                .mapToDouble(value -> getDisplayValueFromRaw(value))
                .average()
                .orElse(0.0);
    }

    /**
     * Helper to calculate display value from raw value
     */
    private int getDisplayValueFromRaw(int rawValue) {
        return Math.max(0, (100 - rawValue) / 5);
    }

    /**
     * Get formatted display string
     */
    public String getDisplayString() {
        if (!hasAnyGoodCompatibilities()) {
            return "No good GF compatibilities";
        }
        
        var goodOnes = getGoodCompatibilities();
        var gfNames = goodOnes.keySet().stream()
                .map(GF::getDisplayName)
                .limit(3) // Show first 3
                .toList();
        
        var result = String.join(", ", gfNames);
        if (goodOnes.size() > 3) {
            result += " (+" + (goodOnes.size() - 3) + " more)";
        }
        
        return result;
    }

    /**
     * Get detailed display string with values
     */
    public String getDetailedDisplayString() {
        if (!hasAnyGoodCompatibilities()) {
            return "No good GF compatibilities";
        }
        
        var goodOnes = getGoodCompatibilities();
        return goodOnes.entrySet().stream()
                .map(entry -> entry.getKey().getDisplayName() + " (" + 
                             getDisplayValue(entry.getKey()) + ")")
                .limit(5) // Show first 5
                .reduce((a, b) -> a + ", " + b)
                .orElse("No compatibilities");
    }

    /**
     * Create a copy with all compatibilities reset to default
     */
    public GFCompatibilityDTO resetToDefault() {
        return createDefault();
    }

    /**
     * Create a copy with random good compatibilities (for testing/examples)
     */
    public GFCompatibilityDTO withRandomGoodCompatibilities() {
        var random = new java.util.Random();
        var newCompatibilities = new EnumMap<GF, Integer>(GF.class);
        
        for (GF gf : GF.values()) {
            // 30% chance of good compatibility
            if (random.nextDouble() < 0.3) {
                var displayValue = random.nextInt(10) + 1; // 1-10
                var rawValue = 100 - (displayValue * 5);
                newCompatibilities.put(gf, rawValue);
            } else {
                newCompatibilities.put(gf, 100); // Default
            }
        }
        
        return new GFCompatibilityDTO(newCompatibilities);
    }
} 