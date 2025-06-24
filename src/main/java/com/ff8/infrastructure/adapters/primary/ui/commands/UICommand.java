package com.ff8.infrastructure.adapters.primary.ui.commands;

/**
 * Base interface for UI commands that encapsulate user actions
 * and coordinate their execution through the domain layer.
 * 
 * @param <T> the type of value being changed
 */
public interface UICommand<T> {
    
    /**
     * Execute the command with the new value.
     * This method should handle:
     * - Input validation
     * - Use case invocation
     * - Error handling
     * 
     * @param newValue the new value from the UI
     * @throws Exception if the command execution fails
     */
    void execute(T newValue) throws Exception;
    
    /**
     * Get a description of what this command does.
     * Useful for logging and debugging.
     * 
     * @return a human-readable description of the command
     */
    String getDescription();
    
    /**
     * Validate the new value before execution.
     * This allows for immediate UI feedback before attempting to execute.
     * 
     * @param newValue the value to validate
     * @return true if the value is valid, false otherwise
     */
    default boolean validate(T newValue) {
        return true; // Default implementation accepts all values
    }
    
    /**
     * Get the current magic index that this command operates on.
     * 
     * @return the magic index
     */
    int getMagicIndex();
} 