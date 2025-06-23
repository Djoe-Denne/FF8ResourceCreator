# Modern Kernel.bin Parser Design Document

## Overview

This document outlines the design for a modern kernel.bin parser that properly handles the FF8 magic data structure discrepancies identified in the current Doomtrain tool. The new design uses object-oriented principles and correctly interprets the ASM structure.

### Core Magic Data Class

```pseudocode
class MagicData {
    // EXACT binary format fields (to preserve when serializing)
    offsetSpellName: Integer        // 0x00 WORD
    offsetSpellDescription: Integer // 0x02 WORD
    magicID: Integer               // 0x04 WORD
    animationTriggered: Integer    // 0x06 BYTE
    attackType: Integer            // 0x07 BYTE
    spellPower: Integer            // 0x08 BYTE
    unknown1: Integer              // 0x09 BYTE
    drawResist: Integer            // 0x0C BYTE
    hitCount: Integer              // 0x0D BYTE
    element: Integer               // 0x0E BYTE
    unknown2: Integer              // 0x0F BYTE
    statusAttackEnabler: Integer   // 0x16 BYTE
    unknown3: Integer              // 0x3A WORD
    
    // Modern object-oriented properties (parsed from bitfields)
    targetInfo: TargetFlags        // from 0x0A defaultTarget
    attackFlags: AttackFlags       // from 0x0B attackFlags
    statusEffects: StatusEffectSet // from 0x10-0x15 statuses1+statuses0
    junctionStats: JunctionStats   // from 0x17-0x1F
    junctionElemental: JunctionElemental // from 0x20-0x23
    junctionStatus: JunctionStatusEffects // from 0x24-0x29
    gfCompatibility: GFCompatibilitySet // from 0x2A-0x39
}
```

### Status Effects (Proper 48-bit handling)

```pseudocode
class StatusEffectSet {
    private statusBits: BitArray[48]  // 6 bytes = 48 bits total
    
    // Properties for easy access
    sleep: Boolean
    haste: Boolean 
    slow: Boolean
    stop: Boolean
    regen: Boolean
    protect: Boolean
    shell: Boolean
    reflect: Boolean
    aura: Boolean
    curse: Boolean
    doom: Boolean
    invincible: Boolean
    petrifying: Boolean
    float: Boolean
    confusion: Boolean
    drain: Boolean
    eject: Boolean
    double: Boolean
    triple: Boolean
    defend: Boolean
    charged: Boolean
    backAttack: Boolean
    vit0: Boolean
    angelWing: Boolean
    hasMagic: Boolean
    summonGF: Boolean
    death: Boolean
    poison: Boolean
    petrify: Boolean
    darkness: Boolean
    silence: Boolean
    berserk: Boolean
    zombie: Boolean
    // ... additional status effects from missing bits
    
    method setBit(index: Integer, value: Boolean)
    method getBit(index: Integer): Boolean
    method getActiveStatuses(): List<StatusEffect>
}

enum StatusEffect {
    SLEEP = 0, HASTE = 1, SLOW = 2, STOP = 3,
    REGEN = 4, PROTECT = 5, SHELL = 6, REFLECT = 7,
    AURA = 8, CURSE = 9, DOOM = 10, INVINCIBLE = 11,
    // ... continue for all 48 possible statuses
}
```

### Target Flags

```pseudocode
class TargetFlags {
    private targetBits: BitArray[8]
    
    dead: Boolean
    single: Boolean  
    enemy: Boolean
    singleSide: Boolean
    // ... other target flags
    
    method getActiveTargets(): List<TargetType>
}

enum TargetType {
    DEAD = 0, SINGLE = 4, ENEMY = 6, SINGLE_SIDE = 3
    // ... based on actual bit meanings
}
```

### Attack Flags

```pseudocode
class AttackFlags {
    private flagBits: BitArray[8]
    
    shelled: Boolean
    reflected: Boolean
    breakDamageLimit: Boolean
    revive: Boolean
    // ... other flags
    
    method getActiveFlags(): List<AttackFlag>
}

enum AttackFlag {
    SHELLED = 0, REFLECTED = 1, BREAK_DAMAGE_LIMIT = 3, REVIVE = 4
    // ... based on actual bit meanings
}
```

### Junction Data

```pseudocode
class JunctionStats {
    hp: Integer
    str: Integer
    vit: Integer
    mag: Integer
    spr: Integer
    spd: Integer
    eva: Integer
    hit: Integer
    luck: Integer
}

class JunctionElemental {
    attackElement: Element
    attackValue: Integer
    defenseElements: List<Element>
    defenseValue: Integer
}

class JunctionStatusEffects {
    attackStatuses: StatusEffectSet  // 16-bit field
    attackValue: Integer
    defenseStatuses: StatusEffectSet  // 16-bit field  
    defenseValue: Integer
}
```

### GF Compatibility

```pseudocode
class GFCompatibilitySet {
    private compatibilities: Map<GF, Integer>
    
    method getCompatibility(gf: GF): Integer
    method setCompatibility(gf: GF, value: Integer)
    method getDisplayValue(gf: GF): Integer {
        return (100 - compatibilities[gf]) / 5
    }
}

enum GF {
    QUEZACOLT, SHIVA, IFRIT, SIREN, BROTHERS, DIABLOS,
    CARBUNCLE, LEVIATHAN, PANDEMONA, CERBERUS, 
    ALEXANDER, DOOMTRAIN, BAHAMUT, CACTUAR, TONBERRY, EDEN
}
```

## Binary Parser

```pseudocode
class KernelBinaryParser {
    
    method parseMagicData(binaryData: ByteArray, offset: Integer): MagicData {
        magic = new MagicData()
        pos = offset
        
        // 0x00-0x03: Text pointers (preserve for exact serialization)
        magic.offsetSpellName = readWord(binaryData, pos); pos += 2
        magic.offsetSpellDescription = readWord(binaryData, pos); pos += 2
        
        // 0x04-0x09: Basic data
        magic.magicID = readWord(binaryData, pos); pos += 2
        magic.animationTriggered = readByte(binaryData, pos); pos += 1
        magic.attackType = readByte(binaryData, pos); pos += 1
        magic.spellPower = readByte(binaryData, pos); pos += 1
        magic.unknown1 = readByte(binaryData, pos); pos += 1
        
        // 0x0A-0x0F: Target, flags, and basic properties
        targetByte = readByte(binaryData, pos); pos += 1
        magic.targetInfo = parseTargetFlags(targetByte)
        
        flagByte = readByte(binaryData, pos); pos += 1
        magic.attackFlags = parseAttackFlags(flagByte)
        
        magic.drawResist = readByte(binaryData, pos); pos += 1
        magic.hitCount = readByte(binaryData, pos); pos += 1
        magic.element = readByte(binaryData, pos); pos += 1
        magic.unknown2 = readByte(binaryData, pos); pos += 1
        
        // 0x10-0x16: Status effects - PROPER 6-byte handling
        statusDword = readDword(binaryData, pos); pos += 4  // 0x10-0x13: 32 bits
        statusWord = readWord(binaryData, pos); pos += 2    // 0x14-0x15: 16 bits
        magic.statusEffects = parseStatusEffects(statusDword, statusWord)
        
        magic.statusAttackEnabler = readByte(binaryData, pos); pos += 1  // 0x16
        
        // Junction stats
        magic.junctionStats = parseJunctionStats(binaryData, pos)
        pos += 9
        
        // Junction elemental and status
        magic.junctionElemental = parseJunctionElemental(binaryData, pos)
        pos += 4
        
        magic.junctionStatus = parseJunctionStatus(binaryData, pos)
        pos += 6
        
        // GF Compatibility (0x2A-0x39: 16 bytes)
        magic.gfCompatibility = parseGFCompatibility(binaryData, pos)
        pos += 16
        
        // 0x3A-0x3B: Unknown3
        magic.unknown3 = readWord(binaryData, pos)
        
        return magic
    }
    
    method parseStatusEffects(statusDword: Integer, statusWord: Integer): StatusEffectSet {
        statusSet = new StatusEffectSet()
        
        // Handle 32-bit DWORD (bits 0-31)
        for i = 0 to 31 {
            if (statusDword & (1 << i)) != 0 {
                statusSet.setBit(i, true)
            }
        }
        
        // Handle 16-bit WORD (bits 32-47)  
        for i = 0 to 15 {
            if (statusWord & (1 << i)) != 0 {
                statusSet.setBit(32 + i, true)
            }
        }
        
        return statusSet
    }
    
    method parseTargetFlags(targetByte: Integer): TargetFlags {
        flags = new TargetFlags()
        for i = 0 to 7 {
            flags.setBit(i, (targetByte & (1 << i)) != 0)
        }
        return flags
    }
    
    method parseAttackFlags(flagByte: Integer): AttackFlags {
        flags = new AttackFlags()
        for i = 0 to 7 {
            flags.setBit(i, (flagByte & (1 << i)) != 0)
        }
        return flags
    }
}
```

## Binary Serialization

```pseudocode
class MagicDataBinarySerializer {
    
    // Serialize MagicData object back to exact binary format
    method serializeToBinary(magic: MagicData): ByteArray {
        buffer = new ByteArray(0x3C)  // Exact struct size: 60 bytes
        pos = 0
        
        // 0x00-0x03: Text pointers (preserve original values)
        writeWord(buffer, pos, magic.offsetSpellName); pos += 2
        writeWord(buffer, pos, magic.offsetSpellDescription); pos += 2
        
        // 0x04-0x05: Magic ID
        writeWord(buffer, pos, magic.magicID); pos += 2
        
        // 0x06: Animation triggered
        writeByte(buffer, pos, magic.animationTriggered); pos += 1
        
        // 0x07: Attack type
        writeByte(buffer, pos, magic.attackType); pos += 1
        
        // 0x08: Spell power
        writeByte(buffer, pos, magic.spellPower); pos += 1
        
        // 0x09: Unknown1
        writeByte(buffer, pos, magic.unknown1); pos += 1
        
        // 0x0A: Default target (serialize TargetFlags back to byte)
        targetByte = serializeTargetFlags(magic.targetInfo)
        writeByte(buffer, pos, targetByte); pos += 1
        
        // 0x0B: Attack flags (serialize AttackFlags back to byte)
        flagByte = serializeAttackFlags(magic.attackFlags)
        writeByte(buffer, pos, flagByte); pos += 1
        
        // 0x0C: Draw resist
        writeByte(buffer, pos, magic.drawResist); pos += 1
        
        // 0x0D: Hit count
        writeByte(buffer, pos, magic.hitCount); pos += 1
        
        // 0x0E: Element
        writeByte(buffer, pos, magic.element); pos += 1
        
        // 0x0F: Unknown2
        writeByte(buffer, pos, magic.unknown2); pos += 1
        
        // 0x10-0x15: Status effects (serialize back to DWORD + WORD)
        statusData = serializeStatusEffects(magic.statusEffects)
        writeDword(buffer, pos, statusData.dword); pos += 4  // 0x10-0x13
        writeWord(buffer, pos, statusData.word); pos += 2    // 0x14-0x15
        
        // 0x16: Status attack enabler
        writeByte(buffer, pos, magic.statusAttackEnabler); pos += 1
        
        // 0x17-0x1F: Junction stats (9 bytes)
        writeByte(buffer, pos, magic.junctionStats.hp); pos += 1
        writeByte(buffer, pos, magic.junctionStats.str); pos += 1
        writeByte(buffer, pos, magic.junctionStats.vit); pos += 1
        writeByte(buffer, pos, magic.junctionStats.mag); pos += 1
        writeByte(buffer, pos, magic.junctionStats.spr); pos += 1
        writeByte(buffer, pos, magic.junctionStats.spd); pos += 1
        writeByte(buffer, pos, magic.junctionStats.eva); pos += 1
        writeByte(buffer, pos, magic.junctionStats.hit); pos += 1
        writeByte(buffer, pos, magic.junctionStats.luck); pos += 1
        
        // 0x20-0x23: Junction elemental (4 bytes)
        writeByte(buffer, pos, magic.junctionElemental.attackElement); pos += 1
        writeByte(buffer, pos, magic.junctionElemental.attackValue); pos += 1
        elemDefenseByte = serializeElementalDefense(magic.junctionElemental.defenseElements)
        writeByte(buffer, pos, elemDefenseByte); pos += 1
        writeByte(buffer, pos, magic.junctionElemental.defenseValue); pos += 1
        
        // 0x24-0x25: Junction status values
        writeByte(buffer, pos, magic.junctionStatus.attackValue); pos += 1
        writeByte(buffer, pos, magic.junctionStatus.defenseValue); pos += 1
        
        // 0x26-0x29: Junction status effects (4 bytes)
        attackStatusWord = serializeJunctionStatusAttack(magic.junctionStatus.attackStatuses)
        defenseStatusWord = serializeJunctionStatusDefense(magic.junctionStatus.defenseStatuses)
        writeWord(buffer, pos, attackStatusWord); pos += 2
        writeWord(buffer, pos, defenseStatusWord); pos += 2
        
        // 0x2A-0x39: GF Compatibility (16 bytes)
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.QUEZACOLT)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.SHIVA)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.IFRIT)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.SIREN)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.BROTHERS)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.DIABLOS)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.CARBUNCLE)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.LEVIATHAN)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.PANDEMONA)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.CERBERUS)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.ALEXANDER)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.DOOMTRAIN)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.BAHAMUT)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.CACTUAR)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.TONBERRY)); pos += 1
        writeByte(buffer, pos, magic.gfCompatibility.getCompatibility(GF.EDEN)); pos += 1
        
        // 0x3A-0x3B: Unknown3
        writeWord(buffer, pos, magic.unknown3)
        
        return buffer
    }
    
    // Helper methods for bitfield serialization
    method serializeTargetFlags(targetInfo: TargetFlags): Integer {
        result = 0
        for i = 0 to 7 {
            if targetInfo.getBit(i) {
                result |= (1 << i)
            }
        }
        return result
    }
    
    method serializeAttackFlags(attackFlags: AttackFlags): Integer {
        result = 0
        for i = 0 to 7 {
            if attackFlags.getBit(i) {
                result |= (1 << i)
            }
        }
        return result
    }
    
    method serializeStatusEffects(statusEffects: StatusEffectSet): StatusData {
        statusData = new StatusData()
        statusData.dword = 0
        statusData.word = 0
        
        // Serialize bits 0-31 to DWORD
        for i = 0 to 31 {
            if statusEffects.getBit(i) {
                statusData.dword |= (1 << i)
            }
        }
        
        // Serialize bits 32-47 to WORD
        for i = 0 to 15 {
            if statusEffects.getBit(32 + i) {
                statusData.word |= (1 << i)
            }
        }
        
        return statusData
    }
    
    method serializeJunctionStatusAttack(statusSet: StatusEffectSet): Integer {
        result = 0
        // Map junction status bits to 16-bit word
        statusBitMap = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12]  // Skip bit 10
        for i = 0 to statusBitMap.length - 1 {
            if statusSet.getBit(statusBitMap[i]) {
                result |= (1 << i)
            }
        }
        return result
    }
    
    method serializeJunctionStatusDefense(statusSet: StatusEffectSet): Integer {
        result = 0
        // Map junction defense status bits to 16-bit word
        statusBitMap = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
        for i = 0 to statusBitMap.length - 1 {
            if statusSet.getBit(statusBitMap[i]) {
                result |= (1 << i)
            }
        }
        return result
    }
}

struct StatusData {
    dword: Integer  // 32-bit
    word: Integer   // 16-bit
}
```

## Usage Example

```pseudocode
main() {
    // Read kernel.bin
    kernelData = readBinaryFile("kernel.bin")
    
    // Parse all magic data
    parser = new KernelBinaryParser()
    serializer = new MagicDataBinarySerializer()
    magicList = new List<MagicData>()
    
    magicOffset = findMagicSectionOffset(kernelData)
    MAGIC_STRUCT_SIZE = 0x3C  // 60 bytes exactly
    
    for i = 0 to MAGIC_COUNT - 1 {
        magic = parser.parseMagicData(kernelData, magicOffset + i * MAGIC_STRUCT_SIZE)
        magicList.add(magic)
    }
    
    // Modern object-oriented analysis
    for each magic in magicList {
        print("Magic ID: " + magic.magicID)
        print("Spell Power: " + magic.spellPower)
        print("Active statuses: " + magic.statusEffects.getActiveStatuses())
        print("Active targets: " + magic.targetInfo.getActiveTargets())
        print("Active attack flags: " + magic.attackFlags.getActiveFlags())
        print("Junction attack statuses: " + magic.junctionStatus.attackStatuses.getActiveStatuses())
        
        // Modify data using modern API
        magic.statusEffects.sleep = true
        magic.targetInfo.enemy = true
        magic.spellPower = 255
    }
    
    // Serialize back to exact binary format
    modifiedKernelData = new ByteArray(kernelData.length)
    copyBytes(kernelData, modifiedKernelData)  // Copy original data
    
    // Write modified magic data back to kernel
    for i = 0 to magicList.length - 1 {
        binaryMagicData = serializer.serializeToBinary(magicList[i])
        writeBytes(modifiedKernelData, magicOffset + i * MAGIC_STRUCT_SIZE, binaryMagicData)
    }
    
    // Save modified kernel.bin
    writeBinaryFile("kernel_modified.bin", modifiedKernelData)
    
    // Verify round-trip accuracy
    verifyParser = new KernelBinaryParser()
    verifiedMagic = verifyParser.parseMagicData(modifiedKernelData, magicOffset)
    print("Round-trip verification: " + (verifiedMagic.spellPower == 255))
}
```