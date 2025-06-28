package com.ff8.domain.entities;

import java.util.*;

/**
 * Represents translations for spell names and descriptions in multiple languages.
 * Supports internationalization with English as the primary language.
 */
public class SpellTranslations {
    private final Map<String, Translation> translations;
    
    /**
     * Represents a translation entry for a specific language
     */
    public static class Translation {
        private final String name;
        private final String description;
        
        public Translation(String name, String description) {
            this.name = name != null ? name : "";
            this.description = description != null ? description : "";
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Translation withName(String newName) {
            return new Translation(newName, this.description);
        }
        
        public Translation withDescription(String newDescription) {
            return new Translation(this.name, newDescription);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Translation that = (Translation) obj;
            return Objects.equals(name, that.name) && Objects.equals(description, that.description);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, description);
        }
        
        @Override
        public String toString() {
            return String.format("Translation{name='%s', description='%s'}", name, description);
        }
    }
    
    /**
     * Creates a new SpellTranslations with English as the default language
     */
    public SpellTranslations(String englishName, String englishDescription) {
        this.translations = new LinkedHashMap<>();
        this.translations.put("English", new Translation(englishName, englishDescription));
    }
    
    /**
     * Creates SpellTranslations from an existing map of translations
     */
    public SpellTranslations(Map<String, Translation> translations) {
        this.translations = new LinkedHashMap<>();
        
        // Ensure English is always first
        if (translations.containsKey("English")) {
            this.translations.put("English", translations.get("English"));
        } else {
            // If no English translation, create an empty one
            this.translations.put("English", new Translation("", ""));
        }
        
        // Add other languages in alphabetical order
        translations.entrySet().stream()
            .filter(entry -> !entry.getKey().equals("English"))
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> this.translations.put(entry.getKey(), entry.getValue()));
    }
    
    /**
     * Get the primary English name
     */
    public String getEnglishName() {
        return translations.get("English").getName();
    }
    
    /**
     * Get the primary English description
     */
    public String getEnglishDescription() {
        return translations.get("English").getDescription();
    }
    
    /**
     * Get translation for a specific language
     */
    public Optional<Translation> getTranslation(String language) {
        return Optional.ofNullable(translations.get(language));
    }
    
    /**
     * Get all available languages
     */
    public Set<String> getAvailableLanguages() {
        return new LinkedHashSet<>(translations.keySet());
    }
    
    /**
     * Get all translations as an immutable map
     */
    public Map<String, Translation> getAllTranslations() {
        return Collections.unmodifiableMap(translations);
    }
    
    /**
     * Add or update a translation for a specific language
     */
    public SpellTranslations withTranslation(String language, String name, String description) {
        if (language == null || language.trim().isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }
        
        Map<String, Translation> newTranslations = new LinkedHashMap<>(this.translations);
        newTranslations.put(language.trim(), new Translation(name, description));
        return new SpellTranslations(newTranslations);
    }
    
    /**
     * Remove a translation for a specific language (except English)
     */
    public SpellTranslations withoutTranslation(String language) {
        if ("English".equals(language)) {
            throw new IllegalArgumentException("Cannot remove English translation");
        }
        
        Map<String, Translation> newTranslations = new LinkedHashMap<>(this.translations);
        newTranslations.remove(language);
        return new SpellTranslations(newTranslations);
    }
    
    /**
     * Update the English translation
     */
    public SpellTranslations withEnglishTranslation(String name, String description) {
        return withTranslation("English", name, description);
    }
    
    /**
     * Check if a language exists
     */
    public boolean hasLanguage(String language) {
        return translations.containsKey(language);
    }
    
    /**
     * Get the number of translations
     */
    public int getLanguageCount() {
        return translations.size();
    }
    
    /**
     * Check if only English translation exists
     */
    public boolean hasOnlyEnglish() {
        return translations.size() == 1 && translations.containsKey("English");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpellTranslations that = (SpellTranslations) obj;
        return Objects.equals(translations, that.translations);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(translations);
    }
    
    @Override
    public String toString() {
        return String.format("SpellTranslations{languages=%s, english='%s'}", 
            translations.keySet(), getEnglishName());
    }
} 