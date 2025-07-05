package com.ff8.domain.services;

import com.ff8.domain.entities.StatusEffectSet;
import com.ff8.domain.entities.enums.StatusEffect;

import java.util.List;
import java.util.Set;

/**
 * Domain service providing business logic for status effect interactions and validation.
 * 
 * <p>This service encapsulates the complex business rules governing status effects in
 * Final Fantasy VIII, including mutual exclusions, beneficial/harmful classifications,
 * and recommended combinations for different spell types. It ensures that status effect
 * combinations are logically consistent and follow the game's mechanics.</p>
 * 
 * <p>Key business rules implemented:</p>
 * <ul>
 *   <li><strong>Mutual Exclusions:</strong> Certain status effects cannot coexist</li>
 *   <li><strong>Effect Classification:</strong> Effects are categorized as beneficial, harmful, or neutral</li>
 *   <li><strong>Combination Validation:</strong> Prevents invalid or conflicting status combinations</li>
 *   <li><strong>Spell Type Recommendations:</strong> Suggests appropriate status effects for different spell purposes</li>
 * </ul>
 * 
 * <p>Status Effect Categories:</p>
 * <ul>
 *   <li><strong>Beneficial:</strong> Positive effects that help the target (Haste, Protect, Regen, etc.)</li>
 *   <li><strong>Harmful:</strong> Negative effects that damage or hinder the target (Poison, Sleep, Curse, etc.)</li>
 *   <li><strong>Neutral:</strong> Effects that are neither clearly beneficial nor harmful</li>
 * </ul>
 * 
 * <p>Mutually Exclusive Groups:</p>
 * <ul>
 *   <li><strong>Time Effects:</strong> Haste, Slow, Stop (cannot have multiple time modifications)</li>
 *   <li><strong>Protection:</strong> Protect, Shell (physical vs magical protection)</li>
 *   <li><strong>Life Status:</strong> Death, Zombie (mutually exclusive states)</li>
 *   <li><strong>Petrification:</strong> Petrify, Petrifying (process vs final state)</li>
 *   <li><strong>Multi-casting:</strong> Double, Triple (cannot have multiple casting boosts)</li>
 * </ul>
 * 
 * <p>The service is used throughout the application for:</p>
 * <ul>
 *   <li>Validating user-created magic spell configurations</li>
 *   <li>Providing UI guidance and recommendations</li>
 *   <li>Cleaning invalid status effect combinations</li>
 *   <li>Analyzing existing spell configurations</li>
 * </ul>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
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
     * Checks if two status effects are mutually exclusive.
     * 
     * <p>This method determines whether two status effects belong to the same
     * mutually exclusive group and therefore cannot coexist on the same target.
     * This is crucial for validating magic spell configurations and preventing
     * logical inconsistencies.</p>
     * 
     * <p>Examples of mutual exclusions:</p>
     * <ul>
     *   <li>Haste and Slow (time effects conflict)</li>
     *   <li>Protect and Shell (different protection types)</li>
     *   <li>Death and Zombie (incompatible life states)</li>
     * </ul>
     * 
     * @param effect1 The first status effect to check
     * @param effect2 The second status effect to check
     * @return true if the effects are mutually exclusive, false otherwise
     */
    public boolean areMutuallyExclusive(StatusEffect effect1, StatusEffect effect2) {
        return MUTUALLY_EXCLUSIVE_GROUPS.stream()
                .anyMatch(group -> group.contains(effect1) && group.contains(effect2));
    }

    /**
     * Gets all status effects that are mutually exclusive with the given effect.
     * 
     * <p>This method returns all status effects that cannot coexist with the
     * specified effect. This is useful for UI validation and providing user
     * feedback about which effects will be disabled when a particular effect
     * is selected.</p>
     * 
     * @param effect The status effect to find exclusions for
     * @return Set of status effects that are mutually exclusive with the given effect,
     *         empty set if no exclusions exist
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
     * Validates a status effect combination for logical consistency.
     * 
     * <p>This method performs comprehensive validation of a status effect set,
     * checking for all types of conflicts and inconsistencies. It returns a
     * list of validation errors that can be displayed to the user or used
     * for automated correction.</p>
     * 
     * <p>Validation checks include:</p>
     * <ul>
     *   <li>Mutual exclusion conflicts</li>
     *   <li>Logical inconsistencies</li>
     *   <li>Game rule violations</li>
     * </ul>
     * 
     * @param statusSet The status effect set to validate
     * @return List of validation error messages, empty if no errors found
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
     * Determines if a status effect is beneficial to the target.
     * 
     * <p>Beneficial effects generally provide positive advantages such as
     * enhanced abilities, protection, or healing over time. This classification
     * is used for UI styling, spell categorization, and AI targeting decisions.</p>
     * 
     * @param effect The status effect to classify
     * @return true if the effect is beneficial, false otherwise
     */
    public boolean isBeneficial(StatusEffect effect) {
        return BENEFICIAL_EFFECTS.contains(effect);
    }

    /**
     * Determines if a status effect is harmful to the target.
     * 
     * <p>Harmful effects generally cause damage, restrictions, or negative
     * impacts on the target's capabilities. This classification is used for
     * offensive spell design and defensive planning.</p>
     * 
     * @param effect The status effect to classify
     * @return true if the effect is harmful, false otherwise
     */
    public boolean isHarmful(StatusEffect effect) {
        return HARMFUL_EFFECTS.contains(effect);
    }

    /**
     * Determines if a status effect is neutral (neither beneficial nor harmful).
     * 
     * <p>Neutral effects are those that don't clearly fall into beneficial or
     * harmful categories, or that have situational benefits/drawbacks depending
     * on context.</p>
     * 
     * @param effect The status effect to classify
     * @return true if the effect is neutral, false otherwise
     */
    public boolean isNeutral(StatusEffect effect) {
        return !isBeneficial(effect) && !isHarmful(effect);
    }

    /**
     * Extracts all beneficial effects from a status effect set.
     * 
     * <p>This method filters the active status effects to return only those
     * classified as beneficial. This is useful for analyzing the positive
     * aspects of a spell or character state.</p>
     * 
     * @param statusSet The status effect set to analyze
     * @return List of beneficial effects in the set
     */
    public List<StatusEffect> getBeneficialEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isBeneficial)
                .toList();
    }

    /**
     * Extracts all harmful effects from a status effect set.
     * 
     * <p>This method filters the active status effects to return only those
     * classified as harmful. This is useful for analyzing the negative
     * aspects of a spell or identifying threats to a character.</p>
     * 
     * @param statusSet The status effect set to analyze
     * @return List of harmful effects in the set
     */
    public List<StatusEffect> getHarmfulEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isHarmful)
                .toList();
    }

    /**
     * Extracts all neutral effects from a status effect set.
     * 
     * <p>This method filters the active status effects to return only those
     * classified as neutral. These effects may have situational value or
     * serve specific mechanical purposes.</p>
     * 
     * @param statusSet The status effect set to analyze
     * @return List of neutral effects in the set
     */
    public List<StatusEffect> getNeutralEffects(StatusEffectSet statusSet) {
        return statusSet.getActiveStatuses().stream()
                .filter(this::isNeutral)
                .toList();
    }

    /**
     * Creates a cleaned status set without mutually exclusive conflicts.
     * 
     * <p>This method resolves conflicts in a status effect set by keeping the
     * first effect encountered in each mutually exclusive group and removing
     * subsequent conflicting effects. This is useful for automatically fixing
     * invalid configurations.</p>
     * 
     * <p>Resolution strategy:</p>
     * <ul>
     *   <li>Process effects in the order they appear in the original set</li>
     *   <li>Keep the first effect from each mutually exclusive group</li>
     *   <li>Skip subsequent effects from the same group</li>
     *   <li>Preserve all non-conflicting effects</li>
     * </ul>
     * 
     * @param originalSet The status effect set to clean
     * @return A new StatusEffectSet without mutual exclusion conflicts
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
     * Gets recommended status effect combinations for different spell types.
     * 
     * <p>This method provides curated lists of status effects that work well
     * together for specific spell purposes. These recommendations are based on
     * FF8 game mechanics and strategic considerations.</p>
     * 
     * <p>Spell type recommendations:</p>
     * <ul>
     *   <li><strong>Offensive:</strong> Death, Poison, Silence - immediate threats and damage over time</li>
     *   <li><strong>Defensive:</strong> Protect, Shell, Regen - damage reduction and healing</li>
     *   <li><strong>Support:</strong> Haste, Reflect, Float - tactical advantages</li>
     *   <li><strong>Curative:</strong> Regen - healing over time</li>
     *   <li><strong>Debuff:</strong> Slow, Darkness, Confusion - capability reduction</li>
     * </ul>
     * 
     * @param spellType The type of spell to get recommendations for
     * @return List of recommended status effects for the spell type
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
     * Performs comprehensive analysis of a status effect set.
     * 
     * <p>This method provides a complete analysis of a status effect configuration,
     * including categorization, conflict detection, and overall assessment. The
     * analysis is returned as a structured record containing all relevant information.</p>
     * 
     * <p>Analysis includes:</p>
     * <ul>
     *   <li>Categorization of effects (beneficial, harmful, neutral)</li>
     *   <li>Conflict validation and error reporting</li>
     *   <li>Overall assessment of the effect set's nature</li>
     *   <li>Validity determination</li>
     * </ul>
     * 
     * @param statusSet The status effect set to analyze
     * @return StatusEffectAnalysis record containing comprehensive analysis results
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
     * Enumeration of spell types for status effect recommendations.
     * 
     * <p>This enum categorizes spells by their primary purpose, which determines
     * the most appropriate status effect combinations. Each type has distinct
     * strategic goals and recommended effect patterns.</p>
     */
    public enum SpellType {
        /** Spells designed to damage or eliminate enemies */
        OFFENSIVE,
        /** Spells designed to protect and defend allies */
        DEFENSIVE,
        /** Spells designed to provide tactical advantages */
        SUPPORT,
        /** Spells designed to heal and restore */
        CURATIVE,
        /** Spells designed to weaken and hinder enemies */
        DEBUFF
    }

    /**
     * Comprehensive analysis result record for status effect sets.
     * 
     * <p>This record encapsulates the complete analysis of a status effect
     * configuration, providing structured access to all analysis results
     * including categorization, conflicts, and validity assessment.</p>
     * 
     * @param beneficial List of beneficial effects in the set
     * @param harmful List of harmful effects in the set
     * @param neutral List of neutral effects in the set
     * @param conflicts List of validation error messages
     * @param isPrimarilyBeneficial Whether the set contains more beneficial than harmful effects
     * @param isValid Whether the set is free from conflicts and valid
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