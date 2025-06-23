package com.ff8.domain.entities;

import java.util.List;

/**
 * Represents attack flags using 8 bits.
 * Uses Java 21 features and common BitSet functionality.
 */
public final class AttackFlags extends AbstractBitFlags implements BinarySerializable {
    private static final int TOTAL_BITS = 8;

    public AttackFlags() {
        super(TOTAL_BITS);
    }

    public AttackFlags(int flagByte) {
        super(TOTAL_BITS, BinarySerializationUtils.byteTobitSet(flagByte, TOTAL_BITS));
    }

    public AttackFlags(AttackFlags other) {
        super(TOTAL_BITS, other.copyBitSet());
    }

    @Override
    public byte[] toBytes() {
        return new byte[] { (byte) toByte() };
    }

    @Override
    public int getBinarySize() {
        return 1;
    }

    /**
     * Convert back to byte for serialization
     */
    public int toByte() {
        return BinarySerializationUtils.bitSetToByte(flagBits, TOTAL_BITS);
    }

    /**
     * Attack flag properties based on FF8 specifications
     */
    public boolean isShelled() { return getBit(0); }
    public void setShelled(boolean value) { setBit(0, value); }

    public boolean isReflected() { return getBit(1); }
    public void setReflected(boolean value) { setBit(1, value); }

    public boolean isBit2() { return getBit(2); }
    public void setBit2(boolean value) { setBit(2, value); }

    public boolean isBreakDamageLimit() { return getBit(3); }
    public void setBreakDamageLimit(boolean value) { setBit(3, value); }

    public boolean isRevive() { return getBit(4); }
    public void setRevive(boolean value) { setBit(4, value); }

    public boolean isBit5() { return getBit(5); }
    public void setBit5(boolean value) { setBit(5, value); }

    public boolean isBit6() { return getBit(6); }
    public void setBit6(boolean value) { setBit(6, value); }

    public boolean isBit7() { return getBit(7); }
    public void setBit7(boolean value) { setBit(7, value); }

    /**
     * Get list of active attack flags as indices
     */
    public List<Integer> getActiveFlags() {
        return getActiveBits();
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

    @Override
    public String toString() {
        return "AttackFlags{bits=" + getActiveFlags() + 
               ", shelled=" + isShelled() + 
               ", reflected=" + isReflected() + 
               ", breakDamageLimit=" + isBreakDamageLimit() + 
               ", revive=" + isRevive() + "}";
    }
} 