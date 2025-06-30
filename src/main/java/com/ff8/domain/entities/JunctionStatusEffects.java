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
     * Maps specific bits to status effects using FF8's junction mapping
     */
    private static StatusEffectSet parseJunctionStatusAttack(int statusWord) {
        var statusSet = new StatusEffectSet();
        
        // FF8 Junction Status Mapping (from main status bits to junction bits)
        // Junction bits 0-6: death(32), poison(33), petrify(34), darkness(35), silence(36), berserk(37), zombie(38)
        // Junction bits 7-12: sleep(0), slow(2), stop(3), curse(9), confusion(14), drain(15)
        var junctionToMainStatusMap = new int[]{
            32, // bit 0 -> death
            33, // bit 1 -> poison  
            34, // bit 2 -> petrify
            35, // bit 3 -> darkness
            36, // bit 4 -> silence
            37, // bit 5 -> berserk
            38, // bit 6 -> zombie
            0,  // bit 7 -> sleep
            2,  // bit 8 -> slow
            3,  // bit 9 -> stop
            9,  // bit 10 -> curse
            14, // bit 11 -> confusion
            15  // bit 12 -> drain
            // bits 13-15 are unused
        };
        
        for (int junctionBit = 0; junctionBit < junctionToMainStatusMap.length; junctionBit++) {
            if ((statusWord & (1 << junctionBit)) != 0) {
                int mainStatusBit = junctionToMainStatusMap[junctionBit];
                statusSet.setBit(mainStatusBit, true);
            }
        }
        
        return statusSet;
    }

    /**
     * Parse junction defense status effects from 16-bit word
     * Uses the same mapping as attack statuses
     */
    private static StatusEffectSet parseJunctionStatusDefense(int statusWord) {
        var statusSet = new StatusEffectSet();
        
        // Same mapping as attack statuses
        var junctionToMainStatusMap = new int[]{
            32, // bit 0 -> death
            33, // bit 1 -> poison  
            34, // bit 2 -> petrify
            35, // bit 3 -> darkness
            36, // bit 4 -> silence
            37, // bit 5 -> berserk
            38, // bit 6 -> zombie
            0,  // bit 7 -> sleep
            2,  // bit 8 -> slow
            3,  // bit 9 -> stop
            9,  // bit 10 -> curse
            14, // bit 11 -> confusion
            15  // bit 12 -> drain
        };
        
        for (int junctionBit = 0; junctionBit < junctionToMainStatusMap.length; junctionBit++) {
            if ((statusWord & (1 << junctionBit)) != 0) {
                int mainStatusBit = junctionToMainStatusMap[junctionBit];
                statusSet.setBit(mainStatusBit, true);
            }
        }
        
        return statusSet;
    }

    /**
     * Serialize attack statuses to 16-bit word
     */
    private int serializeJunctionStatusAttack() {
        int result = 0;
        
        // Reverse mapping: main status bits to junction bits
        var mainToJunctionStatusMap = new int[]{
            7,  // main bit 0 (sleep) -> junction bit 7
            -1, // main bit 1 (haste) -> not available in junction
            8,  // main bit 2 (slow) -> junction bit 8
            9,  // main bit 3 (stop) -> junction bit 9
            -1, // main bit 4 (regen) -> not available
            -1, // main bit 5 (protect) -> not available
            -1, // main bit 6 (shell) -> not available
            -1, // main bit 7 (reflect) -> not available
            -1, // main bit 8 (aura) -> not available
            10, // main bit 9 (curse) -> junction bit 10
            -1, -1, -1, -1, // bits 10-13 not available
            11, // main bit 14 (confusion) -> junction bit 11
            12, // main bit 15 (drain) -> junction bit 12
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // bits 16-31 not available
            0,  // main bit 32 (death) -> junction bit 0
            1,  // main bit 33 (poison) -> junction bit 1
            2,  // main bit 34 (petrify) -> junction bit 2
            3,  // main bit 35 (darkness) -> junction bit 3
            4,  // main bit 36 (silence) -> junction bit 4
            5,  // main bit 37 (berserk) -> junction bit 5
            6   // main bit 38 (zombie) -> junction bit 6
        };
        
        for (int mainBit = 0; mainBit < mainToJunctionStatusMap.length; mainBit++) {
            int junctionBit = mainToJunctionStatusMap[mainBit];
            if (junctionBit >= 0 && attackStatuses.getBit(mainBit)) {
                result |= (1 << junctionBit);
            }
        }
        
        return result;
    }

    /**
     * Serialize defense statuses to 16-bit word
     */
    private int serializeJunctionStatusDefense() {
        int result = 0;
        
        // Same reverse mapping as attack
        var mainToJunctionStatusMap = new int[]{
            7,  // main bit 0 (sleep) -> junction bit 7
            -1, // main bit 1 (haste) -> not available in junction
            8,  // main bit 2 (slow) -> junction bit 8
            9,  // main bit 3 (stop) -> junction bit 9
            -1, // main bit 4 (regen) -> not available
            -1, // main bit 5 (protect) -> not available
            -1, // main bit 6 (shell) -> not available
            -1, // main bit 7 (reflect) -> not available
            -1, // main bit 8 (aura) -> not available
            10, // main bit 9 (curse) -> junction bit 10
            -1, -1, -1, -1, // bits 10-13 not available
            11, // main bit 14 (confusion) -> junction bit 11
            12, // main bit 15 (drain) -> junction bit 12
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // bits 16-31 not available
            0,  // main bit 32 (death) -> junction bit 0
            1,  // main bit 33 (poison) -> junction bit 1
            2,  // main bit 34 (petrify) -> junction bit 2
            3,  // main bit 35 (darkness) -> junction bit 3
            4,  // main bit 36 (silence) -> junction bit 4
            5,  // main bit 37 (berserk) -> junction bit 5
            6   // main bit 38 (zombie) -> junction bit 6
        };
        
        for (int mainBit = 0; mainBit < mainToJunctionStatusMap.length; mainBit++) {
            int junctionBit = mainToJunctionStatusMap[mainBit];
            if (junctionBit >= 0 && defenseStatuses.getBit(mainBit)) {
                result |= (1 << junctionBit);
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