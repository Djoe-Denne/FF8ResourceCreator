package com.ff8.domain.entities;

import lombok.Value;
import lombok.With;

/**
 * Represents junction stat bonuses provided by equipping magic spells to characters.
 * 
 * <p>In Final Fantasy VIII, magic spells can be "junctioned" to characters to provide
 * stat bonuses. Each spell provides bonuses to the eight core character statistics,
 * with the magnitude of the bonus depending on the spell's properties and the number
 * of that spell in the character's inventory.</p>
 * 
 * <p>The junction system affects these character stats:</p>
 * <ul>
 *   <li><strong>HP:</strong> Hit Points - determines maximum health</li>
 *   <li><strong>STR:</strong> Strength - affects physical attack damage</li>
 *   <li><strong>VIT:</strong> Vitality - affects physical defense</li>
 *   <li><strong>MAG:</strong> Magic - affects magical attack power</li>
 *   <li><strong>SPR:</strong> Spirit - affects magical defense</li>
 *   <li><strong>SPD:</strong> Speed - affects action frequency and evasion</li>
 *   <li><strong>EVA:</strong> Evasion - affects physical dodge rate</li>
 *   <li><strong>HIT:</strong> Hit Rate - affects physical accuracy</li>
 *   <li><strong>LUCK:</strong> Luck - affects critical hit rate and other random events</li>
 * </ul>
 * 
 * <p>This class is immutable and uses Lombok for thread safety and clean code.
 * All values are stored as bytes (0-255) to match the binary format used in
 * FF8's kernel.bin file.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@Value
@With
public class JunctionStats implements BinarySerializable {
    
    /** Hit Points bonus (0-255) */
    int hp;
    
    /** Strength bonus (0-255) */
    int str;
    
    /** Vitality bonus (0-255) */
    int vit;
    
    /** Magic bonus (0-255) */
    int mag;
    
    /** Spirit bonus (0-255) */
    int spr;
    
    /** Speed bonus (0-255) */
    int spd;
    
    /** Evasion bonus (0-255) */
    int eva;
    
    /** Hit Rate bonus (0-255) */
    int hit;
    
    /** Luck bonus (0-255) */
    int luck;

    /**
     * Creates junction stats with validation of all values.
     * 
     * <p>All stat values must be within the valid range of 0-255 (byte range).
     * This constructor validates each value and throws an exception if any
     * value is outside the valid range.</p>
     * 
     * @param hp Hit Points bonus (0-255)
     * @param str Strength bonus (0-255)
     * @param vit Vitality bonus (0-255)
     * @param mag Magic bonus (0-255)
     * @param spr Spirit bonus (0-255)
     * @param spd Speed bonus (0-255)
     * @param eva Evasion bonus (0-255)
     * @param hit Hit Rate bonus (0-255)
     * @param luck Luck bonus (0-255)
     * @throws IllegalArgumentException if any value is outside 0-255 range
     */
    public JunctionStats(int hp, int str, int vit, int mag, int spr, int spd, int eva, int hit, int luck) {
        // Validation
        BinarySerializationUtils.validateByteValue(hp, "HP");
        BinarySerializationUtils.validateByteValue(str, "STR");
        BinarySerializationUtils.validateByteValue(vit, "VIT");
        BinarySerializationUtils.validateByteValue(mag, "MAG");
        BinarySerializationUtils.validateByteValue(spr, "SPR");
        BinarySerializationUtils.validateByteValue(spd, "SPD");
        BinarySerializationUtils.validateByteValue(eva, "EVA");
        BinarySerializationUtils.validateByteValue(hit, "HIT");
        BinarySerializationUtils.validateByteValue(luck, "LUCK");
        
        this.hp = hp;
        this.str = str;
        this.vit = vit;
        this.mag = mag;
        this.spr = spr;
        this.spd = spd;
        this.eva = eva;
        this.hit = hit;
        this.luck = luck;
    }

    /**
     * Creates empty junction stats with all bonuses set to zero.
     * 
     * <p>This is useful as a default value or starting point when creating
     * new magic spells that don't provide any stat bonuses.</p>
     * 
     * @return a new JunctionStats instance with all stats set to 0
     */
    public static JunctionStats empty() {
        return new JunctionStats(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Creates junction stats from a byte array (binary format).
     * 
     * <p>This method deserializes junction stats from the binary format used
     * in FF8's kernel.bin file. The format consists of 9 consecutive bytes,
     * one for each stat in order: HP, STR, VIT, MAG, SPR, SPD, EVA, HIT, LUCK.</p>
     * 
     * @param bytes the byte array containing junction data
     * @param offset the starting offset in the byte array
     * @return a new JunctionStats instance with values from the byte array
     * @throws IllegalArgumentException if there aren't enough bytes available
     */
    public static JunctionStats fromBytes(byte[] bytes, int offset) {
        BinarySerializationUtils.validateBytesAvailable(bytes, offset, 9, "junction stats");
        
        return new JunctionStats(
                BinarySerializationUtils.toUnsignedInt(bytes[offset]),     // HP
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 1]), // STR
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 2]), // VIT
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 3]), // MAG
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 4]), // SPR
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 5]), // SPD
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 6]), // EVA
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 7]), // HIT
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 8])  // LUCK
        );
    }

    /**
     * Converts junction stats to a 9-byte array for binary serialization.
     * 
     * <p>The returned byte array contains the stat values in order:
     * HP, STR, VIT, MAG, SPR, SPD, EVA, HIT, LUCK. Each value is stored
     * as a single unsigned byte (0-255).</p>
     * 
     * @return a 9-byte array representing all junction stats
     */
    @Override
    public byte[] toBytes() {
        return new byte[] {
                (byte) hp,
                (byte) str,
                (byte) vit,
                (byte) mag,
                (byte) spr,
                (byte) spd,
                (byte) eva,
                (byte) hit,
                (byte) luck
        };
    }

    /**
     * Gets the binary size of junction stats data.
     * 
     * @return always returns 9 (bytes)
     */
    @Override
    public int getBinarySize() {
        return 9;
    }

    /**
     * Checks if this junction stats object contains any meaningful data.
     * 
     * @return true if any stat bonus is non-zero
     */
    @Override
    public boolean hasData() {
        return hasAnyBonuses();
    }

    /**
     * Checks if any stat bonuses are greater than zero.
     * 
     * <p>This method is useful for determining if a magic spell provides
     * any junction benefits, or if it's purely for offensive/defensive effects.</p>
     * 
     * @return true if at least one stat has a bonus > 0
     */
    public boolean hasAnyBonuses() {
        return hp > 0 || str > 0 || vit > 0 || mag > 0 || 
               spr > 0 || spd > 0 || eva > 0 || hit > 0 || luck > 0;
    }

    /**
     * Calculates the total of all stat bonuses.
     * 
     * <p>This method sums all individual stat bonuses to provide an overall
     * measure of how powerful this junction configuration is. Higher totals
     * indicate more powerful junction effects.</p>
     * 
     * @return the sum of all stat bonuses
     */
    public int getTotalBonuses() {
        return hp + str + vit + mag + spr + spd + eva + hit + luck;
    }

    /**
     * Creates a copy with modified HP bonus.
     * 
     * <p>This method follows the immutable pattern by returning a new instance
     * rather than modifying the existing one.</p>
     * 
     * @param newHp the new HP bonus value (0-255)
     * @return a new JunctionStats instance with updated HP
     */
    public JunctionStats withHp(int newHp) {
        return new JunctionStats(newHp, str, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified STR bonus.
     * 
     * @param newStr the new STR bonus value (0-255)
     * @return a new JunctionStats instance with updated STR
     */
    public JunctionStats withStr(int newStr) {
        return new JunctionStats(hp, newStr, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified VIT bonus.
     * 
     * @param newVit the new VIT bonus value (0-255)
     * @return a new JunctionStats instance with updated VIT
     */
    public JunctionStats withVit(int newVit) {
        return new JunctionStats(hp, str, newVit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified MAG bonus.
     * 
     * @param newMag the new MAG bonus value (0-255)
     * @return a new JunctionStats instance with updated MAG
     */
    public JunctionStats withMag(int newMag) {
        return new JunctionStats(hp, str, vit, newMag, spr, spd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified SPR bonus.
     * 
     * @param newSpr the new SPR bonus value (0-255)
     * @return a new JunctionStats instance with updated SPR
     */
    public JunctionStats withSpr(int newSpr) {
        return new JunctionStats(hp, str, vit, mag, newSpr, spd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified SPD bonus.
     * 
     * @param newSpd the new SPD bonus value (0-255)
     * @return a new JunctionStats instance with updated SPD
     */
    public JunctionStats withSpd(int newSpd) {
        return new JunctionStats(hp, str, vit, mag, spr, newSpd, eva, hit, luck);
    }

    /**
     * Creates a copy with modified EVA bonus.
     * 
     * @param newEva the new EVA bonus value (0-255)
     * @return a new JunctionStats instance with updated EVA
     */
    public JunctionStats withEva(int newEva) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, newEva, hit, luck);
    }

    /**
     * Creates a copy with modified HIT bonus.
     * 
     * @param newHit the new HIT bonus value (0-255)
     * @return a new JunctionStats instance with updated HIT
     */
    public JunctionStats withHit(int newHit) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, eva, newHit, luck);
    }

    /**
     * Creates a copy with modified LUCK bonus.
     * 
     * @param newLuck the new LUCK bonus value (0-255)
     * @return a new JunctionStats instance with updated LUCK
     */
    public JunctionStats withLuck(int newLuck) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, eva, hit, newLuck);
    }
} 