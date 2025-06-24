package com.ff8.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.nio.file.Path;

/**
 * Domain entity representing user preferences and application settings.
 * Contains window state and user interface preferences.
 */
@Value
@Builder(toBuilder = true)
@With
@AllArgsConstructor
public class UserPreferences {
    
    // Window settings
    @Builder.Default
    WindowSettings windowSettings = WindowSettings.defaultSettings();
    
    // File system preferences
    Path lastOpenDirectory; // Can be null for first run
    
    /**
     * Nested class representing window state and positioning
     */
    @Value
    @Builder
    public static class WindowSettings {
        @Builder.Default
        double width = 1200; // Minimum width handled in constructor
        @Builder.Default
        double height = 800; // Minimum height handled in constructor
        @Builder.Default
        double x = 100;
        @Builder.Default
        double y = 100;
        @Builder.Default
        boolean maximized = false;
        
        public WindowSettings(double width, double height, double x, double y, boolean maximized) {
            this.width = Math.max(400, width); // Minimum width
            this.height = Math.max(300, height); // Minimum height
            this.x = x;
            this.y = y;
            this.maximized = maximized;
        }
        
        // Default window settings
        public static WindowSettings defaultSettings() {
            return new WindowSettings(1200, 800, 100, 100, false);
        }
    }
} 