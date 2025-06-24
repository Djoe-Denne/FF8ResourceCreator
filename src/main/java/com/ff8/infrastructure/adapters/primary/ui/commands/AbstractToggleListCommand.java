package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for commands that toggle items in/out of lists.
 * Handles the common pattern of adding/removing items from collections.
 */
public abstract class AbstractToggleListCommand<T> extends AbstractUICommand<T> {
    
    protected AbstractToggleListCommand(MagicEditorUseCase magicEditorUseCase, 
                                       Integer magicIndex, 
                                       String operationDescription) {
        super(magicEditorUseCase, magicIndex, operationDescription);
    }
    
    @Override
    protected final MagicDisplayDTO updateMagicData(MagicDisplayDTO currentMagic, T newValue) {
        List<T> currentList = new ArrayList<>(getCurrentList(currentMagic));
        
        if (currentList.contains(newValue)) {
            currentList.remove(newValue);
        } else {
            currentList.add(newValue);
        }
        
        return updateMagicWithNewList(currentMagic, currentList);
    }
    
    /**
     * Get the current list from the magic data that should be toggled.
     * 
     * @param currentMagic the current magic data
     * @return the list to toggle items in/out of
     */
    protected abstract List<T> getCurrentList(MagicDisplayDTO currentMagic);
    
    /**
     * Create updated magic data with the new list.
     * 
     * @param currentMagic the current magic data
     * @param newList the updated list
     * @return the updated magic data
     */
    protected abstract MagicDisplayDTO updateMagicWithNewList(MagicDisplayDTO currentMagic, List<T> newList);
} 