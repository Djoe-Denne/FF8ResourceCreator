package com.ff8.infrastructure.adapters.primary.ui.commands.junction;

import java.util.List;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.adapters.primary.ui.commands.AbstractToggleListCommand;

/**
 * Command for updating status effect defense values in the magic data.
 * Toggles status effects in/out of the defense list.
 */
public class StatusEffectDefenseJunctionUICommand extends AbstractToggleListCommand<StatusEffect> {
    
    public StatusEffectDefenseJunctionUICommand(MagicEditorUseCase magicEditorUseCase, 
                                               Integer magicIndex) {
        super(magicEditorUseCase, magicIndex, "Update Status Effect Defense");
    }
    
    @Override
    protected List<StatusEffect> getCurrentList(MagicDisplayDTO currentMagic) {
        return currentMagic.junctionStatus().defenseStatuses();
    }
    
    @Override
    protected MagicDisplayDTO updateMagicWithNewList(MagicDisplayDTO currentMagic, List<StatusEffect> newList) {
        return currentMagic.withJunctionStatus(
            currentMagic.junctionStatus().withDefenseStatuses(newList)
        );
    }
} 