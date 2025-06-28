# FF8 Magic Editor - Localized Export Implementation Plan

## Overview

This document outlines the implementation strategy for adding localized text resource export functionality to the FF8 Magic Editor. The feature enables users to save newly created magic spells with multi-language support, generating separate resource files for each language while maintaining binary format compatibility with FF8's kernel structure.

## Current System Analysis

### Existing Architecture Strengths

The current hexagonal architecture provides excellent foundation for this enhancement:

**Domain Layer**: Already contains `SpellTranslations.java` entity, indicating prior consideration for localization support. The `MagicData` entity includes text offset fields (`offsetSpellName`, `offsetSpellDescription`) and extracted text fields that can be extended for multi-language support.

**Application Layer**: The `MagicEditorUseCase` and `KernelFileService` provide clear boundaries for extending save operations. The DTO pattern allows clean separation between internal domain representation and external file formats.

**Infrastructure Layer**: The `KernelBinaryParser` already handles text extraction and offset calculation, providing a foundation for generating new binary structures with calculated offsets.

### Current Limitations

The existing system focuses on reading and editing existing kernel data but lacks:
- Multi-language text management in the domain model
- Export-specific use cases for newly created content
- Resource file generation capabilities
- Offset calculation for newly created text sections

## Proposed Solution Architecture

### Domain Layer Enhancements

**SpellTranslations Entity Extension**
The existing `SpellTranslations` entity will be enhanced to support multiple languages with a map-based structure:

```java
Map<Language, Translation> translations
```

Where each `Translation` contains both spell name and description for a specific language. This maintains domain purity while providing rich behavior for text management operations.

**New Domain Services**
- `TextOffsetCalculationService`: Handles offset calculations for contiguous text layout
- `LanguageValidationService`: Ensures language consistency and English fallback rules
- `ExportValidationService`: Validates that newly created spells have sufficient translation data

### Application Layer Additions

**New Primary Port**
`LocalizedExportUseCase` will define the contract for exporting newly created magic with localized resources:

```java
public interface LocalizedExportUseCase {
    ExportResult exportNewlyCreatedMagic(ExportRequest request);
    List<Language> getRequiredLanguages(Collection<MagicData> newSpells);
    ValidationResult validateExportData(Collection<MagicData> newSpells);
}
```

**New Secondary Ports**
- `ResourceFileGeneratorPort`: Abstracts resource file creation
- `BinaryExportPort`: Handles binary file generation for new magic data
- `TextLayoutPort`: Manages text positioning and offset calculations

**Service Implementation**
`LocalizedExportService` will orchestrate the complex export workflow:

1. **Data Collection**: Gather all newly created magic spells from repository
2. **Language Analysis**: Determine required languages across all new spells
3. **Text Layout Planning**: Calculate optimal text positioning for each language
4. **Offset Calculation**: Determine binary offsets with 511-byte constant adjustment
5. **File Generation**: Create binary and resource files through infrastructure adapters

### Infrastructure Layer Implementation

**New Secondary Adapters**

**ResourceFileGenerator**: Implements contiguous text file generation following the specified rules:
- Maintains consistent offset positioning across languages
- Handles null-termination and padding
- Ensures English fallback for missing translations
- Generates properly formatted resource files

**BinaryExportAdapter**: Creates binary files for newly created magic:
- Calculates text offsets with 511-byte adjustment
- Preserves exact binary format compatibility
- Handles serialization of magic data structures
- Maintains kernel.bin format specifications

**TextLayoutCalculator**: Manages complex text positioning logic:
- Analyzes all translations to determine maximum text lengths
- Calculates padding requirements for offset alignment
- Ensures contiguous layout across multiple languages
- Optimizes file size while maintaining format requirements

## Technical Implementation Strategy

### Text Offset Calculation Algorithm

The system will implement a two-pass algorithm for text layout:

**Pass 1 - Analysis**: Examine all translations to determine:
- Maximum spell name length across all languages
- Maximum description length across all languages
- Required languages based on available translations
- Missing translations requiring English fallback

**Pass 2 - Layout**: Generate final text layout:
- Position spell names at calculated offsets
- Add appropriate null-termination and padding
- Ensure consistent positioning across language files
- Calculate binary offsets with 511-byte adjustment

### File Generation Workflow

**User Interaction Flow**:
1. User selects "Export New Magic..." from File menu
2. System presents folder selection dialog
3. User provides base filename for export
4. System validates export data and presents summary
5. User confirms export operation
6. System generates all required files

**Backend Processing**:
1. `LocalizedExportService` collects newly created magic data
2. Service analyzes required languages and validates completeness
3. `TextLayoutCalculator` determines optimal text positioning
4. `ResourceFileGenerator` creates language-specific resource files
5. `BinaryExportAdapter` generates binary file with calculated offsets
6. File system operations write all files to selected directory

### Error Handling and Validation

**Pre-Export Validation**:
- Verify at least one newly created spell exists
- Ensure all new spells have minimum required translations
- Validate text lengths don't exceed format limitations
- Check file system permissions for target directory

**Runtime Error Recovery**:
- Atomic file operations to prevent partial writes
- Rollback capability if any file generation fails
- Clear error messages for user understanding
- Logging for troubleshooting complex export scenarios

## Integration with Observer Pattern

The export functionality will integrate seamlessly with the existing observer pattern:

**Export Events**: New event types for export operations:
- `ExportStartedEvent`: Notifies UI of export initiation
- `ExportProgressEvent`: Updates progress during file generation
- `ExportCompletedEvent`: Signals successful export completion
- `ExportFailedEvent`: Handles error scenarios with recovery options

**UI Responsiveness**: The export process will maintain UI responsiveness through:
- Background processing for file generation
- Progress indicators for long-running operations
- Cancellation support for user control
- Status updates through observer notifications

## File Format Specifications

**Binary File Structure**:
- Standard magic data structure (60 bytes per spell)
- Text offset fields point to resource file positions + 511 bytes
- Maintains exact compatibility with FF8 kernel format
- Preserves all binary serialization requirements

**Resource File Format**:
- Contiguous null-terminated strings
- Consistent offset positioning across languages
- Optimal padding for alignment requirements
- UTF-8 encoding for international character support

## Testing Strategy

**Unit Testing**:
- Domain services for text layout and validation logic
- Application services for export workflow orchestration
- Infrastructure adapters for file generation accuracy

**Integration Testing**:
- Complete export workflow with various language combinations
- File format validation against FF8 specifications
- Error scenarios and recovery mechanisms

**User Acceptance Testing**:
- Export newly created magic with multiple languages
- Verify generated files work correctly in FF8
- Validate user interface responsiveness during export

## Performance Considerations

**Memory Management**:
- Stream-based file writing for large text sections
- Efficient string handling for multiple languages
- Minimal memory allocation during offset calculations

**Processing Optimization**:
- Single-pass text analysis where possible
- Cached calculations for repeated operations
- Lazy evaluation of complex layout algorithms

## Future Extensibility

The proposed architecture supports future enhancements:
- Additional languages through configuration
- Alternative export formats (XML, JSON)
- Batch export operations
- Import functionality for existing resource files
- Text editor integration for in-application translation

## Conclusion

This implementation plan maintains the established hexagonal architecture while adding sophisticated localized export capabilities. The design preserves domain purity, maintains clear separation of concerns, and provides a foundation for future localization enhancements. The solution addresses all specified requirements while ensuring compatibility with FF8's binary format specifications and the application's existing observer-driven UI architecture. 