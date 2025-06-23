package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.Element;
import lombok.Value;
import lombok.With;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents junction elemental bonuses using Lombok for immutability.
 * Handles elemental attack and defense bonuses.
 */
@Value
@With
public class JunctionElemental implements BinarySerializable {
    Element attackElement;
    int attackValue;
    List<Element> defenseElements;
    int defenseValue;

    /**
     * Create junction elemental with validation
     */
    public JunctionElemental(Element attackElement, int attackValue, List<Element> defenseElements, int defenseValue) {
        if (attackElement == null) throw new IllegalArgumentException("Attack element cannot be null");
        BinarySerializationUtils.validateByteValue(attackValue, "Attack value");
        if (defenseElements == null) throw new IllegalArgumentException("Defense elements cannot be null");
        BinarySerializationUtils.validateByteValue(defenseValue, "Defense value");
        
        this.attackElement = attackElement;
        this.attackValue = attackValue;
        this.defenseElements = List.copyOf(defenseElements);
        this.defenseValue = defenseValue;
    }

    /**
     * Create empty junction elemental
     */
    public static JunctionElemental empty() {
        return new JunctionElemental(Element.NONE, 0, List.of(), 0);
    }

    /**
     * Create from byte array (4 bytes)
     */
    public static JunctionElemental fromBytes(byte[] bytes, int offset) {
        BinarySerializationUtils.validateBytesAvailable(bytes, offset, 4, "junction elemental");

        var attackElement = Element.fromValue(BinarySerializationUtils.toUnsignedInt(bytes[offset]));
        var attackValue = BinarySerializationUtils.toUnsignedInt(bytes[offset + 1]);
        var defenseElementByte = BinarySerializationUtils.toUnsignedInt(bytes[offset + 2]);
        var defenseValue = BinarySerializationUtils.toUnsignedInt(bytes[offset + 3]);

        // Parse defense elements from bitfield
        var defenseElements = parseDefenseElements(defenseElementByte);

        return new JunctionElemental(attackElement, attackValue, defenseElements, defenseValue);
    }

    /**
     * Parse defense elements from byte bitfield
     */
    private static List<Element> parseDefenseElements(int elementByte) {
        return IntStream.range(0, 8)
                .filter(i -> (elementByte & (1 << i)) != 0)
                .mapToObj(i -> switch (i) {
                    case 0 -> Element.FIRE;
                    case 1 -> Element.ICE;
                    case 2 -> Element.THUNDER;
                    case 3 -> Element.EARTH;
                    case 4 -> Element.POISON;
                    case 5 -> Element.WIND;
                    case 6 -> Element.WATER;
                    case 7 -> Element.HOLY;
                    default -> throw new IllegalStateException("Unexpected bit: " + i);
                })
                .toList();
    }

    /**
     * Convert defense elements to byte bitfield
     */
    private int serializeDefenseElements() {
        int result = 0;
        for (Element element : defenseElements) {
            int bit = switch (element) {
                case FIRE -> 0;
                case ICE -> 1;
                case THUNDER -> 2;
                case EARTH -> 3;
                case POISON -> 4;
                case WIND -> 5;
                case WATER -> 6;
                case HOLY -> 7;
                case NONE -> -1; // Skip NONE
                default -> throw new IllegalArgumentException("Unknown element: " + element);
            };
            if (bit >= 0) {
                result |= (1 << bit);
            }
        }
        return result;
    }

    @Override
    public byte[] toBytes() {
        return new byte[] {
                (byte) attackElement.getValue(),
                (byte) attackValue,
                (byte) serializeDefenseElements(),
                (byte) defenseValue
        };
    }

    @Override
    public int getBinarySize() {
        return 4;
    }

    @Override
    public boolean hasData() {
        return hasElementalAttack() || hasElementalDefense();
    }

    /**
     * Check if has elemental attack
     */
    public boolean hasElementalAttack() {
        return attackElement != Element.NONE && attackValue > 0;
    }

    /**
     * Check if has elemental defense
     */
    public boolean hasElementalDefense() {
        return !defenseElements.isEmpty() && defenseValue > 0;
    }

    /**
     * Check if defends against specific element
     */
    public boolean defendsAgainst(Element element) {
        return defenseElements.contains(element);
    }

    /**
     * Add element to defense
     */
    public JunctionElemental addDefenseElement(Element element) {
        if (defenseElements.contains(element)) {
            return this;
        }
        var newElements = new java.util.ArrayList<>(defenseElements);
        newElements.add(element);
        return withDefenseElements(newElements);
    }

    /**
     * Remove element from defense
     */
    public JunctionElemental removeDefenseElement(Element element) {
        var newElements = defenseElements.stream()
                .filter(e -> e != element)
                .toList();
        return withDefenseElements(newElements);
    }
} 