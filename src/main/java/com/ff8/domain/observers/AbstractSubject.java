package com.ff8.domain.observers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base implementation of Subject interface.
 * Provides thread-safe observer management and notification functionality.
 * 
 * @param <T> The type of change event this subject publishes
 */
public abstract class AbstractSubject<T> implements Subject<T> {
    private static final Logger logger = Logger.getLogger(AbstractSubject.class.getName());
    
    // Use CopyOnWriteArrayList for thread-safe concurrent access
    private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();
    
    @Override
    public void registerObserver(Observer<T> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.fine("Registered observer: " + observer.getClass().getSimpleName());
        } else {
            logger.warning("Observer already registered: " + observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeObserver(Observer<T> observer) {
        if (observer == null) {
            return;
        }
        
        boolean removed = observers.remove(observer);
        if (removed) {
            logger.fine("Removed observer: " + observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void notifyObservers(T changeEvent) {
        if (changeEvent == null) {
            logger.warning("Attempted to notify observers with null change event");
            return;
        }
        
        logger.fine("Notifying " + observers.size() + " observers of change: " + changeEvent.getClass().getSimpleName());
        
        // Notify all observers, continuing even if some fail
        for (Observer<T> observer : observers) {
            try {
                observer.update(changeEvent);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Observer update failed: " + observer.getClass().getSimpleName(), e);
                try {
                    observer.onError(e, changeEvent);
                } catch (Exception errorHandlingException) {
                    logger.log(Level.SEVERE, "Observer error handling also failed: " + observer.getClass().getSimpleName(), errorHandlingException);
                }
            }
        }
    }
    
    @Override
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Clear all observers. Useful for testing and cleanup.
     */
    protected void clearObservers() {
        int count = observers.size();
        observers.clear();
        logger.fine("Cleared " + count + " observers");
    }
} 