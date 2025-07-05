package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of all possible status effects in Final Fantasy VIII.
 * 
 * <p>This enum represents the complete set of status effects that can be applied
 * to characters and enemies in FF8. The status effect system uses a 48-bit (6-byte)
 * representation where each status effect corresponds to a specific bit position.</p>
 * 
 * <p>Status effects are divided into categories:</p>
 * <ul>
 *   <li><strong>Beneficial:</strong> Haste, Protect, Shell, Regen, etc.</li>
 *   <li><strong>Detrimental:</strong> Poison, Sleep, Silence, etc.</li>
 *   <li><strong>Fatal:</strong> Death, Petrify</li>
 *   <li><strong>Special:</strong> Aura, Angel Wing, Float</li>
 *   <li><strong>Unknown:</strong> Effects whose purpose is not fully understood</li>
 * </ul>
 * 
 * <p>The enum provides efficient bit-index based lookup for working with the
 * binary status effect representation used in the kernel.bin format.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum StatusEffect {
    // Bits 0-31 (DWORD)
    /** Sleep status - character cannot act until awakened */
    SLEEP(0, "Sleep"),
    
    /** Haste status - increased action frequency */
    HASTE(1, "Haste"),
    
    /** Slow status - decreased action frequency */
    SLOW(2, "Slow"),
    
    /** Stop status - character cannot act at all */
    STOP(3, "Stop"),
    
    /** Regen status - gradually recovers HP over time */
    REGEN(4, "Regen"),
    
    /** Protect status - reduces physical damage taken */
    PROTECT(5, "Protect"),
    
    /** Shell status - reduces magical damage taken */
    SHELL(6, "Shell"),
    
    /** Reflect status - magic spells bounce back to caster */
    REFLECT(7, "Reflect"),
    
    /** Aura status - enables Limit Breaks */
    AURA(8, "Aura"),
    
    /** Curse status - prevents critical hits and limit breaks */
    CURSE(9, "Curse"),
    
    /** Doom status - countdown to death */
    DOOM(10, "Doom"),
    
    /** Invincible status - immune to all damage */
    INVINCIBLE(11, "Invincible"),
    
    /** Petrifying status - countdown to petrification */
    PETRIFYING(12, "Petrifying"),
    
    /** Float status - immune to earth-based attacks */
    FLOAT(13, "Float"),
    
    /** Confusion status - attacks random targets */
    CONFUSION(14, "Confusion"),
    
    /** Drain status - gradually loses HP */
    DRAIN(15, "Drain"),
    
    /** Eject status - removes character from battle */
    EJECT(16, "Eject"),
    
    /** Double status - cast magic twice */
    DOUBLE(17, "Double"),
    
    /** Triple status - cast magic three times */
    TRIPLE(18, "Triple"),
    
    /** Unknown status effect */
    UNKNOWN_19(19, "Unknown 19"),
    
    /** Unknown status effect */
    UNKNOWN_20(20, "Unknown 20"),
    
    /** Defend status - character is defending */
    DEFEND(21, "Defend"),
    
    /** Charged status - Guardian Force is summoning */
    CHARGED(22, "Charged"),
    
    /** Back Attack status - attack from behind */
    BACK_ATTACK(23, "Back Attack"),
    
    /** Vit 0 status - Vitality reduced to zero */
    VIT_0(24, "Vit 0"),
    
    /** Angel Wing status - Rinoa's special state */
    ANGEL_WING(25, "Angel Wing"),
    
    /** Unknown status effect */
    UNKNOWN_26(26, "Unknown 26"),
    
    /** Unknown status effect */
    UNKNOWN_27(27, "Unknown 27"),
    
    /** Unknown status effect */
    UNKNOWN_28(28, "Unknown 28"),
    
    /** Unknown status effect */
    UNKNOWN_29(29, "Unknown 29"),
    
    /** Has Magic status - character can use magic */
    HAS_MAGIC(30, "Has Magic"),
    
    /** Summon GF status - can summon Guardian Forces */
    SUMMON_GF(31, "Summon GF"),
    
    /** Death status - character is KO'd */
    DEATH(32, "Death"),
    
    /** Poison status - gradually loses HP */
    POISON(33, "Poison"),
    
    /** Petrify status - turned to stone, cannot act */
    PETRIFY(34, "Petrify"),
    
    /** Darkness status - physical accuracy reduced */
    DARKNESS(35, "Darkness"),
    
    /** Silence status - cannot cast magic */
    SILENCE(36, "Silence"),
    
    /** Berserk status - attacks automatically but uncontrollably */
    BERSERK(37, "Berserk"),
    
    /** Zombie status - undead, healed by poison/death attacks */
    ZOMBIE(38, "Zombie"),
    
    /** Unknown status effect */
    UNKNOWN_39(39, "Unknown 39");

    /** The bit index in the 48-bit status effect representation */
    private final int bitIndex;
    
    /** The display name for user interfaces */
    private final String displayName;

    // Static array for fast lookup by bit index
    private static final StatusEffect[] BY_BIT_INDEX = new StatusEffect[40]; // 0-39
    
    static {
        for (StatusEffect effect : values()) {
            BY_BIT_INDEX[effect.bitIndex] = effect;
        }
    }

    /**
     * Gets a StatusEffect by its bit index using fast array lookup.
     * 
     * <p>This method provides O(1) lookup performance for converting bit indices
     * from the binary status effect representation to enum values. This is essential
     * for parsing the 48-bit status effect data from kernel.bin files.</p>
     * 
     * @param bitIndex the bit index (0-39) to look up
     * @return the corresponding StatusEffect
     * @throws IllegalArgumentException if the bit index is not valid
     */
    public static StatusEffect fromBitIndex(int bitIndex) {
        if (bitIndex >= 0 && bitIndex < BY_BIT_INDEX.length && BY_BIT_INDEX[bitIndex] != null) {
            return BY_BIT_INDEX[bitIndex];
        }
        throw new IllegalArgumentException("Invalid bit index: " + bitIndex);
    }

    /**
     * Checks if this is a known status effect with understood game mechanics.
     * 
     * <p>Some status effects in the game's data are not fully understood or
     * may be unused. This method helps distinguish between known effects
     * with clear game mechanics and unknown/unused effects.</p>
     * 
     * @return true if this is a known status effect, false if unknown/unused
     */
    public boolean isKnown() {
        return !displayName.startsWith("Unknown");
    }
} 