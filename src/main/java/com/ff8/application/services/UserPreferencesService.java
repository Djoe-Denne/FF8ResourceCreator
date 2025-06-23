package com.ff8.application.services;

import com.ff8.application.ports.primary.UserPreferencesUseCase;
import com.ff8.application.ports.secondary.UserPreferencesPort;
import com.ff8.domain.entities.UserPreferences;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Service implementing user preferences management use cases.
 * Handles loading, saving, and applying user preferences.
 */
public class UserPreferencesService implements UserPreferencesUseCase {
    private static final Logger logger = LoggerFactory.getLogger(UserPreferencesService.class);
    
    private final UserPreferencesPort preferencesPort;
    private UserPreferences currentPreferences;
    
    public UserPreferencesService(UserPreferencesPort preferencesPort) {
        this.preferencesPort = Objects.requireNonNull(preferencesPort, "UserPreferencesPort cannot be null");
        this.currentPreferences = loadPreferences(); // Load on initialization
    }
    
    @Override
    public UserPreferences loadPreferences() {
        try {
            var loadedPreferences = preferencesPort.loadPreferences();
            if (loadedPreferences.isPresent()) {
                logger.info("Loaded user preferences: {}", loadedPreferences.get());
                this.currentPreferences = loadedPreferences.get();
                return this.currentPreferences;
            }
        } catch (UserPreferencesPort.PreferencesException e) {
            logger.warn("Failed to load user preferences, using defaults", e);
        }
        
        // Return default preferences if none exist or loading failed
        this.currentPreferences = createDefaultPreferences();
        logger.info("Using default preferences: {}", this.currentPreferences);
        return this.currentPreferences;
    }
    
    @Override
    public SaveResult savePreferences(UserPreferences preferences) {
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        
        try {
            preferencesPort.savePreferences(preferences);
            this.currentPreferences = preferences;
            logger.info("Saved user preferences: {}", preferences);
            return SaveResult.success();
            
        } catch (UserPreferencesPort.PreferencesException e) {
            logger.error("Failed to save user preferences", e);
            return SaveResult.failure("Failed to save preferences: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void applyWindowSettings(Stage stage, UserPreferences preferences) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        Objects.requireNonNull(preferences, "Preferences cannot be null");
        
        var windowSettings = preferences.getWindowSettings();
        
        if (windowSettings.isMaximized()) {
            stage.setMaximized(true);
        } else {
            stage.setMaximized(false);
            stage.setWidth(windowSettings.getWidth());
            stage.setHeight(windowSettings.getHeight());
            stage.setX(windowSettings.getX());
            stage.setY(windowSettings.getY());
        }
        
        logger.debug("Applied window settings: {}", windowSettings);
    }
    
    @Override
    public UserPreferences.WindowSettings captureWindowSettings(Stage stage) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        
        return new UserPreferences.WindowSettings(
            stage.getWidth(),
            stage.getHeight(),
            stage.getX(),
            stage.getY(),
            stage.isMaximized()
        );
    }
    
    @Override
    public UserPreferences updateLastOpenDirectory(Path directory) {
        var updatedPreferences = currentPreferences.withLastOpenDirectory(directory);
        
        // Auto-save the updated preferences
        var saveResult = savePreferences(updatedPreferences);
        if (saveResult.isFailure()) {
            logger.warn("Failed to auto-save updated directory preference");
        }
        
        return updatedPreferences;
    }
    
    @Override
    public UserPreferences getCurrentPreferences() {
        return currentPreferences;
    }
    
    @Override
    public UserPreferences resetToDefaults(boolean saveToStorage) {
        var defaultPreferences = createDefaultPreferences();
        
        if (saveToStorage) {
            var saveResult = savePreferences(defaultPreferences);
            if (saveResult.isFailure()) {
                logger.warn("Failed to save default preferences to storage");
            }
        } else {
            this.currentPreferences = defaultPreferences;
        }
        
        logger.info("Reset preferences to defaults (saved: {})", saveToStorage);
        return defaultPreferences;
    }
    
    /**
     * Create default user preferences
     */
    private UserPreferences createDefaultPreferences() {
        return UserPreferences.builder()
            .windowSettings(UserPreferences.WindowSettings.defaultSettings())
            .lastOpenDirectory(null) // No last directory on first run
            .build();
    }
    
    /**
     * Save current window state automatically (convenience method)
     */
    public void saveCurrentWindowState(Stage stage) {
        var windowSettings = captureWindowSettings(stage);
        var updatedPreferences = currentPreferences.withWindowSettings(windowSettings);
        
        var saveResult = savePreferences(updatedPreferences);
        if (saveResult.isFailure() && saveResult instanceof SaveResult.Failure failure) {
            logger.warn("Failed to save window state: {}", failure.message());
        }
    }
} 