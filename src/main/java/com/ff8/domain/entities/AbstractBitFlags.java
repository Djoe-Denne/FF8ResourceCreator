package com.ff8.domain.entities;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Abstract base class for bit flag operations.
 * Provides common functionality for classes that manage BitSet data.
 */
public abstract class AbstractBitFlags {
    protected final BitSet flagBits;
    private final int totalBits;

    protected AbstractBitFlags(int totalBits) {
        this.totalBits = totalBits;
        this.flagBits = new BitSet(totalBits);
    }

    protected AbstractBitFlags(int totalBits, BitSet existingBits) {
        this.totalBits = totalBits;
        this.flagBits = (BitSet) existingBits.clone();
    }

    /**
     * Set a specific bit
     */
    public void setBit(int index, boolean value) {
        validateBitIndex(index);
        flagBits.set(index, value);
    }

    /**
     * Get a specific bit
     */
    public boolean getBit(int index) {
        validateBitIndex(index);
        return flagBits.get(index);
    }

    /**
     * Get list of active bit indices
     */
    public List<Integer> getActiveBits() {
        return IntStream.range(0, totalBits)
                .filter(flagBits::get)
                .boxed()
                .toList();
    }

    /**
     * Check if any flags are set
     */
    public boolean hasAnyFlags() {
        return !flagBits.isEmpty();
    }

    /**
     * Clear all flags
     */
    public void clear() {
        flagBits.clear();
    }

    /**
     * Get total number of bits
     */
    public int getTotalBits() {
        return totalBits;
    }

    /**
     * Create a defensive copy of the BitSet
     */
    protected BitSet copyBitSet() {
        return (BitSet) flagBits.clone();
    }

    private void validateBitIndex(int index) {
        if (index < 0 || index >= totalBits) {
            throw new IllegalArgumentException("Bit index must be between 0 and " + (totalBits - 1));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBitFlags that = (AbstractBitFlags) o;
        return totalBits == that.totalBits && Objects.equals(flagBits, that.flagBits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagBits, totalBits);
    }
} 