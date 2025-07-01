package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating integer fields in the magic data.
 * Handles Integer spinner values for various numeric fields.
 */
public class IntegerFieldUICommand extends AbstractFieldCommand<Integer, IntegerFieldUICommand.IntegerFieldType> {
    
    public enum IntegerFieldType {
        DRAW_RESIST,
        HIT_COUNT,
        STATUS_ATTACK,
        MAGIC_ID,
        SPELL_POWER,
        ELEMENTAL_ATTACK_VALUE,
        ELEMENTAL_DEFENSE_VALUE,
        STATUS_ATTACK_VALUE,
        STATUS_DEFENSE_VALUE
    }
    
    public IntegerFieldUICommand(MagicEditorUseCase magicEditorUseCase,
                                IntegerFieldType fieldType,
                                Integer magicIndex) {
        super(magicEditorUseCase, magicIndex, fieldType);
    }
    
    @Override
    protected MagicDisplayDTO updateSpecificField(MagicDisplayDTO currentMagic, Integer newValue, IntegerFieldType fieldType) {
        switch (fieldType) {
            case DRAW_RESIST:
                return currentMagic.withDrawResist(newValue);
            case HIT_COUNT:
                return currentMagic.withHitCount(newValue);
            case STATUS_ATTACK:
                return currentMagic.withStatusAttackEnabler(newValue);
            case MAGIC_ID:
                return currentMagic.withMagicID(newValue);
            case SPELL_POWER:
                return currentMagic.withSpellPower(newValue);
            case ELEMENTAL_ATTACK_VALUE:
                return currentMagic.withJunctionElemental(
                    currentMagic.junctionElemental().withAttackValue(newValue)
                );
            case ELEMENTAL_DEFENSE_VALUE:
                return currentMagic.withJunctionElemental(
                    currentMagic.junctionElemental().withDefenseValue(newValue)
                );
            case STATUS_ATTACK_VALUE:
                return currentMagic.withJunctionStatus(
                    currentMagic.junctionStatus().withAttackValue(newValue)
                );
            case STATUS_DEFENSE_VALUE:
                return currentMagic.withJunctionStatus(
                    currentMagic.junctionStatus().withDefenseValue(newValue)
                );
            default:
                throw new IllegalArgumentException("Unknown integer field type: " + fieldType);
        }
    }
    
    @Override
    public boolean validate(Integer newValue) {
        if (!super.validate(newValue)) {
            return false;
        }
        
        // Validate ranges based on field type
        switch (fieldType) {
            case MAGIC_ID:
                // Magic ID can be higher for extended ranges
                return newValue >= 0 && newValue <= 345;
            case DRAW_RESIST:
            case HIT_COUNT:
            case STATUS_ATTACK:
            case SPELL_POWER:
            case ELEMENTAL_ATTACK_VALUE:
            case ELEMENTAL_DEFENSE_VALUE:
            case STATUS_ATTACK_VALUE:
            case STATUS_DEFENSE_VALUE:
            default:
                // Most other FF8 values are 0-255
                return newValue >= 0 && newValue <= 255;
        }
    }
} 