package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Abstract base class for commands that update specific fields based on a field type enum.
 * Handles the common pattern of using enums to determine which field to update.
 */
public abstract class AbstractFieldCommand<T, E extends Enum<E>> extends AbstractUICommand<T> {
    
    protected final E fieldType;
    
    protected AbstractFieldCommand(MagicEditorUseCase magicEditorUseCase, 
                                  Integer magicIndex, 
                                  E fieldType) {
        super(magicEditorUseCase, magicIndex, 
              String.format("Update %s", fieldType.name().toLowerCase().replace("_", " ")));
        this.fieldType = fieldType;
    }
    
    @Override
    protected final MagicDisplayDTO updateMagicData(MagicDisplayDTO currentMagic, T newValue) {
        return updateSpecificField(currentMagic, newValue, fieldType);
    }
    
    /**
     * Update the specific field based on the field type.
     * 
     * @param currentMagic the current magic data
     * @param newValue the new value to set
     * @param fieldType the type of field to update
     * @return the updated magic data
     */
    protected abstract MagicDisplayDTO updateSpecificField(MagicDisplayDTO currentMagic, T newValue, E fieldType);
    
} 