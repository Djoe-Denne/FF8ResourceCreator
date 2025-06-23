package com.ff8;

import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.infrastructure.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the FF8 Magic Creator application.
 * Demonstrates modern Java 21 features and hexagonal architecture.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting FF8 Magic Creator - Modern Kernel.bin Editor");
            logger.info("Java Version: {}", System.getProperty("java.version"));
            
            // Parse command line arguments using Java 21 pattern matching
            var appMode = parseArguments(args);
            
            switch (appMode) {
                case AppMode.CLI cli -> {
                    logger.info("Starting in CLI mode with file: " + cli.filePath());
                    runCliMode(cli.filePath());
                }
                case AppMode.GUI gui -> {
                    logger.info("Starting in GUI mode");
                    runGuiMode();
                }
                case AppMode.HELP help -> {
                    showHelp();
                }
            }
            
        } catch (Exception e) {
            logger.error("Application failed to start", e);
            System.exit(1);
        }
    }

    /**
     * Parse command line arguments using Java 21 pattern matching
     */
    private static AppMode parseArguments(String[] args) {
        return switch (args.length) {
            case 0 -> new AppMode.GUI();
            case 1 -> switch (args[0]) {
                case "--help", "-h" -> new AppMode.HELP();
                case "--gui", "-g" -> new AppMode.GUI();
                default -> new AppMode.CLI(args[0]);
            };
            case 2 -> switch (args[0]) {
                case "--file", "-f" -> new AppMode.CLI(args[1]);
                default -> throw new IllegalArgumentException("Unknown arguments: " + String.join(" ", args));
            };
            default -> throw new IllegalArgumentException("Too many arguments");
        };
    }

    /**
     * Run in CLI mode for batch processing or testing
     */
    private static void runCliMode(String filePath) {
        logger.info("CLI Mode - Processing file: " + filePath);
        
        try {
            // Initialize application configuration
            ApplicationConfig config = ApplicationConfig.getInstance();
            config.validateConfiguration();
            
            // Get use cases from configuration
            KernelFileUseCase kernelFileUseCase = config.getKernelFileUseCase();
            MagicEditorUseCase magicEditorUseCase = config.getMagicEditorUseCase();
            
            System.out.println("FF8 Magic Creator - CLI Mode");
            System.out.println("File: " + filePath);
            System.out.println("Configuration: " + config.getConfigurationInfo());
            
            // Load kernel.bin file
            logger.info("Loading kernel file...");
            kernelFileUseCase.loadKernelFile(filePath);
            System.out.println("✓ Kernel file loaded successfully");
            
            // Show statistics
            var allMagic = magicEditorUseCase.getAllMagic();
            System.out.println("✓ Found " + allMagic.size() + " magic spells");
            
            // Display first few magic entries
            System.out.println("\nFirst 5 magic spells:");
            allMagic.stream()
                .limit(5)
                .forEach(magic -> System.out.printf("  %d: %s (Power: %d, Element: %s)%n", 
                    magic.magicID(), magic.spellName(), magic.spellPower(), magic.element()));
            
            System.out.println("\nCLI processing complete!");
            
        } catch (Exception e) {
            logger.error("CLI processing failed", e);
            throw new RuntimeException("CLI processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Run in GUI mode using JavaFX
     */
    private static void runGuiMode() {
        logger.info("GUI Mode - Starting JavaFX application");
        
        try {
            // Launch the JavaFX application
            com.ff8.infrastructure.adapters.primary.ui.FF8MagicEditorApp.launchApp(new String[0]);
            
        } catch (Exception e) {
            logger.error("GUI startup failed", e);
            throw new RuntimeException("GUI startup failed: " + e.getMessage(), e);
        }
    }

    /**
     * Show help information
     */
    private static void showHelp() {
        System.out.println("""
            FF8 Magic Creator - Modern Kernel.bin Editor
            
            A modern Java 21 application for editing Final Fantasy VIII magic data
            using hexagonal architecture and proper binary parsing.
            
            Usage:
              java -jar ff8-magic-creator.jar [OPTIONS] [FILE]
            
            Options:
              --help, -h        Show this help message
              --gui, -g         Start in GUI mode (default)
              --file, -f FILE   Process specific kernel.bin file in CLI mode
              FILE              Process kernel.bin file in CLI mode
            
            Examples:
              java -jar ff8-magic-creator.jar
              java -jar ff8-magic-creator.jar --gui
              java -jar ff8-magic-creator.jar kernel.bin
              java -jar ff8-magic-creator.jar --file kernel.bin
            
            Features:
              ✓ Modern Java 21 implementation
              ✓ Proper 48-bit status effect handling
              ✓ Binary preservation for round-trip accuracy
              ✓ Hexagonal architecture for maintainability
              ✓ Comprehensive validation
              ✓ Real-time UI updates
              ✓ Junction system support
              ✓ GF compatibility editing
            
            Architecture:
              - Domain Layer: Core business logic and entities
              - Application Layer: Use cases and services
              - Infrastructure Layer: File I/O and binary parsing
              - UI Layer: JavaFX interface with MVC pattern
            
            Binary Format:
              - Magic struct size: 60 bytes (0x3C)
              - Status effects: 48 bits (6 bytes)
              - Junction stats: 9 bytes
              - GF compatibility: 16 bytes
              - Exact binary preservation maintained
            """);
    }

    /**
     * Sealed interface for application modes using Java 21 sealed types
     */
    public sealed interface AppMode 
            permits AppMode.CLI, AppMode.GUI, AppMode.HELP {
        
        record CLI(String filePath) implements AppMode {}
        record GUI() implements AppMode {}
        record HELP() implements AppMode {}
    }

    /**
     * Application metadata
     */
    public static final class AppInfo {
        public static final String VERSION = "1.0.0";
        public static final String NAME = "FF8 Magic Creator";
        public static final String DESCRIPTION = "Modern Kernel.bin Editor";
        public static final String JAVA_VERSION_REQUIRED = "21";
        
        // Architecture constants
        public static final int MAGIC_STRUCT_SIZE = 0x3C; // 60 bytes
        public static final int STATUS_EFFECT_BITS = 48;  // 6 bytes
        public static final int EXPECTED_MAGIC_COUNT = 256;
        
        private AppInfo() {
            // Utility class
        }
        
        public static void printBanner() {
            System.out.printf("""
                ╔════════════════════════════════════════╗
                ║        %s v%s           ║
                ║           %s            ║
                ║                                        ║
                ║  Java %s • Hexagonal Architecture     ║
                ║  Binary Preservation • Modern Design  ║
                ╚════════════════════════════════════════╝
                %n""", NAME, VERSION, DESCRIPTION, JAVA_VERSION_REQUIRED);
        }
    }

    // Static initialization
    static {
        // Set up logging and system properties
        System.setProperty("ff8.magic.creator.version", AppInfo.VERSION);
        
        // Show banner in CLI
        if (System.console() != null) {
            AppInfo.printBanner();
        }
        
        // Log startup information
        logger.info("FF8 Magic Creator v" + AppInfo.VERSION + " initializing");
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
    }
} 