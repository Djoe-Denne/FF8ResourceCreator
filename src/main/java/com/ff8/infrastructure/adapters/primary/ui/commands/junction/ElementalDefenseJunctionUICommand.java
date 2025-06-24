package com.ff8.infrastructure.adapters.primary.ui.commands.junction;

import com.ff8.application.ports.primary.MagicEditorUseCase;

import java.util.ArrayList;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.domain.entities.enums.Element;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;

/**
 * Command for updating status effect values in the magic data.
 * Handles Boolean checkbox values for status effects.
 */
public class ElementalDefenseJunctionUICommand implements UICommand<Element> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final String description;
    private final Integer magicIndex;
    
    public ElementalDefenseJunctionUICommand(MagicEditorUseCase magicEditorUseCase, 
                                Integer magicIndex) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.description = String.format("Update Elemental Defense for magic %d", 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public void execute(Element newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        MagicDisplayDTO updatedMagic = null;

        var junctionElementalDefenseEffect = new ArrayList<>(currentMagic.junctionElemental().defenseElements());
        if(junctionElementalDefenseEffect.contains(newValue)) {
            junctionElementalDefenseEffect.remove(newValue);
        } else {
            junctionElementalDefenseEffect.add(newValue);
        }
        updatedMagic = currentMagic.withJunctionElemental(currentMagic.junctionElemental().withDefenseElements(junctionElementalDefenseEffect));
        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(Element newValue) {
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