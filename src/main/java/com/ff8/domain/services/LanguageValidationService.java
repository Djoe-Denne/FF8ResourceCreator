package com.ff8.domain.services;

import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;

import java.util.*;

/**
 * Service for validating language consistency and enforcing English fallback rules.
 * Ensures that all exported spells meet the language requirements.
 */
public class LanguageValidationService {
    
    /**
     * Validation result containing any issues found during validation
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> issues;
        private final Set<Language> requiredLanguages;
        private final Map<Language, Integer> languageSpellCounts;
        
        public ValidationResult(boolean isValid, List<String> issues, Set<Language> requiredLanguages, 
                              Map<Language, Integer> languageSpellCounts) {
            this.isValid = isValid;
            this.issues = Collections.unmodifiableList(issues);
            this.requiredLanguages = Collections.unmodifiableSet(requiredLanguages);
            this.languageSpellCounts = Collections.unmodifiableMap(languageSpellCounts);
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getIssues() { return issues; }
        public Set<Language> getRequiredLanguages() { return requiredLanguages; }
        public Map<Language, Integer> getLanguageSpellCounts() { return languageSpellCounts; }
        
        public String getIssuesSummary() {
            return String.join("\n", issues);
        }
    }
    
    private final TextEncodingService textEncodingService;
    
    public LanguageValidationService(TextEncodingService textEncodingService) {
        this.textEncodingService = textEncodingService;
    }
    
    /**
     * Validate that spell translations meet export requirements.
     * Checks for English fallback rules and language consistency.
     * 
     * @param spellTranslations Map of spell index to translations
     * @return Validation result with any issues found
     */
    public ValidationResult validateSpellTranslations(Map<Integer, SpellTranslations> spellTranslations) {
        List<String> issues = new ArrayList<>();
        Set<Language> requiredLanguages = new LinkedHashSet<>();
        Map<Language, Integer> languageSpellCounts = new HashMap<>();
        
        if (spellTranslations.isEmpty()) {
            issues.add("No spell translations provided for export");
            return new ValidationResult(false, issues, requiredLanguages, languageSpellCounts);
        }
        
        // Always include English as required
        requiredLanguages.add(Language.ENGLISH);
        
        // First pass: collect all used languages and count spells per language
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            int spellIndex = entry.getKey();
            SpellTranslations translations = entry.getValue();
            
            // Validate individual spell
            ValidationResult spellResult = validateSingleSpell(spellIndex, translations);
            issues.addAll(spellResult.getIssues());
            
            // Count languages
            for (String languageName : translations.getAvailableLanguages()) {
                Language language = Language.fromDisplayName(languageName);
                requiredLanguages.add(language);
                languageSpellCounts.merge(language, 1, Integer::sum);
            }
        }
        
        // Second pass: validate completeness and consistency
        validateLanguageCompleteness(spellTranslations, requiredLanguages, issues);
        validateEnglishFallbackRules(spellTranslations, requiredLanguages, issues);
        
        boolean isValid = issues.isEmpty();
        return new ValidationResult(isValid, issues, requiredLanguages, languageSpellCounts);
    }
    
    /**
     * Validate a single spell's translations
     */
    private ValidationResult validateSingleSpell(int spellIndex, SpellTranslations translations) {
        List<String> issues = new ArrayList<>();
        
        if (translations == null) {
            issues.add("Spell " + spellIndex + ": No translations provided");
            return new ValidationResult(false, issues, Collections.emptySet(), Collections.emptyMap());
        }
        
        // Validate English translation exists and is not empty
        if (!translations.hasLanguage("English")) {
            issues.add("Spell " + spellIndex + ": Missing English translation");
        } else {
            SpellTranslations.Translation englishTranslation = translations.getTranslation("English").orElse(null);
            if (englishTranslation == null || englishTranslation.getName().trim().isEmpty()) {
                issues.add("Spell " + spellIndex + ": English spell name cannot be empty");
            }
            
            // Validate encoding compatibility
            if (englishTranslation != null) {
                if (!textEncodingService.isTextCompatibleWithEncoding(englishTranslation.getName())) {
                    issues.add("Spell " + spellIndex + ": English name contains characters incompatible with Caesar cipher encoding");
                }
                if (!textEncodingService.isTextCompatibleWithEncoding(englishTranslation.getDescription())) {
                    issues.add("Spell " + spellIndex + ": English description contains characters incompatible with Caesar cipher encoding");
                }
            }
        }
        
        // Validate all other translations
        for (String languageName : translations.getAvailableLanguages()) {
            if (!"English".equals(languageName)) {
                SpellTranslations.Translation translation = translations.getTranslation(languageName).orElse(null);
                if (translation != null) {
                    if (!textEncodingService.isTextCompatibleWithEncoding(translation.getName())) {
                        issues.add("Spell " + spellIndex + " (" + languageName + "): Name contains characters incompatible with Caesar cipher encoding");
                    }
                    if (!textEncodingService.isTextCompatibleWithEncoding(translation.getDescription())) {
                        issues.add("Spell " + spellIndex + " (" + languageName + "): Description contains characters incompatible with Caesar cipher encoding");
                    }
                }
            }
        }
        
        boolean isValid = issues.isEmpty();
        return new ValidationResult(isValid, issues, Collections.emptySet(), Collections.emptyMap());
    }
    
    /**
     * Validate that all spells have translations for all required languages
     */
    private void validateLanguageCompleteness(Map<Integer, SpellTranslations> spellTranslations, 
                                            Set<Language> requiredLanguages, List<String> issues) {
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            int spellIndex = entry.getKey();
            SpellTranslations translations = entry.getValue();
            
            for (Language language : requiredLanguages) {
                if (!translations.hasLanguage(language.getDisplayName())) {
                    // Missing translation - will need English fallback
                    issues.add("Spell " + spellIndex + ": Missing " + language.getDisplayName() + 
                              " translation (English fallback will be used)");
                }
            }
        }
    }
    
    /**
     * Validate English fallback rules are properly implemented
     */
    private void validateEnglishFallbackRules(Map<Integer, SpellTranslations> spellTranslations, 
                                            Set<Language> requiredLanguages, List<String> issues) {
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            int spellIndex = entry.getKey();
            SpellTranslations translations = entry.getValue();
            
            // Ensure English translation exists for fallback
            if (!translations.hasLanguage("English")) {
                issues.add("Spell " + spellIndex + ": Cannot apply English fallback - no English translation available");
                continue;
            }
            
            SpellTranslations.Translation englishTranslation = translations.getTranslation("English").orElse(null);
            if (englishTranslation == null || englishTranslation.getName().trim().isEmpty()) {
                issues.add("Spell " + spellIndex + ": Cannot apply English fallback - English translation is empty");
            }
        }
    }
    
    /**
     * Apply English fallback rules to create complete translations for all required languages
     */
    public Map<Integer, SpellTranslations> applyEnglishFallback(Map<Integer, SpellTranslations> spellTranslations, 
                                                              Set<Language> requiredLanguages) {
        Map<Integer, SpellTranslations> result = new LinkedHashMap<>();
        
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            int spellIndex = entry.getKey();
            SpellTranslations originalTranslations = entry.getValue();
            
            SpellTranslations.Translation englishTranslation = originalTranslations.getTranslation("English")
                .orElse(new SpellTranslations.Translation("", ""));
            
            SpellTranslations updatedTranslations = originalTranslations;
            
            // Add English fallback for missing languages
            for (Language language : requiredLanguages) {
                if (!originalTranslations.hasLanguage(language.getDisplayName())) {
                    updatedTranslations = updatedTranslations.withTranslation(
                        language.getDisplayName(), 
                        englishTranslation.getName(), 
                        englishTranslation.getDescription()
                    );
                }
            }
            
            result.put(spellIndex, updatedTranslations);
        }
        
        return result;
    }
    
    /**
     * Check if a language should be included in export based on usage threshold
     */
    public boolean shouldIncludeLanguage(Language language, Map<Language, Integer> languageSpellCounts, 
                                       int totalSpellCount) {
        if (language.isPrimary()) {
            return true; // Always include English
        }
        
        int spellsWithLanguage = languageSpellCounts.getOrDefault(language, 0);
        return spellsWithLanguage > 0; // Include if at least one spell has this language
    }
    
    /**
     * Get summary of language usage across all spells
     */
    public String getLanguageUsageSummary(Map<Language, Integer> languageSpellCounts, int totalSpellCount) {
        StringBuilder summary = new StringBuilder();
        summary.append("Language Usage Summary:\n");
        
        for (Map.Entry<Language, Integer> entry : languageSpellCounts.entrySet()) {
            Language language = entry.getKey();
            int count = entry.getValue();
            double percentage = (count * 100.0) / totalSpellCount;
            
            summary.append(String.format("- %s: %d/%d spells (%.1f%%)\n", 
                language.getDisplayName(), count, totalSpellCount, percentage));
        }
        
        return summary.toString();
    }
} 