package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;

/**
 * Command for updating status effect values in the magic data.
 * Handles Boolean checkbox values for status effects.
 */
public class StatusEffectAttackUICommand implements UICommand<StatusEffect> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final String description;
    private final Integer magicIndex;
    
    public StatusEffectAttackUICommand(MagicEditorUseCase magicEditorUseCase, 
                                Integer magicIndex) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.description = String.format("Update Status Effect Attack for magic %d", 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public void execute(StatusEffect newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        
        // Create a new mutable list from the current status effects
        var currentStatusEffects = currentMagic.activeStatusEffects();
        var updatedStatusEffects = new java.util.ArrayList<>(currentStatusEffects);
        
        // Toggle the status effect
        if (updatedStatusEffects.contains(newValue)) {
            updatedStatusEffects.remove(newValue);
        } else {
            updatedStatusEffects.add(newValue);
        }
        
        // Create updated magic with the new status effects list
        var updatedMagic = currentMagic.withActiveStatusEffects(updatedStatusEffects);
        
        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(StatusEffect newValue) {
        return newValue != null && magicIndex != null && magicIndex >= 0;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex != null ? magicIndex : -1;
    }
} 