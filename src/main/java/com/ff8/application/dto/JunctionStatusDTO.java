package com.ff8.application.dto;

import com.ff8.domain.entities.enums.StatusEffect;
import java.util.List;

/**
 * Data Transfer Object for junction status effect bonuses.
 * Uses Java 21 record for immutability.
 */
public record JunctionStatusDTO(
        List<StatusEffect> attackStatuses,
        int attackValue,
        List<StatusEffect> defenseStatuses,
        int defenseValue
) {
    /**
     * Validation constructor
     */
    public JunctionStatusDTO {
        if (attackStatuses == null) throw new IllegalArgumentException("Attack statuses cannot be null");
        if (defenseStatuses == null) throw new IllegalArgumentException("Defense statuses cannot be null");
        if (attackValue < 0 || attackValue > 255) throw new IllegalArgumentException("Attack value must be 0-255");
        if (defenseValue < 0 || defenseValue > 255) throw new IllegalArgumentException("Defense value must be 0-255");
        
        // Make defensive copies
        attackStatuses = List.copyOf(attackStatuses);
        defenseStatuses = List.copyOf(defenseStatuses);
    }

    /**
     * Create empty junction status effects
     */
    public static JunctionStatusDTO empty() {
        return new JunctionStatusDTO(List.of(), 0, List.of(), 0);
    }

    /**
     * Check if has status attack
     */
    public boolean hasStatusAttack() {
        return !attackStatuses.isEmpty() && attackValue > 0;
    }

    /**
     * Check if has status defense
     */
    public boolean hasStatusDefense() {
        return !defenseStatuses.isEmpty() && defenseValue > 0;
    }

    /**
     * Check if has any status effects
     */
    public boolean hasAnyEffects() {
        return hasStatusAttack() || hasStatusDefense();
    }

    /**
     * Check if attacks with specific status effect
     */
    public boolean attacksWith(StatusEffect status) {
        return attackStatuses.contains(status);
    }

    /**
     * Check if defends against specific status effect
     */
    public boolean defendsAgainst(StatusEffect status) {
        return defenseStatuses.contains(status);
    }

    /**
     * Create a copy with modified attack statuses
     */
    public JunctionStatusDTO withAttackStatuses(List<StatusEffect> newStatuses) {
        return new JunctionStatusDTO(newStatuses, attackValue, defenseStatuses, defenseValue);
    }

    /**
     * Create a copy with modified attack value
     */
    public JunctionStatusDTO withAttackValue(int newValue) {
        return new JunctionStatusDTO(attackStatuses, newValue, defenseStatuses, defenseValue);
    }

    /**
     * Create a copy with modified defense statuses
     */
    public JunctionStatusDTO withDefenseStatuses(List<StatusEffect> newStatuses) {
        return new JunctionStatusDTO(attackStatuses, attackValue, newStatuses, defenseValue);
    }

    /**
     * Create a copy with modified defense value
     */
    public JunctionStatusDTO withDefenseValue(int newValue) {
        return new JunctionStatusDTO(attackStatuses, attackValue, defenseStatuses, newValue);
    }

    /**
     * Add status to attack
     */
    public JunctionStatusDTO addAttackStatus(StatusEffect status) {
        if (attackStatuses.contains(status)) {
            return this;
        }
        var newStatuses = new java.util.ArrayList<>(attackStatuses);
        newStatuses.add(status);
        return new JunctionStatusDTO(newStatuses, attackValue, defenseStatuses, defenseValue);
    }

    /**
     * Remove status from attack
     */
    public JunctionStatusDTO removeAttackStatus(StatusEffect status) {
        var newStatuses = attackStatuses.stream()
                .filter(s -> s != status)
                .toList();
        return new JunctionStatusDTO(newStatuses, attackValue, defenseStatuses, defenseValue);
    }

    /**
     * Add status to defense
     */
    public JunctionStatusDTO addDefenseStatus(StatusEffect status) {
        if (defenseStatuses.contains(status)) {
            return this;
        }
        var newStatuses = new java.util.ArrayList<>(defenseStatuses);
        newStatuses.add(status);
        return new JunctionStatusDTO(attackStatuses, attackValue, newStatuses, defenseValue);
    }

    /**
     * Remove status from defense
     */
    public JunctionStatusDTO removeDefenseStatus(StatusEffect status) {
        var newStatuses = defenseStatuses.stream()
                .filter(s -> s != status)
                .toList();
        return new JunctionStatusDTO(attackStatuses, attackValue, newStatuses, defenseValue);
    }

    /**
     * Get formatted display string for attack
     */
    public String getAttackDisplayString() {
        if (!hasStatusAttack()) {
            return "No status attack";
        }
        
        var statusNames = attackStatuses.stream()
                .map(StatusEffect::getDisplayName)
                .toList();
        return String.join(", ", statusNames) + " attack +" + attackValue + "%";
    }

    /**
     * Get formatted display string for defense
     */
    public String getDefenseDisplayString() {
        if (!hasStatusDefense()) {
            return "No status defense";
        }
        
        var statusNames = defenseStatuses.stream()
                .map(StatusEffect::getDisplayName)
                .toList();
        return String.join(", ", statusNames) + " defense +" + defenseValue + "%";
    }

    /**
     * Get complete display string
     */
    public String getDisplayString() {
        if (!hasAnyEffects()) {
            return "No status effects";
        }
        
        var parts = new java.util.ArrayList<String>();
        if (hasStatusAttack()) {
            parts.add(getAttackDisplayString());
        }
        if (hasStatusDefense()) {
            parts.add(getDefenseDisplayString());
        }
        
        return String.join(", ", parts);
    }

    /**
     * Get count of beneficial attack statuses
     */
    public int getBeneficialAttackCount() {
        return (int) attackStatuses.stream()
                .filter(status -> isBeneficial(status))
                .count();
    }

    /**
     * Get count of harmful attack statuses
     */
    public int getHarmfulAttackCount() {
        return (int) attackStatuses.stream()
                .filter(status -> isHarmful(status))
                .count();
    }

    /**
     * Simple beneficial status check (would use StatusEffectService in real implementation)
     */
    private boolean isBeneficial(StatusEffect status) {
        return switch (status) {
            case HASTE, REGEN, PROTECT, SHELL, REFLECT, AURA, INVINCIBLE, FLOAT, 
                 DOUBLE, TRIPLE, DEFEND, CHARGED -> true;
            default -> false;
        };
    }

    /**
     * Simple harmful status check (would use StatusEffectService in real implementation)
     */
    private boolean isHarmful(StatusEffect status) {
        return switch (status) {
            case SLEEP, SLOW, STOP, CURSE, DOOM, CONFUSION, DEATH, POISON, 
                 PETRIFY, DARKNESS, SILENCE, BERSERK, ZOMBIE -> true;
            default -> false;
        };
    }
} 