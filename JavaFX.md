# GUI Implementation Strategy for FF8 Magic Editor

## GUI Placement in Hexagonal Architecture

The GUI components are **Primary Adapters** (driving adapters) that call into your application use cases. They belong in the Infrastructure layer despite being the entry point for user interactions.

### Physical vs Logical Architecture

**Critical Understanding**: The GUI spans both compiled source code and runtime resources due to technology constraints.

## Technology Recommendation: JavaFX

**Optimal choice for FF8 Magic Editor because:**
- **Rich Desktop UI**: Native-feeling interface with complex data editing capabilities
- **FXML Separation**: Clean separation between layout definition and business logic
- **CSS Styling**: Professional appearance with modern design capabilities
- **Data Binding**: Efficient synchronization between UI and application state
- **Architecture Compatibility**: Excellent fit with hexagonal architecture principles

## Physical File Organization

### Source Code Structure (Compiled)
```
src/main/java/infrastructure/adapters/primary/ui/
├── controllers/                    # UI event handling and coordination
│   ├── MainController              # Main window and application lifecycle
│   ├── MagicListController         # Magic spell list management
│   ├── GeneralTabController        # General properties editing
│   ├── JunctionTabController       # Junction system settings
│   └── GFCompatibilityController   # Guardian Force compatibility
├── models/                         # UI state management
│   ├── MagicListModel              # Observable magic list state
│   ├── GeneralTabModel             # General tab presentation state
│   ├── JunctionTabModel            # Junction tab presentation state
│   └── GFCompatibilityModel        # GF compatibility presentation state
└── components/                     # Custom UI components
    ├── NumericSpinner              # Enhanced number input control
    ├── StatusCheckboxGrid          # Status effect selection grid
    └── GFCompatibilityGrid         # GF compatibility matrix display
```

### Resources Structure (Runtime Assets)
```
src/main/resources/
├── fxml/                           # UI layout definitions
│   ├── MainWindow.fxml             # Main application window layout
│   ├── MagicListView.fxml          # Magic list panel layout
│   ├── GeneralTab.fxml             # General properties tab layout
│   ├── JunctionTab.fxml            # Junction settings tab layout
│   └── GFCompatibilityTab.fxml     # GF compatibility tab layout
├── css/                            # Visual styling
│   ├── application.css             # Main application theme
│   ├── tabs.css                    # Tab-specific styling
│   └── components.css              # Custom component styling
├── images/                         # Visual assets
│   ├── icons/                      # Application and UI icons
│   │   ├── app-icon.png           # Main application icon
│   │   ├── magic-types/           # Icons for different magic types
│   │   └── elements/              # Elemental magic icons
│   └── backgrounds/               # Background images if needed
└── properties/                     # Configuration
    └── ui.properties              # UI-specific configuration
```

## Controller-View Relationships

### Logical Connection Pattern
Each functional area has a **Controller-View pair**:

**Controller** (Java class in `infrastructure/adapters/primary/ui/controllers/`):
- Handles user interactions and events
- Coordinates with application use cases
- Manages UI state through models
- No direct UI framework dependencies in business logic

**View** (FXML file in `src/main/resources/fxml/`):
- Defines visual layout and component hierarchy
- References controller through `fx:controller` attribute
- Contains no business logic
- Purely declarative UI definition

### Connection Mechanism
The FXML files establish the connection to controllers:
- FXML `fx:controller` attribute specifies the Java controller class
- JavaFX automatically instantiates and links controller to view
- Controller receives references to FXML-defined UI components through injection

## Component Interaction Patterns

### Main Application Flow
**Startup Sequence**:
1. **Main Application** initializes dependency injection container
2. **Main Controller** loads primary window FXML layout
3. **FXML Loader** instantiates controllers and binds to views
4. **Controllers** register with application use cases
5. **User Interface** becomes responsive to user interactions

### User Interaction Flow
**Typical Edit Operation**:
1. **User** modifies value in UI component (spinner, checkbox, dropdown)
2. **JavaFX** triggers change event on bound component
3. **Controller** receives event through event handler method
4. **Controller** validates input and calls appropriate **Use Case**
5. **Use Case** processes business logic and updates domain entities
6. **Use Case** returns result or throws business exception
7. **Controller** updates **UI Model** with new state
8. **UI Model** triggers property change notifications
9. **JavaFX binding system** automatically updates view components

### Inter-Controller Communication
**Event-Driven Coordination**:
- **Magic Selection**: When user selects magic in list, all tab controllers receive notification
- **Data Modification**: When one tab modifies data, other tabs update to reflect changes
- **File Operations**: Load/save operations notify all controllers to refresh state

## UI Architecture Layers

### View Layer (FXML + CSS)
**Responsibilities**:
- Define visual layout and component arrangement
- Specify component properties and initial states
- Reference controllers for event handling
- Apply styling through CSS for consistent appearance

**Characteristics**:
- Purely declarative markup
- No business logic or application knowledge
- Easily modifiable for UI changes
- Version-controllable as text files

### Controller Layer (Java Classes)
**Responsibilities**:
- Handle UI events and user interactions
- Coordinate with application use cases
- Manage view state and data binding
- Provide error handling and user feedback

**Characteristics**:
- Minimal UI framework dependencies
- Focus on orchestration rather than business logic
- Clear separation between UI concerns and application logic
- Easy to unit test with mocked dependencies

### Model Layer (Observable State)
**Responsibilities**:
- Hold presentation-specific state
- Provide data binding properties for views
- Transform between domain objects and UI representations
- Manage UI-specific concerns like selection and filtering

**Characteristics**:
- Observable properties for automatic UI updates
- No direct domain knowledge
- Focus on presentation concerns
- Support for validation and formatting

## Implementation Strategy

### Phase 1: Foundation
**Core Infrastructure**:
- Establish main window with basic menu structure
- Implement file loading dialog and basic error handling
- Create empty tab structure for future development
- Set up dependency injection for controllers

### Phase 2: Magic List
**Data Display**:
- Implement magic list display with search/filter capability
- Add selection handling and basic magic information display
- Create magic list model with observable properties
- Establish pattern for controller-use case interaction

### Phase 3: General Tab
**Basic Editing**:
- Implement general magic properties editing
- Add validation and error feedback mechanisms
- Create custom components for numeric input
- Establish data binding patterns for complex properties

### Phase 4: Advanced Tabs
**Complex Features**:
- Implement junction system editing interface
- Add GF compatibility matrix editing
- Create specialized components for status effect selection
- Implement advanced validation and business rule enforcement

### Phase 5: Polish and Enhancement
**User Experience**:
- Add comprehensive error handling and user feedback
- Implement undo/redo functionality
- Add keyboard shortcuts and accessibility features
- Optimize performance for large data sets

## Technology Integration Benefits

### With Hexagonal Architecture
- **Clean Separation**: UI adapters remain isolated from business logic
- **Testability**: Controllers can be tested independently of JavaFX runtime
- **Flexibility**: Easy to add alternative UI technologies (web, mobile)
- **Maintainability**: Business logic changes don't impact UI structure

### With Application Use Cases
- **Single Responsibility**: Controllers focus solely on UI coordination
- **Business Logic Protection**: Domain rules enforced at use case level
- **Consistent Validation**: Same validation logic across all UI interactions
- **Error Handling**: Domain exceptions properly translated to user feedback

### Development Workflow
- **Parallel Development**: UI designers can work on FXML while developers implement controllers
- **Rapid Prototyping**: FXML allows quick layout iterations without recompilation
- **Designer Collaboration**: FXML files can be edited in specialized design tools
- **Consistent Styling**: CSS enables global styling changes without code modification

## Key Design Principles

### Separation of Concerns
- **Views** handle visual presentation only
- **Controllers** manage user interaction and coordination
- **Models** provide observable state for data binding
- **Use Cases** contain all business logic and validation

### Dependency Management
- Controllers depend on application interfaces, not implementations
- Views have no knowledge of business logic or domain concepts
- Models transform between domain objects and UI representations
- Clear boundaries prevent technology concerns from leaking into business logic

### User Experience Focus
- Immediate feedback for user actions
- Consistent validation and error messaging
- Intuitive navigation and data organization
- Professional appearance appropriate for a specialized tool