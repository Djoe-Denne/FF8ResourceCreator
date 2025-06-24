package com.ff8.domain.observers;

/**
 * Subject interface for the Observer Pattern.
 * Subjects maintain a list of observers and notify them when changes occur.
 * 
 * @param <T> The type of change event this subject publishes
 */
public interface Subject<T> {
    
    /**
     * Register an observer to receive notifications.
     * 
     * @param observer The observer to register
     */
    void registerObserver(Observer<T> observer);
    
    /**
     * Remove an observer from notifications.
     * 
     * @param observer The observer to remove
     */
    void removeObserver(Observer<T> observer);
    
    /**
     * Notify all registered observers of a change.
     * 
     * @param changeEvent The event containing information about what changed
     */
    void notifyObservers(T changeEvent);
    
    /**
     * Get the number of currently registered observers.
     * Useful for debugging and testing.
     * 
     * @return The number of registered observers
     */
    int getObserverCount();
} 