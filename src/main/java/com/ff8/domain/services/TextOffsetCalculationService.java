package com.ff8.domain.services;

import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;

import java.util.*;

/**
 * Service for calculating text offsets in contiguous layout for multi-language resource files.
 * Ensures consistent positioning across all language files as required by FF8 format.
 */
public class TextOffsetCalculationService {
    
    private static final int BINARY_OFFSET_ADJUSTMENT = 511 + 1584; // 511 is the offset for the last spell description, 1584 is the offset for the last spell name (Official English Kernel)
    
    /**
     * Represents the layout information for a single spell's text data
     */
    public static class SpellTextLayout {
        private final int nameOffset;
        private final int descriptionOffset;
        private final int maxNameLength;
        private final int maxDescriptionLength;
        
        public SpellTextLayout(int nameOffset, int descriptionOffset, int maxNameLength, int maxDescriptionLength) {
            this.nameOffset = nameOffset;
            this.descriptionOffset = descriptionOffset;
            this.maxNameLength = maxNameLength;
            this.maxDescriptionLength = maxDescriptionLength;
        }
        
        public int getNameOffset() { return nameOffset; }
        public int getDescriptionOffset() { return descriptionOffset; }
        public int getMaxNameLength() { return maxNameLength; }
        public int getMaxDescriptionLength() { return maxDescriptionLength; }
        
        /**
         * Get the binary offset for spell name (with 511-byte adjustment)
         */
        public int getBinaryNameOffset() {
            return nameOffset + BINARY_OFFSET_ADJUSTMENT;
        }
        
        /**
         * Get the binary offset for spell description (with 511-byte adjustment)
         */
        public int getBinaryDescriptionOffset() {
            return descriptionOffset + BINARY_OFFSET_ADJUSTMENT;
        }
    }
    
    /**
     * Represents the complete text layout for all spells and languages
     */
    public static class TextLayoutResult {
        private final Map<Integer, SpellTextLayout> spellLayouts;
        private final Set<Language> requiredLanguages;
        private final int totalFileSize;
        
        public TextLayoutResult(Map<Integer, SpellTextLayout> spellLayouts, Set<Language> requiredLanguages, int totalFileSize) {
            this.spellLayouts = Collections.unmodifiableMap(spellLayouts);
            this.requiredLanguages = Collections.unmodifiableSet(requiredLanguages);
            this.totalFileSize = totalFileSize;
        }
        
        public Map<Integer, SpellTextLayout> getSpellLayouts() { return spellLayouts; }
        public Set<Language> getRequiredLanguages() { return requiredLanguages; }
        public int getTotalFileSize() { return totalFileSize; }
        
        public SpellTextLayout getLayoutForSpell(int spellIndex) {
            return spellLayouts.get(spellIndex);
        }
    }
    
    private final TextEncodingService textEncodingService;
    
    public TextOffsetCalculationService(TextEncodingService textEncodingService) {
        this.textEncodingService = textEncodingService;
    }
    
    /**
     * Calculate the complete text layout for all spells and languages.
     * This implements the two-pass algorithm described in the implementation plan.
     * 
     * @param spellTranslations Map of spell index to translations
     * @return Complete layout information for all spells and languages
     */
    public TextLayoutResult calculateTextLayout(Map<Integer, SpellTranslations> spellTranslations) {
        if (spellTranslations.isEmpty()) {
            return new TextLayoutResult(Collections.emptyMap(), Collections.emptySet(), 0);
        }
        
        // Pass 1: Analysis - determine maximum lengths and required languages
        AnalysisResult analysis = analyzeTranslations(spellTranslations);
        
        // Pass 2: Layout - calculate final positioning
        return generateLayout(spellTranslations, analysis);
    }
    
    /**
     * Analysis result from the first pass
     */
    private static class AnalysisResult {
        final Set<Language> requiredLanguages;
        final Map<Integer, Integer> maxNameLengths;
        final Map<Integer, Integer> maxDescriptionLengths;
        
        AnalysisResult(Set<Language> requiredLanguages, 
                      Map<Integer, Integer> maxNameLengths, 
                      Map<Integer, Integer> maxDescriptionLengths) {
            this.requiredLanguages = requiredLanguages;
            this.maxNameLengths = maxNameLengths;
            this.maxDescriptionLengths = maxDescriptionLengths;
        }
    }
    
    /**
     * Pass 1: Analyze all translations to determine requirements
     */
    private AnalysisResult analyzeTranslations(Map<Integer, SpellTranslations> spellTranslations) {
        Set<Language> requiredLanguages = new LinkedHashSet<>();
        Map<Integer, Integer> maxNameLengths = new HashMap<>();
        Map<Integer, Integer> maxDescriptionLengths = new HashMap<>();
        
        // Always include English as primary language
        requiredLanguages.add(Language.ENGLISH);
        
        for (Map.Entry<Integer, SpellTranslations> entry : spellTranslations.entrySet()) {
            int spellIndex = entry.getKey();
            SpellTranslations translations = entry.getValue();
            
            int maxNameLength = 0;
            int maxDescLength = 0;
            
            // Analyze all translations for this spell
            for (String languageName : translations.getAvailableLanguages()) {
                Language language = Language.fromDisplayName(languageName);
                requiredLanguages.add(language);
                
                SpellTranslations.Translation translation = translations.getTranslation(languageName).orElse(
                    new SpellTranslations.Translation("", ""));
                
                // Calculate encoded lengths
                String encodedName = textEncodingService.encipherCaesarCode(translation.getName());
                String encodedDesc = textEncodingService.encipherCaesarCode(translation.getDescription());
                
                maxNameLength = Math.max(maxNameLength, encodedName.length() + 1); // +1 for null terminator
                maxDescLength = Math.max(maxDescLength, encodedDesc.length() + 1); // +1 for null terminator
            }
            
            maxNameLengths.put(spellIndex, maxNameLength);
            maxDescriptionLengths.put(spellIndex, maxDescLength);
        }
        
        return new AnalysisResult(requiredLanguages, maxNameLengths, maxDescriptionLengths);
    }
    
    /**
     * Pass 2: Generate the final layout with calculated offsets
     */
    private TextLayoutResult generateLayout(Map<Integer, SpellTranslations> spellTranslations, AnalysisResult analysis) {
        Map<Integer, SpellTextLayout> spellLayouts = new LinkedHashMap<>();
        
        int currentOffset = 0;
        
        // Sort spell indices for consistent ordering
        List<Integer> sortedSpellIndices = new ArrayList<>(spellTranslations.keySet());
        Collections.sort(sortedSpellIndices);
        
        for (int spellIndex : sortedSpellIndices) {
            int maxNameLength = analysis.maxNameLengths.get(spellIndex);
            int maxDescLength = analysis.maxDescriptionLengths.get(spellIndex);
            
            // Calculate offsets for this spell
            int nameOffset = currentOffset;
            int descriptionOffset = currentOffset + maxNameLength;
            
            spellLayouts.put(spellIndex, new SpellTextLayout(
                nameOffset, descriptionOffset, maxNameLength, maxDescLength));
            
            // Move to next spell position
            currentOffset += maxNameLength + maxDescLength;
        }
        
        return new TextLayoutResult(spellLayouts, analysis.requiredLanguages, currentOffset);
    }
    
    /**
     * Calculate padding needed to align text at specific offsets
     * 
     * @param currentLength Current text length
     * @param targetLength Target length with padding
     * @return Number of null bytes needed for padding
     */
    public int calculatePadding(int currentLength, int targetLength) {
        return Math.max(0, targetLength - currentLength);
    }
    
    /**
     * Get the binary offset adjustment constant
     */
    public static int getBinaryOffsetAdjustment() {
        return BINARY_OFFSET_ADJUSTMENT;
    }
} 