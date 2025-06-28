# FF8 Magic Editor - System Documentation

## Overview

The FF8 Magic Editor is a Java application that allows users to load, edit, and save magic data from Final Fantasy VIII kernel.bin files. The software employs clean hexagonal architecture with an observer pattern to ensure real-time synchronization across multiple UI components.

## Architecture Foundation

The application follows **Hexagonal Architecture** (Ports & Adapters) with three distinct layers:

**Domain Layer**: Contains core business logic including MagicData entities, status effects management, junction systems, and validation rules. This layer has no external dependencies and represents pure FF8 game mechanics.

**Application Layer**: Orchestrates business operations through use cases and services. Defines ports (interfaces) for external dependencies and manages data transformation between domain entities and presentation DTOs.

**Infrastructure Layer**: Implements adapters for external systems including binary file parsing, UI controllers, and data persistence. All external framework dependencies are isolated to this layer.

## Binary Data Processing

### File Loading Process

When a kernel.bin file is loaded, the system locates the magic section at a specific byte offset (0x021C). Each magic spell occupies exactly 60 bytes containing binary data for spell properties, junction bonuses, and status effects.

The **KernelBinaryParser** converts raw binary data into structured **MagicData** domain entities. This process handles complex bit manipulation for status effects (48-bit flags), target selection flags, attack behavior flags, and Guardian Force compatibility matrices.

String data (spell names and descriptions) are extracted from separate sections of the kernel file using pointer offsets stored within each magic structure.

### Data Structure Transformation

**MagicData** entities represent the complete domain model with all game mechanics and binary preservation fields. These are converted to **MagicDisplayDTO** objects for UI presentation, which contain only user-relevant properties in a more convenient format.

The transformation preserves critical binary data needed for exact file reconstruction while providing a clean interface for UI interactions.

## User Interaction Flow

### Primary Edit Workflow

User interface actions trigger a consistent flow through the application layers:

1. **User Input**: UI components (spinners, dropdowns, checkboxes) capture user modifications
2. **Controller Validation**: Controllers validate input ranges and format before proceeding
3. **Use Case Delegation**: Controllers call appropriate methods on **MagicEditorService**
4. **Domain Update**: Service validates business rules and updates **MagicData** in the repository
5. **Observer Notification**: Service emits change events to notify all registered observers
6. **UI Synchronization**: All UI components automatically refresh to reflect changes

### New Magic Creation Workflow

The system supports creating entirely new magic spells through the File menu:

1. **User Selection**: User selects "New Magic..." from the File menu (Ctrl+N)
2. **Input Dialog**: System presents a dialog requesting the spell name
3. **Validation**: Controller validates the spell name is not empty
4. **Creation Process**: **MagicEditorService** creates new **MagicData** with default properties and sets the **isNewlyCreated** flag
5. **Repository Storage**: New magic is assigned the next available index and stored
6. **UI Notification**: Observer pattern automatically updates the magic list with green highlighting for newly created spells
7. **Visual Distinction**: Newly created magic appears with a distinctive green background color in the magic list

### Multi-Component Synchronization

The application maintains multiple UI tabs (General, Junction, GF Compatibility) that must stay synchronized when editing the same magic spell. The observer pattern ensures automatic coordination without manual synchronization code.

When users select different magic from the list, all tabs automatically update to display the new selection's properties. Similarly, changes made in one tab immediately reflect in other relevant tabs.

## Observer Pattern Implementation

### Event-Driven Updates

The system implements a publish-subscribe pattern where:

**Publishers (Subjects)**: Services like **MagicEditorService** and **KernelFileService** extend **AbstractSubject** and emit change events when operations complete.

**Subscribers (Observers)**: UI models implement **Observer** interface and register to receive notifications about relevant changes.

**Event Types**: Different events handle various scenarios including magic property updates, new magic creation, duplication operations, and file loading.

### Automatic UI Refresh

When magic data changes, the **MagicEditorService** emits a **MagicDataChangeEvent** containing the updated data and change type. The **MagicListModel** receives this event and automatically updates the UI components bound to its properties.

This pattern eliminates manual UI synchronization code and ensures all interface elements stay current without explicit refresh calls.

## Data Persistence Strategy

### Repository Pattern

The **InMemoryMagicRepository** provides temporary storage for loaded magic data with change tracking capabilities. All modifications are stored in memory until the user explicitly saves the kernel file.

Repository operations include finding magic by index, getting available IDs for new spells, and maintaining the complete collection for serialization back to binary format.

### Binary Serialization

When saving, the system reconstructs the complete kernel.bin file by:

1. Reading the original file to preserve non-magic sections
2. Serializing all **MagicData** entities back to 60-byte binary structures
3. Writing serialized data to correct offsets in the kernel file
4. Preserving exact binary format for game compatibility

## Validation Framework

### Multi-Layer Validation

Validation occurs at multiple levels to ensure data integrity:

**Input Validation**: UI controllers validate ranges and formats before processing
**Business Validation**: **MagicValidationService** enforces FF8 game rules and constraints
**Binary Validation**: Parser validates that serialized data maintains correct structure

### Error Handling

The system provides comprehensive error handling with user-friendly messages for validation failures, file access errors, and parsing problems. Failed operations maintain application stability and provide clear recovery paths.

## Key Technical Features

### Thread Safety

Observer notifications use thread-safe collections and ensure UI updates occur on the JavaFX application thread through **Platform.runLater()** calls.

### Memory Management

The application uses weak references where appropriate and implements proper cleanup for observer subscriptions to prevent memory leaks during extended use.

### Extensibility

The hexagonal architecture allows easy addition of new UI components, file formats, or business rules without modifying existing code. New observers can subscribe to existing events, and new adapters can implement existing ports.

## Performance Characteristics

The system optimizes for responsiveness through:

- In-memory data storage for fast access during editing sessions
- Efficient binary parsing with minimal memory allocation
- Debounced UI updates to prevent excessive refresh cycles
- Lazy loading of complex data structures

The observer pattern ensures minimal overhead by only updating components that actually observe changed data, rather than refreshing entire UI sections unnecessarily. 