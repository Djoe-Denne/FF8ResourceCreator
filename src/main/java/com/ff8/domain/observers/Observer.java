package com.ff8.domain.observers;

/**
 * Observer interface for the Observer Pattern.
 * Observers register with subjects to receive notifications when changes occur.
 * 
 * @param <T> The type of change event this observer can handle
 */
public interface Observer<T> {
    
    /**
     * Called when the observed subject notifies of a change.
     * 
     * @param changeEvent The event containing information about what changed
     */
    void update(T changeEvent);
    
    /**
     * Called when an observer needs to handle an error during notification.
     * Default implementation logs the error and continues.
     * 
     * @param error The error that occurred during notification
     * @param changeEvent The event that was being processed when the error occurred
     */
    default void onError(Throwable error, T changeEvent) {
        // Default error handling - subclasses can override
        System.err.println("Observer error during update: " + error.getMessage());
    }
} 