package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.StatusEffect;
import lombok.EqualsAndHashCode;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents a comprehensive set of status effects using FF8's 48-bit (6-byte) format.
 * 
 * <p>This class manages the complex status effect system from Final Fantasy VIII, where
 * each spell can apply multiple status effects simultaneously. The implementation uses
 * a 48-bit representation (stored as 6 bytes) where each bit corresponds to a specific
 * status effect defined in the {@link StatusEffect} enum.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Exact binary compatibility with FF8's kernel.bin format</li>
 *   <li>Type-safe status effect manipulation using enums</li>
 *   <li>Efficient bit-level operations for performance</li>
 *   <li>Convenient accessor methods for common status effects</li>
 *   <li>Support for both known and unknown status effects</li>
 * </ul>
 * 
 * <p>The 48-bit structure is organized as:</p>
 * <ul>
 *   <li>Bits 0-31: DWORD (first 32 status effects)</li>
 *   <li>Bits 32-47: WORD (last 16 status effects)</li>
 * </ul>
 * 
 * <p>This class extends {@link AbstractBitFlags} to inherit common bit manipulation
 * functionality while providing status effect-specific operations.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
public final class StatusEffectSet extends AbstractBitFlags implements BinarySerializable {
    
    /** Total number of bits used for status effects in FF8 */
    private static final int TOTAL_BITS = 48;

    /**
     * Creates an empty status effect set with no active effects.
     */
    public StatusEffectSet() {
        super(TOTAL_BITS);
    }

    /**
     * Creates a status effect set from an existing BitSet.
     * 
     * @param statusBits the BitSet containing status effect flags
     */
    public StatusEffectSet(BitSet statusBits) {
        super(TOTAL_BITS, statusBits);
    }

    /**
     * Creates a copy of an existing status effect set.
     * 
     * @param other the status effect set to copy
     */
    public StatusEffectSet(StatusEffectSet other) {
        super(TOTAL_BITS, other.copyBitSet());
    }

    /**
     * Creates a status effect set from binary data (DWORD + WORD format).
     * 
     * <p>This method reconstructs a status effect set from the binary format
     * used in FF8's kernel.bin file, where status effects are stored as a
     * 32-bit DWORD followed by a 16-bit WORD.</p>
     * 
     * @param dword the first 32 bits of status effects (bits 0-31)
     * @param word the last 16 bits of status effects (bits 32-47)
     * @return a new StatusEffectSet with the specified effects active
     */
    public static StatusEffectSet fromBinary(int dword, int word) {
        var statusSet = new StatusEffectSet();
        
        // Use utility methods for bit manipulation
        BinarySerializationUtils.dwordToBitSet(dword, statusSet.flagBits, 0);
        BinarySerializationUtils.wordToBitSet(word, statusSet.flagBits, 32);
        
        return statusSet;
    }

    /**
     * Converts the status effect set to a 6-byte array for binary serialization.
     * 
     * <p>The returned byte array follows FF8's little-endian format:</p>
     * <ul>
     *   <li>Bytes 0-3: DWORD containing bits 0-31</li>
     *   <li>Bytes 4-5: WORD containing bits 32-47</li>
     * </ul>
     * 
     * @return a 6-byte array representing all status effects
     */
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

    /**
     * Gets the binary size of this status effect set.
     * 
     * @return always returns 6 (bytes)
     */
    @Override
    public int getBinarySize() {
        return 6;
    }

    /**
     * Checks if this status effect set contains any active effects.
     * 
     * @return true if any status effects are active
     */
    @Override
    public boolean hasData() {
        return hasAnyStatus();
    }

    /**
     * Sets or clears a specific status effect.
     * 
     * <p>This method provides type-safe access to individual status effects
     * using the {@link StatusEffect} enum.</p>
     * 
     * @param status the status effect to modify
     * @param value true to activate the effect, false to deactivate
     */
    public void setStatus(StatusEffect status, boolean value) {
        setBit(status.getBitIndex(), value);
    }

    /**
     * Checks if a specific status effect is active.
     * 
     * @param status the status effect to check
     * @return true if the status effect is active
     */
    public boolean hasStatus(StatusEffect status) {
        return getBit(status.getBitIndex());
    }

    /**
     * Gets a list of all currently active status effects.
     * 
     * <p>This includes both known and unknown status effects. The list
     * is ordered by bit index (0-47).</p>
     * 
     * @return an immutable list of all active status effects
     */
    public List<StatusEffect> getActiveStatuses() {
        return IntStream.range(0, TOTAL_BITS)
                .filter(flagBits::get)
                .mapToObj(StatusEffect::fromBitIndex)
                .toList();
    }

    /**
     * Gets a list of only known active status effects.
     * 
     * <p>This method filters out unknown or unused status effects,
     * returning only those with well-understood game mechanics.</p>
     * 
     * @return an immutable list of known active status effects
     */
    public List<StatusEffect> getKnownActiveStatuses() {
        return getActiveStatuses().stream()
                .filter(StatusEffect::isKnown)
                .toList();
    }

    /**
     * Converts the first 32 bits to a DWORD for binary serialization.
     * 
     * @return a 32-bit integer representing bits 0-31
     */
    public int toDword() {
        return BinarySerializationUtils.bitSetToDword(flagBits);
    }

    /**
     * Converts bits 32-47 to a WORD for binary serialization.
     * 
     * @return a 16-bit integer representing bits 32-47
     */
    public int toWord() {
        return BinarySerializationUtils.bitSetToWord(flagBits, 32);
    }

    // Convenient accessor methods for common status effects
    
    /** Checks if Sleep status is active */
    public boolean isSleep() { return hasStatus(StatusEffect.SLEEP); }
    /** Sets or clears Sleep status */
    public void setSleep(boolean value) { setStatus(StatusEffect.SLEEP, value); }

    /** Checks if Haste status is active */
    public boolean isHaste() { return hasStatus(StatusEffect.HASTE); }
    /** Sets or clears Haste status */
    public void setHaste(boolean value) { setStatus(StatusEffect.HASTE, value); }

    /** Checks if Slow status is active */
    public boolean isSlow() { return hasStatus(StatusEffect.SLOW); }
    /** Sets or clears Slow status */
    public void setSlow(boolean value) { setStatus(StatusEffect.SLOW, value); }

    /** Checks if Stop status is active */
    public boolean isStop() { return hasStatus(StatusEffect.STOP); }
    /** Sets or clears Stop status */
    public void setStop(boolean value) { setStatus(StatusEffect.STOP, value); }

    /** Checks if Regen status is active */
    public boolean isRegen() { return hasStatus(StatusEffect.REGEN); }
    /** Sets or clears Regen status */
    public void setRegen(boolean value) { setStatus(StatusEffect.REGEN, value); }

    /** Checks if Protect status is active */
    public boolean isProtect() { return hasStatus(StatusEffect.PROTECT); }
    /** Sets or clears Protect status */
    public void setProtect(boolean value) { setStatus(StatusEffect.PROTECT, value); }

    /** Checks if Shell status is active */
    public boolean isShell() { return hasStatus(StatusEffect.SHELL); }
    /** Sets or clears Shell status */
    public void setShell(boolean value) { setStatus(StatusEffect.SHELL, value); }

    /** Checks if Reflect status is active */
    public boolean isReflect() { return hasStatus(StatusEffect.REFLECT); }
    /** Sets or clears Reflect status */
    public void setReflect(boolean value) { setStatus(StatusEffect.REFLECT, value); }

    /** Checks if Death status is active */
    public boolean isDeath() { return hasStatus(StatusEffect.DEATH); }
    /** Sets or clears Death status */
    public void setDeath(boolean value) { setStatus(StatusEffect.DEATH, value); }

    /** Checks if Poison status is active */
    public boolean isPoison() { return hasStatus(StatusEffect.POISON); }
    /** Sets or clears Poison status */
    public void setPoison(boolean value) { setStatus(StatusEffect.POISON, value); }

    /** Checks if Petrify status is active */
    public boolean isPetrify() { return hasStatus(StatusEffect.PETRIFY); }
    /** Sets or clears Petrify status */
    public void setPetrify(boolean value) { setStatus(StatusEffect.PETRIFY, value); }

    /** Checks if Zombie status is active */
    public boolean isZombie() { return hasStatus(StatusEffect.ZOMBIE); }
    /** Sets or clears Zombie status */
    public void setZombie(boolean value) { setStatus(StatusEffect.ZOMBIE, value); }

    /**
     * Checks if any status effects are currently active.
     * 
     * <p>This is a convenience method equivalent to checking if the
     * bit set has any flags set.</p>
     * 
     * @return true if at least one status effect is active
     */
    public boolean hasAnyStatus() {
        return hasAnyFlags();
    }

    /**
     * Provides a string representation showing known active status effects.
     * 
     * @return a string representation for debugging and logging
     */
    @Override
    public String toString() {
        return "StatusEffectSet{activeStatuses=" + getKnownActiveStatuses() + "}";
    }
} 