package com.ff8.domain.entities;

import java.util.List;

/**
 * Represents target flags using 8 bits.
 * Uses Java 21 features and common BitSet functionality.
 */
public final class TargetFlags extends AbstractBitFlags implements BinarySerializable {
    private static final int TOTAL_BITS = 8;

    public TargetFlags() {
        super(TOTAL_BITS);
    }

    public TargetFlags(int flagByte) {
        super(TOTAL_BITS, BinarySerializationUtils.byteTobitSet(flagByte, TOTAL_BITS));
    }

    public TargetFlags(TargetFlags other) {
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
     * Target flag properties based on FF8 specifications
     */
    public boolean isDead() { return getBit(0); }
    public void setDead(boolean value) { setBit(0, value); }

    public boolean isBit1() { return getBit(1); }
    public void setBit1(boolean value) { setBit(1, value); }

    public boolean isBit2() { return getBit(2); }
    public void setBit2(boolean value) { setBit(2, value); }

    public boolean isSingleSide() { return getBit(3); }
    public void setSingleSide(boolean value) { setBit(3, value); }

    public boolean isSingle() { return getBit(4); }
    public void setSingle(boolean value) { setBit(4, value); }

    public boolean isBit5() { return getBit(5); }
    public void setBit5(boolean value) { setBit(5, value); }

    public boolean isEnemy() { return getBit(6); }
    public void setEnemy(boolean value) { setBit(6, value); }

    public boolean isBit7() { return getBit(7); }
    public void setBit7(boolean value) { setBit(7, value); }

    /**
     * Get list of active target types as indices
     */
    public List<Integer> getActiveTargets() {
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
        return "TargetFlags{bits=" + getActiveTargets() + 
               ", dead=" + isDead() + 
               ", single=" + isSingle() + 
               ", enemy=" + isEnemy() + 
               ", singleSide=" + isSingleSide() + "}";
    }
} 