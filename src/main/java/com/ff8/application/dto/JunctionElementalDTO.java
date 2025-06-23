package com.ff8.application.dto;

import com.ff8.domain.entities.enums.Element;
import java.util.List;

/**
 * Data Transfer Object for junction elemental bonuses.
 * Uses Java 21 record for immutability.
 */
public record JunctionElementalDTO(
        Element attackElement,
        int attackValue,
        List<Element> defenseElements,
        int defenseValue
) {
    /**
     * Validation constructor
     */
    public JunctionElementalDTO {
        if (attackElement == null) throw new IllegalArgumentException("Attack element cannot be null");
        if (attackValue < 0 || attackValue > 255) throw new IllegalArgumentException("Attack value must be 0-255");
        if (defenseElements == null) throw new IllegalArgumentException("Defense elements cannot be null");
        if (defenseValue < 0 || defenseValue > 255) throw new IllegalArgumentException("Defense value must be 0-255");
        
        // Make defensive copy
        defenseElements = List.copyOf(defenseElements);
    }

    /**
     * Create empty junction elemental
     */
    public static JunctionElementalDTO empty() {
        return new JunctionElementalDTO(Element.NONE, 0, List.of(), 0);
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
     * Check if has any elemental effects
     */
    public boolean hasAnyEffects() {
        return hasElementalAttack() || hasElementalDefense();
    }

    /**
     * Check if defends against specific element
     */
    public boolean defendsAgainst(Element element) {
        return defenseElements.contains(element);
    }

    /**
     * Create a copy with modified attack element
     */
    public JunctionElementalDTO withAttackElement(Element newElement) {
        return new JunctionElementalDTO(newElement, attackValue, defenseElements, defenseValue);
    }

    /**
     * Create a copy with modified attack value
     */
    public JunctionElementalDTO withAttackValue(int newValue) {
        return new JunctionElementalDTO(attackElement, newValue, defenseElements, defenseValue);
    }

    /**
     * Create a copy with modified defense elements
     */
    public JunctionElementalDTO withDefenseElements(List<Element> newElements) {
        return new JunctionElementalDTO(attackElement, attackValue, newElements, defenseValue);
    }

    /**
     * Create a copy with modified defense value
     */
    public JunctionElementalDTO withDefenseValue(int newValue) {
        return new JunctionElementalDTO(attackElement, attackValue, defenseElements, newValue);
    }

    /**
     * Add element to defense
     */
    public JunctionElementalDTO addDefenseElement(Element element) {
        if (defenseElements.contains(element)) {
            return this;
        }
        var newElements = new java.util.ArrayList<>(defenseElements);
        newElements.add(element);
        return new JunctionElementalDTO(attackElement, attackValue, newElements, defenseValue);
    }

    /**
     * Remove element from defense
     */
    public JunctionElementalDTO removeDefenseElement(Element element) {
        var newElements = defenseElements.stream()
                .filter(e -> e != element)
                .toList();
        return new JunctionElementalDTO(attackElement, attackValue, newElements, defenseValue);
    }

    /**
     * Get formatted display string for attack
     */
    public String getAttackDisplayString() {
        if (!hasElementalAttack()) {
            return "No elemental attack";
        }
        return attackElement.getDisplayName() + " attack +" + attackValue + "%";
    }

    /**
     * Get formatted display string for defense
     */
    public String getDefenseDisplayString() {
        if (!hasElementalDefense()) {
            return "No elemental defense";
        }
        
        var elements = defenseElements.stream()
                .map(Element::getDisplayName)
                .toList();
        return String.join(", ", elements) + " defense +" + defenseValue + "%";
    }

    /**
     * Get complete display string
     */
    public String getDisplayString() {
        if (!hasAnyEffects()) {
            return "No elemental effects";
        }
        
        var parts = new java.util.ArrayList<String>();
        if (hasElementalAttack()) {
            parts.add(getAttackDisplayString());
        }
        if (hasElementalDefense()) {
            parts.add(getDefenseDisplayString());
        }
        
        return String.join(", ", parts);
    }
} 