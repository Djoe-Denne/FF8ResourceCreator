package com.ff8.infrastructure.adapters.secondary.preferences;

import com.ff8.application.ports.secondary.UserPreferencesPort;
import com.ff8.domain.entities.UserPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Infrastructure adapter that persists user preferences using Java Properties files.
 * Stores preferences in the appropriate application data directory for each platform:
 * - Windows: %APPDATA%\FF8MagicCreator
 * - macOS: ~/Library/Application Support/FF8MagicCreator  
 * - Linux/Unix: ~/.config/ff8magic-creator
 */
public class PropertiesUserPreferencesAdapter implements UserPreferencesPort {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUserPreferencesAdapter.class);
    
    private static final String PREFERENCES_FILE_NAME = "preferences.properties";
    
    // Property keys
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";
    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_MAXIMIZED = "window.maximized";
    private static final String LAST_OPEN_DIRECTORY = "file.lastOpenDirectory";
    
    private final Path preferencesFile;
    
    public PropertiesUserPreferencesAdapter() {
        // Determine the appropriate application data directory for the current platform
        Path appDir = getApplicationDataDirectory();
        this.preferencesFile = appDir.resolve(PREFERENCES_FILE_NAME);
        
        // Ensure app directory exists
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            logger.warn("Failed to create preferences directory: " + appDir, e);
        }
        
        logger.info("Preferences file location: {}", preferencesFile);
    }
    
    /**
     * Get the appropriate application data directory based on the operating system
     */
    private Path getApplicationDataDirectory() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            // Windows: Use %APPDATA%\FF8MagicCreator
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "FF8MagicCreator");
            } else {
                // Fallback to user.home if APPDATA is not available
                logger.warn("APPDATA environment variable not found, using user home directory");
                return Paths.get(System.getProperty("user.home"), "AppData", "Roaming", "FF8MagicCreator");
            }
        } else if (osName.contains("mac")) {
            // macOS: Use ~/Library/Application Support/FF8MagicCreator
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "FF8MagicCreator");
        } else {
            // Linux/Unix: Use ~/.config/ff8magic-creator (following XDG Base Directory Specification)
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome != null) {
                return Paths.get(xdgConfigHome, "ff8magic-creator");
            } else {
                return Paths.get(System.getProperty("user.home"), ".config", "ff8magic-creator");
            }
        }
    }
    
    @Override
    public Optional<UserPreferences> loadPreferences() throws PreferencesException {
        if (!Files.exists(preferencesFile)) {
            logger.debug("Preferences file does not exist: {}", preferencesFile);
            return Optional.empty();
        }
        
        Properties properties = new Properties();
        
        try (InputStream input = Files.newInputStream(preferencesFile)) {
            properties.load(input);
            
            UserPreferences preferences = parseProperties(properties);
            logger.debug("Loaded preferences from: {}", preferencesFile);
            return Optional.of(preferences);
            
        } catch (IOException | IllegalArgumentException e) {
            throw new PreferencesException("Failed to load preferences from " + preferencesFile, e);
        }
    }
    
    @Override
    public void savePreferences(UserPreferences preferences) throws PreferencesException {
        Properties properties = new Properties();
        
        // Convert preferences to properties
        convertToProperties(preferences, properties);
        
        try (OutputStream output = Files.newOutputStream(preferencesFile)) {
            properties.store(output, "FF8 Magic Creator User Preferences");
            logger.debug("Saved preferences to: {}", preferencesFile);
            
        } catch (IOException e) {
            throw new PreferencesException("Failed to save preferences to " + preferencesFile, e);
        }
    }
    
    @Override
    public boolean preferencesExist() {
        return Files.exists(preferencesFile);
    }
    
    @Override
    public void clearPreferences() throws PreferencesException {
        try {
            Files.deleteIfExists(preferencesFile);
            logger.info("Cleared preferences file: {}", preferencesFile);
        } catch (IOException e) {
            throw new PreferencesException("Failed to delete preferences file " + preferencesFile, e);
        }
    }
    
    /**
     * Parse properties into UserPreferences object
     */
    private UserPreferences parseProperties(Properties properties) {
        // Parse window settings
        double width = parseDouble(properties, WINDOW_WIDTH, 1200.0);
        double height = parseDouble(properties, WINDOW_HEIGHT, 800.0);
        double x = parseDouble(properties, WINDOW_X, 100.0);
        double y = parseDouble(properties, WINDOW_Y, 100.0);
        boolean maximized = parseBoolean(properties, WINDOW_MAXIMIZED, false);
        
        UserPreferences.WindowSettings windowSettings = 
            new UserPreferences.WindowSettings(width, height, x, y, maximized);
        
        // Parse last open directory
        String lastDirString = properties.getProperty(LAST_OPEN_DIRECTORY);
        Path lastOpenDirectory = null;
        if (lastDirString != null && !lastDirString.trim().isEmpty()) {
            try {
                lastOpenDirectory = Paths.get(lastDirString);
                // Verify the directory still exists
                if (!Files.exists(lastOpenDirectory) || !Files.isDirectory(lastOpenDirectory)) {
                    logger.warn("Last open directory no longer exists: {}", lastOpenDirectory);
                    lastOpenDirectory = null;
                }
            } catch (Exception e) {
                logger.warn("Invalid last open directory path: " + lastDirString, e);
            }
        }
        
        return new UserPreferences(windowSettings, lastOpenDirectory);
    }
    
    /**
     * Convert UserPreferences to Properties
     */
    private void convertToProperties(UserPreferences preferences, Properties properties) {
        UserPreferences.WindowSettings windowSettings = preferences.getWindowSettings();
        
        // Window settings
        properties.setProperty(WINDOW_WIDTH, String.valueOf(windowSettings.getWidth()));
        properties.setProperty(WINDOW_HEIGHT, String.valueOf(windowSettings.getHeight()));
        properties.setProperty(WINDOW_X, String.valueOf(windowSettings.getX()));
        properties.setProperty(WINDOW_Y, String.valueOf(windowSettings.getY()));
        properties.setProperty(WINDOW_MAXIMIZED, String.valueOf(windowSettings.isMaximized()));
        
        // Last open directory
        if (preferences.getLastOpenDirectory() != null) {
            properties.setProperty(LAST_OPEN_DIRECTORY, preferences.getLastOpenDirectory().toString());
        }
    }
    
    /**
     * Parse double property with default value
     */
    private double parseDouble(Properties properties, String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for {}: {}, using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Parse boolean property with default value
     */
    private boolean parseBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get the preferences file path (for debugging/testing)
     */
    public Path getPreferencesFilePath() {
        return preferencesFile;
    }
    
    /**
     * Get the application data directory path (for debugging/testing)
     */
    public Path getApplicationDataDirectoryPath() {
        return preferencesFile.getParent();
    }
    
    /**
     * Get a description of where preferences are stored for the current platform
     */
    public String getStorageLocationDescription() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            return "Windows AppData Roaming folder (%APPDATA%\\FF8MagicCreator)";
        } else if (osName.contains("mac")) {
            return "macOS Application Support folder (~/Library/Application Support/FF8MagicCreator)";
        } else {
            return "Linux/Unix config folder (~/.config/ff8magic-creator or $XDG_CONFIG_HOME/ff8magic-creator)";
        }
    }
} 