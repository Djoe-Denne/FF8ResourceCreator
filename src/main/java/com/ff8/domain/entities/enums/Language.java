package com.ff8.domain.entities.enums;

/**
 * Enumeration of supported languages for spell translations.
 * English is the primary language and must always be present.
 */
public enum Language {
    ENGLISH("English", "en"),
    FRENCH("French", "fr"),
    GERMAN("German", "de"),
    SPANISH("Spanish", "es"),
    ITALIAN("Italian", "it"),
    JAPANESE("Japanese", "ja");
    
    private final String displayName;
    private final String code;
    
    Language(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Get Language enum from display name (case-insensitive)
     */
    public static Language fromDisplayName(String displayName) {
        if (displayName == null) {
            return ENGLISH;
        }
        
        for (Language lang : values()) {
            if (lang.displayName.equalsIgnoreCase(displayName.trim())) {
                return lang;
            }
        }
        
        // Default to English if not found
        return ENGLISH;
    }
    
    /**
     * Check if this is the primary language (English)
     */
    public boolean isPrimary() {
        return this == ENGLISH;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 