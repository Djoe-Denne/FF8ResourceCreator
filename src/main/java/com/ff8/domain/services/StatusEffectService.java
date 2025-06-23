package com.ff8.domain.services;

import com.ff8.domain.entities.StatusEffectSet;
import com.ff8.domain.entities.enums.StatusEffect;

import java.util.List;
import java.util.Set;

/**
 * Domain service for status effect business logic.
 * Contains rules about status effect interactions and combinations.
 */
public class StatusEffectService {

    // Mutually exclusive status effects
    private static final Set<Set<StatusEffect>> MUTUALLY_EXCLUSIVE_GROUPS = Set.of(
            Set.of(StatusEffect.HASTE, StatusEffect.SLOW, StatusEffect.STOP),
            Set.of(StatusEffect.PROTECT, StatusEffect.SHELL),
            Set.of(StatusEffect.DEATH, StatusEffect.ZOMBIE),
            Set.of(StatusEffect.PETRIFY, StatusEffect.PETRIFYING),
            Set.of(StatusEffect.DOUBLE, StatusEffect.TRIPLE)
    );

    // Beneficial status effects
    private static final Set<StatusEffect> BENEFICIAL_EFFECTS = Set.of(
            StatusEffect.HASTE, StatusEffect.REGEN, StatusEffect.PROTECT,
            StatusEffect.SHELL, StatusEffect.REFLECT, StatusEffect.AURA,
            StatusEffect.INVINCIBLE, StatusEffect.FLOAT, StatusEffect.DOUBLE,
            StatusEffect.TRIPLE, StatusEffect.DEFEND, StatusEffect.CHARGED,
            StatusEffect.ANGEL_WING, StatusEffect.HAS_MAGIC, StatusEffect.SUMMON_GF
    );

    // Harmful status effects
    private static final Set<StatusEffect> HARMFUL_EFFECTS = Set.of(
            StatusEffect.SLEEP, StatusEffect.SLOW, StatusEffect.STOP,
            StatusEffect.CURSE, StatusEffect.DOOM, StatusEffect.PETRIFYING,
            StatusEffect.CONFUSION, StatusEffect.DRAIN, StatusEffect.EJECT,
            StatusEffect.BACK_ATTACK, StatusEffect.VIT_0, StatusEffect.DEATH,
            StatusEffect.POISON, StatusEffect.PETRIFY, StatusEffect.DARKNESS,
            StatusEffect.SILENCE, StatusEffect.BERSERK, StatusEffect.ZOMBIE
    );

    /**
     * Check if two status effects are mutually exclusive
     */
    public boolean areMutuallyExclusive(StatusEffect effect1, StatusEffect effect2) {
        return MUTUALLY_EXCLUSIVE_GROUPS.stream()
                .anyMatch(group -> group.contains(effect1) && group.contains(effect2));
    }

    /**
     * Get mutually exclusive effects for a given effect
     */
    public Set<StatusEffect> getMutuallyExclusiveEffects(StatusEffect effect) {
        return MUTUALLY_EXCLUSIVE_GROUPS.stream()
                .filter(group -> group.contains(effect))
                .findFirst()
                .map(group -> {
                    var result = Set.copyOf(group);
                    result.remove(effect);
                    return result;
                })
                .orElse(Set.of());
    }

    /**
     * Validate status effect combination
     */
    public List<String> validateStatusCombination(StatusEffectSet statusSet) {
        var errors = new java.util.ArrayList<String>();
        var activeEffects = statusSet.getActiveStatuses();

        // Check for mutually exclusive effects
        for (var group : MUTUALLY_EXCLUSIVE_GROUPS) {
            var activeInGroup = activeEffects.stream()
                    .filter(group::contains)
                    .toList();
            
            if (activeInGroup.size() > 1) {
                errors.add("Mutually exclusive status effects: " + 
                          activeInGroup.stream()
                                  .map(StatusEffect::getDisplayName)
                                  .toList());
            }
        }

        return errors;
    }

    /**
     * Check if status effect is beneficial
     */
    public boolean isBeneficial(StatusEffect effect) {
        return BENEFICIAL_EFFECTS.contains(effect);
    }

    /**
     * Check if status effect is harmful
     */
    public boolean isHarmful(StatusEffect effect) {
        return HARMFUL_EFFECTS.contains(effect);
    }

    /**
     * Check if status effect is neutral (neither beneficial nor harmful)
     */
    public boolean isNeutral(StatusEffect effect) {
        return !isBeneficial(effect) && !isHarmful(effect);
    }

    /**
     * Get beneficial effects from status set
     */
    public List<StatusEffect> getBeneficialEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isBeneficial)
                .toList();
    }

    /**
     * Get harmful effects from status set
     */
    public List<StatusEffect> getHarmfulEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isHarmful)
                .toList();
    }

    /**
     * Get neutral effects from status set
     */
    public List<StatusEffect> getNeutralEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isNeutral)
                .toList();
    }

    /**
     * Create a cleaned status set without mutually exclusive conflicts
     * Keeps the first effect in each mutually exclusive group
     */
    public StatusEffectSet cleanStatusSet(StatusEffectSet originalSet) {
        var cleanedSet = new StatusEffectSet();
        var activeEffects = originalSet.getActiveStatuses();
        var processedGroups = new java.util.HashSet<Set<StatusEffect>>();

        for (var effect : activeEffects) {
            var exclusiveGroup = MUTUALLY_EXCLUSIVE_GROUPS.stream()
                    .filter(group -> group.contains(effect))
                    .findFirst();

            if (exclusiveGroup.isPresent()) {
                if (!processedGroups.contains(exclusiveGroup.get())) {
                    // First effect in this group, add it
                    cleanedSet.setStatus(effect, true);
                    processedGroups.add(exclusiveGroup.get());
                }
                // Skip subsequent effects in the same group
            } else {
                // Not in any exclusive group, add it
                cleanedSet.setStatus(effect, true);
            }
        }

        return cleanedSet;
    }

    /**
     * Get recommended status combinations for different spell types
     */
    public List<StatusEffect> getRecommendedCombination(SpellType spellType) {
        return switch (spellType) {
            case OFFENSIVE -> List.of(StatusEffect.DEATH, StatusEffect.POISON, StatusEffect.SILENCE);
            case DEFENSIVE -> List.of(StatusEffect.PROTECT, StatusEffect.SHELL, StatusEffect.REGEN);
            case SUPPORT -> List.of(StatusEffect.HASTE, StatusEffect.REFLECT, StatusEffect.FLOAT);
            case CURATIVE -> List.of(StatusEffect.REGEN);
            case DEBUFF -> List.of(StatusEffect.SLOW, StatusEffect.DARKNESS, StatusEffect.CONFUSION);
        };
    }

    /**
     * Analyze status effect coverage
     */
    public StatusEffectAnalysis analyzeStatusEffects(StatusEffectSet statusSet) {
        var beneficial = getBeneficialEffects(statusSet);
        var harmful = getHarmfulEffects(statusSet);
        var neutral = getNeutralEffects(statusSet);
        var conflicts = validateStatusCombination(statusSet);

        return new StatusEffectAnalysis(
                beneficial, harmful, neutral, conflicts,
                beneficial.size() > harmful.size(),
                conflicts.isEmpty()
        );
    }

    /**
     * Enum for spell types
     */
    public enum SpellType {
        OFFENSIVE, DEFENSIVE, SUPPORT, CURATIVE, DEBUFF
    }

    /**
     * Record for status effect analysis results
     */
    public record StatusEffectAnalysis(
            List<StatusEffect> beneficial,
            List<StatusEffect> harmful,
            List<StatusEffect> neutral,
            List<String> conflicts,
            boolean isPrimarilyBeneficial,
            boolean isValid
    ) {}
} 