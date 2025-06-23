package com.ff8.domain.entities;

import lombok.Value;
import lombok.With;

/**
 * Represents junction stat bonuses using Lombok for immutability.
 * All values are immutable for thread safety.
 */
@Value
@With
public class JunctionStats implements BinarySerializable {
    int hp;
    int str;
    int vit;
    int mag;
    int spr;
    int spd;
    int eva;
    int hit;
    int luck;

    /**
     * Create junction stats with validation
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
     * Create empty junction stats
     */
    public static JunctionStats empty() {
        return new JunctionStats(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Create from byte array (9 bytes)
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

    @Override
    public int getBinarySize() {
        return 9;
    }

    @Override
    public boolean hasData() {
        return hasAnyBonuses();
    }

    /**
     * Check if any stats are non-zero
     */
    public boolean hasAnyBonuses() {
        return hp > 0 || str > 0 || vit > 0 || mag > 0 || 
               spr > 0 || spd > 0 || eva > 0 || hit > 0 || luck > 0;
    }

    /**
     * Get total stat bonus points
     */
    public int getTotalBonuses() {
        return hp + str + vit + mag + spr + spd + eva + hit + luck;
    }

    /**
     * Create a copy with modified HP
     */
    public JunctionStats withHp(int newHp) {
        return new JunctionStats(newHp, str, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified STR
     */
    public JunctionStats withStr(int newStr) {
        return new JunctionStats(hp, newStr, vit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified VIT
     */
    public JunctionStats withVit(int newVit) {
        return new JunctionStats(hp, str, newVit, mag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified MAG
     */
    public JunctionStats withMag(int newMag) {
        return new JunctionStats(hp, str, vit, newMag, spr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified SPR
     */
    public JunctionStats withSpr(int newSpr) {
        return new JunctionStats(hp, str, vit, mag, newSpr, spd, eva, hit, luck);
    }

    /**
     * Create a copy with modified SPD
     */
    public JunctionStats withSpd(int newSpd) {
        return new JunctionStats(hp, str, vit, mag, spr, newSpd, eva, hit, luck);
    }

    /**
     * Create a copy with modified EVA
     */
    public JunctionStats withEva(int newEva) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, newEva, hit, luck);
    }

    /**
     * Create a copy with modified HIT
     */
    public JunctionStats withHit(int newHit) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, eva, newHit, luck);
    }

    /**
     * Create a copy with modified LUCK
     */
    public JunctionStats withLuck(int newLuck) {
        return new JunctionStats(hp, str, vit, mag, spr, spd, eva, hit, newLuck);
    }
} 