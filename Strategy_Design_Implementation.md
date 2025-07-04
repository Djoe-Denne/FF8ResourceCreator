# Strategy Design Pattern Implementation for KernelBinaryParser

## Overview

The `KernelBinaryParser` has been refactored to use the Strategy design pattern, making it extensible to handle multiple sections of the FF8 kernel.bin file. This design separates the parsing logic for each section into dedicated strategy classes.

## Architecture

### Core Components

1. **`SectionType` enum** - Defines available sections in kernel.bin
2. **`SectionParserStrategy<T>` interface** - Contract for section-specific parsers
3. **`MagicSectionParser`** - Concrete strategy for magic data
4. **`KernelBinaryParser`** - Context class that manages strategies

### Class Structure

```
KernelBinaryParser (Context)
├── Map<SectionType, SectionParserStrategy<?>> strategies
├── Set<SectionType> activeSections
├── registerStrategy(SectionParserStrategy<?>)
├── enableSection(SectionType)
├── disableSection(SectionType)
└── getStrategy(SectionType)

SectionParserStrategy<T> (Strategy Interface)
├── parseItem(byte[], int, int): T
├── serializeItem(T): byte[]
├── parseAllItems(byte[]): List<T>
├── serializeAllItems(List<T>, byte[]): byte[]
├── findSectionOffset(byte[]): int
├── getItemStructSize(): int
├── getExpectedItemCount(): int
├── validateSectionStructure(byte[]): ValidationResult
├── calculateSectionChecksum(byte[]): String
└── extractItemNames(byte[]): List<String>

MagicSectionParser (Concrete Strategy)
└── implements SectionParserStrategy<MagicData>
```

## Usage Examples

### Basic Usage (Backward Compatible)

```java
// Default constructor - only magic section enabled
KernelBinaryParser parser = new KernelBinaryParser();

// Parse magic data as before
List<MagicData> magicList = parser.parseAllMagicData(kernelData);
```

### Multi-Section Usage

```java
// Enable specific sections
List<SectionType> sections = Arrays.asList(
    SectionType.MAGIC,
    SectionType.WEAPONS,  // When implemented
    SectionType.ITEMS     // When implemented
);

KernelBinaryParser parser = new KernelBinaryParser(sections);

// Parse magic data
List<MagicData> magicList = parser.parseAllMagicData(kernelData);

// Future: Parse weapon data
// SectionParserStrategy<WeaponData> weaponStrategy = parser.getStrategy(SectionType.WEAPONS);
// List<WeaponData> weapons = weaponStrategy.parseAllItems(kernelData);
```

### Dynamic Section Management

```java
KernelBinaryParser parser = new KernelBinaryParser();

// Enable additional sections at runtime
parser.enableSection(SectionType.ITEMS);
parser.disableSection(SectionType.MAGIC);

// Check what sections are enabled
Set<SectionType> enabled = parser.getEnabledSections();
```

## Adding New Section Strategies

To add support for a new section (e.g., Weapons), follow these steps:

### 1. Update SectionType Enum

```java
public enum SectionType {
    MAGIC("Magic", 0x021C, 0x3C, 56, "Contains spell/magic data"),
    WEAPONS("Weapons", 0x1234, 0x28, 33, "Contains weapon data"), // Add new section
    // ... other sections
}
```

### 2. Create Data Entity

```java
@Data
@Builder
public class WeaponData {
    private int index;
    private int weaponId;
    private String name;
    private int attackPower;
    // ... other weapon properties
}
```

### 3. Implement Strategy

```java
public class WeaponSectionParser implements SectionParserStrategy<WeaponData> {
    
    @Override
    public SectionType getSectionType() {
        return SectionType.WEAPONS;
    }
    
    @Override
    public WeaponData parseItem(byte[] binaryData, int offset, int index) throws BinaryParseException {
        // Implement weapon-specific parsing logic
        // ...
        return WeaponData.builder()
            .index(index)
            .weaponId(weaponId)
            .name(extractedName)
            .attackPower(attackPower)
            .build();
    }
    
    @Override
    public byte[] serializeItem(WeaponData weapon) throws BinaryParseException {
        // Implement weapon-specific serialization logic
        // ...
    }
    
    // Implement other required methods...
}
```

### 4. Register Strategy

```java
// In KernelBinaryParser constructor
public KernelBinaryParser() {
    registerStrategy(new MagicSectionParser());
    registerStrategy(new WeaponSectionParser()); // Add new strategy
    // ...
}
```

### 5. Add Interface Methods (if needed)

If the new section needs specific interface methods (like magic has `parseMagicData`), add them to `BinaryParserPort`:

```java
public interface BinaryParserPort {
    // Existing magic methods...
    
    // New weapon methods
    WeaponData parseWeaponData(byte[] binaryData, int offset) throws BinaryParseException;
    List<WeaponData> parseAllWeaponData(byte[] kernelData) throws BinaryParseException;
    byte[] serializeWeaponData(WeaponData weapon) throws BinaryParseException;
    byte[] serializeAllWeaponData(List<WeaponData> weapons, byte[] originalKernelData) throws BinaryParseException;
}
```

And implement them in `KernelBinaryParser`:

```java
@Override
public WeaponData parseWeaponData(byte[] binaryData, int offset) throws BinaryParseException {
    SectionParserStrategy<WeaponData> weaponStrategy = getStrategy(SectionType.WEAPONS);
    if (weaponStrategy == null) {
        throw new BinaryParseException("Weapon section parser strategy not available");
    }
    return weaponStrategy.parseItem(binaryData, offset, -1);
}
```

## Benefits

1. **Extensibility**: Easy to add new sections without modifying existing code
2. **Separation of Concerns**: Each section has its own parsing logic
3. **Testability**: Strategies can be tested independently
4. **Flexibility**: Enable/disable sections as needed
5. **Maintainability**: Changes to one section don't affect others

## Backward Compatibility

The refactored `KernelBinaryParser` maintains full backward compatibility with existing code. All existing magic-related methods work exactly as before, but now delegate to the `MagicSectionParser` strategy internally.

## Testing Strategy

Each strategy should have comprehensive unit tests:

```java
@Test
public void testMagicSectionParser() {
    MagicSectionParser parser = new MagicSectionParser();
    assertEquals(SectionType.MAGIC, parser.getSectionType());
    assertEquals(0x3C, parser.getItemStructSize());
    assertEquals(56, parser.getExpectedItemCount());
    
    // Test parsing and serialization...
}
```

Integration tests should verify the `KernelBinaryParser` works with multiple strategies:

```java
@Test
public void testMultiSectionParsing() {
    List<SectionType> sections = Arrays.asList(SectionType.MAGIC, SectionType.WEAPONS);
    KernelBinaryParser parser = new KernelBinaryParser(sections);
    
    assertTrue(parser.isSectionEnabled(SectionType.MAGIC));
    assertTrue(parser.isSectionEnabled(SectionType.WEAPONS));
    assertFalse(parser.isSectionEnabled(SectionType.ITEMS));
}
``` 