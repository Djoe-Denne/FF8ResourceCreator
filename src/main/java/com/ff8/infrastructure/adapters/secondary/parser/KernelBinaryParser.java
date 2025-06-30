package com.ff8.infrastructure.adapters.secondary.parser;

import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.*;
import com.ff8.domain.exceptions.BinaryParseException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KernelBinaryParser implements BinaryParserPort {
    private static final Logger logger = Logger.getLogger(KernelBinaryParser.class.getName());
    private static final int MAGIC_STRUCT_SIZE = 0x3C; // 60 bytes
    private static final int MAGIC_SECTION_OFFSET = 0x021C; // Standard offset
    private static final int EXPECTED_MAGIC_COUNT = 56;
    private static final int STRING_SECTION_OFFSET = 0x5188; // Base offset for string data
    
    @Override
    public MagicData parseMagicData(byte[] binaryData, int offset) throws BinaryParseException {
        return parseMagicData(binaryData, offset, -1); // Default index for backward compatibility
    }
    
    /**
     * Parse magic data with kernel index (position in kernel file)
     */
    public MagicData parseMagicData(byte[] binaryData, int offset, int kernelIndex) throws BinaryParseException {
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
            logger.info("Building MagicData with kernelIndex=" + kernelIndex + ", magicID=" + magicID + ", spellName='" + extractedName + "'");
            return MagicData.builder()
                    .index(kernelIndex)
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
    public byte[] serializeMagicData(MagicData magic) throws BinaryParseException {
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
        buffer.putShort((short) serializeJunctionStatusAttack(junctionStatus.getAttackStatuses()));
        buffer.putShort((short) serializeJunctionStatusDefense(junctionStatus.getDefenseStatuses()));
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
        int[] statusBitMap = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
        for (int i = 0; i < statusBitMap.length && i < 16; i++) {
            if ((statusWord & statusBitMap[i]) != 0) {
                statusSet.setBit(statusBitMap[i], true);
            }
        }
        return statusSet;
    }
    
    private int serializeElementalDefense(List<Element> defenseElements) {
        int result = 0;
        for (Element element : defenseElements) {
            result |= (1 << element.getValue());
        }
        return result;
    }
    
    private int serializeJunctionStatusAttack(StatusEffectSet statusSet) {
        int result = 0;
        int[] statusBitMap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12}; // Skip bit 10
        for (int i = 0; i < statusBitMap.length; i++) {
            if (statusSet.getBit(statusBitMap[i])) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private int serializeJunctionStatusDefense(StatusEffectSet statusSet) {
        int result = 0;
        int[] statusBitMap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        for (int i = 0; i < statusBitMap.length; i++) {
            if (statusSet.getBit(statusBitMap[i])) {
                result |= (1 << i);
            }
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

    // Missing interface methods implementation
    
    @Override
    public List<MagicData> parseAllMagicData(byte[] kernelData) throws BinaryParseException {
        logger.info("Starting to parse all magic data from kernel.bin");
        logger.info("Kernel data size: " + kernelData.length + " bytes (0x" + Integer.toHexString(kernelData.length) + ")");
        
        List<MagicData> magicList = new ArrayList<>();
        int offset = findMagicSectionOffset(kernelData);
        
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
                MagicData magic = parseMagicData(kernelData, currentOffset, i);
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
    public byte[] serializeAllMagicData(List<MagicData> magicDataList, byte[] originalKernelData) throws BinaryParseException {
        byte[] result = originalKernelData.clone();
        int offset = findMagicSectionOffset(result);
        
        for (int i = 0; i < magicDataList.size(); i++) {
            byte[] magicBytes = serializeMagicData(magicDataList.get(i));
            System.arraycopy(magicBytes, 0, result, offset + (i * MAGIC_STRUCT_SIZE), MAGIC_STRUCT_SIZE);
        }
        
        return result;
    }

    @Override
    public int findMagicSectionOffset(byte[] kernelData) throws BinaryParseException {
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
    public int getMagicStructSize() {
        return MAGIC_STRUCT_SIZE;
    }

    @Override
    public int getExpectedMagicCount() {
        return EXPECTED_MAGIC_COUNT;
    }

    @Override
    public ValidationResult validateKernelStructure(byte[] kernelData) {
        List<String> issues = new ArrayList<>();
        
        if (kernelData == null) {
            return new ValidationResult(false, "Kernel data is null", List.of("Null data"), 0, 0);
        }
        
        if (kernelData.length < MAGIC_SECTION_OFFSET + (EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE)) {
            issues.add("File too small for expected magic section");
        }
        
        boolean isValid = issues.isEmpty();
        String message = isValid ? "Valid kernel structure" : "Invalid kernel structure";
        
        return new ValidationResult(isValid, message, issues, MAGIC_SECTION_OFFSET, EXPECTED_MAGIC_COUNT);
    }

    @Override
    public List<String> extractSpellNames(byte[] kernelData) throws BinaryParseException {
        // This would typically extract spell names from the text section
        // For now, return placeholder names
        List<String> names = new ArrayList<>();
        for (int i = 0; i < EXPECTED_MAGIC_COUNT; i++) {
            names.add("Spell " + i);
        }
        return names;
    }

    @Override
    public String calculateMagicSectionChecksum(byte[] kernelData) {
        // Simple checksum calculation for the magic section
        try {
            int offset = findMagicSectionOffset(kernelData);
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
    public boolean isValidKernelFile(byte[] data) {
        if (data == null || data.length < 1024) {
            return false;
        }
        
        // Check for minimum expected size
        return data.length >= MAGIC_SECTION_OFFSET + (EXPECTED_MAGIC_COUNT * MAGIC_STRUCT_SIZE);
    }
} 