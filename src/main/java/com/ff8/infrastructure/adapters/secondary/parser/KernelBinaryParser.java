package com.ff8.infrastructure.adapters.secondary.parser;

import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.SectionParserStrategy;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.*;
import com.ff8.domain.exceptions.BinaryParseException;

import java.util.*;
import java.util.logging.Logger;

/**
 * Main parser for FF8 kernel.bin file that uses Strategy pattern to handle different sections.
 * Each section type (Magic, Weapons, Items, etc.) has its own specialized parser strategy.
 */
public class KernelBinaryParser implements BinaryParserPort {
    private static final Logger logger = Logger.getLogger(KernelBinaryParser.class.getName());
    
    private final Map<SectionType, SectionParserStrategy<?>> strategies = new HashMap<>();
    private final Set<SectionType> activeSections = new HashSet<>();

    /**
     * Constructor that initializes available strategies
     */
    public KernelBinaryParser() {
        // Register available strategies
        registerStrategy(new MagicSectionParser());
        // Future strategies would be registered here:
        // registerStrategy(new WeaponSectionParser());
        // registerStrategy(new ItemSectionParser());
        // etc.
        
        // By default, enable only the magic section (for backward compatibility)
        activeSections.add(SectionType.MAGIC);
    }
    
    /**
     * Constructor that allows specifying which sections to parse
     */
    public KernelBinaryParser(List<SectionType> sectionsToEnable) {
        this(); // Initialize strategies
        
        // Clear defaults and set specified sections
        activeSections.clear();
        for (SectionType section : sectionsToEnable) {
            if (strategies.containsKey(section)) {
                activeSections.add(section);
                logger.info("Enabled section parser for: " + section.getDisplayName());
            } else {
                logger.warning("No strategy available for section: " + section.getDisplayName());
            }
        }
    }
    
    /**
     * Register a new section parser strategy
     */
    public void registerStrategy(SectionParserStrategy<?> strategy) {
        strategies.put(strategy.getSectionType(), strategy);
        logger.info("Registered parser strategy for: " + strategy.getSectionType().getDisplayName());
    }
    
    /**
     * Enable parsing for a specific section
     */
    public void enableSection(SectionType section) {
        if (strategies.containsKey(section)) {
            activeSections.add(section);
            logger.info("Enabled section: " + section.getDisplayName());
        } else {
            throw new IllegalArgumentException("No strategy available for section: " + section.getDisplayName());
        }
    }
    
    /**
     * Disable parsing for a specific section
     */
    public void disableSection(SectionType section) {
        activeSections.remove(section);
        logger.info("Disabled section: " + section.getDisplayName());
    }
    
    /**
     * Get the strategy for a specific section type
     */
    @SuppressWarnings("unchecked")
    public <T> SectionParserStrategy<T> getStrategy(SectionType sectionType) {
        return (SectionParserStrategy<T>) strategies.get(sectionType);
    }
    
    /**
     * Check if a section is currently enabled for parsing
     */
    public boolean isSectionEnabled(SectionType section) {
        return activeSections.contains(section);
    }
    
    /**
     * Get all enabled sections
     */
    public Set<SectionType> getEnabledSections() {
        return new HashSet<>(activeSections);
    }
    
    @Override
    public MagicData parseMagicData(byte[] binaryData, int offset) throws BinaryParseException {
        return parseMagicData(binaryData, offset, -1); // Default index for backward compatibility
    }
    
    /**
     * Parse magic data with kernel index (position in kernel file)
     */
    public MagicData parseMagicData(byte[] binaryData, int offset, int kernelIndex) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.parseItem(binaryData, offset, kernelIndex);
    }
    
    @Override
    public byte[] serializeMagicData(MagicData magic) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.serializeItem(magic);
    }
    
    @Override
    public List<MagicData> parseAllMagicData(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.parseAllItems(kernelData);
    }

    @Override
    public byte[] serializeAllMagicData(List<MagicData> magicDataList, byte[] originalKernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.serializeAllItems(magicDataList, originalKernelData);
    }

    @Override
    public int findMagicSectionOffset(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.findSectionOffset(kernelData);
    }

    @Override
    public int getMagicStructSize() {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new IllegalStateException("Magic section parser strategy not available");
        }
        return magicStrategy.getItemStructSize();
    }

    @Override
    public int getExpectedMagicCount() {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new IllegalStateException("Magic section parser strategy not available");
        }
        return magicStrategy.getExpectedItemCount();
    }

    @Override
    public ValidationResult validateKernelStructure(byte[] kernelData) {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            return new ValidationResult(false, "Magic section parser strategy not available", 
                                      List.of("Strategy not available"), 0, 0);
        }
        return magicStrategy.validateSectionStructure(kernelData);
    }

    @Override
    public List<String> extractSpellNames(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.extractItemNames(kernelData);
    }

    @Override
    public String calculateMagicSectionChecksum(byte[] kernelData) {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            return "ERROR: Strategy not available";
        }
        return magicStrategy.calculateSectionChecksum(kernelData);
    }

    @Override
    public boolean isValidKernelFile(byte[] data) {
        if (data == null || data.length < 1024) {
            return false;
        }
        
        // Check validity for all enabled sections
        for (SectionType section : activeSections) {
            SectionParserStrategy<?> strategy = strategies.get(section);
            if (strategy != null) {
                ValidationResult result = strategy.validateSectionStructure(data);
                if (!result.isValid()) {
                    return false;
                }
            }
        }
        
        return true;
    }

     }  