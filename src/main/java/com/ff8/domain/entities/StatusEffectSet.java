package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.StatusEffect;
import lombok.EqualsAndHashCode;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a set of status effects using 48 bits (6 bytes).
 * Uses Java 21 features for modern implementation.
 */
@EqualsAndHashCode(callSuper = true)
public final class StatusEffectSet extends AbstractBitFlags implements BinarySerializable {
    private static final int TOTAL_BITS = 48;

    public StatusEffectSet() {
        super(TOTAL_BITS);
    }

    public StatusEffectSet(BitSet statusBits) {
        super(TOTAL_BITS, statusBits);
    }

    public StatusEffectSet(StatusEffectSet other) {
        super(TOTAL_BITS, other.copyBitSet());
    }

    /**
     * Create from 32-bit DWORD + 16-bit WORD
     */
    public static StatusEffectSet fromBinary(int dword, int word) {
        var statusSet = new StatusEffectSet();
        
        // Use utility methods for bit manipulation
        BinarySerializationUtils.dwordToBitSet(dword, statusSet.flagBits, 0);
        BinarySerializationUtils.wordToBitSet(word, statusSet.flagBits, 32);
        
        return statusSet;
    }

    @Override
    public byte[] toBytes() {
        var bytes = new byte[6];
        
        // Convert to DWORD + WORD format
        int dword = toDword();
        int word = toWord();
        
        // Write as little-endian
        bytes[0] = (byte) (dword & 0xFF);
        bytes[1] = (byte) ((dword >> 8) & 0xFF);
        bytes[2] = (byte) ((dword >> 16) & 0xFF);
        bytes[3] = (byte) ((dword >> 24) & 0xFF);
        bytes[4] = (byte) (word & 0xFF);
        bytes[5] = (byte) ((word >> 8) & 0xFF);
        
        return bytes;
    }

    @Override
    public int getBinarySize() {
        return 6;
    }

    @Override
    public boolean hasData() {
        return hasAnyStatus();
    }

    /**
     * Set a specific status effect using enum
     */
    public void setStatus(StatusEffect status, boolean value) {
        setBit(status.getBitIndex(), value);
    }

    /**
     * Get a specific status effect using enum
     */
    public boolean hasStatus(StatusEffect status) {
        return getBit(status.getBitIndex());
    }

    /**
     * Get list of all active status effects
     */
    public List<StatusEffect> getActiveStatuses() {
        return IntStream.range(0, TOTAL_BITS)
                .filter(flagBits::get)
                .mapToObj(StatusEffect::fromBitIndex)
                .toList();
    }

    /**
     * Get list of only known active status effects
     */
    public List<StatusEffect> getKnownActiveStatuses() {
        return getActiveStatuses().stream()
                .filter(StatusEffect::isKnown)
                .toList();
    }

    /**
     * Convert to 32-bit DWORD (bits 0-31)
     */
    public int toDword() {
        return BinarySerializationUtils.bitSetToDword(flagBits);
    }

    /**
     * Convert to 16-bit WORD (bits 32-47)
     */
    public int toWord() {
        return BinarySerializationUtils.bitSetToWord(flagBits, 32);
    }

    /**
     * Convenient properties for common status effects
     */
    public boolean isSleep() { return hasStatus(StatusEffect.SLEEP); }
    public void setSleep(boolean value) { setStatus(StatusEffect.SLEEP, value); }

    public boolean isHaste() { return hasStatus(StatusEffect.HASTE); }
    public void setHaste(boolean value) { setStatus(StatusEffect.HASTE, value); }

    public boolean isSlow() { return hasStatus(StatusEffect.SLOW); }
    public void setSlow(boolean value) { setStatus(StatusEffect.SLOW, value); }

    public boolean isStop() { return hasStatus(StatusEffect.STOP); }
    public void setStop(boolean value) { setStatus(StatusEffect.STOP, value); }

    public boolean isRegen() { return hasStatus(StatusEffect.REGEN); }
    public void setRegen(boolean value) { setStatus(StatusEffect.REGEN, value); }

    public boolean isProtect() { return hasStatus(StatusEffect.PROTECT); }
    public void setProtect(boolean value) { setStatus(StatusEffect.PROTECT, value); }

    public boolean isShell() { return hasStatus(StatusEffect.SHELL); }
    public void setShell(boolean value) { setStatus(StatusEffect.SHELL, value); }

    public boolean isReflect() { return hasStatus(StatusEffect.REFLECT); }
    public void setReflect(boolean value) { setStatus(StatusEffect.REFLECT, value); }

    public boolean isDeath() { return hasStatus(StatusEffect.DEATH); }
    public void setDeath(boolean value) { setStatus(StatusEffect.DEATH, value); }

    public boolean isPoison() { return hasStatus(StatusEffect.POISON); }
    public void setPoison(boolean value) { setStatus(StatusEffect.POISON, value); }

    public boolean isPetrify() { return hasStatus(StatusEffect.PETRIFY); }
    public void setPetrify(boolean value) { setStatus(StatusEffect.PETRIFY, value); }

    public boolean isZombie() { return hasStatus(StatusEffect.ZOMBIE); }
    public void setZombie(boolean value) { setStatus(StatusEffect.ZOMBIE, value); }

    /**
     * Check if any status effects are active
     */
    public boolean hasAnyStatus() {
        return hasAnyFlags();
    }

    @Override
    public String toString() {
        return "StatusEffectSet{activeStatuses=" + getKnownActiveStatuses() + "}";
    }
} 