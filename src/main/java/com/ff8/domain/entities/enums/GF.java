package com.ff8.domain.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of Guardian Forces (GFs) in Final Fantasy VIII.
 * 
 * <p>Guardian Forces are summonable creatures that provide various benefits
 * to characters in FF8. They can be junctioned to characters to provide
 * stat bonuses, abilities, and access to powerful summon attacks.</p>
 * 
 * <p>GF compatibility affects:</p>
 * <ul>
 *   <li>AP (Ability Points) gain rate when learning abilities</li>
 *   <li>How quickly characters can summon the GF in battle</li>
 *   <li>The effectiveness of GF-based abilities</li>
 * </ul>
 * 
 * <p>Each magic spell has compatibility values for all 16 GFs, stored as
 * a 16-byte array in the binary format. This enum provides the mapping
 * between array indices and GF names.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum GF {
    /** Lightning-elemental GF - first GF typically obtained */
    QUEZACOLT(0, "Quezacolt"),
    
    /** Ice-elemental GF - early game summon */
    SHIVA(1, "Shiva"),
    
    /** Fire-elemental GF - early game summon */
    IFRIT(2, "Ifrit"),
    
    /** Support GF with healing and status abilities */
    SIREN(3, "Siren"),
    
    /** Earth-elemental GF - twin summons */
    BROTHERS(4, "Brothers"),
    
    /** Dark-elemental GF with time manipulation abilities */
    DIABLOS(5, "Diablos"),
    
    /** Support GF with protective abilities */
    CARBUNCLE(6, "Carbuncle"),
    
    /** Water-elemental GF - powerful summon */
    LEVIATHAN(7, "Leviathan"),
    
    /** Wind-elemental GF with devastating attacks */
    PANDEMONA(8, "Pandemona"),
    
    /** Multi-headed GF with status abilities */
    CERBERUS(9, "Cerberus"),
    
    /** Holy-elemental GF - powerful defensive summon */
    ALEXANDER(10, "Alexander"),
    
    /** Poison-elemental GF with status attacks */
    DOOMTRAIN(11, "Doomtrain"),
    
    /** Dragon GF - ultimate non-elemental summon */
    BAHAMUT(12, "Bahamut"),
    
    /** Cactus GF with fixed damage attacks */
    CACTUAR(13, "Cactuar"),
    
    /** Knife-wielding GF with special abilities */
    TONBERRY(14, "Tonberry"),
    
    /** Ultimate GF - most powerful summon in the game */
    EDEN(15, "Eden");

    /** The array index used in binary compatibility data */
    private final int index;
    
    /** The display name for user interfaces */
    private final String displayName;

    // Static array for fast lookup by index
    private static final GF[] BY_INDEX = new GF[16]; // 0-15
    
    static {
        for (GF gf : values()) {
            BY_INDEX[gf.index] = gf;
        }
    }

    /**
     * Gets a GF by its array index using fast array lookup.
     * 
     * <p>This method provides O(1) lookup performance for converting array indices
     * from the binary GF compatibility data to enum values. Each magic spell
     * stores compatibility values as a 16-byte array where each byte corresponds
     * to a specific GF.</p>
     * 
     * @param index the array index (0-15) to look up
     * @return the corresponding GF
     * @throws IllegalArgumentException if the index is not valid
     */
    public static GF fromIndex(int index) {
        if (index >= 0 && index < BY_INDEX.length && BY_INDEX[index] != null) {
            return BY_INDEX[index];
        }
        throw new IllegalArgumentException("Invalid GF index: " + index);
    }
} 