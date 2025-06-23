package com.ff8.application.ports.primary;

import com.ff8.domain.entities.UserPreferences;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Primary port for user preferences management.
 * Defines the use cases for handling application settings and window state.
 */
public interface UserPreferencesUseCase {
    
    /**
     * Load user preferences from storage.
     * Returns default preferences if none exist.
     * 
     * @return user preferences (never null)
     */
    UserPreferences loadPreferences();
    
    /**
     * Save current user preferences to storage.
     * 
     * @param preferences the preferences to save
     * @return result of the save operation
     */
    SaveResult savePreferences(UserPreferences preferences);
    
    /**
     * Apply window settings from preferences to a JavaFX stage.
     * 
     * @param stage the stage to configure
     * @param preferences the preferences containing window settings
     */
    void applyWindowSettings(Stage stage, UserPreferences preferences);
    
    /**
     * Capture current window state from a JavaFX stage.
     * 
     * @param stage the stage to capture settings from
     * @return window settings representing the current state
     */
    UserPreferences.WindowSettings captureWindowSettings(Stage stage);
    
    /**
     * Update the last opened directory preference.
     * 
     * @param directory the directory to remember
     * @return updated user preferences
     */
    UserPreferences updateLastOpenDirectory(Path directory);
    
    /**
     * Get the current user preferences in memory.
     * 
     * @return current preferences
     */
    UserPreferences getCurrentPreferences();
    
    /**
     * Reset preferences to defaults and optionally save.
     * 
     * @param saveToStorage whether to persist the reset to storage
     * @return default preferences
     */
    UserPreferences resetToDefaults(boolean saveToStorage);
    
    /**
     * Result of a save operation
     */
    sealed interface SaveResult permits SaveResult.Success, SaveResult.Failure {
        record Success() implements SaveResult {}
        record Failure(String message, Throwable cause) implements SaveResult {}
        
        static SaveResult success() {
            return new Success();
        }
        
        static SaveResult failure(String message, Throwable cause) {
            return new Failure(message, cause);
        }
        
        default boolean isSuccess() {
            return this instanceof Success;
        }
        
        default boolean isFailure() {
            return this instanceof Failure;
        }
    }
} 