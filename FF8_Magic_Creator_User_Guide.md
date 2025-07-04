# FF8 Magic Creator - User Guide

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [User Interface Guide](#user-interface-guide)
4. [Creating and Editing Magic](#creating-and-editing-magic)
5. [Export Functionality](#export-functionality)
6. [File Formats and Outputs](#file-formats-and-outputs)
7. [Advanced Features](#advanced-features)
8. [Troubleshooting](#troubleshooting)

## Overview

FF8 Magic Creator is a powerful Java application designed for modding Final Fantasy VIII by allowing you to load, edit, and create new magic spells. The application works with the game's kernel.bin file and provides a modern, user-friendly interface for manipulating magic data while maintaining perfect compatibility with the original game format.

### Key Features
- **Load and edit existing magic** from kernel.bin files
- **Create entirely new magic spells** with custom properties
- **Multi-language support** for spell names and descriptions  
- **Real-time validation** of magic data according to FF8 rules
- **Export functionality** to generate game-compatible files
- **Modern UI** with tabbed interface for different magic properties
- **Automatic synchronization** across all UI components

### What You Can Modify
- Spell names and descriptions (with multi-language support)
- Attack power, element, and damage type
- Target selection (single, group, enemy, ally, etc.)
- Status effects (both beneficial and harmful)
- Junction bonuses (stat increases when equipped)
- Elemental attack/defense properties
- Guardian Force compatibility
- Draw resistance and hit count
- Attack behavior flags

## Getting Started

### System Requirements
- Java 21 or higher
- JavaFX runtime
- Windows 10/11 (primary support)
- At least 512MB RAM
- 50MB free disk space

### Installation
1. Download the latest release
2. Extract to your preferred location
3. Run `gradlew.bat` to build the application
4. Launch using `java -jar build/libs/FF8_magic_creator.jar`

### First Launch
1. The application opens with an empty state
2. Use **File → Open kernel.bin...** (Ctrl+O) to load an existing kernel file
3. Alternatively, use **File → New Magic...** (Ctrl+N) to start creating new spells immediately

## User Interface Guide

### Main Window Layout

The application uses a split-pane layout with:

**Left Panel: Magic List**
- Shows all available magic spells
- Newly created spells appear with green highlighting
- Click any spell to select and edit it, edition only possible for none base basic
- Click right on spell allow to copy a magic to create a new magic
- Displays spell names, power, and element at a glance

**Right Panel: Tabbed Editor**
- **General Tab**: Basic properties (name, power, element, target selection)
- **Junction Tab**: Stat bonuses when junctioned to characters
- **GF Compatibility Tab**: Guardian Force compatibility settings
- **Raw View Tab**: Advanced binary data view for expert users

**Menu Bar**
- **File Menu**: Open, save, export, and create operations
- **Help Menu**: Application information

**Status Bar**
- Shows current operation status
- Progress bar for long operations
- File loading status

### Navigation Tips
- Use **Ctrl+O** to quickly open kernel files
- Use **Ctrl+N** to create new magic
- Use **Ctrl+E** to export newly created magic
- All changes are automatically saved to the in-memory model
- Select different magic from the list to edit multiple spells

## Creating and Editing Magic

### Loading Existing Magic
1. **File → Open kernel.bin...** (Ctrl+O)
2. Navigate to your FF8 installation directory
3. Select `kernel.bin` (typically in the game's main directory)
4. Wait for parsing to complete
5. Magic list populates with all 41 original FF8 spells

### Creating New Magic
1. **File → New Magic...** (Ctrl+N)
2. Enter a spell name in the dialog
3. Click OK to create the spell
4. New spell appears in the list with green highlighting
5. New spell is automatically selected for editing

### Editing Magic Properties

#### General Tab
**Basic Properties:**
- **Magic ID**: Unique identifier (0-345)
- **Spell Name**: Click to edit with multi-language support
- **Description**: Click to edit detailed spell description
- **Element**: Choose from None, Fire, Ice, Thunder, Earth, Poison, Wind, Water, Holy
- **Attack Type**: Physical, Magic, Curative, or special types
- **Spell Power**: Base damage/healing (0-255)
- **Draw Resist**: Difficulty of drawing from enemies (0-255)
- **Hit Count**: Number of hits per cast (1-255)

**Target Selection (Checkboxes):**
- **Target Dead**: Can target KO'd characters
- **Target Single**: Affects single target
- **Target Enemy**: Can target enemies
- **Target Ally**: Can target party members
- Additional targeting flags for advanced behavior

**Status Effects:**
- Select which status effects the spell inflicts
- Includes all FF8 status effects: Sleep, Haste, Slow, Stop, Regen, etc.
- Mix beneficial and harmful effects as needed
- **Status Attack Strength**: Power of status effect application

**Attack Flags:**
- **Shelled**: Affected by Shell status
- **Reflected**: Can be reflected
- **Revive**: Revives KO'd targets  
- **Break Damage Limit**: Allows damage over 9999
- Additional behavior modifiers

#### Junction Tab
Configure stat bonuses when the magic is junctioned to characters:

**Stat Bonuses:**
- **HP, STR, VIT, MAG, SPR, SPD, LCK**: 0-255 bonus per magic stocked
- Higher values provide greater stat increases

**Elemental Properties:**
- **Elemental Attack**: Add elemental damage to physical attacks
- **Elemental Defense**: Reduce/absorb elemental damage
- Choose percentage values and specific elements

**Status Junction:**
- **Status Attack**: Add status effects to physical attacks  
- **Status Defense**: Resist specific status effects
- Select which status effects to affect

#### GF Compatibility Tab
Set how well the magic works with each Guardian Force:
- **Good Compatibility**: GF gains AP faster
- **Poor Compatibility**: GF gains AP slower
- **Neutral**: Normal AP gain
- Configure for all 16 Guardian Forces

#### Raw View Tab
For advanced users only:
- Shows exact binary representation
- Displays all 60 bytes of magic data
- Useful for debugging or precise modifications
- **Warning**: Editing here requires deep knowledge of FF8's format

### Multi-Language Support
1. Click on spell name or description fields
2. Translation Editor dialog opens
3. Add translations for different languages
4. English is always required (fallback language)
5. Add French, German, Spanish, etc. as needed
6. Translations are used in export files

## Export Functionality

### When to Export
- Only export after creating new magic spells
- Original magic from kernel.bin doesn't need exporting
- Export generates game-compatible files for modding

### Export Process
1. Create one or more new magic spells
2. **File → Export Newly Created Magic...** (Ctrl+E)
3. Choose target directory for output files
4. Enter base filename (e.g., "custom_magic")
5. Click Export to generate files

### Export Validation
The application automatically validates:
- All new magic has required properties
- Spell names and descriptions are present
- Values are within valid ranges
- Required languages have translations
- Binary format compatibility

### What Gets Exported
**Binary File**: `{basename}.bin`
- Contains magic logic data (power, elements, targeting, etc.)
- Compatible with FF8's kernel format
- References text data via offsets

**Language Resource Files**: `{basename}_{lang}.bin`
- `custom_magic_en.bin`: English text
- `custom_magic_fr.bin`: French text (if translations provided)
- Additional language files as needed
- Contains spell names and descriptions
- Uses FF8's text encoding (Caesar cipher)

**Export Summary**:
- Number of spells exported
- Total file size
- Languages included
- File locations

## File Formats and Outputs

### Input Formats
**kernel.bin**: Original FF8 kernel file
- Contains all game data including magic
- Magic section starts at offset 0x021C
- Each spell is exactly 60 bytes
- Includes text pointers and binary data

**Magic Binary Files**: Standalone magic data
- Can load custom magic files
- Same format as kernel magic section
- Useful for sharing custom magic

### Output Formats
**Binary Magic File** (`.bin`)
- Contains complete magic data structure
- 60 bytes per spell
- Includes text offset references
- Game-compatible format

**Language Resource Files** (`.bin`)
- Text data for spell names/descriptions
- Uses FF8's Caesar cipher encoding
- Null-terminated strings
- Padded for consistent offsets across languages

### File Size Estimates
- Binary file: ~60 bytes per new spell
- Text files: Variable based on description length
- Typical total: 200-500 bytes per spell across all files

## Advanced Features

### Observer Pattern
- All UI components automatically synchronize
- Changes in one tab immediately update others
- Magic list updates in real-time
- No manual refresh needed

### Validation System
**Real-time Validation:**
- Prevents invalid values during input
- Shows immediate feedback for errors
- Maintains data integrity

**Export Validation:**
- Comprehensive checks before export
- Ensures game compatibility
- Reports specific issues found

### Memory Management
- All edits stored in memory until export
- Original files never modified during editing
- Can work on multiple magic files simultaneously
- Automatic cleanup of resources

### User Preferences
- Window size and position saved
- Last opened directory remembered
- Settings persist between sessions
- Automatic backup of preferences

## Troubleshooting

### Common Issues

**"Could not load kernel.bin"**
- Ensure file is valid FF8 kernel file
- Check file permissions
- Verify file is not corrupted
- Try with different kernel.bin from backup

**"Export validation failed"**
- Check that all required fields are filled
- Ensure spell names are not empty
- Verify numeric values are in valid ranges
- Add English translations for all new spells

**"Unable to create output files"**
- Check target directory permissions
- Ensure enough disk space available
- Close other applications using the files
- Try exporting to different location

**Application won't start**
- Verify Java 21+ is installed
- Check JavaFX runtime is available
- Review console output for error messages
- Try rebuilding with `gradlew clean build`

### Performance Tips
- Load smaller kernel files for faster startup
- Create magic in batches before exporting
- Use Raw View tab sparingly (performance impact)
- Close unused tabs if editing many spells

### Getting Help
- Check the logs for detailed error information
- Review the technical documentation for advanced topics
- Validate your magic data before reporting issues
- Include exact error messages when seeking help

### Limitations
- Maximum 255 magic spells total (FF8 engine limit)
- Text descriptions limited by display space
- Some binary fields are unknown/experimental
- Export only works with newly created magic

---

**Note**: This application is designed for Final Fantasy VIII modding purposes. Always backup your original game files before applying any modifications. The creators are not responsible for any damage to game installations or save files. 