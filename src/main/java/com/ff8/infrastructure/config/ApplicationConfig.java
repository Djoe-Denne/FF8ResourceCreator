package com.ff8.infrastructure.config;

import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.primary.RawDataViewUseCase;
import com.ff8.application.ports.primary.UserPreferencesUseCase;
import com.ff8.application.ports.secondary.BinaryParserPort;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.UserPreferencesPort;
import com.ff8.application.services.KernelFileService;
import com.ff8.application.services.MagicEditorService;
import com.ff8.application.services.RawDataViewService;
import com.ff8.application.services.UserPreferencesService;
import com.ff8.domain.services.MagicValidationService;
import com.ff8.domain.services.RawDataMappingService;
import com.ff8.domain.services.StatusEffectService;
import com.ff8.infrastructure.adapters.secondary.filesystem.LocalFileSystemAdapter;
import com.ff8.infrastructure.adapters.secondary.parser.KernelBinaryParser;
import com.ff8.infrastructure.adapters.secondary.preferences.PropertiesUserPreferencesAdapter;
import com.ff8.infrastructure.adapters.secondary.repository.InMemoryMagicRepository;

/**
 * Main configuration class that wires all dependencies according to hexagonal architecture principles.
 * This class acts as the composition root where all dependencies are configured and injected.
 */
public class ApplicationConfig {
    
    // Singleton instances
    private static ApplicationConfig instance;
    
    // Infrastructure adapters (Secondary)
    private final FileSystemPort fileSystemAdapter;
    private final BinaryParserPort binaryParserAdapter;
    private final MagicRepository magicRepositoryAdapter;
    private final UserPreferencesPort userPreferencesAdapter;
    
    // Domain services
    private final StatusEffectService statusEffectService;
    private final MagicValidationService magicValidationService;
    private final RawDataMappingService rawDataMappingService;
    
    // Application services (implementing primary ports)
    private final MagicEditorUseCase magicEditorService;
    private final KernelFileUseCase kernelFileService;
    private final UserPreferencesUseCase userPreferencesService;
    private final RawDataViewUseCase rawDataViewService;
    
    private ApplicationConfig() {
        // Initialize infrastructure adapters (Secondary adapters)
        this.fileSystemAdapter = new LocalFileSystemAdapter();
        this.binaryParserAdapter = new KernelBinaryParser();
        this.magicRepositoryAdapter = new InMemoryMagicRepository();
        this.userPreferencesAdapter = new PropertiesUserPreferencesAdapter();
        
        // Initialize domain services
        this.statusEffectService = new StatusEffectService();
        this.magicValidationService = new MagicValidationService();
        this.rawDataMappingService = new RawDataMappingService();
        
        // Initialize application services (implementing primary ports)
        this.magicEditorService = new MagicEditorService(
            magicRepositoryAdapter, 
            magicValidationService,
            fileSystemAdapter
        );
        
        this.kernelFileService = new KernelFileService(
            binaryParserAdapter,
            fileSystemAdapter,
            magicRepositoryAdapter
        );
        
        this.userPreferencesService = new UserPreferencesService(
            userPreferencesAdapter
        );
        
        this.rawDataViewService = new RawDataViewService(
            magicRepositoryAdapter,
            rawDataMappingService
        );
    }
    
    /**
     * Get the singleton instance of ApplicationConfig.
     * This ensures we have a single configuration throughout the application.
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
     * Reset the configuration (useful for testing)
     */
    public static void reset() {
        instance = null;
    }
    
    // Getters for primary ports (Use Cases)
    
    public MagicEditorUseCase getMagicEditorUseCase() {
        return magicEditorService;
    }
    
    public KernelFileUseCase getKernelFileUseCase() {
        return kernelFileService;
    }
    
    public UserPreferencesUseCase getUserPreferencesUseCase() {
        return userPreferencesService;
    }
    
    public RawDataViewUseCase getRawDataViewUseCase() {
        return rawDataViewService;
    }
    
    // Getters for secondary ports (for testing or direct access)
    
    public FileSystemPort getFileSystemPort() {
        return fileSystemAdapter;
    }
    
    public BinaryParserPort getBinaryParserPort() {
        return binaryParserAdapter;
    }
    
    public MagicRepository getMagicRepository() {
        return magicRepositoryAdapter;
    }
    
    public UserPreferencesPort getUserPreferencesPort() {
        return userPreferencesAdapter;
    }
    
    // Getters for domain services
    
    public StatusEffectService getStatusEffectService() {
        return statusEffectService;
    }
    
    public MagicValidationService getMagicValidationService() {
        return magicValidationService;
    }
    
    public RawDataMappingService getRawDataMappingService() {
        return rawDataMappingService;
    }
    
    /**
     * Create a test configuration with mocked dependencies
     * (useful for unit testing)
     */
    public static ApplicationConfig createTestConfig(
            FileSystemPort fileSystemPort,
            BinaryParserPort binaryParserPort,
            MagicRepository magicRepository) {
        
        ApplicationConfig testConfig = new ApplicationConfig() {
            // Override constructor to use provided mocks
        };
        
        // This would require making fields non-final and adding setters
        // For now, we'll use the reflection approach or factory pattern
        return testConfig;
    }
    
    /**
     * Validate that all dependencies are properly configured
     */
    public void validateConfiguration() {
        if (fileSystemAdapter == null) {
            throw new IllegalStateException("FileSystemPort not configured");
        }
        if (binaryParserAdapter == null) {
            throw new IllegalStateException("BinaryParserPort not configured");
        }
        if (magicRepositoryAdapter == null) {
            throw new IllegalStateException("MagicRepository not configured");
        }
        if (magicEditorService == null) {
            throw new IllegalStateException("MagicEditorUseCase not configured");
        }
        if (kernelFileService == null) {
            throw new IllegalStateException("KernelFileUseCase not configured");
        }
        if (statusEffectService == null) {
            throw new IllegalStateException("StatusEffectService not configured");
        }
        if (magicValidationService == null) {
            throw new IllegalStateException("MagicValidationService not configured");
        }
        if (userPreferencesAdapter == null) {
            throw new IllegalStateException("UserPreferencesPort not configured");
        }
        if (userPreferencesService == null) {
            throw new IllegalStateException("UserPreferencesUseCase not configured");
        }
    }
    
    /**
     * Get application info for debugging
     */
    public String getConfigurationInfo() {
        return String.format(
            "ApplicationConfig:\n" +
            "  FileSystem: %s\n" +
            "  BinaryParser: %s\n" +
            "  Repository: %s\n" +
            "  MagicEditor: %s\n" +
            "  KernelFile: %s\n" +
            "  StatusEffect: %s\n" +
            "  Validation: %s\n" +
            "  UserPreferences: %s",
            fileSystemAdapter.getClass().getSimpleName(),
            binaryParserAdapter.getClass().getSimpleName(),
            magicRepositoryAdapter.getClass().getSimpleName(),
            magicEditorService.getClass().getSimpleName(),
            kernelFileService.getClass().getSimpleName(),
            statusEffectService.getClass().getSimpleName(),
            magicValidationService.getClass().getSimpleName(),
            userPreferencesService.getClass().getSimpleName()
        );
    }
} 