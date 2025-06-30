package com.ff8.application.services;

import com.ff8.application.dto.ExportRequestDTO;
import com.ff8.application.dto.ExportResultDTO;
import com.ff8.application.ports.primary.LocalizedExportUseCase;
import com.ff8.application.ports.secondary.BinaryExportPort;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.ResourceFileGeneratorPort;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.Language;
import com.ff8.domain.events.ExportCompletedEvent;
import com.ff8.domain.events.ExportFailedEvent;
import com.ff8.domain.events.ExportStartedEvent;
import com.ff8.domain.observers.AbstractSubject;
import com.ff8.domain.services.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for localized export operations.
 * Orchestrates the complex export workflow including validation, text encoding, 
 * layout calculation, and file generation.
 */
public class LocalizedExportService extends AbstractSubject implements LocalizedExportUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalizedExportService.class);
    
    private final MagicRepository magicRepository;
    private final TextEncodingService textEncodingService;
    private final TextOffsetCalculationService textOffsetCalculationService;
    private final LanguageValidationService languageValidationService;
    private final ExportValidationService exportValidationService;
    private final ResourceFileGeneratorPort resourceFileGenerator;
    private final BinaryExportPort binaryExportPort;
    
    public LocalizedExportService(
            MagicRepository magicRepository,
            TextEncodingService textEncodingService,
            TextOffsetCalculationService textOffsetCalculationService,
            LanguageValidationService languageValidationService,
            ExportValidationService exportValidationService,
            ResourceFileGeneratorPort resourceFileGenerator,
            BinaryExportPort binaryExportPort) {
        this.magicRepository = magicRepository;
        this.textEncodingService = textEncodingService;
        this.textOffsetCalculationService = textOffsetCalculationService;
        this.languageValidationService = languageValidationService;
        this.exportValidationService = exportValidationService;
        this.resourceFileGenerator = resourceFileGenerator;
        this.binaryExportPort = binaryExportPort;
    }
    
    @Override
    public ExportResultDTO exportNewlyCreatedMagic(ExportRequestDTO request) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting localized export for base filename: {}", request.baseFileName());
        
        try {
            // Emit export started event
            notifyObservers(new ExportStartedEvent(request.baseFileName(), LocalDateTime.now()));
            
            // Step 1: Collect newly created magic data
            Collection<MagicData> newlyCreatedMagic = getNewlyCreatedMagic();
            if (newlyCreatedMagic.isEmpty()) {
                logger.warn("No newly created magic found for export");
                return ExportResultDTO.validationFailure(
                    List.of("No newly created magic spells found to export"),
                    List.of()
                );
            }
            
            // Step 2: Validate export data
            ExportValidationService.ExportValidationResult validationResult = 
                exportValidationService.validateForExport(newlyCreatedMagic);
            
            if (!validationResult.isValid()) {
                logger.error("Export validation failed with {} errors", validationResult.getErrors().size());
                ExportResultDTO result = ExportResultDTO.validationFailure(
                    validationResult.getErrors(), 
                    validationResult.getWarnings()
                );
                notifyObservers(new ExportFailedEvent(request.baseFileName(), result.errors(), LocalDateTime.now()));
                return result;
            }
            
            // Step 3: Prepare translations with encoding and fallback
            Map<Integer, SpellTranslations> spellTranslations = prepareTranslations(newlyCreatedMagic);
            
            // Step 4: Calculate text layout
            TextOffsetCalculationService.TextLayoutResult textLayout = 
                textOffsetCalculationService.calculateTextLayout(spellTranslations);
            
            // Step 5: Generate resource files
            ResourceFileGeneratorPort.ResourceGenerationResult resourceResult = 
                resourceFileGenerator.generateResourceFiles(
                    spellTranslations, textLayout, request.targetDirectory(), request.baseFileName()
                );
            
            if (!resourceResult.success()) {
                logger.error("Resource file generation failed");
                List<String> errors = resourceResult.errors().values().stream().toList();
                ExportResultDTO result = ExportResultDTO.failure(errors, validationResult.getWarnings(), 
                    System.currentTimeMillis() - startTime);
                notifyObservers(new ExportFailedEvent(request.baseFileName(), errors, LocalDateTime.now()));
                return result;
            }
            
            // Step 6: Generate binary file
            BinaryExportPort.BinaryGenerationResult binaryResult = 
                binaryExportPort.generateBinaryFile(
                    newlyCreatedMagic, textLayout, request.getBinaryFilePath()
                );
            
            if (!binaryResult.success()) {
                logger.error("Binary file generation failed: {}", binaryResult.error());
                List<String> errors = List.of("Binary file generation failed: " + binaryResult.error());
                ExportResultDTO result = ExportResultDTO.failure(errors, validationResult.getWarnings(),
                    System.currentTimeMillis() - startTime);
                notifyObservers(new ExportFailedEvent(request.baseFileName(), errors, LocalDateTime.now()));
                return result;
            }
            
            // Step 7: Build successful result
            List<Path> allCreatedFiles = new ArrayList<>();
            allCreatedFiles.addAll(resourceResult.createdFiles().values());
            allCreatedFiles.add(binaryResult.createdFile());
            
            ExportResultDTO.ExportSummaryDTO summary = new ExportResultDTO.ExportSummaryDTO(
                newlyCreatedMagic.size(),
                allCreatedFiles.size(),
                textLayout.getRequiredLanguages().stream()
                    .map(Language::getDisplayName).collect(Collectors.toSet()),
                resourceResult.totalBytesWritten() + binaryResult.bytesWritten(),
                request.baseFileName()
            );
            
            long duration = System.currentTimeMillis() - startTime;
            ExportResultDTO result = ExportResultDTO.success(validationResult.getWarnings(), summary, allCreatedFiles, duration);
            
            logger.info("Export completed successfully in {} ms. Created {} files totaling {} bytes", 
                duration, allCreatedFiles.size(), summary.totalFileSize());
            
            // Emit export completed event
            notifyObservers(new ExportCompletedEvent(request.baseFileName(), allCreatedFiles, LocalDateTime.now()));
            
            return result;
            
        } catch (Exception e) {
            logger.error("Unexpected error during export", e);
            List<String> errors = List.of("Unexpected error during export: " + e.getMessage());
            ExportResultDTO result = ExportResultDTO.failure(errors, List.of(), 
                System.currentTimeMillis() - startTime);
            notifyObservers(new ExportFailedEvent(request.baseFileName(), errors, LocalDateTime.now()));
            return result;
        }
    }
    
    @Override
    public List<Language> getRequiredLanguages() {
        Collection<MagicData> newlyCreatedMagic = getNewlyCreatedMagic();
        Set<Language> languages = new LinkedHashSet<>();
        
        // Always include English
        languages.add(Language.ENGLISH);
        
        for (MagicData magic : newlyCreatedMagic) {
            SpellTranslations translations = magic.getTranslations();
            if (translations != null) {
                for (String languageName : translations.getAvailableLanguages()) {
                    languages.add(Language.fromDisplayName(languageName));
                }
            }
        }
        
        return new ArrayList<>(languages);
    }
    
    @Override
    public ExportValidationService.ExportValidationResult validateExportData() {
        Collection<MagicData> newlyCreatedMagic = getNewlyCreatedMagic();
        return exportValidationService.validateForExport(newlyCreatedMagic);
    }
    
    @Override
    public Collection<MagicData> getNewlyCreatedMagic() {
        return magicRepository.findAll().stream()
            .filter(MagicData::isNewlyCreated)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasNewlyCreatedMagic() {
        return !getNewlyCreatedMagic().isEmpty();
    }
    
    @Override
    public ExportPreviewDTO getExportPreview(ExportRequestDTO request) {
        try {
            Collection<MagicData> newlyCreatedMagic = getNewlyCreatedMagic();
            
            if (newlyCreatedMagic.isEmpty()) {
                return new ExportPreviewDTO(
                    false, 0, List.of(),
                    List.of("No newly created magic spells found to export"),
                    List.of(), 0, List.of()
                );
            }
            
            // Validate the export
            ExportValidationService.ExportValidationResult validation = 
                exportValidationService.validateForExport(newlyCreatedMagic);
            
            List<Language> requiredLanguages = getRequiredLanguages();
            
            // Calculate estimated file size
            long estimatedSize = 0;
            estimatedSize += binaryExportPort.calculateBinaryFileSize(newlyCreatedMagic);
            
            // Estimate resource file sizes (rough calculation)
            for (MagicData magic : newlyCreatedMagic) {
                SpellTranslations translations = magic.getTranslations();
                if (translations != null) {
                    for (Language language : requiredLanguages) {
                        SpellTranslations.Translation translation = translations.getTranslation(language.getDisplayName())
                            .orElse(translations.getTranslation("English").orElse(new SpellTranslations.Translation("", "")));
                        estimatedSize += textEncodingService.getEncodedLength(translation.getName()) + 1;
                        estimatedSize += textEncodingService.getEncodedLength(translation.getDescription()) + 1;
                    }
                }
            }
            
            // Build list of files that would be created
            List<String> filesToCreate = new ArrayList<>();
            filesToCreate.add(request.baseFileName() + ".bin");
            for (Language language : requiredLanguages) {
                filesToCreate.add(request.baseFileName() + "_" + language.getDisplayName().toLowerCase() + ".resources.bin");
            }
            
            return new ExportPreviewDTO(
                validation.isValid(),
                newlyCreatedMagic.size(),
                requiredLanguages,
                validation.getErrors(),
                validation.getWarnings(),
                estimatedSize,
                filesToCreate
            );
            
        } catch (Exception e) {
            logger.error("Error generating export preview", e);
            return new ExportPreviewDTO(
                false, 0, List.of(),
                List.of("Error generating preview: " + e.getMessage()),
                List.of(), 0, List.of()
            );
        }
    }
    
    /**
     * Prepare translations with encoding and English fallback
     */
    private Map<Integer, SpellTranslations> prepareTranslations(Collection<MagicData> newlyCreatedMagic) {
        Map<Integer, SpellTranslations> spellTranslations = new LinkedHashMap<>();
        
        for (MagicData magic : newlyCreatedMagic) {
            spellTranslations.put(magic.getIndex(), magic.getTranslations());
        }
        
        // Apply English fallback rules
        Set<Language> requiredLanguages = getRequiredLanguages().stream().collect(Collectors.toSet());
        spellTranslations = languageValidationService.applyEnglishFallback(spellTranslations, requiredLanguages);
        
        return spellTranslations;
    }
} 