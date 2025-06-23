package com.ff8.domain.entities;

/**
 * Value object representing a junction attack/defense value pair.
 * Used to eliminate duplication in junction-related classes.
 */
public record JunctionValue(
        int attackValue,
        int defenseValue
) {
    /**
     * Validation constructor
     */
    public JunctionValue {
        BinarySerializationUtils.validateByteValue(attackValue, "Attack value");
        BinarySerializationUtils.validateByteValue(defenseValue, "Defense value");
    }

    /**
     * Create empty junction value
     */
    public static JunctionValue empty() {
        return new JunctionValue(0, 0);
    }

    /**
     * Create junction value with only attack
     */
    public static JunctionValue attackOnly(int value) {
        return new JunctionValue(value, 0);
    }

    /**
     * Create junction value with only defense
     */
    public static JunctionValue defenseOnly(int value) {
        return new JunctionValue(0, value);
    }

    /**
     * Check if has any attack value
     */
    public boolean hasAttack() {
        return attackValue > 0;
    }

    /**
     * Check if has any defense value
     */
    public boolean hasDefense() {
        return defenseValue > 0;
    }

    /**
     * Check if has any value
     */
    public boolean hasAnyValue() {
        return hasAttack() || hasDefense();
    }

    /**
     * Convert to 2-byte array (attack, defense)
     */
    public byte[] toBytes() {
        return new byte[] {
                (byte) attackValue,
                (byte) defenseValue
        };
    }

    /**
     * Create from 2-byte array
     */
    public static JunctionValue fromBytes(byte[] bytes, int offset) {
        BinarySerializationUtils.validateBytesAvailable(bytes, offset, 2, "junction value");
        
        return new JunctionValue(
                BinarySerializationUtils.toUnsignedInt(bytes[offset]),
                BinarySerializationUtils.toUnsignedInt(bytes[offset + 1])
        );
    }
} 