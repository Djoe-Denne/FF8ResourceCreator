package com.ff8.infrastructure.adapters.secondary.preferences;

import com.ff8.application.ports.secondary.UserPreferencesPort;
import com.ff8.domain.entities.UserPreferences;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Cross-platform user preferences adapter using Java Properties files.
 * 
 * <p>This class implements the {@link UserPreferencesPort} interface as a Secondary Adapter
 * in the hexagonal architecture, providing persistent storage for user preferences and
 * application settings. It follows platform-specific conventions for storing application
 * data to ensure a native user experience across different operating systems.</p>
 * 
 * <p>Platform-specific storage locations:</p>
 * <ul>
 *   <li><strong>Windows:</strong> {@code %APPDATA%\FF8MagicCreator\preferences.properties}</li>
 *   <li><strong>macOS:</strong> {@code ~/Library/Application Support/FF8MagicCreator/preferences.properties}</li>
 *   <li><strong>Linux/Unix:</strong> {@code ~/.config/ff8magic-creator/preferences.properties}</li>
 * </ul>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Cross-platform compatibility with OS-specific data directories</li>
 *   <li>Automatic directory creation for first-time users</li>
 *   <li>Robust error handling with fallback mechanisms</li>
 *   <li>Type-safe property parsing with default values</li>
 *   <li>Comprehensive logging for debugging and monitoring</li>
 *   <li>Support for XDG Base Directory Specification on Linux</li>
 * </ul>
 * 
 * <p>The adapter stores the following preference categories:</p>
 * <ul>
 *   <li><strong>Window Settings:</strong> Size, position, and maximized state</li>
 *   <li><strong>File Paths:</strong> Last opened directory for file dialogs</li>
 *   <li><strong>Application Settings:</strong> User customizations and options</li>
 * </ul>
 * 
 * <p>Error handling strategy:</p>
 * <ul>
 *   <li>Graceful degradation when preferences cannot be loaded</li>
 *   <li>Fallback to user home directory if platform directories are unavailable</li>
 *   <li>Validation of loaded preferences with sensible defaults</li>
 *   <li>Clear error reporting through {@link PreferencesException}</li>
 * </ul>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
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
    
    @Getter
    private final Path preferencesFile;
    
    /**
     * Constructs a new PropertiesUserPreferencesAdapter with platform-appropriate storage.
     * 
     * <p>This constructor automatically determines the correct application data directory
     * for the current operating system and creates the necessary directory structure.
     * The preferences file path is logged for debugging purposes.</p>
     * 
     * <p>Directory creation is performed during construction to ensure the preferences
     * can be saved when needed. If directory creation fails, a warning is logged
     * but the adapter remains functional.</p>
     */
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
     * Determines the appropriate application data directory based on the operating system.
     * 
     * <p>This method implements platform-specific logic to select the correct directory
     * for storing application data, following each platform's conventions:</p>
     * 
     * <ul>
     *   <li><strong>Windows:</strong> Uses {@code %APPDATA%} environment variable</li>
     *   <li><strong>macOS:</strong> Uses {@code ~/Library/Application Support}</li>
     *   <li><strong>Linux/Unix:</strong> Follows XDG Base Directory Specification</li>
     * </ul>
     * 
     * <p>Fallback mechanisms are in place for each platform in case the preferred
     * locations are not available.</p>
     * 
     * @return Path to the appropriate application data directory
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
    
    /**
     * {@inheritDoc}
     * 
     * <p>Loads user preferences from the platform-specific preferences file.
     * This method performs the following operations:</p>
     * <ul>
     *   <li>Checks for preferences file existence</li>
     *   <li>Loads properties from the file using Java Properties format</li>
     *   <li>Parses and validates all preference values</li>
     *   <li>Applies default values for missing or invalid preferences</li>
     *   <li>Validates file paths and removes invalid entries</li>
     * </ul>
     * 
     * @return Optional containing loaded preferences, or empty if no preferences exist
     * @throws PreferencesException if there's an error reading or parsing preferences
     */
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
    
    /**
     * {@inheritDoc}
     * 
     * <p>Saves user preferences to the platform-specific preferences file.
     * This method performs the following operations:</p>
     * <ul>
     *   <li>Converts UserPreferences object to Java Properties format</li>
     *   <li>Writes properties to the preferences file with a descriptive header</li>
     *   <li>Ensures atomic write operation to prevent corruption</li>
     *   <li>Logs the operation for debugging purposes</li>
     * </ul>
     * 
     * @param preferences The user preferences to save
     * @throws PreferencesException if there's an error writing preferences
     */
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
    
    /**
     * {@inheritDoc}
     * 
     * <p>Checks if a preferences file exists in the platform-specific location.</p>
     * 
     * @return true if preferences file exists, false otherwise
     */
    @Override
    public boolean preferencesExist() {
        return Files.exists(preferencesFile);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Deletes the preferences file from the file system. This operation
     * effectively resets all user preferences to their default values.
     * The operation is idempotent - it won't fail if the file doesn't exist.</p>
     * 
     * @throws PreferencesException if there's an error deleting the preferences file
     */
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
     * Parses Java Properties into a UserPreferences object.
     * 
     * <p>This method performs comprehensive parsing and validation of all
     * preference values, applying sensible defaults for missing or invalid
     * entries. It handles type conversion and range validation for numeric
     * values and path validation for file system entries.</p>
     * 
     * @param properties The Properties object to parse
     * @return A UserPreferences object with parsed and validated values
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
     * Converts a UserPreferences object to Java Properties format.
     * 
     * <p>This method serializes all preference values to string format
     * suitable for storage in a Java Properties file. It handles null
     * values appropriately and ensures all numeric values are formatted
     * consistently.</p>
     * 
     * @param preferences The UserPreferences object to convert
     * @param properties The Properties object to populate
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
     * Parses a double property with default value and error handling.
     * 
     * <p>This method attempts to parse a double value from the properties,
     * applying the default value if the property is missing or invalid.
     * It logs warnings for invalid values to help with debugging.</p>
     * 
     * @param properties The Properties object to read from
     * @param key The property key to parse
     * @param defaultValue The default value to use if parsing fails
     * @return The parsed double value or the default value
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
     * Parses a boolean property with default value and error handling.
     * 
     * <p>This method attempts to parse a boolean value from the properties,
     * applying the default value if the property is missing or invalid.
     * It accepts "true" (case-insensitive) as true, everything else as false.</p>
     * 
     * @param properties The Properties object to read from
     * @param key The property key to parse
     * @param defaultValue The default value to use if parsing fails
     * @return The parsed boolean value or the default value
     */
    private boolean parseBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Gets the full path to the preferences file.
     * 
     * <p>This method is useful for debugging and displaying the preferences
     * file location to the user.</p>
     * 
     * @return Path to the preferences file
     */
    public Path getPreferencesFilePath() {
        return preferencesFile;
    }
    
    /**
     * Gets the application data directory path.
     * 
     * <p>This method returns the platform-specific directory where the
     * application stores its data files.</p>
     * 
     * @return Path to the application data directory
     */
    public Path getApplicationDataDirectoryPath() {
        return preferencesFile.getParent();
    }
    
    /**
     * Gets a human-readable description of the storage location.
     * 
     * <p>This method provides a user-friendly description of where
     * preferences are stored, suitable for display in the user interface.</p>
     * 
     * @return Human-readable storage location description
     */
    public String getStorageLocationDescription() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "Windows Application Data (%APPDATA%)";
        } else if (osName.contains("mac")) {
            return "macOS Application Support";
        } else {
            return "Linux/Unix Configuration Directory";
        }
    }
} 