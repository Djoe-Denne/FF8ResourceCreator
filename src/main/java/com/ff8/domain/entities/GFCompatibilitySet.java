package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.GF;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.EnumMap;
import java.util.Map;

/**
 * Represents GF compatibility values using Java 21 features.
 * Each GF has a compatibility value from 0-255.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(callSuper = false, includeFieldNames = false)
public final class GFCompatibilitySet {
    @EqualsAndHashCode.Include
    private final Map<GF, Integer> compatibilities;

    public GFCompatibilitySet() {
        this.compatibilities = new EnumMap<>(GF.class);
        // Initialize all GFs with default compatibility (100 = no effect)
        for (GF gf : GF.values()) {
            compatibilities.put(gf, 100);
        }
    }

    public GFCompatibilitySet(Map<GF, Integer> compatibilities) {
        this.compatibilities = new EnumMap<>(GF.class);
        for (GF gf : GF.values()) {
            var value = compatibilities.getOrDefault(gf, 100);
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException("Compatibility for " + gf + " must be 0-255, got " + value);
            }
            this.compatibilities.put(gf, value);
        }
    }

    /**
     * Create from byte array (16 bytes, one per GF)
     */
    public static GFCompatibilitySet fromBytes(byte[] bytes, int offset) {
        if (bytes.length < offset + 16) {
            throw new IllegalArgumentException("Not enough bytes for GF compatibility");
        }

        var compatibilities = new EnumMap<GF, Integer>(GF.class);
        var gfs = GF.values();
        
        for (int i = 0; i < 16 && i < gfs.length; i++) {
            compatibilities.put(gfs[i], Byte.toUnsignedInt(bytes[offset + i]));
        }

        return new GFCompatibilitySet(compatibilities);
    }

    /**
     * Get compatibility value for a GF (0-255)
     */
    public int getCompatibility(GF gf) {
        return compatibilities.get(gf);
    }

    /**
     * Set compatibility value for a GF (0-255)
     */
    public void setCompatibility(GF gf, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Compatibility must be 0-255, got " + value);
        }
        compatibilities.put(gf, value);
    }

    /**
     * Get display value for UI (typically (100 - value) / 5)
     * Higher values = worse compatibility
     */
    public int getDisplayValue(GF gf) {
        var rawValue = getCompatibility(gf);
        return Math.max(0, (100 - rawValue) / 5);
    }

    /**
     * Set compatibility from display value
     */
    public void setDisplayValue(GF gf, int displayValue) {
        if (displayValue < 0 || displayValue > 20) {
            throw new IllegalArgumentException("Display value must be 0-20, got " + displayValue);
        }
        var rawValue = 100 - (displayValue * 5);
        setCompatibility(gf, rawValue);
    }

    /**
     * Convert to byte array for serialization
     */
    public byte[] toBytes() {
        var bytes = new byte[16];
        var gfs = GF.values();
        
        for (int i = 0; i < 16 && i < gfs.length; i++) {
            bytes[i] = (byte) compatibilities.get(gfs[i]).intValue();
        }
        
        return bytes;
    }

    /**
     * Get all compatibilities as read-only map
     */
    public Map<GF, Integer> getAllCompatibilities() {
        return Map.copyOf(compatibilities);
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
     * Reset all compatibilities to default (no effect)
     */
    public void resetToDefault() {
        for (GF gf : GF.values()) {
            compatibilities.put(gf, 100);
        }
    }

    /**
     * Create a copy of this compatibility set
     */
    public GFCompatibilitySet copy() {
        return new GFCompatibilitySet(compatibilities);
    }

    /**
     * Create a copy with modified compatibility for one GF
     */
    public GFCompatibilitySet withCompatibility(GF gf, int value) {
        var newCompatibilities = new EnumMap<>(compatibilities);
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Compatibility must be 0-255, got " + value);
        }
        newCompatibilities.put(gf, value);
        return new GFCompatibilitySet(newCompatibilities);
    }

    /**
     * Custom toString showing good compatibilities
     */
    public String toDisplayString() {
        var goodCompatibilities = getGoodCompatibilities();
        return "GFCompatibilitySet{goodCompatibilities=" + goodCompatibilities + "}";
    }
} 