package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating integer fields in the magic data.
 * Handles Integer spinner values for various numeric fields.
 */
public class IntegerFieldUICommand implements UICommand<Integer> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final IntegerFieldType fieldType;
    private final String description;
    private final Integer magicIndex;
    
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
        this.magicEditorUseCase = magicEditorUseCase;
        this.fieldType = fieldType;
        this.magicIndex = magicIndex;
        this.description = String.format("Update %s for magic %d", 
            fieldType.name().toLowerCase().replace("_", " "), 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public void execute(Integer newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        
        // Get current magic data
        MagicDisplayDTO currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        MagicDisplayDTO updatedMagic = null;

        switch (fieldType) {
            case DRAW_RESIST:
                updatedMagic = currentMagic.withDrawResist(newValue);
                break;
            case HIT_COUNT:
                updatedMagic = currentMagic.withHitCount(newValue);
                break;
            case STATUS_ATTACK:
                updatedMagic = currentMagic.withStatusAttackEnabler(newValue);
                break;
            case MAGIC_ID:
                updatedMagic = currentMagic.withMagicID(newValue);
                break;
            case SPELL_POWER:
                updatedMagic = currentMagic.withSpellPower(newValue);
                break;
            case ELEMENTAL_ATTACK_VALUE:
                updatedMagic = currentMagic.withJunctionElemental(currentMagic.junctionElemental().withAttackValue(newValue));
                break;
            case ELEMENTAL_DEFENSE_VALUE:
                updatedMagic = currentMagic.withJunctionElemental(currentMagic.junctionElemental().withDefenseValue(newValue));
                break;
            case STATUS_ATTACK_VALUE:
                updatedMagic = currentMagic.withJunctionStatus(currentMagic.junctionStatus().withAttackValue(newValue));
                break;
            case STATUS_DEFENSE_VALUE:
                updatedMagic = currentMagic.withJunctionStatus(currentMagic.junctionStatus().withDefenseValue(newValue));
                break;
            default:
                throw new IllegalArgumentException("Unknown integer field type: " + fieldType);
        }

        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(Integer newValue) {
        if (newValue == null || magicIndex == null || magicIndex < 0) {
            return false;
        }
        
        // Validate ranges based on field type
        switch (fieldType) {
            case DRAW_RESIST, HIT_COUNT, STATUS_ATTACK, 
                 MAGIC_ID, SPELL_POWER, ELEMENTAL_ATTACK_VALUE, 
                 ELEMENTAL_DEFENSE_VALUE, STATUS_ATTACK_VALUE, 
                 STATUS_DEFENSE_VALUE:
                return newValue >= 0 && newValue <= 255;
            default:
                return false;
        }
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex;
    }
} 