package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating enum fields in the magic data.
 * Handles Enum values for various dropdown/combo box fields.
 */
public class AttackTypeFieldUICommand implements UICommand<AttackType> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final String description;
    private final Integer magicIndex;
    
    public AttackTypeFieldUICommand(MagicEditorUseCase magicEditorUseCase, 
                             Integer magicIndex) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.description = String.format("Update Element for magic %d", 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public void execute(AttackType newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        MagicDisplayDTO updatedMagic = currentMagic.withAttackType(newValue);

        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(AttackType newValue) {
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