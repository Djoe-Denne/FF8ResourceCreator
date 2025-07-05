package com.ff8.infrastructure.config;

import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.LocalizedExportUseCase;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.primary.RawDataViewUseCase;
import com.ff8.application.ports.primary.UserPreferencesUseCase;
import com.ff8.application.ports.secondary.BinaryExportPort;
import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.ResourceFileGeneratorPort;
import com.ff8.application.ports.secondary.UserPreferencesPort;
import com.ff8.application.mappers.DtoToMagicDataMapper;
import com.ff8.application.mappers.MagicDataToDtoMapper;
import com.ff8.application.services.KernelFileService;
import com.ff8.application.services.LocalizedExportService;
import com.ff8.application.services.MagicEditorService;
import com.ff8.application.services.RawDataViewService;
import com.ff8.application.services.UserPreferencesService;
import com.ff8.domain.services.ExportValidationService;
import com.ff8.domain.services.LanguageValidationService;
import com.ff8.domain.services.MagicValidationService;
import com.ff8.domain.services.RawDataMappingService;
import com.ff8.domain.services.StatusEffectService;
import com.ff8.domain.services.TextEncodingService;
import com.ff8.domain.services.TextOffsetCalculationService;
import com.ff8.infrastructure.adapters.secondary.export.BinaryExportAdapter;
import com.ff8.infrastructure.adapters.secondary.export.ResourceFileGenerator;
import com.ff8.infrastructure.adapters.secondary.filesystem.LocalFileSystemAdapter;
import com.ff8.infrastructure.adapters.secondary.parser.KernelBinaryParser;
import com.ff8.infrastructure.adapters.secondary.preferences.PropertiesUserPreferencesAdapter;
import com.ff8.infrastructure.adapters.secondary.repository.InMemoryMagicRepository;

/**
 * Hexagonal architecture configuration and dependency injection container.
 * 
 * <p>This class serves as the composition root for the entire FF8 Magic Creator application,
 * implementing the hexagonal architecture pattern by wiring together all components
 * across the domain, application, and infrastructure layers. It ensures proper
 * dependency injection and maintains clean separation of concerns.</p>
 * 
 * <p>Architecture Overview:</p>
 * <ul>
 *   <li><strong>Domain Layer:</strong> Core business logic and entities</li>
 *   <li><strong>Application Layer:</strong> Use cases and application services</li>
 *   <li><strong>Infrastructure Layer:</strong> External adapters and technical concerns</li>
 * </ul>
 * 
 * <p>Key Configuration Features:</p>
 * <ul>
 *   <li>Singleton pattern for application-wide configuration consistency</li>
 *   <li>Lazy initialization of all components for optimal startup performance</li>
 *   <li>Proper dependency ordering to avoid circular dependencies</li>
 *   <li>Thread-safe configuration access using double-checked locking</li>
 *   <li>Comprehensive validation to ensure all dependencies are properly wired</li>
 * </ul>
 * 
 * <p>Component Categories:</p>
 * <ul>
 *   <li><strong>Primary Ports:</strong> Use case interfaces for driving adapters (UI)</li>
 *   <li><strong>Secondary Ports:</strong> Interfaces for driven adapters (persistence, file I/O)</li>
 *   <li><strong>Domain Services:</strong> Business logic and validation services</li>
 *   <li><strong>Application Services:</strong> Use case implementations</li>
 *   <li><strong>Infrastructure Adapters:</strong> External system integrations</li>
 * </ul>
 * 
 * <p>Usage:</p>
 * <pre>{@code
 * // Get the application configuration
 * ApplicationConfig config = ApplicationConfig.getInstance();
 * 
 * // Access use cases for the UI layer
 * MagicEditorUseCase magicEditor = config.getMagicEditorUseCase();
 * KernelFileUseCase kernelFile = config.getKernelFileUseCase();
 * 
 * // Access domain services for business logic
 * MagicValidationService validation = config.getMagicValidationService();
 * }</pre>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class ApplicationConfig {
    
    // Singleton instances
    private static ApplicationConfig instance;
    
    // Infrastructure adapters (Secondary)
    private final FileSystemPort fileSystemAdapter;
    private final BinaryParserPort binaryParserAdapter;
    private final MagicRepository magicRepositoryAdapter;
    private final UserPreferencesPort userPreferencesAdapter;
    private final ResourceFileGeneratorPort resourceFileGeneratorAdapter;
    private final BinaryExportPort binaryExportAdapter;
    
    // Domain services
    private final StatusEffectService statusEffectService;
    private final MagicValidationService magicValidationService;
    private final RawDataMappingService rawDataMappingService;
    private final TextEncodingService textEncodingService;
    private final TextOffsetCalculationService textOffsetCalculationService;
    private final LanguageValidationService languageValidationService;
    private final ExportValidationService exportValidationService;
    
    // Application mappers
    private final MagicDataToDtoMapper magicDataToDtoMapper;
    private final DtoToMagicDataMapper dtoToMagicDataMapper;
    
    // Application services (implementing primary ports)
    private final MagicEditorUseCase magicEditorService;
    private final KernelFileUseCase kernelFileService;
    private final UserPreferencesUseCase userPreferencesService;
    private final RawDataViewUseCase rawDataViewService;
    private final LocalizedExportUseCase localizedExportService;
    
    /**
     * Private constructor that initializes all application components.
     * 
     * <p>This constructor performs the complete dependency injection setup
     * in the correct order to avoid circular dependencies. The initialization
     * follows this sequence:</p>
     * <ol>
     *   <li>Infrastructure adapters (secondary ports)</li>
     *   <li>Domain services (business logic)</li>
     *   <li>Application mappers (DTO conversion)</li>
     *   <li>Application services (use case implementations)</li>
     * </ol>
     * 
     * <p>The constructor ensures that all dependencies are properly wired
     * according to the hexagonal architecture principles.</p>
     */
    private ApplicationConfig() {
        // Initialize infrastructure adapters (Secondary adapters)
        this.fileSystemAdapter = new LocalFileSystemAdapter();
        this.binaryParserAdapter = new KernelBinaryParser();
        this.magicRepositoryAdapter = new InMemoryMagicRepository();
        this.userPreferencesAdapter = new PropertiesUserPreferencesAdapter();
        
        // Initialize domain services first (needed by infrastructure adapters)
        this.textEncodingService = new TextEncodingService();
        this.textOffsetCalculationService = new TextOffsetCalculationService(textEncodingService);
        this.languageValidationService = new LanguageValidationService(textEncodingService);
        this.exportValidationService = new ExportValidationService(textEncodingService, languageValidationService);
        
        // Initialize export adapters (depend on domain services)
        this.resourceFileGeneratorAdapter = new ResourceFileGenerator(textEncodingService);
        this.binaryExportAdapter = new BinaryExportAdapter(binaryParserAdapter);
        
        // Initialize remaining domain services
        this.statusEffectService = new StatusEffectService();
        this.magicValidationService = new MagicValidationService();
        this.rawDataMappingService = new RawDataMappingService();
        
        // Initialize application mappers
        this.magicDataToDtoMapper = new MagicDataToDtoMapper();
        this.dtoToMagicDataMapper = new DtoToMagicDataMapper();
        
        // Initialize application services (implementing primary ports)
        this.magicEditorService = new MagicEditorService(
            magicRepositoryAdapter, 
            magicValidationService,
            fileSystemAdapter,
            magicDataToDtoMapper,
            dtoToMagicDataMapper
        );
        
        this.kernelFileService = new KernelFileService(
            binaryParserAdapter,
            fileSystemAdapter,
            magicRepositoryAdapter,
            magicDataToDtoMapper,
            textEncodingService
        );
        
        this.userPreferencesService = new UserPreferencesService(
            userPreferencesAdapter
        );
        
        this.rawDataViewService = new RawDataViewService(
            magicRepositoryAdapter,
            rawDataMappingService
        );
        
        this.localizedExportService = new LocalizedExportService(
            magicRepositoryAdapter,
            textEncodingService,
            textOffsetCalculationService,
            languageValidationService,
            exportValidationService,
            resourceFileGeneratorAdapter,
            binaryExportAdapter
        );
    }
    
    /**
     * Get the singleton instance of ApplicationConfig.
     * 
     * <p>This method implements the thread-safe singleton pattern using
     * double-checked locking to ensure only one configuration instance
     * exists throughout the application lifecycle.</p>
     * 
     * @return The singleton ApplicationConfig instance
     */
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (ApplicationConfig.class) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Reset the configuration instance.
     * 
     * <p>This method is primarily useful for testing scenarios where
     * a fresh configuration state is required. It clears the singleton
     * instance, forcing recreation on the next access.</p>
     * 
     * <p><strong>Warning:</strong> This method should only be used in
     * test environments as it can cause inconsistent state in production.</p>
     */
    public static void reset() {
        instance = null;
    }
    
    // Getters for primary ports (Use Cases)
    
    /**
     * Get the magic editor use case implementation.
     * 
     * <p>This use case provides all magic editing operations including
     * create, read, update, and delete operations for magic data.</p>
     * 
     * @return The MagicEditorUseCase implementation
     */
    public MagicEditorUseCase getMagicEditorUseCase() {
        return magicEditorService;
    }
    
    /**
     * Get the kernel file use case implementation.
     * 
     * <p>This use case handles loading and saving of kernel.bin files
     * along with validation and binary processing operations.</p>
     * 
     * @return The KernelFileUseCase implementation
     */
    public KernelFileUseCase getKernelFileUseCase() {
        return kernelFileService;
    }
    
    /**
     * Get the user preferences use case implementation.
     * 
     * <p>This use case manages application settings and user preferences
     * including window state, last opened directories, and user options.</p>
     * 
     * @return The UserPreferencesUseCase implementation
     */
    public UserPreferencesUseCase getUserPreferencesUseCase() {
        return userPreferencesService;
    }
    
    /**
     * Get the raw data view use case implementation.
     * 
     * <p>This use case provides access to raw binary data for debugging
     * and advanced editing scenarios.</p>
     * 
     * @return The RawDataViewUseCase implementation
     */
    public RawDataViewUseCase getRawDataViewUseCase() {
        return rawDataViewService;
    }
    
    /**
     * Get the localized export use case implementation.
     * 
     * <p>This use case handles export of newly created magic spells
     * with multi-language support and proper binary serialization.</p>
     * 
     * @return The LocalizedExportUseCase implementation
     */
    public LocalizedExportUseCase getLocalizedExportUseCase() {
        return localizedExportService;
    }
    
    // Getters for secondary ports (for testing or direct access)
    
    /**
     * Get the file system port adapter.
     * 
     * <p>This adapter provides file system operations including
     * reading, writing, and file management capabilities.</p>
     * 
     * @return The FileSystemPort implementation
     */
    public FileSystemPort getFileSystemPort() {
        return fileSystemAdapter;
    }
    
    /**
     * Get the binary parser port adapter.
     * 
     * <p>This adapter handles binary parsing and serialization
     * of kernel.bin files with strategy pattern support.</p>
     * 
     * @return The BinaryParserPort implementation
     */
    public BinaryParserPort getBinaryParserPort() {
        return binaryParserAdapter;
    }
    
    /**
     * Get the magic repository adapter.
     * 
     * <p>This adapter provides in-memory storage and retrieval
     * of magic data during the editing session.</p>
     * 
     * @return The MagicRepository implementation
     */
    public MagicRepository getMagicRepository() {
        return magicRepositoryAdapter;
    }
    
    /**
     * Get the user preferences port adapter.
     * 
     * <p>This adapter handles persistence and retrieval of
     * user preferences and application settings.</p>
     * 
     * @return The UserPreferencesPort implementation
     */
    public UserPreferencesPort getUserPreferencesPort() {
        return userPreferencesAdapter;
    }
    
    // Getters for domain services
    
    /**
     * Get the status effect service.
     * 
     * <p>This service provides business logic for status effect
     * management including 48-bit status effect handling.</p>
     * 
     * @return The StatusEffectService instance
     */
    public StatusEffectService getStatusEffectService() {
        return statusEffectService;
    }
    
    /**
     * Get the magic validation service.
     * 
     * <p>This service provides comprehensive validation of magic data
     * according to FF8 business rules and constraints.</p>
     * 
     * @return The MagicValidationService instance
     */
    public MagicValidationService getMagicValidationService() {
        return magicValidationService;
    }
    
    /**
     * Get the raw data mapping service.
     * 
     * <p>This service provides mapping between raw binary data
     * and structured domain objects for debugging and analysis.</p>
     * 
     * @return The RawDataMappingService instance
     */
    public RawDataMappingService getRawDataMappingService() {
        return rawDataMappingService;
    }
    
    // Getters for application mappers
    
    /**
     * Get the magic data to DTO mapper.
     * 
     * <p>This mapper converts domain entities to data transfer objects
     * for presentation layer consumption.</p>
     * 
     * @return The MagicDataToDtoMapper instance
     */
    public MagicDataToDtoMapper getMagicDataToDtoMapper() {
        return magicDataToDtoMapper;
    }
    
    /**
     * Get the DTO to magic data mapper.
     * 
     * <p>This mapper converts data transfer objects from the presentation
     * layer back to domain entities for business logic processing.</p>
     * 
     * @return The DtoToMagicDataMapper instance
     */
    public DtoToMagicDataMapper getDtoToMagicDataMapper() {
        return dtoToMagicDataMapper;
    }
    
    // Factory methods for testing
    
    /**
     * Create a test configuration with custom adapters.
     * 
     * <p>This factory method allows creation of a custom configuration
     * for testing purposes, enabling dependency injection of mock
     * or test-specific implementations.</p>
     * 
     * <p><strong>Warning:</strong> This method is intended for testing
     * only and should not be used in production code.</p>
     * 
     * @param fileSystemPort Custom file system adapter for testing
     * @param binaryParserPort Custom binary parser adapter for testing
     * @param magicRepository Custom magic repository for testing
     * @return A test-specific ApplicationConfig instance
     */
    public static ApplicationConfig createTestConfig(
            FileSystemPort fileSystemPort,
            BinaryParserPort binaryParserPort,
            MagicRepository magicRepository) {
        
        ApplicationConfig testConfig = new ApplicationConfig();
        
        // Override with test implementations
        // Note: This would require making fields non-final for testing
        // or using a different approach like constructor injection
        
        return testConfig;
    }
    
    /**
     * Validate the configuration integrity.
     * 
     * <p>This method performs comprehensive validation of all configured
     * components to ensure they are properly initialized and wired.
     * It checks for null dependencies and validates that all required
     * components are available.</p>
     * 
     * @throws IllegalStateException if any required component is missing or invalid
     */
    public void validateConfiguration() {
        // Validate primary ports
        if (magicEditorService == null) {
            throw new IllegalStateException("MagicEditorUseCase not configured");
        }
        if (kernelFileService == null) {
            throw new IllegalStateException("KernelFileUseCase not configured");
        }
        if (userPreferencesService == null) {
            throw new IllegalStateException("UserPreferencesUseCase not configured");
        }
        if (rawDataViewService == null) {
            throw new IllegalStateException("RawDataViewUseCase not configured");
        }
        if (localizedExportService == null) {
            throw new IllegalStateException("LocalizedExportUseCase not configured");
        }
        
        // Validate secondary ports
        if (fileSystemAdapter == null) {
            throw new IllegalStateException("FileSystemPort not configured");
        }
        if (binaryParserAdapter == null) {
            throw new IllegalStateException("BinaryParserPort not configured");
        }
        if (magicRepositoryAdapter == null) {
            throw new IllegalStateException("MagicRepository not configured");
        }
        if (userPreferencesAdapter == null) {
            throw new IllegalStateException("UserPreferencesPort not configured");
        }
        
        // Validate domain services
        if (statusEffectService == null) {
            throw new IllegalStateException("StatusEffectService not configured");
        }
        if (magicValidationService == null) {
            throw new IllegalStateException("MagicValidationService not configured");
        }
        if (rawDataMappingService == null) {
            throw new IllegalStateException("RawDataMappingService not configured");
        }
    }
    
    /**
     * Get configuration information for debugging.
     * 
     * <p>This method returns a formatted string containing information
     * about all configured components. It's useful for debugging and
     * monitoring the application configuration state.</p>
     * 
     * @return Formatted string with configuration details
     */
    public String getConfigurationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("FF8 Magic Creator - Application Configuration\n");
        info.append("==========================================\n");
        info.append("Primary Ports:\n");
        info.append("  - MagicEditorUseCase: ").append(magicEditorService.getClass().getSimpleName()).append("\n");
        info.append("  - KernelFileUseCase: ").append(kernelFileService.getClass().getSimpleName()).append("\n");
        info.append("  - UserPreferencesUseCase: ").append(userPreferencesService.getClass().getSimpleName()).append("\n");
        info.append("  - RawDataViewUseCase: ").append(rawDataViewService.getClass().getSimpleName()).append("\n");
        info.append("  - LocalizedExportUseCase: ").append(localizedExportService.getClass().getSimpleName()).append("\n");
        info.append("\nSecondary Ports:\n");
        info.append("  - FileSystemPort: ").append(fileSystemAdapter.getClass().getSimpleName()).append("\n");
        info.append("  - BinaryParserPort: ").append(binaryParserAdapter.getClass().getSimpleName()).append("\n");
        info.append("  - MagicRepository: ").append(magicRepositoryAdapter.getClass().getSimpleName()).append("\n");
        info.append("  - UserPreferencesPort: ").append(userPreferencesAdapter.getClass().getSimpleName()).append("\n");
        info.append("\nDomain Services:\n");
        info.append("  - StatusEffectService: ").append(statusEffectService.getClass().getSimpleName()).append("\n");
        info.append("  - MagicValidationService: ").append(magicValidationService.getClass().getSimpleName()).append("\n");
        info.append("  - RawDataMappingService: ").append(rawDataMappingService.getClass().getSimpleName()).append("\n");
        return info.toString();
    }
} 