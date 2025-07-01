package com.ff8.domain.services;

import com.ff8.application.dto.RawViewDTO;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.GF;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for mapping MagicData to raw binary field representation.
 * This service understands the exact binary structure and provides hex view data.
 */
public class RawDataMappingService {
    
    /**
     * Map a MagicData entity to raw view fields with proper offsets and hex values.
     */
    public RawViewDTO mapToRawView(MagicData magic) {
        List<RawViewDTO.RawFieldEntry> fields = new ArrayList<>();
        
        // 0x00-0x03: Text pointers
        fields.add(new RawViewDTO.RawFieldEntry("00000000", "WORD", "offsetSpellName", 
                formatWord(magic.getOffsetSpellName())));
        fields.add(new RawViewDTO.RawFieldEntry("00000002", "WORD", "offsetSpellDescription", 
                formatWord(magic.getOffsetSpellDescription())));
        
        // 0x04-0x09: Basic data
        fields.add(new RawViewDTO.RawFieldEntry("00000004", "WORD", "magicID", 
                formatWord(magic.getMagicID())));
        fields.add(new RawViewDTO.RawFieldEntry("00000006", "BYTE", "animationTriggered", 
                formatByte(magic.getAnimationTriggered())));
        fields.add(new RawViewDTO.RawFieldEntry("00000007", "BYTE", "attackType", 
                formatByte(magic.getAttackType().getValue())));
        fields.add(new RawViewDTO.RawFieldEntry("00000008", "BYTE", "spellPower", 
                formatByte(magic.getSpellPower())));
        fields.add(new RawViewDTO.RawFieldEntry("00000009", "BYTE", "unknown1", 
                formatByte(magic.getUnknown1())));
        
        // 0x0A-0x0F: Target, flags, and basic properties
        fields.add(new RawViewDTO.RawFieldEntry("0000000A", "BYTE", "defaultTarget", 
                formatByte(serializeTargetFlags(magic.getTargetInfo()))));
        fields.add(new RawViewDTO.RawFieldEntry("0000000B", "BYTE", "attackFlags", 
                formatByte(serializeAttackFlags(magic.getAttackFlags()))));
        fields.add(new RawViewDTO.RawFieldEntry("0000000C", "BYTE", "drawResist", 
                formatByte(magic.getDrawResist())));
        fields.add(new RawViewDTO.RawFieldEntry("0000000D", "BYTE", "hitCount", 
                formatByte(magic.getHitCount())));
        fields.add(new RawViewDTO.RawFieldEntry("0000000E", "BYTE", "element", 
                formatByte(magic.getElement().getValue())));
        fields.add(new RawViewDTO.RawFieldEntry("0000000F", "BYTE", "unknown2", 
                formatByte(magic.getUnknown2())));
        
        // 0x10-0x16: Status effects
        StatusData statusData = serializeStatusEffects(magic.getStatusEffects());
        fields.add(new RawViewDTO.RawFieldEntry("00000010", "DWORD", "statuses1", 
                formatDword(statusData.dword)));
        fields.add(new RawViewDTO.RawFieldEntry("00000014", "WORD", "statuses0", 
                formatWord(statusData.word)));
        fields.add(new RawViewDTO.RawFieldEntry("00000016", "BYTE", "statusAttackEnabler", 
                formatByte(magic.getStatusAttackEnabler())));
        
        // 0x17-0x1F: Junction stats
        var junctionStats = magic.getJunctionStats();
        fields.add(new RawViewDTO.RawFieldEntry("00000017", "BYTE", "hpJunctionValue", 
                formatByte(junctionStats.getHp())));
        fields.add(new RawViewDTO.RawFieldEntry("00000018", "BYTE", "strJunctionValue", 
                formatByte(junctionStats.getStr())));
        fields.add(new RawViewDTO.RawFieldEntry("00000019", "BYTE", "vitJunctionValue", 
                formatByte(junctionStats.getVit())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001A", "BYTE", "magJunctionValue", 
                formatByte(junctionStats.getMag())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001B", "BYTE", "sprJunctionValue", 
                formatByte(junctionStats.getSpr())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001C", "BYTE", "spdJunctionValue", 
                formatByte(junctionStats.getSpd())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001D", "BYTE", "evaJunctionValue", 
                formatByte(junctionStats.getEva())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001E", "BYTE", "hitJunctionValue", 
                formatByte(junctionStats.getHit())));
        fields.add(new RawViewDTO.RawFieldEntry("0000001F", "BYTE", "luckJunctionValue", 
                formatByte(junctionStats.getLuck())));
        
        // 0x20-0x23: Junction elemental
        var junctionElemental = magic.getJunctionElemental();
        fields.add(new RawViewDTO.RawFieldEntry("00000020", "BYTE", "jElemAttack", 
                formatByte(junctionElemental.getAttackElement().getValue())));
        fields.add(new RawViewDTO.RawFieldEntry("00000021", "BYTE", "jElemAttackValue", 
                formatByte(junctionElemental.getAttackValue())));
        fields.add(new RawViewDTO.RawFieldEntry("00000022", "BYTE", "jElemDefense", 
                formatByte(serializeElementalDefense(junctionElemental.getDefenseElements()))));
        fields.add(new RawViewDTO.RawFieldEntry("00000023", "BYTE", "jElemDefenseValue", 
                formatByte(junctionElemental.getDefenseValue())));
        
        // 0x24-0x29: Junction status
        var junctionStatus = magic.getJunctionStatus();
        fields.add(new RawViewDTO.RawFieldEntry("00000024", "BYTE", "jStatusAttackValue", 
                formatByte(junctionStatus.getAttackValue())));
        fields.add(new RawViewDTO.RawFieldEntry("00000025", "BYTE", "jStatusDefenseValue", 
                formatByte(junctionStatus.getDefenseValue())));
        fields.add(new RawViewDTO.RawFieldEntry("00000026", "WORD", "jStatusesAttack", 
                formatWord(serializeJunctionStatusAttack(junctionStatus.getAttackStatuses()))));
        fields.add(new RawViewDTO.RawFieldEntry("00000028", "WORD", "jStatusesDefend", 
                formatWord(serializeJunctionStatusDefense(junctionStatus.getDefenseStatuses()))));
        
        // 0x2A-0x39: GF Compatibility
        var gfCompatibility = magic.getGfCompatibility();
        GF[] gfs = {
            GF.QUEZACOLT, GF.SHIVA, GF.IFRIT, GF.SIREN,
            GF.BROTHERS, GF.DIABLOS, GF.CARBUNCLE, GF.LEVIATHAN,
            GF.PANDEMONA, GF.CERBERUS, GF.ALEXANDER, GF.DOOMTRAIN,
            GF.BAHAMUT, GF.CACTUAR, GF.TONBERRY, GF.EDEN
        };
        
        String[] gfNames = {
            "quezacoltCompatibility", "shivaCompatibility", "ifritCompatibility", "sirenCompatibility",
            "brothersCompatibility", "diablosCompatibility", "carbuncleCompatibility", "leviathanCompatibility",
            "pandemonaCompatibility", "cerberusCompatibility", "alexanderCompatibility", "doomtrainCompatibility",
            "bahamutCompatibility", "cactuarCompatibility", "tonberryCompatibility", "edenCompatibility"
        };
        
        for (int i = 0; i < 16; i++) {
            String offset = String.format("%08X", 0x2A + i);
            int value = (i < gfs.length) ? gfCompatibility.getCompatibility(gfs[i]) : 0;
            fields.add(new RawViewDTO.RawFieldEntry(offset, "BYTE", gfNames[i], formatByte(value)));
        }
        
        // 0x3A-0x3B: Unknown3
        fields.add(new RawViewDTO.RawFieldEntry("0000003A", "WORD", "unknown3", 
                formatWord(magic.getUnknown3())));
        
        return new RawViewDTO(fields);
    }
    
    private String formatByte(int value) {
        return String.format("%02X", value & 0xFF);
    }
    
    private String formatWord(int value) {
        return String.format("%04X", value & 0xFFFF);
    }
    
    private String formatDword(long value) {
        return String.format("%08X", value & 0xFFFFFFFFL);
    }
    
    // Helper methods to serialize complex objects back to binary values
    private int serializeTargetFlags(com.ff8.domain.entities.TargetFlags targetInfo) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            if (targetInfo.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private int serializeAttackFlags(com.ff8.domain.entities.AttackFlags attackFlags) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            if (attackFlags.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private StatusData serializeStatusEffects(com.ff8.domain.entities.StatusEffectSet statusEffects) {
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
    
    private int serializeElementalDefense(java.util.List<com.ff8.domain.entities.enums.Element> defenseElements) {
        int result = 0;
        for (com.ff8.domain.entities.enums.Element element : defenseElements) {
            result |= element.getValue();
        }
        return result;
    }
    
    private int serializeJunctionStatusAttack(com.ff8.domain.entities.StatusEffectSet statusSet) {
        int result = 0;
        for (int i = 0; i < 16; i++) {
            if (statusSet.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    private int serializeJunctionStatusDefense(com.ff8.domain.entities.StatusEffectSet statusSet) {
        int result = 0;
        for (int i = 0; i < 16; i++) {
            if (statusSet.getBit(i)) {
                result |= (1 << i);
            }
        }
        return result;
    }
    
    // Helper class to hold status data
    private static class StatusData {
        long dword;  // 32-bit
        int word;    // 16-bit
    }
} 