# FF8 Kernel.bin Parser - Hexagonal Architecture Proposal

## Architecture Overview

This application follows **Hexagonal Architecture (Ports & Adapters)** with **MVC pattern** to create a maintainable and testable FF8 magic data editor. The architecture ensures clean separation of concerns, testability, and flexibility for future enhancements.

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (MVC)                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │    Views    │  │ Controllers │  │   Models    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────┬───────────────────────────────────────┘
                      │ Primary Adapters
┌─────────────────────┼───────────────────────────────────────┐
│                     │        Application Core               │
│  ┌─────────────────┐│┌─────────────────┐                   │
│  │  Primary Ports  │││  Domain Logic   │                   │
│  │  (Use Cases)    │││  (Entities)     │                   │
│  └─────────────────┘│└─────────────────┘                   │
│                     │                                       │
│  ┌─────────────────┐│                                       │
│  │ Secondary Ports │││                                       │
│  │ (Repositories)  │││                                       │
│  └─────────────────┘│                                       │
└─────────────────────┼───────────────────────────────────────┘
                      │ Secondary Adapters
┌─────────────────────┼───────────────────────────────────────┐
│                     │    Infrastructure                     │
│  ┌─────────────────┐│┌─────────────────┐                   │
│  │  File System    │││  Binary Parser  │                   │
│  │    Adapter      │││     Adapter     │                   │
│  └─────────────────┘│└─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

### Physical File Organization
```
ff8-magic-editor/
├── src/main/
│   ├── java/                           # Compiled source code
│   │   ├── domain/                     # Core Business Logic (Hexagon Center)
│   │   │   ├── entities/               # Core business objects
│   │   │   │   ├── MagicData           # Main magic spell entity
│   │   │   │   ├── StatusEffectSet     # 48-bit status effect management
│   │   │   │   ├── TargetFlags         # Target selection flags
│   │   │   │   ├── AttackFlags         # Attack behavior flags
│   │   │   │   ├── JunctionStats       # Stat junction values
│   │   │   │   ├── JunctionElemental   # Elemental junction data
│   │   │   │   ├── JunctionStatusEffects # Status junction data
│   │   │   │   ├── GFCompatibilitySet  # GF compatibility matrix
│   │   │   │   └── enums/              # Domain enumerations
│   │   │   │       ├── StatusEffect    # All possible status effects
│   │   │   │       ├── Element         # Elemental types
│   │   │   │       ├── GF              # Guardian Force types
│   │   │   │       └── AttackType      # Magic attack categories
│   │   │   ├── services/               # Domain business logic
│   │   │   │   ├── MagicValidationService # Business rule validation
│   │   │   │   └── StatusEffectService # Status effect logic
│   │   │   └── exceptions/             # Domain-specific errors
│   │   │       ├── InvalidMagicDataException
│   │   │       └── BinaryParseException
│   │   │
│   │   ├── application/                # Application Layer (Use Cases)
│   │   │   ├── ports/                  # Interface definitions
│   │   │   │   ├── primary/            # Driving interfaces (inbound)
│   │   │   │   │   ├── MagicEditorUseCase  # Edit magic data operations
│   │   │   │   │   ├── KernelFileUseCase   # File load/save operations
│   │   │   │   │   └── MagicSearchUseCase  # Search and filter operations
│   │   │   │   └── secondary/          # Driven interfaces (outbound)
│   │   │   │       ├── MagicRepository # Data persistence abstraction
│   │   │   │       ├── BinaryParserPort # Binary parsing abstraction
│   │   │   │       └── FileSystemPort  # File system abstraction
│   │   │   ├── services/               # Use case implementations
│   │   │   │   ├── MagicEditorService  # Magic editing orchestration
│   │   │   │   ├── KernelFileService   # File management orchestration
│   │   │   │   └── MagicSearchService  # Search functionality
│   │   │   └── dto/                    # Data transfer objects
│   │   │       ├── MagicDisplayDTO     # UI representation of magic data
│   │   │       ├── JunctionStatsDTO    # Junction statistics for UI
│   │   │       └── GFCompatibilityDTO  # GF compatibility for UI
│   │   │
│   │   └── infrastructure/             # Infrastructure Layer (Adapters)
│   │       ├── adapters/
│   │       │   ├── secondary/          # Driven adapters (outbound)
│   │       │   │   ├── repository/     # Data storage implementations
│   │       │   │   │   └── InMemoryMagicRepository  # In-memory data store
│   │       │   │   ├── parser/         # Binary format handling
│   │       │   │   │   ├── KernelBinaryParser       # Read binary data
│   │       │   │   │   ├── MagicDataBinarySerializer # Write binary data
│   │       │   │   │   └── BinaryReaderWriter       # Low-level binary ops
│   │       │   │   └── filesystem/     # File system operations
│   │       │   │       └── LocalFileSystemAdapter   # Local file handling
│   │       │   └── primary/            # Driving adapters (inbound)
│   │       │       └── ui/             # User interface implementations
│   │       │           ├── controllers/ # UI event handling and coordination
│   │       │           │   ├── MainController           # Main window coordination
│   │       │           │   ├── MagicListController      # Magic list management
│   │       │           │   ├── GeneralTabController     # General properties tab
│   │       │           │   ├── JunctionTabController    # Junction settings tab
│   │       │           │   └── GFCompatibilityTabController # GF compatibility tab
│   │       │           └── models/     # UI state management
│   │       │               ├── MagicListModel          # Magic list UI state
│   │       │               ├── GeneralTabModel         # General tab UI state
│   │       │               ├── JunctionTabModel        # Junction tab UI state
│   │       │               └── GFCompatibilityTabModel # GF tab UI state
│   │       └── config/                 # Application configuration
│   │           ├── ApplicationConfig   # Dependency injection setup
│   │           └── DependencyInjection # Component wiring
│   │
│   └── resources/                      # Runtime resources (not compiled)
│       ├── fxml/                       # UI layout definitions (JavaFX)
│       │   ├── MainWindow.fxml         # Main application window layout
│       │   ├── MagicListView.fxml      # Magic spell list layout
│       │   ├── GeneralTab.fxml         # General properties view layout
│       │   ├── JunctionTab.fxml        # Junction settings view layout
│       │   └── GFCompatibilityTab.fxml # GF compatibility view layout
│       ├── css/                        # Stylesheets
│       │   └── application.css         # Main application styling
│       ├── images/                     # Visual assets
│       │   └── icons/                  # Application icons
│       └── properties/                 # Configuration files
│           └── application.properties  # Application settings
```

### Architectural Note: Physical vs Logical Structure

**Important**: While FXML files are physically located in `src/main/resources/fxml/`, they are **logically** part of the Infrastructure → Primary Adapters → UI layer. This separation exists because:

- **FXML files are resources**, not compiled source code
- **JavaFX loads them at runtime** as resource files
- **Maven/Gradle convention** requires resources in `src/main/resources/`
- **Controller-View binding** connects Java controllers to FXML layouts

The relationship between controllers and views:
- **Controller**: `src/main/java/infrastructure/adapters/primary/ui/controllers/GeneralTabController.java`
- **View**: `src/main/resources/fxml/GeneralTab.fxml`
- **Connection**: FXML files reference controllers via `fx:controller` attribute

## Core Components and Responsibilities

### Domain Layer (Business Core)

**Purpose**: Contains the essential business logic and rules for FF8 magic data manipulation.

**Key Responsibilities**:
- **MagicData Entity**: Central aggregate representing a complete magic spell with all properties
- **Status Effect Management**: Proper handling of 48-bit status effect combinations
- **Junction System**: Modeling the complex junction relationships between magic and character stats
- **GF Compatibility**: Managing the compatibility matrix between spells and Guardian Forces
- **Business Validation**: Ensuring all magic data modifications follow FF8 game rules

**Design Principles**:
- No dependencies on external frameworks or technologies
- Pure business logic with clear, expressive domain language
- Immutable value objects where appropriate
- Rich domain model with behavior, not just data containers

### Application Layer (Use Cases)

**Purpose**: Orchestrates business operations and defines application capabilities.

**Primary Ports** (Inbound Interfaces):
- **MagicEditorUseCase**: Defines all magic editing operations available to users
- **KernelFileUseCase**: Manages kernel.bin file loading, saving, and validation
- **MagicSearchUseCase**: Provides search and filtering capabilities across magic data

**Secondary Ports** (Outbound Interfaces):
- **MagicRepository**: Abstracts data persistence without specifying implementation
- **BinaryParserPort**: Abstracts binary format parsing without coupling to specific formats
- **FileSystemPort**: Abstracts file operations without coupling to specific file systems

**Service Implementations**:
- Coordinate between domain entities and external systems
- Handle transaction boundaries and error recovery
- Implement complex use case workflows
- Manage data transformation between layers

### Infrastructure Layer (Adapters)

**Secondary Adapters** (Driven/Outbound):
- **Binary Parser**: Handles the complex FF8 kernel.bin format with exact byte-level precision
- **File System Adapter**: Manages file I/O operations with error handling and validation
- **Repository Implementation**: Provides in-memory data storage with change tracking

**Primary Adapters** (Driving/Inbound):
- **UI Controllers**: Handle user interactions and coordinate with use cases
- **UI Models**: Manage presentation state and data binding
- **UI Views**: Define visual layout and user interaction patterns

## Key Interactions and Data Flow

### Application Startup Sequence
1. **Dependency Injection Container** initializes all components
2. **Configuration** wires together ports and adapters
3. **Main Controller** launches the user interface
4. **Application** awaits user interaction

### File Loading Workflow
1. **User** selects "Load kernel.bin" from menu
2. **Main Controller** receives UI event
3. **Controller** calls **KernelFileUseCase** with file path
4. **Use Case** calls **FileSystemPort** to read binary data
5. **File System Adapter** reads file and returns byte array
6. **Use Case** calls **BinaryParserPort** to parse data
7. **Binary Parser** creates **MagicData** entities from binary format
8. **Use Case** calls **MagicRepository** to store parsed data
9. **Repository** stores data and notifies of completion
10. **Controllers** refresh UI to display loaded magic data

### Magic Data Editing Workflow
1. **User** modifies spell power in General tab
2. **General Tab Controller** receives change event
3. **Controller** validates input and calls **MagicEditorUseCase**
4. **Use Case** retrieves current magic data from **MagicRepository**
5. **Use Case** calls domain validation through **MagicValidationService**
6. **Domain** validates business rules and constraints
7. **Use Case** updates **MagicData** entity with new values
8. **Use Case** stores updated entity via **MagicRepository**
9. **Repository** persists changes and marks file as modified
10. **Controllers** update UI to reflect changes

### File Saving Workflow
1. **User** selects "Save kernel.bin" from menu
2. **Main Controller** calls **KernelFileUseCase** for save operation
3. **Use Case** retrieves all modified magic data from **MagicRepository**
4. **Use Case** calls **BinaryParserPort** to serialize data
5. **Binary Serializer** converts **MagicData** entities back to exact binary format
6. **Use Case** calls **FileSystemPort** to write binary data
7. **File System Adapter** writes data to file system
8. **Use Case** marks file as saved and notifies completion

### Search and Filter Operations
1. **User** types in search field of magic list
2. **Magic List Controller** receives text change event
3. **Controller** calls **MagicSearchUseCase** with filter criteria
4. **Use Case** queries **MagicRepository** for matching magic data
5. **Repository** returns filtered results
6. **Controller** updates **Magic List Model** with results
7. **UI** refreshes to show filtered magic list

## Architecture Benefits

### Hexagonal Architecture Advantages
- **Testability**: Business logic completely isolated from external concerns
- **Flexibility**: Easy to swap UI frameworks, file formats, or storage mechanisms
- **Maintainability**: Clear boundaries between business logic and technical implementation
- **Domain Focus**: Core game logic remains central and protected from technical changes

### MVC Pattern Advantages
- **Separation of Concerns**: Clear division between presentation, interaction, and state
- **Reusable Components**: Models can be shared across different views
- **Independent Development**: Teams can work on UI, business logic, and data access separately
- **Testing**: Each component can be tested in isolation

### Implementation Strategy
- **Dependency Injection**: Maintain dependency rule through container-managed dependencies
- **Event-Driven Communication**: Loose coupling between UI components through event system
- **Immutable DTOs**: Thread-safe data transfer with clear boundaries
- **Consistent Error Handling**: Domain exceptions propagated appropriately through layers
- **Binary Format Preservation**: Exact round-trip compatibility with original kernel.bin format
