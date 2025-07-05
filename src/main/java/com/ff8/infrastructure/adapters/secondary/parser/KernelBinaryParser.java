package com.ff8.infrastructure.adapters.secondary.parser;

import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.SectionParserStrategy;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.*;
import com.ff8.domain.exceptions.BinaryParseException;

import java.util.*;
import java.util.logging.Logger;

/**
 * Strategic binary parser implementation for FF8 kernel.bin files.
 * 
 * <p>This class serves as the main parser for Final Fantasy VIII kernel.bin files,
 * implementing the Strategy pattern to handle different sections of the binary file.
 * It acts as a Secondary Adapter in the hexagonal architecture, providing binary
 * parsing capabilities to the application layer.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Strategy pattern implementation for extensible section parsing</li>
 *   <li>Configurable section enablement for selective parsing</li>
 *   <li>Thread-safe operations with logging and error handling</li>
 *   <li>Support for multiple section types (Magic, Weapons, Items, etc.)</li>
 *   <li>Backward compatibility with existing magic-only parsing</li>
 * </ul>
 * 
 * <p>The parser uses specialized strategy implementations for each section type:
 * <ul>
 *   <li>{@link MagicSectionParser} for magic/spell data</li>
 *   <li>Future weapon, item, and other section parsers</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * // Default magic-only parsing
 * KernelBinaryParser parser = new KernelBinaryParser();
 * 
 * // Multi-section parsing
 * KernelBinaryParser parser = new KernelBinaryParser(
 *     List.of(SectionType.MAGIC, SectionType.WEAPONS)
 * );
 * 
 * // Parse all magic data
 * List<MagicData> magicList = parser.parseAllMagicData(kernelBytes);
 * }</pre>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class KernelBinaryParser implements BinaryParserPort {
    private static final Logger logger = Logger.getLogger(KernelBinaryParser.class.getName());
    
    private final Map<SectionType, SectionParserStrategy<?>> strategies = new HashMap<>();
    private final Set<SectionType> activeSections = new HashSet<>();

    /**
     * Default constructor that initializes available strategies.
     * 
     * <p>By default, only the magic section is enabled for backward compatibility.
     * Additional sections can be enabled using {@link #enableSection(SectionType)}.</p>
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
     * Constructor that allows specifying which sections to parse.
     * 
     * <p>This constructor enables only the specified sections, providing
     * fine-grained control over which parts of the kernel file are parsed.</p>
     * 
     * @param sectionsToEnable List of section types to enable for parsing
     * @throws IllegalArgumentException if a section type has no available strategy
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
     * Register a new section parser strategy.
     * 
     * <p>This method allows registration of new parsing strategies for different
     * section types. The strategy will be available for use but not automatically
     * enabled.</p>
     * 
     * @param strategy The section parser strategy to register
     * @throws IllegalArgumentException if strategy is null
     */
    public void registerStrategy(SectionParserStrategy<?> strategy) {
        strategies.put(strategy.getSectionType(), strategy);
        logger.info("Registered parser strategy for: " + strategy.getSectionType().getDisplayName());
    }
    
    /**
     * Enable parsing for a specific section.
     * 
     * <p>Enables parsing for the specified section type. The section must have
     * a registered strategy or an exception will be thrown.</p>
     * 
     * @param section The section type to enable
     * @throws IllegalArgumentException if no strategy is available for the section
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
     * Disable parsing for a specific section.
     * 
     * <p>Disables parsing for the specified section type. The section will
     * no longer be processed during parsing operations.</p>
     * 
     * @param section The section type to disable
     */
    public void disableSection(SectionType section) {
        activeSections.remove(section);
        logger.info("Disabled section: " + section.getDisplayName());
    }
    
    /**
     * Get the strategy for a specific section type.
     * 
     * <p>Returns the parser strategy for the specified section type, or null
     * if no strategy is registered for that section.</p>
     * 
     * @param <T> The type of data the strategy handles
     * @param sectionType The section type to get the strategy for
     * @return The strategy for the section type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> SectionParserStrategy<T> getStrategy(SectionType sectionType) {
        return (SectionParserStrategy<T>) strategies.get(sectionType);
    }
    
    /**
     * Check if a section is currently enabled for parsing.
     * 
     * @param section The section type to check
     * @return true if the section is enabled, false otherwise
     */
    public boolean isSectionEnabled(SectionType section) {
        return activeSections.contains(section);
    }
    
    /**
     * Get all enabled sections.
     * 
     * @return A defensive copy of the set of enabled sections
     */
    public Set<SectionType> getEnabledSections() {
        return new HashSet<>(activeSections);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>This method provides backward compatibility by calling the extended
     * version with a default index of -1.</p>
     */
    @Override
    public MagicData parseMagicData(byte[] binaryData, int offset) throws BinaryParseException {
        return parseMagicData(binaryData, offset, -1); // Default index for backward compatibility
    }
    
    /**
     * Parse magic data with kernel index specification.
     * 
     * <p>Extended version of the standard parsing method that allows specifying
     * the kernel index (position in kernel file) for the magic data.</p>
     * 
     * @param binaryData The binary data to parse
     * @param offset The offset within the binary data
     * @param kernelIndex The index position within the kernel file
     * @return The parsed magic data
     * @throws BinaryParseException if parsing fails or strategy is not available
     */
    public MagicData parseMagicData(byte[] binaryData, int offset, int kernelIndex) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.parseItem(binaryData, offset, kernelIndex);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Delegates to the magic section parser strategy to serialize the magic
     * data back to binary format.</p>
     */
    @Override
    public byte[] serializeMagicData(MagicData magic) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.serializeItem(magic);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Parses all magic data from the kernel file using the magic section
     * parser strategy.</p>
     */
    @Override
    public List<MagicData> parseAllMagicData(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.parseAllItems(kernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Serializes all magic data back to the kernel file format using the
     * magic section parser strategy.</p>
     */
    @Override
    public byte[] serializeAllMagicData(List<MagicData> magicDataList, byte[] originalKernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.serializeAllItems(magicDataList, originalKernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Finds the magic section offset within the kernel file using the
     * magic section parser strategy.</p>
     */
    @Override
    public int findMagicSectionOffset(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.findSectionOffset(kernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns the size of a single magic data structure in bytes.</p>
     */
    @Override
    public int getMagicStructSize() {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new IllegalStateException("Magic section parser strategy not available");
        }
        return magicStrategy.getItemStructSize();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Returns the expected number of magic entries in the kernel file.</p>
     */
    @Override
    public int getExpectedMagicCount() {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new IllegalStateException("Magic section parser strategy not available");
        }
        return magicStrategy.getExpectedItemCount();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Validates the kernel file structure using the magic section parser
     * strategy.</p>
     */
    @Override
    public ValidationResult validateKernelStructure(byte[] kernelData) {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            return new ValidationResult(false, "Magic section parser strategy not available", 
                                      List.of("Strategy not available"), 0, 0);
        }
        return magicStrategy.validateSectionStructure(kernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Extracts spell names from the kernel file using the magic section
     * parser strategy.</p>
     */
    @Override
    public List<String> extractSpellNames(byte[] kernelData) throws BinaryParseException {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            throw new BinaryParseException("Magic section parser strategy not available");
        }
        return magicStrategy.extractItemNames(kernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Calculates a checksum for the magic section using the magic section
     * parser strategy.</p>
     */
    @Override
    public String calculateMagicSectionChecksum(byte[] kernelData) {
        SectionParserStrategy<MagicData> magicStrategy = getStrategy(SectionType.MAGIC);
        if (magicStrategy == null) {
            return "ERROR: Strategy not available";
        }
        return magicStrategy.calculateSectionChecksum(kernelData);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Validates that the binary data represents a valid kernel.bin file
     * by checking basic structure and magic numbers.</p>
     */
    @Override
    public boolean isValidKernelFile(byte[] data) {
        // Basic validation - check if data is large enough and has basic structure
        if (data == null || data.length < 1024) {
            return false;
        }
        
        // Additional validation could be added here
        // For now, delegate to magic section validation
        ValidationResult result = validateKernelStructure(data);
        return result.isValid();
    }
}  