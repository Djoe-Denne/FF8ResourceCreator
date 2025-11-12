# FF8 Magic Creator - Modern Kernel.bin Editor

A modern Java 21 application for editing Final Fantasy VIII magic data using hexagonal architecture and proper binary parsing.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Gradle](https://img.shields.io/badge/Gradle-8.14.2-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🚀 Features

- **Modern Architecture**: Built with hexagonal architecture for maintainability
- **Proper Binary Parsing**: Exact 48-bit status effect handling with round-trip accuracy
- **Real-time UI**: JavaFX interface with live updates and validation
- **Junction System**: Complete support for junction stats editing
- **GF Compatibility**: Edit Guardian Force compatibility values
- **Status Effects**: Comprehensive 48-bit status effect management
- **Binary Preservation**: Maintains exact binary format for compatibility
- **CLI Support**: Command-line interface for batch processing
- **Cross-Platform**: Works on Windows, Linux, and macOS

## 📋 Requirements

- **Java 21** or higher
- **JavaFX 21** (included via Gradle)
- **Gradle 8.14.2** (wrapper included)

## 🎯 Quick Start

### Option 1: Run from Source (Recommended for Development)
```bash
./gradlew run
```

### Option 2: Use Pre-built JAR
```bash
# Build the JAR
./gradlew build

# Run the JAR
java -jar build/libs/FF8_magic_creator-1.0.0.jar
```

### Option 3: Use Native Executable (New! 🎉)
```bash
# Create Windows executable
./gradlew createWindowsExecutable

# Run the executable
build/jpackage/FF8MagicCreator/FF8MagicCreator.exe

# Or use the convenient batch script
build/jpackage/FF8MagicCreator/run_ff8_magic_creator.bat
```

## 🛠️ Building Native Executables

### Windows
```bash
# Creates self-contained application (no Java required on target machine)
./gradlew createWindowsExecutable

# Creates MSI installer (requires WiX tools)
./gradlew createWindowsInstaller
```

### Linux
```bash
# Creates .deb package
./gradlew createLinuxExecutable
```

### macOS
```bash
# Creates .dmg package
./gradlew createMacExecutable
```

## 💻 Usage

### GUI Mode (Default)
```bash
# Launch GUI
java -jar FF8_magic_creator-1.0.0.jar
# or
java -jar FF8_magic_creator-1.0.0.jar --gui
```

### CLI Mode
```bash
# Process a kernel.bin file
java -jar FF8_magic_creator-1.0.0.jar kernel.bin
# or
java -jar FF8_magic_creator-1.0.0.jar --file kernel.bin
```

### Help
```bash
java -jar FF8_magic_creator-1.0.0.jar --help
```

## 🏗️ Project Structure

```
FF8_magic_creator/
├── src/main/java/com/ff8/
│   ├── application/          # Application Layer (Use Cases)
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── mappers/         # DTO Mappers
│   │   ├── ports/           # Interface Definitions
│   │   └── services/        # Application Services
│   ├── domain/              # Domain Layer (Business Logic)
│   │   ├── entities/        # Core Business Entities
│   │   ├── events/          # Domain Events
│   │   ├── exceptions/      # Domain Exceptions
│   │   └── services/        # Domain Services
│   ├── infrastructure/      # Infrastructure Layer
│   │   ├── adapters/        # External Adapters
│   │   │   ├── primary/     # UI Controllers
│   │   │   └── secondary/   # File I/O, Persistence
│   │   └── config/          # Configuration
│   └── Main.java           # Application Entry Point
├── src/main/resources/
│   ├── css/                # Stylesheets
│   ├── fxml/               # JavaFX Layouts
│   └── images/             # Application Icons
└── docs/                   # Documentation
```

## 🔧 Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

- **Domain Layer**: Pure business logic with no external dependencies
- **Application Layer**: Use cases and application services
- **Infrastructure Layer**: External concerns (UI, File I/O, etc.)

### Key Design Patterns
- **Hexagonal Architecture**: Clean separation of concerns
- **Command Pattern**: UI commands for undo/redo functionality
- **Observer Pattern**: Event-driven updates
- **Repository Pattern**: Data access abstraction
- **Factory Pattern**: Object creation
- **Strategy Pattern**: Pluggable algorithms

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "MagicEditorServiceTest"
```

## 📊 Binary Format Details

### Magic Data Structure (60 bytes)
- **Attack Type**: 1 byte
- **Element**: 1 byte  
- **Status Attack**: 6 bytes (48-bit)
- **Status Defense**: 6 bytes (48-bit)
- **Spell Power**: 1 byte
- **Hit Count**: 1 byte
- **Target Flags**: 1 byte
- **GF Compatibility**: 16 bytes
- **Junction Stats**: 9 bytes
- **Other Fields**: 18 bytes

### Status Effects (48-bit)
Proper handling of all 48 FF8 status effects with exact binary preservation.

## 📚 Documentation

- **[User Guide](FF8_Magic_Creator_User_Guide.md)**: Complete usage instructions
- **[Faithful User Documentation](docs/FF8_User_Documentation.md)**: Combined editor and injector workflow guide
- **[Contributing Guide](CONTRIBUTING.md)**: Development standards and guidelines
- **[Architecture](archi.md)**: Technical architecture details
- **[JavaFX Setup](JavaFX.md)**: JavaFX configuration and setup
- **[Editor Documentation](FF8_Magic_Editor_Documentation.md)**: Editor-specific features

## 🎮 Final Fantasy VIII Integration

### Supported Files
- **kernel.bin**: Main magic data file

## 🚨 Important Notes

- **Test changes thoroughly** in-game before distributing
- **Binary preservation** ensures round-trip accuracy
- **Status effects** use exact 48-bit representation

## 🤝 Contributing

We welcome contributions from the community! Please read our **[Contributing Guide](CONTRIBUTING.md)** for detailed information about:

- 🚀 **Modern Java 21** features and best practices
- 🔧 **Lombok** usage guidelines
- 🏗️ **Architecture** principles and layer boundaries
- 🧪 **Testing** standards and frameworks
- 📚 **Documentation** requirements

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow the [Contributing Guide](CONTRIBUTING.md) for development standards
4. Write tests and update documentation
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Final Fantasy VIII by Square Enix
- Java and JavaFX communities
- Hexagonal Architecture pattern by Alistair Cockburn
- All contributors and testers

---

**Made with ❤️ for the FF8 modding community** 