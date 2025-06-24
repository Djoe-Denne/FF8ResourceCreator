package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.Element;
import com.ff8.infrastructure.adapters.primary.ui.commands.AbstractUICommand;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating the element field in the magic data.
 * Handles Element enum values for element dropdown/combo box.
 */
public class ElementFieldUICommand extends AbstractUICommand<Element> {
    
    public ElementFieldUICommand(MagicEditorUseCase magicEditorUseCase, 
                                Integer magicIndex) {
        super(magicEditorUseCase, magicIndex, "Update Element");
    }
    
    @Override
    protected MagicDisplayDTO updateMagicData(MagicDisplayDTO currentMagic, Element newValue) {
        return currentMagic.withElement(newValue);
    }
} 