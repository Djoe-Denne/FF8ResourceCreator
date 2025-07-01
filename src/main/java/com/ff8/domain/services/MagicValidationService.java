package com.ff8.domain.services;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.exceptions.InvalidMagicDataException;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for validating magic data.
 * Contains business rules and validation logic.
 */
public class MagicValidationService {

    /**
     * Validate magic data and throw exception if invalid
     */
    public void validateMagicData(MagicData magic) throws InvalidMagicDataException {
        var errors = validateMagicDataAndCollectErrors(magic);
        if (!errors.isEmpty()) {
            throw new InvalidMagicDataException("Magic data validation failed: " + String.join(", ", errors));
        }
    }

    /**
     * Validate magic data and return list of validation errors
     */
    public List<String> validateMagicDataAndCollectErrors(MagicData magic) {
        var errors = new ArrayList<String>();

        // Basic field validation
        if (magic.getMagicID() < 0 || magic.getMagicID() > 345) {
            errors.add("Magic ID must be 0-345");
        }

        if (magic.getSpellPower() < 0 || magic.getSpellPower() > 255) {
            errors.add("Spell power must be 0-255");
        }

        if (magic.getHitCount() < 0 || magic.getHitCount() > 255) {
            errors.add("Hit count must be 0-255");
        }

        if (magic.getDrawResist() < 0 || magic.getDrawResist() > 255) {
            errors.add("Draw resist must be 0-255");
        }

        // Business rule validation
        if ((magic.getAttackType() == AttackType.CURATIVE_MAGIC || magic.getAttackType() == AttackType.CURATIVE_ITEM) && magic.getSpellPower() == 0) {
            errors.add("Curative spells should have spell power > 0");
        }

        if (magic.getElement() == Element.NONE && magic.getSpellPower() > 0) {
            // This might be OK for some spells, just a warning
        }

        // Junction validation
        if (magic.getJunctionStats() != null) {
            var stats = magic.getJunctionStats();
            if (stats.getTotalBonuses() > 2000) { // Reasonable limit
                errors.add("Total junction stat bonuses seem excessive (>" + 2000 + ")");
            }
        }

        // Status effect validation
        if (magic.hasStatusEffects()) {
            var activeStatuses = magic.getStatusEffects().getActiveStatuses();
            if (activeStatuses.size() > 10) { // Reasonable limit
                errors.add("Too many active status effects (" + activeStatuses.size() + " > 10)");
            }
        }

        return errors;
    }

    /**
     * Check if magic data is valid (no validation errors)
     */
    public boolean isValid(MagicData magic) {
        return validateMagicDataAndCollectErrors(magic).isEmpty();
    }

    /**
     * Validate spell power for specific attack type
     */
    public boolean isValidSpellPowerForType(int power, AttackType type) {
        return switch (type) {
            case CURATIVE_MAGIC, CURATIVE_ITEM -> power > 0;
            case PHYSICAL_ATTACK, MAGIC_ATTACK, MAGIC_ATTACK_IGNORE_TARGET_SPR -> power >= 0;
            case NONE, SUMMON_ITEM, GF, SCAN, LV_DOWN, LV_UP, CARD -> true; // Any power is OK
            default -> true; // For all other attack types, allow any power
        };
    }

    /**
     * Validate element compatibility with attack type
     */
    public boolean isValidElementForType(Element element, AttackType type) {
        return switch (type) {
            case PHYSICAL_ATTACK, PHYSICAL_ATTACK_IGNORE_TARGET_VIT -> element == Element.NONE || element == Element.EARTH;
            case MAGIC_ATTACK, MAGIC_ATTACK_IGNORE_TARGET_SPR -> true; // Any element is valid for magical attacks
            case CURATIVE_MAGIC, CURATIVE_ITEM -> element == Element.NONE || element == Element.HOLY;
            default -> true; // For all other attack types, allow any element
        };
    }

    /**
     * Get recommended elements for attack type
     */
    public List<Element> getRecommendedElementsForType(AttackType type) {
        return switch (type) {
            case PHYSICAL_ATTACK, PHYSICAL_ATTACK_IGNORE_TARGET_VIT -> List.of(Element.NONE, Element.EARTH);
            case MAGIC_ATTACK, MAGIC_ATTACK_IGNORE_TARGET_SPR -> List.of(Element.FIRE, Element.ICE, Element.THUNDER, Element.WATER, 
                                   Element.WIND, Element.POISON, Element.HOLY);
            case CURATIVE_MAGIC, CURATIVE_ITEM -> List.of(Element.NONE, Element.HOLY);
            default -> List.of(Element.NONE); // For all other attack types, recommend NONE
        };
    }

    /**
     * Validate magic ID uniqueness (would be used with repository)
     */
    public boolean isValidMagicId(int magicId) {
        // In a real implementation, this would check against a repository
        // For now, just basic range validation
        return magicId >= 0 && magicId <= 345;
    }

    /**
     * Get validation summary for magic data
     */
    public ValidationSummary getValidationSummary(MagicData magic) {
        var errors = validateMagicDataAndCollectErrors(magic);
        var warnings = getValidationWarnings(magic);
        return new ValidationSummary(errors, warnings, errors.isEmpty());
    }

    /**
     * Get validation warnings (non-critical issues)
     */
    private List<String> getValidationWarnings(MagicData magic) {
        var warnings = new ArrayList<String>();

        if (magic.getSpellPower() == 0 && (magic.getAttackType() == AttackType.MAGIC_ATTACK || magic.getAttackType() == AttackType.MAGIC_ATTACK_IGNORE_TARGET_SPR)) {
            warnings.add("Magical spell with 0 power may not be effective");
        }

        if (!magic.hasStatusEffects() && !magic.hasJunctionBonuses() && magic.getSpellPower() == 0) {
            warnings.add("Spell has no effects, junction bonuses, or power");
        }

        if (magic.getHitCount() > 16) {
            warnings.add("Very high hit count (" + magic.getHitCount() + ") may cause performance issues");
        }

        return warnings;
    }

    /**
     * Record for validation results
     */
    public record ValidationSummary(
            List<String> errors,
            List<String> warnings,
            boolean isValid
    ) {}
} 