package com.ff8.domain.services;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.exceptions.InvalidMagicDataException;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for validating magic data according to FF8 business rules.
 * 
 * <p>This service encapsulates the validation logic for magic spells, ensuring they
 * conform to the game's mechanics and constraints. It provides both strict validation
 * (throwing exceptions) and flexible validation (returning error lists) to support
 * different use cases throughout the application.</p>
 * 
 * <p>The service validates multiple aspects of magic data:</p>
 * <ul>
 *   <li>Basic field constraints (value ranges, required fields)</li>
 *   <li>Business rule validation (attack type compatibility, power requirements)</li>
 *   <li>Cross-field validation (element and attack type combinations)</li>
 *   <li>Junction system constraints (reasonable stat bonuses)</li>
 *   <li>Status effect limitations (maximum effects per spell)</li>
 * </ul>
 * 
 * <p>This service is part of the domain layer and contains only business logic,
 * with no dependencies on external frameworks or infrastructure concerns.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class MagicValidationService {

    /**
     * Validates magic data and throws an exception if invalid.
     * 
     * <p>This method performs comprehensive validation and throws an exception
     * containing all validation errors if any are found. Use this method when
     * you need strict validation with failure handling.</p>
     * 
     * @param magic the magic data to validate
     * @throws InvalidMagicDataException if validation fails, containing all error messages
     */
    public void validateMagicData(MagicData magic) throws InvalidMagicDataException {
        var errors = validateMagicDataAndCollectErrors(magic);
        if (!errors.isEmpty()) {
            throw new InvalidMagicDataException("Magic data validation failed: " + String.join(", ", errors));
        }
    }

    /**
     * Validates magic data and returns a list of validation errors.
     * 
     * <p>This method performs the same validation as {@link #validateMagicData(MagicData)}
     * but returns errors as a list instead of throwing an exception. Use this method
     * when you need to handle validation errors programmatically or display them
     * to the user.</p>
     * 
     * @param magic the magic data to validate
     * @return a list of validation error messages, empty if validation passes
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
     * Checks if magic data is valid without throwing exceptions.
     * 
     * <p>This is a convenience method that returns a boolean indicating whether
     * the magic data passes all validation rules. It's equivalent to checking
     * if {@link #validateMagicDataAndCollectErrors(MagicData)} returns an empty list.</p>
     * 
     * @param magic the magic data to validate
     * @return true if the magic data is valid, false otherwise
     */
    public boolean isValid(MagicData magic) {
        return validateMagicDataAndCollectErrors(magic).isEmpty();
    }

    /**
     * Validates spell power for a specific attack type.
     * 
     * <p>Different attack types have different power requirements:</p>
     * <ul>
     *   <li>Curative spells should have power > 0 to be effective</li>
     *   <li>Attack spells can have power >= 0 (including 0 for status-only effects)</li>
     *   <li>Utility spells (scan, card, etc.) can have any power value</li>
     * </ul>
     * 
     * @param power the spell power to validate
     * @param type the attack type to validate against
     * @return true if the power is valid for the given attack type
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
     * Validates element compatibility with attack type.
     * 
     * <p>Certain attack types work better with specific elements:</p>
     * <ul>
     *   <li>Physical attacks typically use None or Earth elements</li>
     *   <li>Magic attacks can use any element</li>
     *   <li>Curative spells typically use None or Holy elements</li>
     * </ul>
     * 
     * @param element the element to validate
     * @param type the attack type to validate against
     * @return true if the element is valid for the given attack type
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
     * Gets recommended elements for a specific attack type.
     * 
     * <p>Provides a list of elements that work well with the given attack type.
     * This can be used to guide users when creating new spells or validating
     * existing ones.</p>
     * 
     * @param type the attack type to get recommendations for
     * @return a list of recommended elements for the attack type
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
     * Validates that a magic ID is within the valid range.
     * 
     * <p>Magic IDs in FF8 must be within the range 0-345. In a full implementation,
     * this method would also check for uniqueness against a repository.</p>
     * 
     * @param magicId the magic ID to validate
     * @return true if the magic ID is valid
     */
    public boolean isValidMagicId(int magicId) {
        // In a real implementation, this would check against a repository
        // For now, just basic range validation
        return magicId >= 0 && magicId <= 345;
    }

    /**
     * Gets a comprehensive validation summary for magic data.
     * 
     * <p>This method provides both errors (critical issues that prevent the spell
     * from working) and warnings (non-critical issues that might indicate problems
     * but don't prevent functionality).</p>
     * 
     * @param magic the magic data to validate
     * @return a ValidationSummary containing errors, warnings, and validity status
     */
    public ValidationSummary getValidationSummary(MagicData magic) {
        var errors = validateMagicDataAndCollectErrors(magic);
        var warnings = getValidationWarnings(magic);
        return new ValidationSummary(errors, warnings, errors.isEmpty());
    }

    /**
     * Gets validation warnings for magic data.
     * 
     * <p>Warnings are non-critical issues that might indicate problems but don't
     * prevent the spell from functioning. They help users create more effective
     * and meaningful spells.</p>
     * 
     * @param magic the magic data to check for warnings
     * @return a list of warning messages
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
     * Record containing comprehensive validation results.
     * 
     * <p>This record provides a complete validation summary including:</p>
     * <ul>
     *   <li>Errors - critical issues that prevent the spell from working</li>
     *   <li>Warnings - non-critical issues that might indicate problems</li>
     *   <li>Validity status - whether the spell passes all critical validations</li>
     * </ul>
     * 
     * @param errors list of critical validation errors
     * @param warnings list of non-critical validation warnings
     * @param isValid true if the spell passes all critical validations
     */
    public record ValidationSummary(
            List<String> errors,
            List<String> warnings,
            boolean isValid
    ) {}
} 