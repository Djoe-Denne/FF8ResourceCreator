package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Abstract base class for UI commands that provides common functionality.
 * Reduces boilerplate code and ensures consistent behavior across all commands.
 */
public abstract class AbstractUICommand<T> implements UICommand<T> {
    
    protected final MagicEditorUseCase magicEditorUseCase;
    protected final Integer magicIndex;
    protected final String description;
    
    protected AbstractUICommand(MagicEditorUseCase magicEditorUseCase, 
                               Integer magicIndex, 
                               String operationDescription) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.description = String.format("%s for magic %d", 
            operationDescription, 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public final void execute(T newValue) {
        validateExecution(newValue);
        
        // Get current magic data
        MagicDisplayDTO currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        
        // Delegate to subclass for the actual update logic
        MagicDisplayDTO updatedMagic = updateMagicData(currentMagic, newValue);
        
        // Save the updated magic data
        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    /**
     * Template method for subclasses to implement their specific update logic.
     * 
     * @param currentMagic the current magic data
     * @param newValue the new value to apply
     * @return the updated magic data
     */
    protected abstract MagicDisplayDTO updateMagicData(MagicDisplayDTO currentMagic, T newValue);
    
    /**
     * Validates that the command can be executed.
     * Subclasses can override for additional validation.
     */
    protected void validateExecution(T newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        if (!validate(newValue)) {
            throw new IllegalArgumentException("Invalid value for command execution: " + newValue);
        }
    }
    
    @Override
    public boolean validate(T newValue) {
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