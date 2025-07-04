package com.ff8.infrastructure.adapters.secondary.parser;

import com.ff8.application.ports.secondary.BinaryParserPort.ValidationResult;
import com.ff8.application.ports.secondary.SectionParserStrategy;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.*;
import com.ff8.domain.exceptions.BinaryParseException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Strategy implementation for parsing the Magic section of the FF8 kernel.bin file.
 * Handles parsing and serialization of spell/magic data including stats, effects, and junction information.
 */
public class MagicSectionParser implements SectionParserStrategy<MagicData> {
    private static final Logger logger = Logger.getLogger(MagicSectionParser.class.getName());
    
    private static final int MAGIC_STRUCT_SIZE = 0x3C; // 60 bytes
    private static final int MAGIC_SECTION_OFFSET = 0x021C; // Standard offset
    private static final int EXPECTED_MAGIC_COUNT = 56;
    private static final int STRING_SECTION_OFFSET = 0x5188; // Base offset for string data
    
    @Override
    public SectionType getSectionType() {
        return SectionType.MAGIC;
    }
    
    @Override
    public MagicData parseItem(byte[] binaryData, int offset, int index) throws BinaryParseException {
        if (binaryData == null) {
            throw new BinaryParseException("Binary data cannot be null");
        }
        
        if (offset < 0 || offset + MAGIC_STRUCT_SIZE > binaryData.length) {
            throw new BinaryParseException("Invalid offset: " + offset + " for binary data of length " + binaryData.length);
        }
        
        try {
            logger.info("Parsing magic data at offset 0x" + Integer.toHexString(offset) + " (" + offset + ")");
            
            ByteBuffer buffer = ByteBuffer.wrap(binaryData, offset, MAGIC_STRUCT_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // Log first 16 bytes for debugging
            StringBuilder hexDump = new StringBuilder("First 16 bytes: ");
            buffer.mark();
            for (int i = 0; i < Math.min(16, MAGIC_STRUCT_SIZE); i++) {
                hexDump.append(String.format("%02X ", buffer.get() & 0xFF));
            }
            logger.info(hexDump.toString());
            buffer.reset();
            
            // 0x00-0x03: Text pointers (preserve for exact serialization)
            int offsetSpellName = buffer.getShort() & 0xFFFF;
            int offsetSpellDescription = buffer.getShort() & 0xFFFF;
            logger.info("Raw text pointers from binary: spell=0x" + Integer.toHexString(offsetSpellName) + " (" + offsetSpellName + "), desc=0x" + Integer.toHexString(offsetSpellDescription) + " (" + offsetSpellDescription + ")");
            
            // Log the first few bytes for debugging
            int currentPos = buffer.position();
            buffer.position(0);
            StringBuilder offsetHexDump = new StringBuilder("First 8 bytes of magic struct: ");
            for (int i = 0; i < 8; i++) {
                offsetHexDump.append(String.format("%02X ", buffer.get() & 0xFF));
            }
            logger.info(offsetHexDump.toString());
            buffer.position(currentPos); // Restore position instead of using mark/reset
            
            // 0x04-0x05: Magic ID
            int magicID = buffer.getShort() & 0xFFFF;
            logger.info("Magic ID: " + magicID);
            
            // 0x06: Animation triggered
            int animationTriggered = buffer.get() & 0xFF;
            
            // 0x07: Attack type
            int attackTypeValue = buffer.get() & 0xFF;
            logger.info("Attack type value: " + attackTypeValue);
            AttackType attackType = AttackType.fromValue(attackTypeValue);
            
            // 0x08: Spell power
            int spellPower = buffer.get() & 0xFF;
            logger.info("Spell power: " + spellPower);
            
            // 0x09: Unknown1
            int unknown1 = buffer.get() & 0xFF;
            
            // 0x0A: Default target (parse TargetFlags)
            int targetByte = buffer.get() & 0xFF;
            TargetFlags targetInfo = parseTargetFlags(targetByte);
            
            // 0x0B: Attack flags
            int flagByte = buffer.get() & 0xFF;
            AttackFlags attackFlags = parseAttackFlags(flagByte);
            
            // 0x0C: Draw resist
            int drawResist = buffer.get() & 0xFF;
            
            // 0x0D: Hit count
            int hitCount = buffer.get() & 0xFF;
            logger.info("Hit count: " + hitCount);
            
            // 0x0E: Element
            int elementValue = buffer.get() & 0xFF;
            logger.info("Element value: " + elementValue + " (0x" + Integer.toHexString(elementValue) + ")");
            Element element = Element.fromValue(elementValue);
            
            // 0x0F: Unknown2
            int unknown2 = buffer.get() & 0xFF;
            
            // 0x10-0x15: Status effects (6 bytes = 48 bits)
            long statusDword = buffer.getInt() & 0xFFFFFFFFL; // 32 bits
            int statusWord = buffer.getShort() & 0xFFFF;      // 16 bits
            StatusEffectSet statusEffects = parseStatusEffects(statusDword, statusWord);
            
            // 0x16: Status attack enabler
            int statusAttackEnabler = buffer.get() & 0xFF;
            
            // 0x17-0x1F: Junction stats (9 bytes)
            JunctionStats junctionStats = parseJunctionStats(buffer);
            
            // 0x20-0x23: Junction elemental (4 bytes)
            JunctionElemental junctionElemental = parseJunctionElemental(buffer);
            
            // 0x24-0x29: Junction status (6 bytes)
            JunctionStatusEffects junctionStatus = parseJunctionStatus(buffer);
            
            // 0x2A-0x39: GF Compatibility (16 bytes)
            GFCompatibilitySet gfCompatibility = parseGFCompatibility(buffer);
            
            // 0x3A-0x3B: Unknown3
            int unknown3 = buffer.getShort() & 0xFFFF;

            // Extract strings using the offsets
            String extractedName = "";
            String extractedDescription = "";
            
            if (offsetSpellName >= 0) {
                int nameOffset = STRING_SECTION_OFFSET + offsetSpellName;
                logger.info("Calculating spell name offset: 0x" + Integer.toHexString(STRING_SECTION_OFFSET) + " + 0x" + Integer.toHexString(offsetSpellName) + " = 0x" + Integer.toHexString(nameOffset));
                String rawName = extractNullTerminatedString(binaryData, nameOffset);
                extractedName = decipherCaesarCode(rawName);
                logger.info("=== Extracted spell name: '" + rawName + "' -> deciphered: '" + extractedName + "' ===");
            } else {
                logger.warning("Spell name offset is negative - no name to extract");
            }
            
            if (offsetSpellDescription >= 0) {
                int descOffset = STRING_SECTION_OFFSET + offsetSpellDescription;
                logger.info("Calculating spell description offset: 0x" + Integer.toHexString(STRING_SECTION_OFFSET) + " + 0x" + Integer.toHexString(offsetSpellDescription) + " = 0x" + Integer.toHexString(descOffset));
                String rawDescription = extractNullTerminatedString(binaryData, descOffset);
                extractedDescription = decipherCaesarCode(rawDescription);
                logger.info("=== Extracted spell description: '" + rawDescription + "' -> deciphered: '" + extractedDescription + "' ===");
            } else {
                logger.warning("Spell description offset is negative - no description to extract");
            }
            
            
            // Use Lombok Builder pattern for immutable MagicData
            logger.info("Building MagicData with kernelIndex=" + index + ", magicID=" + magicID + ", spellName='" + extractedName + "'");
            return MagicData.builder()
                    .index(index)
                    .offsetSpellName(offsetSpellName)
                    .offsetSpellDescription(offsetSpellDescription)
                    .extractedSpellName(extractedName)
                    .extractedSpellDescription(extractedDescription)
                    .magicID(magicID)
                    .animationTriggered(animationTriggered)
                    .attackType(attackType)
                    .spellPower(spellPower)
                    .unknown1(unknown1)
                    .drawResist(drawResist)
                    .hitCount(hitCount)
                    .element(element)
                    .unknown2(unknown2)
                    .statusAttackEnabler(statusAttackEnabler)
                    .unknown3(unknown3)
                    .targetInfo(targetInfo)
                    .attackFlags(attackFlags)
                    .statusEffects(statusEffects)
                    .junctionStats(junctionStats)
                    .junctionElemental(junctionElemental)
                    .junctionStatus(junctionStatus)
                    .gfCompatibility(gfCompatibility)
                    .build();
            
        } catch (Exception e) {
            throw new BinaryParseException("Failed to parse magic data at offset " + offset + ": " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] serializeItem(MagicData magic) throws BinaryParseException {
        if (magic == null) {
            throw new BinaryParseException("Magic data cannot be null");
        }
        
        try {
            ByteBuffer buffer = ByteBuffer.allocate(MAGIC_STRUCT_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // 0x00-0x03: Text pointers
            buffer.putShort((short) magic.getOffsetSpellName());
            buffer.putShort((short) magic.getOffsetSpellDescription());
            
            // 0x04-0x05: Magic ID
            buffer.putShort((short) magic.getMagicID());
            
            // 0x06: Animation triggered
            buffer.put((byte) magic.getAnimationTriggered());
            
            // 0x07: Attack type
            buffer.put((byte) magic.getAttackType().getValue());
            
            // 0x08: Spell power
            buffer.put((byte) magic.getSpellPower());
            
            // 0x09: Unknown1
            buffer.put((byte) magic.getUnknown1());
            
            // 0x0A: Default target
            buffer.put((byte) serializeTargetFlags(magic.getTargetInfo()));
            
            // 0x0B: Attack flags
            buffer.put((byte) serializeAttackFlags(magic.getAttackFlags()));
            
            // 0x0C: Draw resist
            buffer.put((byte) magic.getDrawResist());
            
            // 0x0D: Hit count
            buffer.put((byte) magic.getHitCount());
            
            // 0x0E: Element
            buffer.put((byte) magic.getElement().getValue());
            
            // 0x0F: Unknown2
            buffer.put((byte) magic.getUnknown2());
            
            // 0x10-0x15: Status effects
            StatusData statusData = serializeStatusEffects(magic.getStatusEffects());
            buffer.putInt((int) statusData.dword);
            buffer.putShort((short) statusData.word);
            
            // 0x16: Status attack enabler
            buffer.put((byte) magic.getStatusAttackEnabler());
            
            // 0x17-0x1F: Junction stats
            serializeJunctionStats(buffer, magic.getJunctionStats());
            
            // 0x20-0x23: Junction elemental
            serializeJunctionElemental(buffer, magic.getJunctionElemental());
            
            // 0x24-0x29: Junction status
            serializeJunctionStatus(buffer, magic.getJunctionStatus());
            
            // 0x2A-0x39: GF Compatibility
            serializeGFCompatibility(buffer, magic.getGfCompatibility());
            
            // 0x3A-0x3B: Unknown3
            buffer.putShort((short) magic.getUnknown3());
            
            return buffer.array();
            
        } catch (Exception e) {
            throw new BinaryParseException("Failed to serialize magic data: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<MagicData> parseAllItems(byte[] kernelData) throws BinaryParseException {
        logger.info("Starting to parse all magic data from kernel.bin");
        logger.info("Kernel data size: " + kernelData.length + " bytes (0x" + Integer.toHexString(kernelData.length) + ")");
        
        List<MagicData> magicList = new ArrayList<>();
        int offset = findSectionOffset(kernelData);
        
        logger.info("Magic section offset: 0x" + Integer.toHexString(offset) + " (" + offset + ")");
        logger.info("Magic struct size: " + MAGIC_STRUCT_SIZE + " bytes");
        logger.info("Expected magic count: " + EXPECTED_MAGIC_COUNT);
        logger.info("Total magic section size: " + (EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE) + " bytes");
        
        int magicSectionEnd = offset + (EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE);
        logger.info("Magic section end: 0x" + Integer.toHexString(magicSectionEnd) + " (" + magicSectionEnd + ")");
        
        if (magicSectionEnd > kernelData.length) {
            throw new BinaryParseException("Magic section would extend beyond file: need " + magicSectionEnd + " but file is only " + kernelData.length + " bytes");
        }
        
        for (int i = 0; i < EXPECTED_MAGIC_COUNT; i++) {
            int currentOffset = offset + (i * MAGIC_STRUCT_SIZE);
            logger.info("Parsing magic entry " + i + " at offset 0x" + Integer.toHexString(currentOffset) + " (" + currentOffset + ")");
            
            try {
                MagicData magic = parseItem(kernelData, currentOffset, i);
                magicList.add(magic);
                logger.info("Successfully parsed magic entry " + i + " with index=" + magic.getIndex() + 
                           ", magicID=" + magic.getMagicID() + ", name='" + magic.getExtractedSpellName() + "'");
            } catch (Exception e) {
                logger.severe("Failed to parse magic entry " + i + " at offset " + currentOffset + ": " + e.getMessage());
                throw e;
            }
        }
        
        logger.info("Successfully parsed all " + magicList.size() + " magic entries");
        return magicList;
    }
    
    @Override
    public byte[] serializeAllItems(List<MagicData> magicDataList, byte[] originalKernelData) throws BinaryParseException {
        byte[] result = originalKernelData.clone();
        int offset = findSectionOffset(result);
        
        for (int i = 0; i < magicDataList.size(); i++) {
            byte[] magicBytes = serializeItem(magicDataList.get(i));
            System.arraycopy(magicBytes, 0, result, offset + (i * MAGIC_STRUCT_SIZE), MAGIC_STRUCT_SIZE);
        }
        
        return result;
    }
    
    @Override
    public int getItemStructSize() {
        return MAGIC_STRUCT_SIZE;
    }
    
    @Override
    public int getExpectedItemCount() {
        return EXPECTED_MAGIC_COUNT;
    }
    
    @Override
    public int findSectionOffset(byte[] kernelData) throws BinaryParseException {
        // For now, return the standard offset. In a real implementation,
        // this would scan the kernel.bin structure to find the actual offset
        logger.info("Using standard magic section offset: 0x" + Integer.toHexString(MAGIC_SECTION_OFFSET) + " (" + MAGIC_SECTION_OFFSET + ")");
        logger.info("Kernel data size: " + kernelData.length + " bytes");
        
        // Let's examine what's at the supposed magic section offset
        if (kernelData.length > MAGIC_SECTION_OFFSET + 16) {
            StringBuilder hexDump = new StringBuilder("Data at magic section offset 0x" + Integer.toHexString(MAGIC_SECTION_OFFSET) + ": ");
            for (int i = 0; i < 16; i++) {
                hexDump.append(String.format("%02X ", kernelData[MAGIC_SECTION_OFFSET + i] & 0xFF));
            }
            logger.info(hexDump.toString());
        }
        
        return MAGIC_SECTION_OFFSET;
    }
    
    @Override
    public ValidationResult validateSectionStructure(byte[] kernelData) {
        List<String> issues = new ArrayList<>();
        
        if (kernelData == null) {
            return new ValidationResult(false, "Kernel data is null", List.of("Null data"), 0, 0);
        }
        
        if (kernelData.length < MAGIC_SECTION_OFFSET + (EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE)) {
            issues.add("File too small for expected magic section");
        }
        
        boolean isValid = issues.isEmpty();
        String message = isValid ? "Valid magic section structure" : "Invalid magic section structure";
        
        return new ValidationResult(isValid, message, issues, MAGIC_SECTION_OFFSET, EXPECTED_MAGIC_COUNT);
    }
    
    @Override
    public String calculateSectionChecksum(byte[] kernelData) {
        // Simple checksum calculation for the magic section
        try {
            int offset = findSectionOffset(kernelData);
            int sectionSize = EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE;
            
            long checksum = 0;
            for (int i = 0; i < sectionSize; i++) {
                checksum += Byte.toUnsignedInt(kernelData[offset + i]);
            }
            
            return String.format("%08X", checksum & 0xFFFFFFFFL);
        } catch (Exception e) {
            return "ERROR";
        }
    }
    
    @Override
    public List<String> extractItemNames(byte[] kernelData) throws BinaryParseException {
        // This would typically extract spell names from the text section
        // For now, return placeholder names
        List<String> names = new ArrayList<>();
        for (int i = 0; i < EXPECTED_MAGIC_COUNT; i++) {
            names.add("Spell " + i);
        }
        return names;
    }
    
    // Private helper methods (moved from the original KernelBinaryParser)
    
    private TargetFlags parseTargetFlags(int targetByte) {
        TargetFlags flags = new TargetFlags();
        for (int i = 0; i < 8; i++) {
            flags.setBit(i, (targetByte & (1 << i)) != 0);
        }
        return flags;
    }
    
    private AttackFlags parseAttackFlags(int flagByte) {
        AttackFlags flags = new AttackFlags();
        for (int i = 0; i < 8; i++) {
            flags.setBit(i, (flagByte & (1 << i)) != 0);
        }
        return flags;
    }
    
    private StatusEffectSet parseStatusEffects(long statusDword, int statusWord) {
        StatusEffectSet statusSet = new StatusEffectSet();
        
        // Handle 32-bit DWORD (bits 0-31)
        for (int i = 0; i < 32; i++) {
            if ((statusDword & (1L << i)) != 0) {
                statusSet.setBit(i, true);
            }
        }
        
        // Handle 16-bit WORD (bits 32-47)
        for (int i = 0; i < 16; i++) {
            if ((statusWord & (1 << i)) != 0) {
                statusSet.setBit(32 + i, true);
            }
        }
        
        return statusSet;
    }
    
    private JunctionStats parseJunctionStats(ByteBuffer buffer) {
        int hp = buffer.get() & 0xFF;
        int str = buffer.get() & 0xFF;
        int vit = buffer.get() & 0xFF;
        int mag = buffer.get() & 0xFF;
        int spr = buffer.get() & 0xFF;
        int spd = buffer.get() & 0xFF;
        int eva = buffer.get() & 0xFF;
        int hit = buffer.get() & 0xFF;
        int luck = buffer.get() & 0xFF;
        return new JunctionStats(hp, str, vit, mag, spr, spd, eva, hit, luck);
    }
    
    private JunctionElemental parseJunctionElemental(ByteBuffer buffer) {
        int attackElement = buffer.get() & 0xFF;
        int attackValue = buffer.get() & 0xFF;
        int defenseElementByte = buffer.get() & 0xFF;
        int defenseValue = buffer.get() & 0xFF;
        
        Element attack = Element.fromValue(attackElement);
        List<Element> defenseElements = parseElementalDefense(defenseElementByte);
        
        return new JunctionElemental(attack, attackValue, defenseElements, defenseValue);
    }
    
    private JunctionStatusEffects parseJunctionStatus(ByteBuffer buffer) {
        int attackValue = buffer.get() & 0xFF;
        int defenseValue = buffer.get() & 0xFF;
        
        int attackStatusWord = buffer.getShort() & 0xFFFF;
        int defenseStatusWord = buffer.getShort() & 0xFFFF;
        
        StatusEffectSet attackStatuses = parseJunctionStatus(attackStatusWord);
        StatusEffectSet defenseStatuses = parseJunctionStatus(defenseStatusWord);
        
        return new JunctionStatusEffects(attackStatuses, attackValue, defenseStatuses, defenseValue);
    }
    
    private GFCompatibilitySet parseGFCompatibility(ByteBuffer buffer) {
        GFCompatibilitySet compatibility = new GFCompatibilitySet();
        
        GF[] gfs = GF.values();
        for (int i = 0; i < Math.min(16, gfs.length); i++) {
            int value = buffer.get() & 0xFF;
            compatibility.setCompatibility(gfs[i], value);
        }
        
        return compatibility;
    }
    
    // Serialization helper methods
    private int serializeTargetFlags(TargetFlags targetInfo) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            if (targetInfo.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private int serializeAttackFlags(AttackFlags attackFlags) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            if (attackFlags.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private StatusData serializeStatusEffects(StatusEffectSet statusEffects) {
        StatusData statusData = new StatusData();
        statusData.dword = 0;
        statusData.word = 0;
        
        // Serialize bits 0-31 to DWORD
        for (int i = 0; i < 32; i++) {
            if (statusEffects.getBit(i)) {
                statusData.dword |= (1L << i);
            }
        }
        
        // Serialize bits 32-47 to WORD
        for (int i = 0; i < 16; i++) {
            if (statusEffects.getBit(32 + i)) {
                statusData.word |= (1 << i);
            }
        }
        
        return statusData;
    }
    
    private void serializeJunctionStats(ByteBuffer buffer, JunctionStats stats) {
        buffer.put((byte) stats.getHp());
        buffer.put((byte) stats.getStr());
        buffer.put((byte) stats.getVit());
        buffer.put((byte) stats.getMag());
        buffer.put((byte) stats.getSpr());
        buffer.put((byte) stats.getSpd());
        buffer.put((byte) stats.getEva());
        buffer.put((byte) stats.getHit());
        buffer.put((byte) stats.getLuck());
    }
    
    private void serializeJunctionElemental(ByteBuffer buffer, JunctionElemental elemental) {
        buffer.put((byte) elemental.getAttackElement().getValue());
        buffer.put((byte) elemental.getAttackValue());
        buffer.put((byte) serializeElementalDefense(elemental.getDefenseElements()));
        buffer.put((byte) elemental.getDefenseValue());
    }
    
    private void serializeJunctionStatus(ByteBuffer buffer, JunctionStatusEffects junctionStatus) {
        buffer.put((byte) junctionStatus.getAttackValue());
        buffer.put((byte) junctionStatus.getDefenseValue());
        buffer.putShort((short) serializeJunctionStatus(junctionStatus.getAttackStatuses()));
        buffer.putShort((short) serializeJunctionStatus(junctionStatus.getDefenseStatuses()));
    }
    
    private void serializeGFCompatibility(ByteBuffer buffer, GFCompatibilitySet compatibility) {
        GF[] gfs = GF.values();
        for (int i = 0; i < 16; i++) {
            if (i < gfs.length) {
                buffer.put((byte) compatibility.getCompatibility(gfs[i]));
            } else {
                buffer.put((byte) 0);
            }
        }
    }
    
    // Helper methods for complex field parsing
    private List<Element> parseElementalDefense(int defenseElementByte) {
        List<Element> elements = new ArrayList<>();
        int[] elementBitMap = {1, 2, 4, 8, 16, 32, 64, 128};
        for (int i = 0; i < 8; i++) {
            if ((defenseElementByte & elementBitMap[i]) != 0) {
                try {
                    elements.add(Element.fromValue(elementBitMap[i]));
                } catch (IllegalArgumentException e) {
                    // Skip unknown elements
                }
            }
        }
        return elements;
    }

    private StatusEffectSet parseJunctionStatus(int statusWord) {
        StatusEffectSet statusSet = new StatusEffectSet();
        
        // FF8 Junction Status Mapping (from junction bits to main status bits)
        // Junction bits 0-6: death(32), poison(33), petrify(34), darkness(35), silence(36), berserk(37), zombie(38)
        // Junction bits 7-12: sleep(0), slow(2), stop(3), curse(9), confusion(14), drain(15)
        int[] junctionToMainStatusMap = {
            32, // bit 0 -> death
            33, // bit 1 -> poison  
            34, // bit 2 -> petrify
            35, // bit 3 -> darkness
            36, // bit 4 -> silence
            37, // bit 5 -> berserk
            38, // bit 6 -> zombie
            0,  // bit 7 -> sleep
            2,  // bit 8 -> slow
            3,  // bit 9 -> stop
            9,  // bit 10 -> curse
            14, // bit 11 -> confusion
            15  // bit 12 -> drain
            // bits 13-15 are unused
        };
        
        for (int junctionBit = 0; junctionBit < junctionToMainStatusMap.length; junctionBit++) {
            if ((statusWord & (1 << junctionBit)) != 0) {
                int mainStatusBit = junctionToMainStatusMap[junctionBit];
                statusSet.setBit(mainStatusBit, true);
            }
        }
        return statusSet;
    }

    private int serializeJunctionStatus(StatusEffectSet statusSet) {
        int result = 0;
        
        // Reverse mapping: main status bits to junction bits
        int[] mainToJunctionStatusMap = new int[39]; // 0-38
        for (int i = 0; i < mainToJunctionStatusMap.length; i++) {
            mainToJunctionStatusMap[i] = -1; // default to unavailable
        }
        
        // Set up the reverse mapping
        mainToJunctionStatusMap[0] = 7;   // sleep -> junction bit 7
        mainToJunctionStatusMap[2] = 8;   // slow -> junction bit 8
        mainToJunctionStatusMap[3] = 9;   // stop -> junction bit 9
        mainToJunctionStatusMap[9] = 10;  // curse -> junction bit 10
        mainToJunctionStatusMap[14] = 11; // confusion -> junction bit 11
        mainToJunctionStatusMap[15] = 12; // drain -> junction bit 12
        mainToJunctionStatusMap[32] = 0;  // death -> junction bit 0
        mainToJunctionStatusMap[33] = 1;  // poison -> junction bit 1
        mainToJunctionStatusMap[34] = 2;  // petrify -> junction bit 2
        mainToJunctionStatusMap[35] = 3;  // darkness -> junction bit 3
        mainToJunctionStatusMap[36] = 4;  // silence -> junction bit 4
        mainToJunctionStatusMap[37] = 5;  // berserk -> junction bit 5
        mainToJunctionStatusMap[38] = 6;  // zombie -> junction bit 6
        
        for (int mainBit = 0; mainBit < mainToJunctionStatusMap.length; mainBit++) {
            int junctionBit = mainToJunctionStatusMap[mainBit];
            if (junctionBit >= 0 && statusSet.getBit(mainBit)) {
                result |= (1 << junctionBit);
            }
        }
        return result;
    }
    
    private int serializeElementalDefense(List<Element> defenseElements) {
        int result = 0;
        for (Element element : defenseElements) {
            result |= element.getValue();
        }
        return result;
    }
    
    /**
     * Decipher FF8's Caesar cipher encoding
     * Rules:
     * - Uppercase letters: Subtract 4
     * - Digits: Add 15  
     * - Lowercase letters: Add 2
     * @param encryptedText The encrypted text from the binary
     * @return The deciphered text
     */
    private String decipherCaesarCode(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        StringBuilder decrypted = new StringBuilder();
        
        for (char c : encryptedText.toCharArray()) {
            char decryptedChar = c;
            
            if (c >= 'D' && c <= '^') {
                // Uppercase: Subtract 4
                decryptedChar = (char) (c - 4);
            } else if (c >= '0' && c <= '9') {
                // Digits: Add 15 (with wraparound)
                decryptedChar = (char) (c + 15);
            } else if (c >= '_' && c <= 'x') {
                // Lowercase: Add 2
                decryptedChar = (char) (c + 2);
            }
            // Other characters (spaces, punctuation) remain unchanged
            
            decrypted.append(decryptedChar);
        }
        
        return decrypted.toString();
    }

    /**
     * Extract a null-terminated string from binary data
     * @param binaryData The complete binary data
     * @param offset The offset where the string starts
     * @return The extracted string, or empty string if invalid offset
     */
    private String extractNullTerminatedString(byte[] binaryData, int offset) {
        logger.info("Extracting string at offset 0x" + Integer.toHexString(offset) + " (" + offset + ")");
        
        if (binaryData == null) {
            logger.warning("Binary data is null");
            return "";
        }
        
        if (offset < 0) {
            logger.warning("Negative offset: " + offset);
            return "";
        }
        
        if (offset >= binaryData.length) {
            logger.warning("Offset " + offset + " exceeds binary data length " + binaryData.length);
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        int maxLength = Math.min(100, binaryData.length - offset); // Limit to 100 chars for safety
        
        for (int i = 0; i < maxLength; i++) {
            byte b = binaryData[offset + i];
            if (b == 0) { // Null terminator
                logger.info("Found null terminator at position " + i + ", extracted string: '" + sb.toString() + "'");
                break;
            }
            // Only add printable ASCII characters
            if (b >= 32 && b <= 126) {
                sb.append((char) (b & 0xFF));
            } else {
                logger.info("Non-printable byte at position " + i + ": 0x" + Integer.toHexString(b & 0xFF));
                // For debugging, let's see what we get if we include non-printable chars
                sb.append((char) (b & 0xFF));
            }
        }
        
        String result = sb.toString();
        logger.info("Final extracted string: '" + result + "' (length: " + result.length() + ")");
        return result;
    }

    // Inner class for status data serialization
    private static class StatusData {
        long dword;  // 32-bit
        int word;    // 16-bit
    }
} 