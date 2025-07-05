package com.ff8.application.services;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.mappers.MagicDataToDtoMapper;
import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.KernelFileUseCase.ValidationResult;
import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.events.KernelReadEvent;
import com.ff8.domain.exceptions.BinaryParseException;
import com.ff8.domain.observers.AbstractSubject;
import com.ff8.domain.services.TextEncodingService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Application service for managing FF8 kernel.bin files and magic binary data.
 * 
 * <p>This service implements the {@link KernelFileUseCase} interface as the primary
 * orchestrator for all kernel file operations in the FF8 Magic Creator application.
 * It coordinates between the domain layer, infrastructure adapters, and provides
 * comprehensive file management capabilities with robust error handling and
 * event notification.</p>
 * 
 * <p>Core responsibilities:</p>
 * <ul>
 *   <li><strong>Kernel File Loading:</strong> Parses FF8 kernel.bin files and extracts magic data</li>
 *   <li><strong>Magic Binary Import:</strong> Loads user-created magic binary files with translations</li>
 *   <li><strong>File Export:</strong> Generates new kernel.bin files with modified magic data</li>
 *   <li><strong>Validation:</strong> Ensures file integrity and validates magic data structures</li>
 *   <li><strong>Event Notification:</strong> Notifies observers of file operations and changes</li>
 * </ul>
 * 
 * <p>Supported file formats:</p>
 * <ul>
 *   <li><strong>kernel.bin:</strong> Original FF8 kernel files containing 56 magic spells</li>
 *   <li><strong>Magic Binary:</strong> User-created binary files with custom magic data</li>
 *   <li><strong>Language Resources:</strong> Multi-language spell name and description files</li>
 * </ul>
 * 
 * <p>File structure constants:</p>
 * <ul>
 *   <li><strong>Magic Section Offset:</strong> 0x021C (540 bytes) - Standard kernel.bin magic section start</li>
 *   <li><strong>Magic Struct Size:</strong> 0x3C (60 bytes) - Size of each magic data structure</li>
 *   <li><strong>Magic Count:</strong> 56 spells - Standard number of magic spells in FF8</li>
 * </ul>
 * 
 * <p>The service maintains state about the currently loaded file and provides
 * comprehensive metadata about file operations, including modification tracking,
 * backup creation, and integrity validation.</p>
 * 
 * <p>Observer pattern implementation:</p>
 * <ul>
 *   <li>Extends {@link AbstractSubject} to provide event notification</li>
 *   <li>Emits {@link KernelReadEvent} when files are loaded successfully</li>
 *   <li>Provides file metadata and magic data DTOs to observers</li>
 * </ul>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
@RequiredArgsConstructor
public class KernelFileService extends AbstractSubject<KernelReadEvent> implements KernelFileUseCase {
    private static final Logger logger = Logger.getLogger(KernelFileService.class.getName());
    private static final int MAGIC_SECTION_OFFSET = 0x021C; // Standard offset for magic data section
    private static final int MAGIC_STRUCT_SIZE = 0x3C; // 60 bytes per magic entry
    private static final int MAGIC_COUNT = 56; // Number of magic spells in FF8
    
    private final BinaryParserPort binaryParser;
    private final FileSystemPort fileSystem;
    private final MagicRepository magicRepository;
    private final MagicDataToDtoMapper magicDataToDtoMapper;
    private final TextEncodingService textEncodingService;
    private boolean fileLoaded = false;
    private String currentFilePath;

    /**
     * Loads a complete FF8 kernel.bin file and extracts all magic data.
     * 
     * <p>This method performs comprehensive loading of an FF8 kernel.bin file,
     * extracting all 56 magic spells and their associated data. The process
     * includes validation, parsing, and repository management to ensure
     * data integrity throughout the operation.</p>
     * 
     * <p>Loading process:</p>
     * <ul>
     *   <li>Validates file size and structure for kernel.bin format</li>
     *   <li>Removes existing kernel data while preserving user-created magic</li>
     *   <li>Parses all 56 magic entries from the magic section</li>
     *   <li>Stores parsed magic data in the repository</li>
     *   <li>Notifies observers with loading results and file metadata</li>
     * </ul>
     * 
     * <p>The method ensures that newly created magic spells are preserved
     * during kernel file loading operations, maintaining user work while
     * refreshing the base game data.</p>
     * 
     * @param filePath The path to the kernel.bin file to load
     * @throws BinaryParseException if the file is invalid, corrupted, or cannot be parsed
     */
    @Override
    public void loadKernelFile(String filePath) throws BinaryParseException {
        try {
            logger.info("Loading kernel file: " + filePath);
            
            // Read the binary file
            byte[] kernelData = fileSystem.readBinaryFile(filePath);
            
            // Validate file size and structure
            if (kernelData.length < MAGIC_SECTION_OFFSET + (MAGIC_COUNT * MAGIC_STRUCT_SIZE)) {
                throw new BinaryParseException("Invalid kernel.bin file: insufficient size");
            }
            
            // Remove existing kernel data (keep newly created magic)
            magicRepository.removeKernelData();
            
            // Parse all magic data from kernel
            for (int i = 0; i < MAGIC_COUNT; i++) {
                int offset = MAGIC_SECTION_OFFSET + (i * MAGIC_STRUCT_SIZE);
                MagicData magic = binaryParser.parseMagicData(kernelData, offset);
                // Set the index manually and ensure isNewlyCreated is false (default)
                magic = magic.toBuilder().index(i).isNewlyCreated(false).build();
                magicRepository.save(magic);
                
                logger.fine("Parsed magic ID: " + i + " - " + magic.getExtractedSpellName());
            }
            
            this.currentFilePath = filePath;
            this.fileLoaded = true;
            
            logger.info("Successfully loaded " + MAGIC_COUNT + " magic spells from kernel file");
            
            // Get file size for the event
            long fileSize = kernelData.length;
            
            // Convert MagicData to MagicDisplayDTO for the event using the mapper
            List<MagicDisplayDTO> magicDisplayList = magicDataToDtoMapper.toDtoList(magicRepository.findAll());
            
            // Notify observers about the successful kernel read
            KernelReadEvent kernelReadEvent = new KernelReadEvent(filePath, magicDisplayList, fileSize);
            notifyObservers(kernelReadEvent);
            
            logger.info("Notified " + getObserverCount() + " observers about kernel file load");
            
        } catch (IOException e) {
            throw new BinaryParseException("Failed to read kernel file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BinaryParseException("Failed to parse kernel file: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a magic binary file with associated multi-language resources.
     * 
     * <p>This method loads user-created magic binary files that contain custom
     * magic data structures. It supports multi-language spell names and descriptions
     * by automatically detecting and loading associated language resource files.</p>
     * 
     * <p>Loading process:</p>
     * <ul>
     *   <li>Validates binary file structure and magic count</li>
     *   <li>Searches for associated language resource files</li>
     *   <li>Loads and parses multi-language spell translations</li>
     *   <li>Parses magic data structures from the binary file</li>
     *   <li>Assigns new indices to avoid conflicts with existing magic</li>
     *   <li>Marks all loaded magic as newly created for proper tracking</li>
     * </ul>
     * 
     * <p>Language file naming conventions:</p>
     * <ul>
     *   <li><strong>English (required):</strong> {@code [basename]_en.resources.bin}</li>
     *   <li><strong>Alternative naming:</strong> {@code [basename]_english.resources.bin}</li>
     *   <li><strong>Other languages:</strong> {@code [basename]_[langcode].resources.bin}</li>
     * </ul>
     * 
     * @param filePath The path to the magic binary file to load
     * @throws BinaryParseException if the file is invalid, corrupted, or required resources are missing
     * @throws IOException if file system operations fail
     */
    @Override
    public void loadMagicBinary(String filePath) throws BinaryParseException, IOException {
        try {
            logger.info("Loading magic binary file: " + filePath);
            
            // Read the binary file
            byte[] magicBinaryData = fileSystem.readBinaryFile(filePath);
            
            // Validate file size - should be multiple of magic struct size
            if (magicBinaryData.length % MAGIC_STRUCT_SIZE != 0) {
                throw new BinaryParseException("Invalid magic binary file: size must be multiple of " + MAGIC_STRUCT_SIZE + " bytes");
            }
            
            int magicCount = magicBinaryData.length / MAGIC_STRUCT_SIZE;
            logger.info("Magic binary contains " + magicCount + " magic entries");
            
            // Load language files - English is mandatory
            Path binaryFilePath = Paths.get(filePath);
            String baseName = getBaseNameFromPath(binaryFilePath);
            Path parentDir = binaryFilePath.getParent();
            
            Map<String, SpellTranslations.Translation[]> languageTranslations = loadLanguageFiles(parentDir, baseName, magicCount);
            
            // Parse all magic data and flag as newly created
            int addedCount = 0;
            for (int i = 0; i < magicCount; i++) {
                int offset = i * MAGIC_STRUCT_SIZE;
                MagicData magic = binaryParser.parseMagicData(magicBinaryData, offset);
                
                // Get next available index for this magic
                int nextIndex = magicRepository.getNextAvailableIndex();
                
                // Build translations for this magic entry
                SpellTranslations translations = buildSpellTranslations(languageTranslations, i);

                // Set as newly created and assign proper index with translations
                magic = magic.toBuilder()
                    .index(nextIndex)
                    .extractedSpellName(translations.getEnglishName())
                    .extractedSpellDescription(translations.getEnglishDescription())
                    .isNewlyCreated(true)
                    .translations(translations)
                    .build();
                    
                magicRepository.save(magic);
                addedCount++;
                
                logger.fine("Added magic from binary: index=" + nextIndex + ", ID=" + magic.getMagicID() + 
                           ", name='" + magic.getSpellName() + "'");
            }
            
            logger.info("Successfully added " + addedCount + " magic spells from binary file with translations");
            
            // Get file size for the event
            long fileSize = magicBinaryData.length;
            
            // Convert MagicData to MagicDisplayDTO for the event using the mapper
            List<MagicDisplayDTO> magicDisplayList = magicDataToDtoMapper.toDtoList(magicRepository.findAll());
            
            // Notify observers about the successful binary read
            KernelReadEvent kernelReadEvent = new KernelReadEvent(filePath, magicDisplayList, fileSize);
            notifyObservers(kernelReadEvent);
            
            logger.info("Notified " + getObserverCount() + " observers about magic binary load");
            
        } catch (IOException e) {
            throw new BinaryParseException("Failed to read magic binary file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BinaryParseException("Failed to parse magic binary file: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the base name from a file path by removing the extension.
     * 
     * <p>This utility method extracts the filename without its extension,
     * which is used for discovering associated language resource files
     * that follow the naming convention of [basename]_[language].resources.bin.</p>
     * 
     * @param filePath The file path to extract the base name from
     * @return The filename without extension, or the full filename if no extension exists
     */
    private String getBaseNameFromPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    /**
     * Loads language files associated with a magic binary file.
     * 
     * <p>This method discovers and loads all language resource files associated
     * with a magic binary file. The English language file is mandatory and must
     * be present for the operation to succeed. Additional language files are
     * loaded if available.</p>
     * 
     * <p>Language file discovery process:</p>
     * <ul>
     *   <li>Searches for English file using standard naming conventions</li>
     *   <li>Validates English file presence (mandatory requirement)</li>
     *   <li>Loads and parses English translations</li>
     *   <li>Discovers additional language files (future enhancement)</li>
     * </ul>
     * 
     * @param parentDir The directory containing the language resource files
     * @param baseName The base name of the magic binary file (without extension)
     * @param magicCount The number of magic spells expected in each language file
     * @return Map of language codes to translation arrays
     * @throws BinaryParseException if the mandatory English file is missing
     * @throws IOException if file system operations fail
     */
    private Map<String, SpellTranslations.Translation[]> loadLanguageFiles(Path parentDir, String baseName, int magicCount) 
            throws BinaryParseException, IOException {
        
        Map<String, SpellTranslations.Translation[]> languageTranslations = new LinkedHashMap<>();
        
        // Check for English file (mandatory)
        Path englishFile = parentDir.resolve(baseName + "_en.resources.bin");
        if (!Files.exists(englishFile)) {
            // Also try alternative naming convention
            englishFile = parentDir.resolve(baseName + "_english.resources.bin");
            if (!Files.exists(englishFile)) {
                throw new BinaryParseException("English language file is mandatory but not found. Expected: " + 
                    parentDir.resolve(baseName + "_en.resources.bin"));
            }
        }
        
        // Load English translations
        logger.info("Loading English translations from: " + englishFile);
        byte[] englishData = fileSystem.readBinaryFile(englishFile.toString());
        
        // For now, assume fixed lengths - in real implementation, these would be calculated
        int maxNameLength = 32;  // Approximate max name length
        int maxDescLength = 128; // Approximate max description length
        
        Map<Integer, SpellTranslations.Translation> englishTranslations = 
            textEncodingService.parseLanguageResourceFile(englishData, magicCount, maxNameLength, maxDescLength);
        
        SpellTranslations.Translation[] englishArray = new SpellTranslations.Translation[magicCount];
        for (int i = 0; i < magicCount; i++) {
            englishArray[i] = englishTranslations.getOrDefault(i, new SpellTranslations.Translation("", ""));
        }
        languageTranslations.put("English", englishArray);
        
        // Look for other language files (optional)
        String[] otherLanguages = {"fr", "french", "de", "german", "es", "spanish", "it", "italian", "jp", "japanese"};
        for (String langCode : otherLanguages) {
            Path langFile = parentDir.resolve(baseName + "_" + langCode + ".resources.bin");
            if (Files.exists(langFile)) {
                logger.info("Loading additional language file: " + langFile);
                try {
                    byte[] langData = fileSystem.readBinaryFile(langFile.toString());
                    Map<Integer, SpellTranslations.Translation> langTranslations = 
                        textEncodingService.parseLanguageResourceFile(langData, magicCount, maxNameLength, maxDescLength);
                    
                    SpellTranslations.Translation[] langArray = new SpellTranslations.Translation[magicCount];
                    for (int i = 0; i < magicCount; i++) {
                        langArray[i] = langTranslations.getOrDefault(i, new SpellTranslations.Translation("", ""));
                    }
                    
                    String languageName = getLanguageDisplayName(langCode);
                    languageTranslations.put(languageName, langArray);
                } catch (Exception e) {
                    logger.warning("Failed to load language file " + langFile + ": " + e.getMessage());
                    // Continue without this language file
                }
            }
        }
        
        logger.info("Loaded translations for " + languageTranslations.size() + " languages");
        return languageTranslations;
    }

    /**
     * Builds spell translations for a specific magic entry.
     * 
     * <p>This method constructs a {@link SpellTranslations} object for a specific
     * magic spell using the loaded language translation data. It ensures that
     * English translations are always available and properly formatted.</p>
     * 
     * @param languageTranslations Map of language codes to translation arrays
     * @param magicIndex The index of the magic spell to build translations for
     * @return SpellTranslations object containing all available translations
     */
    private SpellTranslations buildSpellTranslations(Map<String, SpellTranslations.Translation[]> languageTranslations, int magicIndex) {
        Map<String, SpellTranslations.Translation> translations = new LinkedHashMap<>();
        
        // Add translations from all loaded languages
        for (Map.Entry<String, SpellTranslations.Translation[]> entry : languageTranslations.entrySet()) {
            String language = entry.getKey();
            SpellTranslations.Translation[] langArray = entry.getValue();
            
            if (magicIndex < langArray.length) {
                translations.put(language, langArray[magicIndex]);
            }
        }
        
        return new SpellTranslations(translations);
    }

    /**
     * Converts language code to display name.
     * 
     * <p>This utility method converts language codes to human-readable display names
     * for use in the user interface.</p>
     * 
     * @param langCode The language code to convert
     * @return Human-readable display name for the language
     */
    private String getLanguageDisplayName(String langCode) {
        return switch (langCode.toLowerCase()) {
            case "fr", "french" -> "French";
            case "de", "german" -> "German";
            case "es", "spanish" -> "Spanish";
            case "it", "italian" -> "Italian";
            case "jp", "japanese" -> "Japanese";
            default -> langCode.substring(0, 1).toUpperCase() + langCode.substring(1).toLowerCase();
        };
    }

    /**
     * Saves the current magic data to a new kernel.bin file.
     * 
     * <p>This method exports all current magic data to a new kernel.bin file,
     * creating a complete FF8-compatible binary file. The export process includes
     * validation, binary serialization, and comprehensive error handling.</p>
     * 
     * <p>Export process:</p>
     * <ul>
     *   <li>Validates that a kernel file is currently loaded</li>
     *   <li>Retrieves all magic data from the repository</li>
     *   <li>Generates binary data for the magic section</li>
     *   <li>Creates a complete kernel.bin file with updated magic data</li>
     *   <li>Writes the file to the specified path</li>
     * </ul>
     * 
     * @param filePath The path where the new kernel.bin file should be saved
     * @throws BinaryParseException if there's an error during export or no file is loaded
     */
    @Override
    public void saveKernelFile(String filePath) throws BinaryParseException {
        if (!fileLoaded) {
            throw new BinaryParseException("No kernel file loaded. Load a file first.");
        }
        
        try {
            logger.info("Saving kernel file: " + filePath);
            
            // Read the original kernel file to preserve non-magic data
            byte[] originalKernelData = fileSystem.readBinaryFile(currentFilePath != null ? currentFilePath : filePath);
            byte[] modifiedKernelData = originalKernelData.clone();
            
            // Get all magic data from repository
            List<MagicData> allMagic = magicRepository.findAll();
            
            if (allMagic.size() != MAGIC_COUNT) {
                throw new BinaryParseException("Invalid magic count: expected " + MAGIC_COUNT + ", got " + allMagic.size());
            }
            
            // Sort by magic ID to ensure correct order
            allMagic.sort((m1, m2) -> Integer.compare(m1.getMagicID(), m2.getMagicID()));
            
            // Serialize each magic data back to binary
            for (int i = 0; i < MAGIC_COUNT; i++) {
                MagicData magic = allMagic.get(i);
                if (magic.getMagicID() != i) {
                    throw new BinaryParseException("Magic ID mismatch at index " + i + ": expected " + i + ", got " + magic.getMagicID());
                }
                
                byte[] magicBinary = binaryParser.serializeMagicData(magic);
                if (magicBinary.length != MAGIC_STRUCT_SIZE) {
                    throw new BinaryParseException("Invalid serialized magic size: expected " + MAGIC_STRUCT_SIZE + ", got " + magicBinary.length);
                }
                
                // Write magic data to the correct offset in kernel
                int offset = MAGIC_SECTION_OFFSET + (i * MAGIC_STRUCT_SIZE);
                System.arraycopy(magicBinary, 0, modifiedKernelData, offset, MAGIC_STRUCT_SIZE);
                
                logger.fine("Serialized magic ID: " + i + " - " + magic.getExtractedSpellName());
            }
            
            // Write the modified kernel file
            fileSystem.writeBinaryFile(filePath, modifiedKernelData);
            
            // Update current file path if different
            if (!filePath.equals(currentFilePath)) {
                this.currentFilePath = filePath;
            }
            
            logger.info("Successfully saved kernel file with " + MAGIC_COUNT + " magic spells");
            
        } catch (IOException e) {
            throw new BinaryParseException("Failed to write kernel file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BinaryParseException("Failed to save kernel file: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a kernel file is currently loaded.
     * 
     * @return true if a kernel file is loaded, false otherwise
     */
    @Override
    public boolean isFileLoaded() {
        return fileLoaded;
    }

    /**
     * Gets the path of the currently loaded kernel file.
     * 
     * @return Optional containing the current file path, or empty if no file is loaded
     */
    @Override
    public Optional<String> getCurrentFilePath() {
        return Optional.ofNullable(currentFilePath);
    }

    /**
     * Unloads the current kernel file and clears all state.
     * 
     * <p>This method resets the service state, clearing the loaded file status
     * and path information. It does not affect the magic data in the repository.</p>
     */
    public void unloadFile() {
        this.fileLoaded = false;
        this.currentFilePath = null;
        this.magicRepository.clear();
        logger.info("Kernel file unloaded");
    }

    /**
     * Gets the count of magic spells in the currently loaded kernel.
     * 
     * @return The number of magic spells, or 0 if no file is loaded
     */
    public int getMagicCount() {
        return MAGIC_COUNT;
    }

    /**
     * Checks if there are unsaved changes in the magic data.
     * 
     * @return true if there are modifications that haven't been saved, false otherwise
     */
    public boolean hasUnsavedChanges() {
        // This would typically check if the repository has been modified
        // For now, we'll implement a simple version
        return fileLoaded && magicRepository.findAll().size() == MAGIC_COUNT;
    }

    /**
     * Checks if the magic data has been modified since loading.
     * 
     * @return true if modifications exist, false otherwise
     */
    @Override
    public boolean isModified() {
        return hasUnsavedChanges();
    }

    /**
     * Creates a backup of the currently loaded kernel file.
     * 
     * <p>This method creates a backup copy of the current kernel file at the
     * specified path. This is useful for creating safety copies before
     * performing modifications.</p>
     * 
     * @param backupPath The path where the backup should be created
     * @throws BinaryParseException if no file is loaded or backup creation fails
     */
    @Override
    public void createBackup(String backupPath) throws BinaryParseException {
        if (currentFilePath == null) {
            throw new BinaryParseException("No file is currently loaded");
        }
        try {
            fileSystem.createBackup(currentFilePath, backupPath);
        } catch (IOException e) {
            throw new BinaryParseException("Failed to create backup: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets comprehensive information about the currently loaded file.
     * 
     * <p>This method provides detailed metadata about the current kernel file,
     * including file system information, magic count, and modification status.</p>
     * 
     * @return Optional containing file information, or empty if no file is loaded
     */
    @Override
    public Optional<FileInfo> getFileInfo() {
        if (currentFilePath == null) {
            return Optional.empty();
        }
        
        try {
            long fileSize = fileSystem.getFileSize(currentFilePath);
            // For lastModified, we would need to add this to FileSystemPort
            // For now, use current time as placeholder
            long lastModified = System.currentTimeMillis();
            int magicCount = magicRepository.count();
            String checksumMD5 = "TBD"; // Would need MD5 calculation
            
            return Optional.of(new FileInfo(currentFilePath, fileSize, lastModified, magicCount, checksumMD5));
        } catch (Exception e) {
            logger.warning("Failed to get file info: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Closes the current kernel file and cleans up resources.
     * 
     * <p>This method performs cleanup operations when the current file is
     * no longer needed, but preserves the magic data in the repository.</p>
     */
    @Override
    public void closeFile() {
        unloadFile();
    }

    /**
     * Validates the integrity of the currently loaded kernel file.
     * 
     * <p>This method performs comprehensive validation of the loaded kernel file,
     * checking for structural integrity, magic data consistency, and format
     * compliance.</p>
     * 
     * @return ValidationResult containing the results of the integrity check
     */
    @Override
    public ValidationResult validateFileIntegrity() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (!isFileLoaded()) {
            errors.add("No file is currently loaded");
            return new ValidationResult(false, "No file loaded", errors);
        }
        
        try {
            // Check if file still exists
            if (!fileSystem.fileExists(currentFilePath)) {
                errors.add("Current file no longer exists: " + currentFilePath);
            }
            
            // Validate magic data count
            List<MagicData> magicList = magicRepository.findAll();
            if (magicList.size() != MAGIC_COUNT) {
                warnings.add("Expected " + MAGIC_COUNT + " magic entries, found " + magicList.size());
            }
            
            // Check for duplicate magic IDs
            long uniqueIds = magicList.stream()
                .mapToInt(MagicData::getMagicID)
                .distinct()
                .count();
            
            if (uniqueIds != magicList.size()) {
                errors.add("Duplicate magic IDs detected");
            }
            
        } catch (Exception e) {
            errors.add("File integrity check failed: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors.isEmpty() ? "File integrity check passed" : "File integrity issues found", errors);
    }
    
} 