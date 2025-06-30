package com.ff8.application.ports.primary;

import com.ff8.application.dto.ExportRequestDTO;
import com.ff8.application.dto.ExportResultDTO;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.Language;
import com.ff8.domain.services.ExportValidationService;

import java.util.Collection;
import java.util.List;

/**
 * Primary port for localized export operations.
 * Defines the contract for exporting newly created magic spells with multi-language support.
 */
public interface LocalizedExportUseCase {
    
    /**
     * Export newly created magic spells to binary and resource files
     * 
     * @param request Export request containing target directory, filename, and options
     * @return Result of the export operation with success status and details
     */
    ExportResultDTO exportNewlyCreatedMagic(ExportRequestDTO request);
    
    /**
     * Get all required languages across newly created spells
     * 
     * @return List of languages that have translations in newly created spells
     */
    List<Language> getRequiredLanguages();
    
    /**
     * Validate export data before performing the actual export
     * 
     * @return Validation result with errors, warnings, and export summary
     */
    ExportValidationService.ExportValidationResult validateExportData();
    
    /**
     * Get all newly created magic spells that would be included in export
     * 
     * @return Collection of newly created magic data
     */
    Collection<MagicData> getNewlyCreatedMagic();
    
    /**
     * Check if there are any newly created magic spells available for export
     * 
     * @return true if there are newly created spells, false otherwise
     */
    boolean hasNewlyCreatedMagic();
    
    /**
     * Get a preview of what would be exported without actually performing the export
     * 
     * @param request Export request to preview
     * @return Preview information including validation results and estimated file sizes
     */
    ExportPreviewDTO getExportPreview(ExportRequestDTO request);
    
    /**
     * Preview information for export operations
     */
    record ExportPreviewDTO(
        boolean canExport,
        int spellCount,
        List<Language> requiredLanguages,
        List<String> errors,
        List<String> warnings,
        long estimatedTotalSize,
        List<String> filesToCreate
    ) {
        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }
        
        public String getPreviewSummary() {
            if (!canExport) {
                return "Cannot export: " + errors.size() + " error(s) found";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Ready to export ").append(spellCount).append(" spell(s) ");
            sb.append("in ").append(requiredLanguages.size()).append(" language(s)");
            
            if (hasWarnings()) {
                sb.append(" (").append(warnings.size()).append(" warning(s))");
            }
            
            return sb.toString();
        }
    }
} 