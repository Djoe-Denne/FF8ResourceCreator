package com.ff8.domain.services;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;

import java.util.*;

/**
 * Service for validating that newly created spells have sufficient data for export.
 * Ensures all export requirements are met before file generation.
 */
public class ExportValidationService {
    
    /**
     * Comprehensive validation result for export operations
     */
    public static class ExportValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final List<String> warnings;
        private final ExportSummary summary;
        
        public ExportValidationResult(boolean isValid, List<String> errors, List<String> warnings, ExportSummary summary) {
            this.isValid = isValid;
            this.errors = Collections.unmodifiableList(errors);
            this.warnings = Collections.unmodifiableList(warnings);
            this.summary = summary;
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public ExportSummary getSummary() { return summary; }
        
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public boolean hasErrors() { return !errors.isEmpty(); }
        
        public String getErrorsSummary() {
            return String.join("\n", errors);
        }
        
        public String getWarningsSummary() {
            return String.join("\n", warnings);
        }
    }
    
    /**
     * Summary of what will be exported
     */
    public static class ExportSummary {
        private final int totalSpells;
        private final Set<Language> languages;
        private final Map<Language, Integer> spellsPerLanguage;
        private final int estimatedTotalFileSize;
        
        public ExportSummary(int totalSpells, Set<Language> languages, 
                           Map<Language, Integer> spellsPerLanguage, int estimatedTotalFileSize) {
            this.totalSpells = totalSpells;
            this.languages = Collections.unmodifiableSet(languages);
            this.spellsPerLanguage = Collections.unmodifiableMap(spellsPerLanguage);
            this.estimatedTotalFileSize = estimatedTotalFileSize;
        }
        
        public int getTotalSpells() { return totalSpells; }
        public Set<Language> getLanguages() { return languages; }
        public Map<Language, Integer> getSpellsPerLanguage() { return spellsPerLanguage; }
        public int getEstimatedTotalFileSize() { return estimatedTotalFileSize; }
        
        public int getLanguageCount() { return languages.size(); }
        public int getTotalFilesCount() { return languages.size() + 1; } // +1 for binary file
    }
    
    private final TextEncodingService textEncodingService;
    private final LanguageValidationService languageValidationService;
    
    public ExportValidationService(TextEncodingService textEncodingService, 
                                 LanguageValidationService languageValidationService) {
        this.textEncodingService = textEncodingService;
        this.languageValidationService = languageValidationService;
    }
    
    /**
     * Validate that the provided magic data can be exported successfully
     * 
     * @param newlyCreatedMagic Collection of newly created magic spells to export
     * @return Validation result with errors, warnings, and export summary
     */
    public ExportValidationResult validateForExport(Collection<MagicData> newlyCreatedMagic) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Basic validation
        if (newlyCreatedMagic == null || newlyCreatedMagic.isEmpty()) {
            errors.add("No newly created magic spells found to export");
            return new ExportValidationResult(false, errors, warnings, 
                new ExportSummary(0, Collections.emptySet(), Collections.emptyMap(), 0));
        }
        
        // Filter only newly created spells
        List<MagicData> actuallyNewSpells = newlyCreatedMagic.stream()
            .filter(MagicData::isNewlyCreated)
            .toList();
        
        if (actuallyNewSpells.isEmpty()) {
            errors.add("No newly created magic spells found (all spells appear to be from original kernel file)");
            return new ExportValidationResult(false, errors, warnings,
                new ExportSummary(0, Collections.emptySet(), Collections.emptyMap(), 0));
        }
        
        // Collect translations for validation
        Map<Integer, SpellTranslations> spellTranslations = new LinkedHashMap<>();
        for (MagicData magic : actuallyNewSpells) {
            spellTranslations.put(magic.getIndex(), magic.getTranslations());
        }
        
        // Validate individual spells
        validateIndividualSpells(actuallyNewSpells, errors, warnings);
        
        // Validate translations and language consistency
        LanguageValidationService.ValidationResult languageResult = 
            languageValidationService.validateSpellTranslations(spellTranslations);
        
        if (!languageResult.isValid()) {
            errors.addAll(languageResult.getIssues().stream()
                .filter(issue -> issue.contains("cannot") || issue.contains("Missing English"))
                .toList());
            warnings.addAll(languageResult.getIssues().stream()
                .filter(issue -> !issue.contains("cannot") && !issue.contains("Missing English"))
                .toList());
        }
        
        // Validate file size limits
        validateFileSizeLimits(spellTranslations, errors, warnings);
        
        // Generate summary
        ExportSummary summary = generateExportSummary(actuallyNewSpells, languageResult);
        
        boolean isValid = errors.isEmpty();
        return new ExportValidationResult(isValid, errors, warnings, summary);
    }
    
    /**
     * Validate individual spells for export readiness
     */
    private void validateIndividualSpells(List<MagicData> spells, List<String> errors, List<String> warnings) {
        for (MagicData magic : spells) {
            validateSingleSpell(magic, errors, warnings);
        }
    }
    
    /**
     * Validate a single spell for export readiness
     */
    private void validateSingleSpell(MagicData magic, List<String> errors, List<String> warnings) {
        String spellId = "Spell " + magic.getIndex() + " (" + magic.getSpellName() + ")";
        
        // Check if it's actually newly created
        if (!magic.isNewlyCreated()) {
            warnings.add(spellId + ": Not flagged as newly created, will be skipped");
            return;
        }
        
        // Validate translations exist
        SpellTranslations translations = magic.getTranslations();
        if (translations == null) {
            errors.add(spellId + ": No translations available");
            return;
        }
        
        // Validate English translation
        if (!translations.hasLanguage("English")) {
            errors.add(spellId + ": Missing English translation");
        } else {
            SpellTranslations.Translation englishTranslation = translations.getTranslation("English").orElse(null);
            if (englishTranslation == null || englishTranslation.getName().trim().isEmpty()) {
                errors.add(spellId + ": English spell name is empty");
            }
        }
        
        // Validate magic ID is reasonable
        if (magic.getMagicID() < 0 || magic.getMagicID() > 65535) {
            warnings.add(spellId + ": Magic ID " + magic.getMagicID() + " is outside normal range (0-65535)");
        }
        
        // Check for potential issues with spell properties
        validateSpellProperties(magic, warnings);
    }
    
    /**
     * Validate spell properties for potential issues
     */
    private void validateSpellProperties(MagicData magic, List<String> warnings) {
        String spellId = "Spell " + magic.getIndex() + " (" + magic.getSpellName() + ")";
        
        // Check for unusual values that might indicate issues
        if (magic.getSpellPower() > 255) {
            warnings.add(spellId + ": Spell power " + magic.getSpellPower() + " exceeds normal range (0-255)");
        }
        
        if (magic.getHitCount() > 16) {
            warnings.add(spellId + ": Hit count " + magic.getHitCount() + " is unusually high");
        }
        
        if (magic.getDrawResist() > 255) {
            warnings.add(spellId + ": Draw resist " + magic.getDrawResist() + " exceeds normal range (0-255)");
        }
    }
    
    /**
     * Validate that generated files won't exceed reasonable size limits
     */
    private void validateFileSizeLimits(Map<Integer, SpellTranslations> spellTranslations, 
                                      List<String> errors, List<String> warnings) {
        // Calculate estimated resource file sizes
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            SpellTranslations translations = entry.getValue();
            
            for (String languageName : translations.getAvailableLanguages()) {
                SpellTranslations.Translation translation = translations.getTranslation(languageName).orElse(null);
                if (translation != null) {
                    int nameLength = textEncodingService.getEncodedLength(translation.getName());
                    int descLength = textEncodingService.getEncodedLength(translation.getDescription());
                    
                    if (nameLength > 100) {
                        warnings.add("Spell " + entry.getKey() + " (" + languageName + "): Name is very long (" + nameLength + " characters)");
                    }
                    
                    if (descLength > 500) {
                        warnings.add("Spell " + entry.getKey() + " (" + languageName + "): Description is very long (" + descLength + " characters)");
                    }
                }
            }
        }
    }
    
    /**
     * Generate export summary with statistics
     */
    private ExportSummary generateExportSummary(List<MagicData> spells, 
                                               LanguageValidationService.ValidationResult languageResult) {
        int totalSpells = spells.size();
        Set<Language> languages = languageResult.getRequiredLanguages();
        Map<Language, Integer> spellsPerLanguage = languageResult.getLanguageSpellCounts();
        
        // Estimate total file size (rough calculation)
        int estimatedSize = calculateEstimatedFileSize(spells, languages);
        
        return new ExportSummary(totalSpells, languages, spellsPerLanguage, estimatedSize);
    }
    
    /**
     * Calculate estimated total file size for all generated files
     */
    private int calculateEstimatedFileSize(List<MagicData> spells, Set<Language> languages) {
        int binaryFileSize = spells.size() * 60; // 60 bytes per magic struct
        
        int resourceFilesSize = 0;
        for (MagicData spell : spells) {
            SpellTranslations translations = spell.getTranslations();
            if (translations != null) {
                for (Language language : languages) {
                    SpellTranslations.Translation translation = translations.getTranslation(language.getDisplayName())
                        .orElse(translations.getTranslation("English").orElse(new SpellTranslations.Translation("", "")));
                    
                    resourceFilesSize += textEncodingService.getEncodedLength(translation.getName()) + 1; // +1 for null terminator
                    resourceFilesSize += textEncodingService.getEncodedLength(translation.getDescription()) + 1; // +1 for null terminator
                }
            }
        }
        
        return binaryFileSize + resourceFilesSize;
    }
    
    /**
     * Check if export operation should proceed despite warnings
     */
    public boolean shouldProceedWithWarnings(ExportValidationResult result) {
        if (result.hasErrors()) {
            return false; // Never proceed with errors
        }
        
        // Could implement more sophisticated logic here
        // For now, allow proceeding with warnings
        return true;
    }
    
    /**
     * Get a user-friendly export summary for display
     */
    public String getExportSummaryForDisplay(ExportSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Export Summary:\n");
        sb.append("- Spells to export: ").append(summary.getTotalSpells()).append("\n");
        sb.append("- Languages: ").append(summary.getLanguageCount()).append(" (");
        sb.append(String.join(", ", summary.getLanguages().stream()
            .map(Language::getDisplayName).toList())).append(")\n");
        sb.append("- Total files: ").append(summary.getTotalFilesCount()).append(" (1 binary + ");
        sb.append(summary.getLanguageCount()).append(" resource files)\n");
        sb.append("- Estimated size: ").append(summary.getEstimatedTotalFileSize()).append(" bytes");
        
        return sb.toString();
    }
} 