package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.TargetFlag;
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
     * Set a specific target flag using enum
     */
    public void setFlag(TargetFlag flag, boolean value) {
        setBit(flag.getBitIndex(), value);
    }

    /**
     * Get a specific target flag using enum
     */
    public boolean hasFlag(TargetFlag flag) {
        return getBit(flag.getBitIndex());
    }

    /**
     * Get list of all active target flags
     */
    public List<TargetFlag> getActiveFlags() {
        return getActiveBits().stream()
                .map(TargetFlag::fromBitIndex)
                .toList();
    }

    /**
     * Get list of only known active target flags
     */
    public List<TargetFlag> getKnownActiveFlags() {
        return getActiveFlags().stream()
                .filter(TargetFlag::isKnown)
                .toList();
    }

    /**
     * Convenient properties for common target flags
     */
    public boolean isDead() { return hasFlag(TargetFlag.DEAD); }
    public void setDead(boolean value) { setFlag(TargetFlag.DEAD, value); }

    public boolean isSingle() { return hasFlag(TargetFlag.SINGLE); }
    public void setSingle(boolean value) { setFlag(TargetFlag.SINGLE, value); }

    public boolean isSingleSide() { return hasFlag(TargetFlag.SINGLE_SIDE); }
    public void setSingleSide(boolean value) { setFlag(TargetFlag.SINGLE_SIDE, value); }

    public boolean isEnemy() { return hasFlag(TargetFlag.ENEMY); }
    public void setEnemy(boolean value) { setFlag(TargetFlag.ENEMY, value); }

    @Override
    public String toString() {
        return "TargetFlags{activeFlags=" + getKnownActiveFlags() + "}";
    }
} 