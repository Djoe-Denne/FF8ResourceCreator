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
     * Extract base name from file path (remove extension)
     */
    private String getBaseNameFromPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }

    /**
     * Load language files associated with the magic binary
     * English file is mandatory - throws exception if not found
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
     * Build SpellTranslations object for a specific magic entry
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
     * Get display name for language code
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

    @Override
    public boolean isFileLoaded() {
        return fileLoaded;
    }

    @Override
    public Optional<String> getCurrentFilePath() {
        return Optional.ofNullable(currentFilePath);
    }

    public void unloadFile() {
        this.fileLoaded = false;
        this.currentFilePath = null;
        this.magicRepository.clear();
        logger.info("Kernel file unloaded");
    }

    public int getMagicCount() {
        return MAGIC_COUNT;
    }

    public boolean hasUnsavedChanges() {
        // This would typically check if the repository has been modified
        // For now, we'll implement a simple version
        return fileLoaded && magicRepository.findAll().size() == MAGIC_COUNT;
    }

    @Override
    public boolean isModified() {
        return hasUnsavedChanges();
    }

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
    
    @Override
    public void closeFile() {
        unloadFile();
    }

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