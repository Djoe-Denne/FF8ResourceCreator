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

## Internationalization Strategy: Offset-Aligned Resource Files

### Core Concept

To achieve true internationalization while maintaining binary compatibility, the system employs a **split-architecture approach**:

- **Common Binary File**: Contains magic logic data (power, elements, status effects, targeting, etc.) with offset references
- **Language-Specific Resource Files**: Contain only text data (spell names and descriptions) with consistent offset positioning

### Offset Consistency Requirement

The critical constraint is that **each spell's text must start at identical offsets across all language files**. This ensures the binary file's offset references remain valid regardless of which language resource is loaded.

### Practical Implementation Example

Consider two spells with English and French translations:

**Spell Data:**
```
Spell 1: "Overflow" | "Huge amount of water coming from out of nowhere overflowing the room"
         "Inondation" | "Description plus courte"

Spell 2: "Heat" | "Shorter description"  
         "Chaleur" | "Vague de chaleur suffoquante"
```

**Text Layout Analysis:**
```
                    Name Length    Description Length
English:  Overflow      8 chars    Huge amount... 65 chars
French:   Inondation   10 chars    Description... 21 chars
          
English:  Heat          4 chars    Shorter desc.. 19 chars  
French:   Chaleur       7 chars    Vague de...    26 chars
```

**Resource File Generation with Padding:**

**English Resource File (magic_text_en.bin):**
```
Offset 0:   "Overflow\0\0\0.....Huge amount of water coming from out of nowhere overflowing the room\0"
Offset 80:  "Heat\0\0\0\0\0\0\0\0\0\0\0Shorter description\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"
```

**French Resource File (magic_text_fr.bin):**
```
Offset 0:   "Inondation\0\0\0\0\0\0\0Description plus courte\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"
Offset 80:  "Chaleur\0\0\0\0\0\0\0\0\0Vague de chaleur suffoquante\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"
```

Where `\0` represents null bytes and each spell's text block occupies exactly 80 bytes total (ensuring consistent offsets).

**Binary File References:**
```
Magic Data Entry 1:
  - spell_name_offset: 0
  - description_offset: 11  (after "Inondation\0" = 10 chars + 1 null)
  
Magic Data Entry 2:  
  - spell_name_offset: 80
  - description_offset: 91  (80 + 7 chars "Chaleur" + 4 padding nulls)
```

### Advanced Padding Strategy

**Maximum Length Calculation:**
For each spell position, calculate the maximum space needed across all languages:

```java
public class TextLayoutCalculator {
    
    public TextLayout calculateLayout(List<SpellTranslations> spells) {
        Map<Integer, SpellBlockLayout> layouts = new HashMap<>();
        
        for (int i = 0; i < spells.size(); i++) {
            SpellTranslations spell = spells.get(i);
            
            // Find maximum lengths across all languages (after Caesar cipher encoding)
            int maxNameLength = spell.getTranslations().values().stream()
                .mapToInt(t -> encipherCaesarCode(t.getName()).length())
                .max().orElse(0);
                
            int maxDescLength = spell.getTranslations().values().stream()
                .mapToInt(t -> encipherCaesarCode(t.getDescription()).length())
                .max().orElse(0);
            
            // Calculate block size with null terminators and alignment
            int blockSize = maxNameLength + 1 + maxDescLength + 1;
            blockSize = alignTo16Bytes(blockSize); // Optional: align for performance
            
            layouts.put(i, new SpellBlockLayout(
                calculateOffset(layouts.values()), 
                maxNameLength + 1, 
                maxDescLength + 1,
                blockSize
            ));
        }
        
        return new TextLayout(layouts);
    }
}
```

## Proposed Solution Architecture

### Domain Layer Enhancements

**SpellTranslations Entity Extension**
The existing `SpellTranslations` entity will be enhanced to support multiple languages with a map-based structure:

```java
Map<Language, Translation> translations
```

Where each `Translation` contains both spell name and description for a specific language. This maintains domain purity while providing rich behavior for text management operations.

**New Domain Services**
- `TextOffsetCalculationService`: Handles offset calculations for contiguous text layout with cross-language padding
- `LanguageValidationService`: Ensures language consistency and English fallback rules
- `ExportValidationService`: Validates that newly created spells have sufficient translation data
- `TextEncodingService`: Manages FF8 Caesar cipher encoding for spell text content
- `TextLayoutService`: Calculates optimal text positioning with maximum length analysis across languages

### Application Layer Additions

**New Primary Port**
`LocalizedExportUseCase` will define the contract for exporting newly created magic with localized resources:

```java
public interface LocalizedExportUseCase {
    ExportResult exportNewlyCreatedMagic(ExportRequest request);
    List<Language> getRequiredLanguages(Collection<MagicData> newSpells);
    ValidationResult validateExportData(Collection<MagicData> newSpells);
    TextLayoutPreview previewTextLayout(Collection<MagicData> newSpells);
}
```

**New Secondary Ports**
- `ResourceFileGeneratorPort`: Abstracts resource file creation with language-specific padding
- `BinaryExportPort`: Handles binary file generation for new magic data
- `TextLayoutPort`: Manages text positioning and cross-language offset calculations

**Service Implementation**
`LocalizedExportService` will orchestrate the complex export workflow:

1. **Data Collection**: Gather all newly created magic spells from repository
2. **Language Analysis**: Determine required languages across all new spells
3. **Cross-Language Text Analysis**: Calculate maximum text lengths for each spell across all languages
4. **Text Layout Planning**: Calculate optimal text positioning with consistent offset alignment
5. **Offset Calculation**: Determine binary offsets with 511-byte constant adjustment
6. **File Generation**: Create binary and language-specific resource files through infrastructure adapters

### Infrastructure Layer Implementation

**New Secondary Adapters**

**ResourceFileGenerator**: Implements cross-language contiguous text file generation:
- Analyzes maximum text lengths across all languages for each spell position
- Maintains consistent offset positioning across languages through strategic padding
- Handles null-termination and alignment padding
- Ensures English fallback for missing translations
- Applies FF8 Caesar cipher encoding to all text content
- Generates properly formatted resource files for each language

**BinaryExportAdapter**: Creates binary files for newly created magic:
- Calculates text offsets with 511-byte adjustment
- References common offset positions that work across all language files
- Preserves exact binary format compatibility
- Handles serialization of magic data structures
- Maintains kernel.bin format specifications

**TextLayoutCalculator**: Manages complex cross-language text positioning logic:
- Analyzes all encoded translations to determine maximum text lengths per spell position
- Calculates padding requirements for offset alignment across languages
- Ensures contiguous layout across multiple languages and spells
- Optimizes file size while maintaining format requirements
- Works with Caesar cipher encoded text for accurate length calculations
- Provides layout previews for validation before export

## Technical Implementation Strategy

### Text Encoding Requirements

All spell names and descriptions must be encrypted using FF8's Caesar cipher encoding before being written to resource files. This matches the format expected by the game engine.

**Caesar Cipher Algorithm** (reverse of `decipherCaesarCode`):
- **Uppercase letters (A-Z)**: Add 4 to get encoded range (D-^)
- **Digits (0-9)**: Subtract 15 with appropriate range handling
- **Lowercase letters (a-z)**: Subtract 2 to get encoded range (_-x)
- **Other characters**: Remain unchanged (spaces, punctuation)

**Implementation Strategy**:
The `TextEncodingService` will provide the inverse operation of the existing `decipherCaesarCode` method found in `KernelBinaryParser`. This ensures perfect compatibility with the game's text format expectations.

```java
public String encipherCaesarCode(String plainText) {
    // Inverse operations of decipherCaesarCode
    // - Uppercase: Add 4 (A-Z → D-^)
    // - Digits: Subtract 15 (0-9 → range adjustment)
    // - Lowercase: Subtract 2 (a-z → _-x)
}
```

### Cross-Language Text Offset Calculation Algorithm

The system will implement a three-pass algorithm for optimal cross-language text layout:

**Pass 1 - Language Collection**: Gather all available languages:
- Scan all spell translations to identify available languages
- Determine required fallback patterns (English as primary fallback)
- Validate minimum translation coverage requirements

**Pass 2 - Cross-Language Analysis**: Examine all translations to determine:
- Maximum encoded spell name length across all languages for each spell position
- Maximum encoded description length across all languages for each spell position  
- Required languages based on available translations
- Missing translations requiring English fallback
- Text compatibility with Caesar cipher encoding across all languages

**Pass 3 - Unified Layout Generation**: Generate final text layout for all languages:
- Apply Caesar cipher encoding to all text content across all languages
- Calculate unified block sizes based on maximum lengths per spell position
- Position encoded spell names at calculated offsets (consistent across languages)
- Add appropriate null-termination and padding to reach block size
- Ensure consistent positioning across all language files
- Calculate binary offsets with 511-byte adjustment for unified reference

### File Generation Workflow

**User Interaction Flow**:
1. User selects "Export New Magic..." from File menu
2. System analyzes available translations and presents language summary
3. System presents folder selection dialog with export preview
4. User provides base filename for export (e.g., "custom_magic")
5. System validates export data and presents detailed file generation summary
6. User confirms export operation
7. System generates all required files:
   - `custom_magic.bin` (common binary data)
   - `custom_magic_en.bin` (English text resources)
   - `custom_magic_fr.bin` (French text resources)
   - `custom_magic_[lang].bin` (additional language files as needed)

**Backend Processing**:
1. `LocalizedExportService` collects newly created magic data
2. Service analyzes required languages and validates completeness
3. `TextEncodingService` applies Caesar cipher encoding to all text content across languages
4. `TextLayoutCalculator` determines optimal text positioning for encoded text with cross-language consistency
5. `ResourceFileGenerator` creates language-specific resource files with unified offset positioning
6. `BinaryExportAdapter` generates binary file with calculated offsets that reference unified positions
7. File system operations write all files to selected directory with consistent naming

### Error Handling and Validation

**Pre-Export Validation**:
- Verify at least one newly created spell exists
- Ensure all new spells have minimum required translations
- Validate text lengths don't exceed format limitations (accounting for cipher encoding and cross-language maximum)
- Verify text content is compatible with Caesar cipher encoding across all languages
- Check file system permissions for target directory
- Validate offset calculations don't exceed binary format limits

**Runtime Error Recovery**:
- Atomic file operations to prevent partial writes across multiple language files
- Rollback capability if any file generation fails (removes all generated files)
- Clear error messages for user understanding
- Logging for troubleshooting complex export scenarios
- Detailed reporting of which languages/spells caused failures

## Integration with Observer Pattern

The export functionality will integrate seamlessly with the existing observer pattern:

**Export Events**: New event types for export operations:
- `ExportStartedEvent`: Notifies UI of export initiation with language count
- `ExportProgressEvent`: Updates progress during multi-language file generation
- `ExportCompletedEvent`: Signals successful export completion with file listing
- `ExportFailedEvent`: Handles error scenarios with recovery options

**UI Responsiveness**: The export process will maintain UI responsiveness through:
- Background processing for multi-language file generation
- Progress indicators for long-running cross-language operations
- Cancellation support for user control
- Status updates through observer notifications with per-language progress

## File Format Specifications

**Binary File Structure**:
- Standard magic data structure (60 bytes per spell)
- Text offset fields point to unified resource file positions + 511 bytes
- Maintains exact compatibility with FF8 kernel format
- Preserves all binary serialization requirements
- Works with any language resource file due to consistent offset positioning

**Resource File Format (Per Language)**:
- Contiguous null-terminated strings with FF8 Caesar cipher encoding
- Consistent offset positioning across all languages through strategic padding
- Optimal padding for alignment requirements while maintaining cross-language compatibility
- Text encoded using FF8's proprietary Caesar cipher algorithm
- Binary-compatible format for direct game engine consumption
- Each language file independently loadable with same binary file

**File Naming Convention**:
- Binary file: `{basename}.bin`
- English resources: `{basename}_en.bin`  
- French resources: `{basename}_fr.bin`
- Additional languages: `{basename}_{lang_code}.bin`

This approach ensures complete separation of logic and presentation while maintaining perfect binary compatibility and enabling seamless language switching at runtime.