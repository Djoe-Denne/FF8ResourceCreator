# FF8 Magic Creator & FFScriptLoader — User Documentation

This guide explains how to create, validate, and inject new magic for *Final Fantasy VIII* using the **FF8 Magic Creator** editor together with the **FFScriptLoader** injection framework. It consolidates information from existing manuals and codebases into a single, GitHub-friendly reference.

---

## Table of Contents
1. [Product Overview](#product-overview)
2. [System Requirements](#system-requirements)
3. [Installation & Setup](#installation--setup)
4. [Project Structure at a Glance](#project-structure-at-a-glance)
5. [User Interface Walkthrough](#user-interface-walkthrough)
6. [Creating and Editing Magic](#creating-and-editing-magic)
7. [Validation & Export Formats](#validation--export-formats)
8. [Command-Line Usage](#command-line-usage)
9. [Using FFScriptLoader for In-Game Injection](#using-ffscriptloader-for-in-game-injection)
10. [Full Example: From Spell Concept to In-Game Test](#full-example-from-spell-concept-to-in-game-test)
11. [Troubleshooting Checklist](#troubleshooting-checklist)
12. [Reference Appendix](#reference-appendix)

---

## Product Overview

| Component | Purpose | Key Capabilities |
|-----------|---------|------------------|
| **FF8 Magic Creator** | JavaFX desktop editor for `kernel.bin` magic entries | Hexagonal architecture, real-time validation, multilingual text editing, export of magic and localization payloads |
| **FFScriptLoader** | 32-bit DLL injection framework | Injector + host DLL, task-driven TOML configuration, MinHook-based memory redirection, plugin ecosystem (memory expansion, patching, loaders) |

- FF8 Magic Creator focuses on authoring high-quality magic data while preserving the exact binary layout the game expects.
- FFScriptLoader delivers those changes into the live game process without repacking the original `kernel.bin`. It expands memory, patches instructions, and loads your exported binaries at runtime.

---

## System Requirements

### FF8 Magic Creator
- Windows 10/11 (primary support), macOS, or Linux (JavaFX)
- Java 21+ with JavaFX runtime (bundled via Gradle wrapper)
- 512 MB RAM, 50 MB disk space
- Access to FF8 `kernel.bin` for reference (optional)

### FFScriptLoader
- Windows 10/11 with 32-bit compatibility
- Visual C++ Redistributable (if using pre-built binaries)
- Final Fantasy VIII PC (`FF8_EN.exe`)
- Administrator rights recommended for injector

---

## Installation & Setup

### FF8 Magic Creator
```bash
# Clone and build
git clone https://github.com/Djoe-Denne/FF8_magic_creator.git
cd FF8_magic_creator
./gradlew build

# Launch the GUI
./gradlew run
# or run the assembled JAR
java -jar build/libs/FF8_magic_creator-1.0.0.jar
```

To create platform-specific packages:
- `./gradlew createWindowsExecutable`
- `./gradlew createLinuxExecutable`
- `./gradlew createMacExecutable`

### FFScriptLoader
```bash
git clone https://github.com/Djoe-Denne/FFScriptLoader.git
cd FFScriptLoader
cmake -B build -A Win32 -DBUILD_TESTING=OFF
cmake --build build --config Release
```

Outputs of interest:
- `build/bin/app_injector.exe`
- `build/bin/app_hook.dll`
- `build/bin/tasks/memory_plugin.dll`

---

## Project Structure at a Glance

```text
FF8_magic_creator/
├─ src/main/java/com/ff8/
│  ├─ domain/                # MagicData entities, validation services
│  ├─ application/           # Use cases, DTOs, ports
│  └─ infrastructure/        # JavaFX controllers, adapters, file I/O
└─ src/main/resources/
   ├─ fxml/                  # JavaFX layouts
   └─ images/                # UI assets

FFScriptLoader/
├─ injector/                 # Command-line injector
├─ app_hook/                 # Host DLL (MinHook, plugin runtime)
├─ memory_plugin/            # Built-in memory & patch tasks
└─ config_ff8/               # Example FF8 task configurations
```

Architecture highlights:
- Magic Creator follows Ports & Adapters with MVC UI, enabling strict domain isolation.
- FFScriptLoader provides a modular plugin system (`IPlugin`, `IPluginHost`) and task scheduler driven by TOML configuration dependencies.

---

## User Interface Walkthrough

### Main Window Layout
- **Magic Browser (left)**: list of base and custom spells; right-click to duplicate.
- **Tabbed Editor (right)**:
  - `General`: Attack type, element, hit count, targeting flags.
  - `Junction`: Stat boosts, elemental affinities, status defense.
  - `GF Compatibility`: Values per Guardian Force.
  - `Raw View`: 60-byte binary inspection.
- **Menu Bar**: File (open, new, export), Tools, Help.
- **Status Bar**: Operation feedback, progress bars during parsing/export.

### Keyboard Shortcuts
- `Ctrl+O` open kernel file, `Ctrl+N` new magic, `Ctrl+E` export new spells.
- In-text fields support multiline editing and language switching.

### Multi-language Editor
- Double-click name/description to open translation dialog.
- English mandatory; add French, German, Spanish, etc. as needed.
- Validates per-language byte budget for FF8 text encoding.

---

## Creating and Editing Magic

### 1. Load Reference Data
1. Launch FF8 Magic Creator.
2. `File → Open kernel.bin…`.
3. Browse to FF8 installation and select `kernel.bin`.
4. After parsing, base spells populate the Magic Browser.

### 2. Create a New Spell
1. `File → New Magic…`.
2. Provide an internal name (used in exports).
3. New entry appears highlighted in green.

### 3. Configure General Properties
- Set attack type, element, spell power, draw resistance, hit count.
- Toggle targeting flags (`Target Enemy`, `Target Ally`, `Revive`, etc.).
- Configure 48-bit status attack and defense bitfields.

### 4. Tune Junction Effects
- Assign stat junction values (HP/STR/VIT/MAG/SPR/SPD/LCK).
- Set elemental attack/defense multipliers.
- Choose status attack/defense affinities.

### 5. Adjust GF Compatibility
- Modify compatibility values (0–255) for each Guardian Force.
- Positive bias speeds AP gain when magic is junctioned.

### 6. Review Raw View (Optional)
- Inspect byte-level layout for advanced validation.
- Cross-check offsets before exporting.

---

## Validation & Export Formats

### Real-Time Validation
- UI restricts invalid ranges.
- Missing required translations flagged during editing.
- Binary constraints (e.g., 60-byte structure, status bitfields) enforced.

### Export Workflow
1. Finish editing all new spells.
2. `File → Export Newly Created Magic…`.
3. Choose output directory and base filename (`custom_magic`).
4. Tool emits:
   - `{basename}.bin`: 60-byte-per-spell magic data.
   - `{basename}_{lang}.bin`: Localized text binaries (Caesar cipher encoded).
   - Export summary report.

### File Format Essentials
- Each spell: 60 bytes containing attack data, flags, GF compatibility, junction stats.
- Text binaries map to FF8 string tables; offsets align with original kernel layout.
- Exports only include newly created spells to prevent accidental base-game edits.

---

## Command-Line Usage

FF8 Magic Creator includes CLI endpoints for automation or batch workflows.

```bash
# Launch GUI (explicit)
java -jar FF8_magic_creator-1.0.0.jar --gui

# Process a kernel file via CLI
java -jar FF8_magic_creator-1.0.0.jar --file path/to/kernel.bin

# Display available options
java -jar FF8_magic_creator-1.0.0.jar --help
```

Combine with FFScriptLoader exports by scripting CLI runs followed by injector tasks.

---

## Using FFScriptLoader for In-Game Injection

FFScriptLoader bridges exported binaries into the running FF8 process without replacing `kernel.bin`.

### Deployment Layout
```text
FF8_Mod_Tools/
├─ app_injector.exe
├─ app_hook.dll
├─ plugins/
│  └─ memory_plugin.dll
└─ config/
   ├─ tasks.toml
   └─ tasks/
      ├─ copy_magic_memory_config.toml
      ├─ magic_patch.toml
      ├─ copy_magic_text_memory_config.toml
      ├─ magic_text_patch.toml
      ├─ load_new_magic.toml
      └─ load_new_magic_text.toml
```

### How It Works
1. **Injector Phase**: `app_injector.exe FF8_EN.exe app_hook.dll --config-dir ... --plugin--dir ...` locates the process, allocates memory, and loads `app_hook.dll`.
2. **Host Initialization**: Host DLL initializes logging, reads `tasks.toml`, loads plugins, and schedules tasks.
3. **Plugin Execution**:
   - `CopyMemoryTask` duplicates original magic/text memory into expanded heap regions.
   - `PatchMemoryTask` rewrites instructions to point to the new regions.
   - `LoadInMemoryTask` writes exported `.bin` files into the redirected memory.
4. **Runtime Hooks**: MinHook trampolines ensure the game reads from your custom blobs.

### Configuration Tips
- Use `followBy` chains in `tasks.toml` to order memory copy → load → patch.
- Validate addresses against your FF8 build; ASLR can influence offsets.
- Place exported binaries from Magic Creator where `load_new_magic.toml` expects them (e.g., `mods/xtender/magic/custom_magic.bin`).

---

## Full Example: From Spell Concept to In-Game Test

### Scenario
Create a new spell “Astra Cascade” that deals multi-hit Holy damage, boosts MAG when junctioned, and add it to FF8 via runtime injection.

#### Part 1 — Author in FF8 Magic Creator
1. **Plan the Spell**: Determine stats, element (Holy), status effects (e.g., Confuse), and junction bonuses (+40 MAG).
2. **Load Base Data**: Open `kernel.bin` for context.
3. **Duplicate Template**: Right-click “Holy” spell → “Duplicate” to copy baseline targeting.
4. **Edit General Tab**:
   - Name & Description in desired languages.
   - Set Attack Type `Magic`, Spell Power `110`, Hit Count `4`.
   - Enable `Target Enemy`, disable `Target Ally`.
   - Enable status effect `Confuse` with strength `32`.
5. **Adjust Junction Tab**:
   - MAG junction `40`, SPD `10`.
   - Elemental attack `Holy` 100%.
6. **GF Compatibility**: Favor `Alexander`, `Carbuncle` by +10 points.
7. **Export**: `custom_magic.bin`, `custom_magic_en.bin`, `custom_magic_fr.bin`.

#### Part 2 — Prepare FFScriptLoader
1. Copy exports into `mods/xtender/magic/` (or configured path).
2. Update `load_new_magic.toml`:
   ```toml
   [load.NEW_MAGIC]
   binary = "./mods/xtender/magic/custom_magic.bin"
   readFromContext = "ff8.magic.k_magic_data"
   ```
3. Update `load_new_magic_text.toml` with language paths.
4. Ensure `tasks.toml` enables the sequence:
   ```toml
   [tasks.copy_magic_memory_config]
   followBy = ["load_new_magic", "magic_patch"]
   enabled = true
   ```
5. Launch FF8 (`FF8_EN.exe`).

#### Part 3 — Inject and Verify
1. Run injector as Administrator:
   ```bat
   app_injector.exe FF8_EN.exe app_hook.dll --config-dir configs --plugin-dir plugins
   ```
2. Monitor `app_hook.log` for successful task execution (look for “LoadInMemoryTask completed”).
3. In FF8, open the Magic menu:
   - Verify “Astra Cascade” appears with localized name.
   - Check junction screen for MAG boost.
4. Test in battle to confirm multi-hit Holy damage and status effect.

#### Clean-Up & Iteration
- Adjust stats or descriptions in Magic Creator, re-export, and re-run injector (no need to restart if tasks re-run).
- Version-control TOML configs and exported binaries for reproducibility.

---

## Troubleshooting Checklist

- **Injector fails**: Confirm 32-bit build, admin rights, matching architecture, `app_hook.dll` present.
- **Magic not visible**: Check `load_new_magic.toml` paths, ensure exported filenames match, review `app_hook.log`.
- **Text garbled**: Verify translations exported for current language, ensure Caesar cipher encoding via Magic Creator.
- **Game crashes**: Review patched addresses, confirm ASLR disabled or addresses adjusted, validate memory sizes.
- **Export validation errors**: Ensure all required fields filled, names/descriptions not empty, numeric ranges valid.

---

## Reference Appendix

- **Binary Layout**: 60-byte structure includes attack data, status bitfields, GF compatibility matrix, junction stats.
- **Status Bitfields**: 48-bit masks for attack/defense; refer to `StatusEffect` enum in domain layer.
- **Guardian Forces**: Compatibility tweaks influence AP gain; all 16 GF slots available.
- **Plugin Interfaces**:
  - `IPlugin`, `IPluginHost`, and task registration APIs.
  - Provided memory plugin demonstrates copy → patch → load pattern.

Additional documentation:
- [`FF8_Magic_Creator_User_Guide.md`](../FF8_Magic_Creator_User_Guide.md)
- [`docs/EXAMPLE_FF8.md` in FFScriptLoader](../../FFScriptLoader/docs/EXAMPLE_FF8.md)
- [`docs/PLUGIN_DEVELOPMENT.md` in FFScriptLoader](../../FFScriptLoader/docs/PLUGIN_DEVELOPMENT.md)

---

Happy modding! Back up original FF8 files before applying any changes, and iterate safely using version control for both exported binaries and injector configurations.


