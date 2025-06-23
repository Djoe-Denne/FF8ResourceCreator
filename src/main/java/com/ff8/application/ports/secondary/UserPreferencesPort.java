package com.ff8.application.ports.secondary;

import com.ff8.domain.entities.UserPreferences;

import java.util.Optional;

/**
 * Port for persisting and retrieving user preferences.
 * This interface defines the contract for saving/loading application settings
 * such as window state, last opened directories, etc.
 */
public interface UserPreferencesPort {
    
    /**
     * Load user preferences from persistent storage.
     * 
     * @return Optional containing user preferences if they exist, empty otherwise
     * @throws PreferencesException if there's an error reading preferences
     */
    Optional<UserPreferences> loadPreferences() throws PreferencesException;
    
    /**
     * Save user preferences to persistent storage.
     * 
     * @param preferences the preferences to save
     * @throws PreferencesException if there's an error saving preferences
     */
    void savePreferences(UserPreferences preferences) throws PreferencesException;
    
    /**
     * Check if preferences file exists
     * 
     * @return true if preferences exist in storage
     */
    boolean preferencesExist();
    
    /**
     * Delete all stored preferences (useful for reset functionality)
     * 
     * @throws PreferencesException if there's an error deleting preferences
     */
    void clearPreferences() throws PreferencesException;
    
    /**
     * Exception thrown when there's an error with preferences operations
     */
    class PreferencesException extends Exception {
        public PreferencesException(String message) {
            super(message);
        }
        
        public PreferencesException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 