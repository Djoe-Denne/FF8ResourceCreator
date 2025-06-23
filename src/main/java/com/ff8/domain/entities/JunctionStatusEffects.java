package com.ff8.domain.entities;

import lombok.Value;
import lombok.With;

/**
 * Represents junction status effect bonuses using Lombok for immutability.
 * Handles both attack and defense status effects with their values.
 */
@Value
@With
public class JunctionStatusEffects implements BinarySerializable {
    StatusEffectSet attackStatuses;
    int attackValue;
    StatusEffectSet defenseStatuses;
    int defenseValue;

    /**
     * Create junction status effects with validation
     */
    public JunctionStatusEffects(StatusEffectSet attackStatuses, int attackValue, StatusEffectSet defenseStatuses, int defenseValue) {
        if (attackStatuses == null) throw new IllegalArgumentException("Attack statuses cannot be null");
        if (defenseStatuses == null) throw new IllegalArgumentException("Defense statuses cannot be null");
        BinarySerializationUtils.validateByteValue(attackValue, "Attack value");
        BinarySerializationUtils.validateByteValue(defenseValue, "Defense value");
        
        this.attackStatuses = attackStatuses;
        this.attackValue = attackValue;
        this.defenseStatuses = defenseStatuses;
        this.defenseValue = defenseValue;
    }

    /**
     * Create empty junction status effects
     */
    public static JunctionStatusEffects empty() {
        return new JunctionStatusEffects(
                new StatusEffectSet(),
                0,
                new StatusEffectSet(),
                0
        );
    }

    /**
     * Create from byte array (6 bytes: attackValue, defenseValue, attackStatusWord, defenseStatusWord)
     */
    public static JunctionStatusEffects fromBytes(byte[] bytes, int offset) {
        BinarySerializationUtils.validateBytesAvailable(bytes, offset, 6, "junction status effects");

        var attackValue = BinarySerializationUtils.toUnsignedInt(bytes[offset]);
        var defenseValue = BinarySerializationUtils.toUnsignedInt(bytes[offset + 1]);
        
        // Read 16-bit words for status effects
        var attackStatusWord = Short.toUnsignedInt(BinarySerializationUtils.readShortLE(bytes, offset + 2));
        var defenseStatusWord = Short.toUnsignedInt(BinarySerializationUtils.readShortLE(bytes, offset + 4));

        // Map 16-bit status words to limited status sets
        var attackStatuses = parseJunctionStatusAttack(attackStatusWord);
        var defenseStatuses = parseJunctionStatusDefense(defenseStatusWord);

        return new JunctionStatusEffects(attackStatuses, attackValue, defenseStatuses, defenseValue);
    }

    /**
     * Parse junction attack status effects from 16-bit word
     * Maps specific bits to status effects (excluding bit 10)
     */
    private static StatusEffectSet parseJunctionStatusAttack(int statusWord) {
        var statusSet = new StatusEffectSet();
        
        // Known junction attack status mappings (skip bit 10)
        var statusBitMap = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12};
        
        for (int i = 0; i < statusBitMap.length && i < 16; i++) {
            if ((statusWord & (1 << i)) != 0) {
                statusSet.setBit(statusBitMap[i], true);
            }
        }
        
        return statusSet;
    }

    /**
     * Parse junction defense status effects from 16-bit word
     */
    private static StatusEffectSet parseJunctionStatusDefense(int statusWord) {
        var statusSet = new StatusEffectSet();
        
        // Known junction defense status mappings
        var statusBitMap = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        
        for (int i = 0; i < statusBitMap.length && i < 16; i++) {
            if ((statusWord & (1 << i)) != 0) {
                statusSet.setBit(statusBitMap[i], true);
            }
        }
        
        return statusSet;
    }

    /**
     * Serialize attack statuses to 16-bit word
     */
    private int serializeJunctionStatusAttack() {
        int result = 0;
        var statusBitMap = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12}; // Skip bit 10
        
        for (int i = 0; i < statusBitMap.length; i++) {
            if (attackStatuses.getBit(statusBitMap[i])) {
                result |= (1 << i);
            }
        }
        
        return result;
    }

    /**
     * Serialize defense statuses to 16-bit word
     */
    private int serializeJunctionStatusDefense() {
        int result = 0;
        var statusBitMap = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        
        for (int i = 0; i < statusBitMap.length; i++) {
            if (defenseStatuses.getBit(statusBitMap[i])) {
                result |= (1 << i);
            }
        }
        
        return result;
    }

    @Override
    public byte[] toBytes() {
        var attackStatusWord = serializeJunctionStatusAttack();
        var defenseStatusWord = serializeJunctionStatusDefense();
        
        var bytes = new byte[6];
        bytes[0] = (byte) attackValue;
        bytes[1] = (byte) defenseValue;
        BinarySerializationUtils.writeShortLE(bytes, 2, attackStatusWord);
        BinarySerializationUtils.writeShortLE(bytes, 4, defenseStatusWord);
        
        return bytes;
    }

    @Override
    public int getBinarySize() {
        return 6;
    }

    @Override
    public boolean hasData() {
        return hasStatusAttack() || hasStatusDefense();
    }

    /**
     * Check if has status attack
     */
    public boolean hasStatusAttack() {
        return attackStatuses.hasAnyStatus() && attackValue > 0;
    }

    /**
     * Check if has status defense
     */
    public boolean hasStatusDefense() {
        return defenseStatuses.hasAnyStatus() && defenseValue > 0;
    }

    /**
     * Create a copy with modified attack statuses
     */
    public JunctionStatusEffects withAttackStatuses(StatusEffectSet newStatuses) {
        return new JunctionStatusEffects(newStatuses, attackValue, defenseStatuses, defenseValue);
    }

    /**
     * Create a copy with modified attack value
     */
    public JunctionStatusEffects withAttackValue(int newValue) {
        return new JunctionStatusEffects(attackStatuses, newValue, defenseStatuses, defenseValue);
    }

    /**
     * Create a copy with modified defense statuses
     */
    public JunctionStatusEffects withDefenseStatuses(StatusEffectSet newStatuses) {
        return new JunctionStatusEffects(attackStatuses, attackValue, newStatuses, defenseValue);
    }

    /**
     * Create a copy with modified defense value
     */
    public JunctionStatusEffects withDefenseValue(int newValue) {
        return new JunctionStatusEffects(attackStatuses, attackValue, defenseStatuses, newValue);
    }
} 