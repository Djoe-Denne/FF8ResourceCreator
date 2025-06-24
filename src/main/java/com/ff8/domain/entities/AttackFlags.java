package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.AttackFlag;
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
     * Set a specific attack flag using enum
     */
    public void setFlag(AttackFlag flag, boolean value) {
        setBit(flag.getBitIndex(), value);
    }

    /**
     * Get a specific attack flag using enum
     */
    public boolean hasFlag(AttackFlag flag) {
        return getBit(flag.getBitIndex());
    }

    /**
     * Get list of all active attack flags
     */
    public List<AttackFlag> getActiveFlags() {
        return getActiveBits().stream()
                .map(AttackFlag::fromBitIndex)
                .toList();
    }

    /**
     * Get list of only known active attack flags
     */
    public List<AttackFlag> getKnownActiveFlags() {
        return getActiveFlags().stream()
                .filter(AttackFlag::isKnown)
                .toList();
    }

    /**
     * Convenient properties for common attack flags
     */
    public boolean isShelled() { return hasFlag(AttackFlag.SHELLED); }
    public void setShelled(boolean value) { setFlag(AttackFlag.SHELLED, value); }

    public boolean isReflected() { return hasFlag(AttackFlag.REFLECTED); }
    public void setReflected(boolean value) { setFlag(AttackFlag.REFLECTED, value); }

    public boolean isBreakDamageLimit() { return hasFlag(AttackFlag.BREAK_DAMAGE_LIMIT); }
    public void setBreakDamageLimit(boolean value) { setFlag(AttackFlag.BREAK_DAMAGE_LIMIT, value); }

    public boolean isRevive() { return hasFlag(AttackFlag.REVIVE); }
    public void setRevive(boolean value) { setFlag(AttackFlag.REVIVE, value); }

    @Override
    public String toString() {
        return "AttackFlags{activeFlags=" + getKnownActiveFlags() + "}";
    }
} 