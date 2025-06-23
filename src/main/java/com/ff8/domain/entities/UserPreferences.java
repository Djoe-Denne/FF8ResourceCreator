package com.ff8.domain.entities;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Domain entity representing user preferences and application settings.
 * Contains window state and user interface preferences.
 */
public class UserPreferences {
    
    // Window settings
    private final WindowSettings windowSettings;
    
    // File system preferences
    private final Path lastOpenDirectory;
    
    public UserPreferences(WindowSettings windowSettings, Path lastOpenDirectory) {
        this.windowSettings = Objects.requireNonNull(windowSettings, "Window settings cannot be null");
        this.lastOpenDirectory = lastOpenDirectory; // Can be null for first run
    }
    
    // Getters
    public WindowSettings getWindowSettings() {
        return windowSettings;
    }
    
    public Path getLastOpenDirectory() {
        return lastOpenDirectory;
    }
    
    // Builder pattern for easy construction
    public static UserPreferencesBuilder builder() {
        return new UserPreferencesBuilder();
    }
    
    // Convenience methods for creating modified versions
    public UserPreferences withWindowSettings(WindowSettings newWindowSettings) {
        return new UserPreferences(newWindowSettings, this.lastOpenDirectory);
    }
    
    public UserPreferences withLastOpenDirectory(Path newLastOpenDirectory) {
        return new UserPreferences(this.windowSettings, newLastOpenDirectory);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreferences that = (UserPreferences) o;
        return Objects.equals(windowSettings, that.windowSettings) &&
               Objects.equals(lastOpenDirectory, that.lastOpenDirectory);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(windowSettings, lastOpenDirectory);
    }
    
    @Override
    public String toString() {
        return "UserPreferences{" +
               "windowSettings=" + windowSettings +
               ", lastOpenDirectory=" + lastOpenDirectory +
               '}';
    }
    
    /**
     * Nested class representing window state and positioning
     */
    public static class WindowSettings {
        private final double width;
        private final double height;
        private final double x;
        private final double y;
        private final boolean maximized;
        
        public WindowSettings(double width, double height, double x, double y, boolean maximized) {
            this.width = Math.max(400, width); // Minimum width
            this.height = Math.max(300, height); // Minimum height
            this.x = x;
            this.y = y;
            this.maximized = maximized;
        }
        
        // Getters
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public double getX() { return x; }
        public double getY() { return y; }
        public boolean isMaximized() { return maximized; }
        
        // Default window settings
        public static WindowSettings defaultSettings() {
            return new WindowSettings(1200, 800, 100, 100, false);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WindowSettings that = (WindowSettings) o;
            return Double.compare(that.width, width) == 0 &&
                   Double.compare(that.height, height) == 0 &&
                   Double.compare(that.x, x) == 0 &&
                   Double.compare(that.y, y) == 0 &&
                   maximized == that.maximized;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(width, height, x, y, maximized);
        }
        
        @Override
        public String toString() {
            return "WindowSettings{" +
                   "width=" + width +
                   ", height=" + height +
                   ", x=" + x +
                   ", y=" + y +
                   ", maximized=" + maximized +
                   '}';
        }
    }
    
    /**
     * Builder class for UserPreferences
     */
    public static class UserPreferencesBuilder {
        private WindowSettings windowSettings = WindowSettings.defaultSettings();
        private Path lastOpenDirectory;
        
        public UserPreferencesBuilder windowSettings(WindowSettings windowSettings) {
            this.windowSettings = windowSettings;
            return this;
        }
        
        public UserPreferencesBuilder windowSettings(double width, double height, double x, double y, boolean maximized) {
            this.windowSettings = new WindowSettings(width, height, x, y, maximized);
            return this;
        }
        
        public UserPreferencesBuilder lastOpenDirectory(Path lastOpenDirectory) {
            this.lastOpenDirectory = lastOpenDirectory;
            return this;
        }
        
        public UserPreferences build() {
            return new UserPreferences(windowSettings, lastOpenDirectory);
        }
    }
} 