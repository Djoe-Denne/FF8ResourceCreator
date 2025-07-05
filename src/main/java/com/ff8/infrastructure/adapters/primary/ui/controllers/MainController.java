package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.ExportRequestDTO;
import com.ff8.application.dto.ExportResultDTO;
import com.ff8.application.ports.primary.KernelFileUseCase;
import com.ff8.application.ports.primary.LocalizedExportUseCase;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.primary.UserPreferencesUseCase;
import com.ff8.infrastructure.config.ApplicationConfig;
import com.ff8.infrastructure.adapters.primary.ui.models.MagicListModel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * Primary UI controller for the FF8 Magic Editor application.
 * 
 * <p>This controller serves as the main coordinator for the application's user interface,
 * implementing the hexagonal architecture pattern as a Primary Adapter. It handles
 * file operations, menu actions, and coordinates between child controllers and
 * application use cases.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Coordinate main window operations and lifecycle</li>
 *   <li>Handle file loading and saving operations</li>
 *   <li>Manage menu actions and user preferences</li>
 *   <li>Coordinate between child controllers (tabs, magic list)</li>
 *   <li>Implement observer pattern for real-time UI updates</li>
 *   <li>Handle error display and user feedback</li>
 * </ul>
 * 
 * <p>The controller integrates with the application layer through use case interfaces,
 * maintaining proper separation of concerns and ensuring the UI remains decoupled
 * from business logic.</p>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private MenuBar menuBar;
    @FXML private MenuItem openKernelMenuItem;
    @FXML private MenuItem openMagicBinaryMenuItem;
    @FXML private MenuItem newMagicMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    
    @FXML private SplitPane mainSplitPane;
    @FXML private TabPane mainTabPane;
    
    // Included FXML controllers (automatically injected by FXMLLoader)
    @FXML private MagicListController magicListViewController;
    @FXML private GeneralTabController generalTabController;
    @FXML private JunctionTabController junctionTabController;
    @FXML private GFCompatibilityController gfCompatibilityTabController;
    @FXML private RawViewTabController rawViewTabController;
    
    // Injected dependencies
    private final KernelFileUseCase kernelFileUseCase;
    private final MagicEditorUseCase magicEditorUseCase;
    private final UserPreferencesUseCase userPreferencesUseCase;
    private final LocalizedExportUseCase localizedExportUseCase;
    
    // UI Models
    private final MagicListModel magicListModel;
    
    // Current file information
    private File currentFile;
    
    /**
     * Constructor for dependency injection.
     * 
     * <p>Initializes the controller with required application services and sets up
     * the observer pattern for automatic UI updates. The constructor establishes
     * connections between the UI model and application services.</p>
     * 
     * @param config the application configuration containing injected dependencies
     */
    public MainController(ApplicationConfig config) {
        this.kernelFileUseCase = config.getKernelFileUseCase();
        this.magicEditorUseCase = config.getMagicEditorUseCase();
        this.userPreferencesUseCase = config.getUserPreferencesUseCase();
        this.localizedExportUseCase = config.getLocalizedExportUseCase();
        this.magicListModel = new MagicListModel();
        
        // Register MagicListModel as observer of KernelFileService for automatic updates
        if (kernelFileUseCase instanceof com.ff8.application.services.KernelFileService) {
            com.ff8.application.services.KernelFileService kernelFileService = 
                (com.ff8.application.services.KernelFileService) kernelFileUseCase;
            kernelFileService.registerObserver(magicListModel);
            logger.info("Registered MagicListModel as observer of KernelFileService");
        }
        
        // Register MagicListModel as observer of MagicEditorService for automatic updates
        if (magicEditorUseCase instanceof com.ff8.application.services.MagicEditorService) {
            com.ff8.application.services.MagicEditorService magicEditorService = 
                (com.ff8.application.services.MagicEditorService) magicEditorUseCase;
            
            // Create a custom observer that delegates to the MagicListModel's updateMagicData method
            magicEditorService.registerObserver(changeEvent -> {
                try {
                    magicListModel.updateMagicData(changeEvent);
                } catch (Exception e) {
                    magicListModel.onMagicDataChangeError(e, changeEvent);
                }
            });
            logger.info("Registered MagicListModel as observer of MagicEditorService");
        }
        
        logger.info("MainController initialized");
    }
    
    /**
     * Initializes the controller after FXML loading.
     * 
     * <p>This method is called automatically by JavaFX after the FXML file has been
     * loaded and all @FXML annotated fields have been injected. It sets up the
     * UI components, menu actions, and child controllers.</p>
     * 
     * @param location the location used to resolve relative paths for the root object
     * @param resources the resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMenuActions();
        setupStatusBar();
        
        // Initialize with empty state
        updateUIState();
        
        // Set up child controllers (they are injected by FXML loader)
        setupChildControllers();
        
        logger.info("MainController UI initialized");
    }
    
    /**
     * Sets up child controllers after FXML loading.
     * 
     * <p>Establishes connections between child controllers and their required
     * models and parent references. This ensures proper coordination between
     * the main controller and its child components.</p>
     */
    private void setupChildControllers() {
        // Connect models and parent references
        if (magicListViewController != null) {
            magicListViewController.setMagicListModel(magicListModel);
            magicListViewController.setMainController(this);
        }
        
        if (generalTabController != null) {
            generalTabController.setMainController(this);
        }
        
        if (junctionTabController != null) {
            junctionTabController.setMainController(this);
        }
        
        if (gfCompatibilityTabController != null) {
            gfCompatibilityTabController.setMainController(this);
        }
        
        if (rawViewTabController != null) {
            rawViewTabController.setMainController(this);
        }
        
        logger.info("Child controllers configured");
    }
    
    /**
     * Sets up menu actions and keyboard shortcuts.
     * 
     * <p>Configures event handlers for all menu items and establishes their
     * enabled/disabled states based on application state.</p>
     */
    private void setupMenuActions() {
        openKernelMenuItem.setOnAction(e -> openKernelFile());
        openMagicBinaryMenuItem.setOnAction(e -> openMagicBinary());
        newMagicMenuItem.setOnAction(e -> createNewMagic());
        exportMenuItem.setOnAction(e -> exportNewlyCreatedMagic());
        exitMenuItem.setOnAction(e -> exitApplication());
        aboutMenuItem.setOnAction(e -> showAbout());
        
        // New Magic is always enabled
        newMagicMenuItem.setDisable(false);
        
        // Export is enabled if there are newly created magic spells
        exportMenuItem.setDisable(!localizedExportUseCase.hasNewlyCreatedMagic());
    }
    
    /**
     * Sets up the status bar with initial state.
     * 
     * <p>Configures the progress bar visibility and initial status message.</p>
     */
    private void setupStatusBar() {
        progressBar.setVisible(false);
        statusLabel.setText("Ready - Please open a kernel.bin file or load magic binary");
    }
    
    /**
     * Handles the "Open Kernel File" menu action.
     * 
     * <p>Displays a file chooser dialog for selecting kernel.bin files and
     * initiates the file loading process. Uses user preferences to set the
     * initial directory.</p>
     */
    @FXML
    private void openKernelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Kernel.bin File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Kernel Files", "*.bin", "kernel.bin")
        );
        
        // Set initial directory from user preferences
        var preferences = userPreferencesUseCase.getCurrentPreferences();
        if (preferences.getLastOpenDirectory() != null && 
            java.nio.file.Files.exists(preferences.getLastOpenDirectory()) &&
            java.nio.file.Files.isDirectory(preferences.getLastOpenDirectory())) {
            
            fileChooser.setInitialDirectory(preferences.getLastOpenDirectory().toFile());
            logger.debug("Set initial directory to: {}", preferences.getLastOpenDirectory());
        }
        
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // Update last opened directory preference
            userPreferencesUseCase.updateLastOpenDirectory(file.getParentFile().toPath());
            loadKernelFile(file);
        }
    }
    
    /**
     * Handles the "Open Magic Binary" menu action.
     * 
     * <p>Displays a file chooser dialog for selecting magic binary files and
     * initiates the file loading process.</p>
     */
    @FXML
    private void openMagicBinary() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Magic Binary File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Binary Files", "*.bin", "*.dat", "*.*")
        );
        
        // Set initial directory from user preferences
        var preferences = userPreferencesUseCase.getCurrentPreferences();
        if (preferences.getLastOpenDirectory() != null && 
            java.nio.file.Files.exists(preferences.getLastOpenDirectory()) &&
            java.nio.file.Files.isDirectory(preferences.getLastOpenDirectory())) {
            
            fileChooser.setInitialDirectory(preferences.getLastOpenDirectory().toFile());
            logger.debug("Set initial directory to: {}", preferences.getLastOpenDirectory());
        }
        
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // Update last opened directory preference
            userPreferencesUseCase.updateLastOpenDirectory(file.getParentFile().toPath());
            loadMagicBinary(file);
        }
    }
    
    /**
     * Handles the "New Magic" menu action.
     * 
     * <p>Displays a dialog for creating a new magic spell, validates the input,
     * and creates the new spell using the application services.</p>
     */
    @FXML
    private void createNewMagic() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Magic");
        dialog.setHeaderText("Create a new magic spell");
        dialog.setContentText("Enter spell name:");
        
        // Show dialog and wait for user input
        dialog.showAndWait().ifPresent(spellName -> {
            if (spellName.trim().isEmpty()) {
                showError("Spell name cannot be empty", null);
                return;
            }
            
            // Create new magic in a background task
            Task<Void> createTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Creating new magic spell...");
                    magicEditorUseCase.createNewMagic(spellName.trim());
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    updateMessage("New magic spell created successfully");
                    updateUIState();
                }
                
                @Override
                protected void failed() {
                    updateMessage("Failed to create new magic spell");
                    Throwable exception = getException();
                    logger.error("Failed to create new magic spell", exception);
                    showError("Failed to create new magic spell: " + exception.getMessage(), exception);
                }
            };
            
            // Bind progress bar to task
            progressBar.progressProperty().bind(createTask.progressProperty());
            statusLabel.textProperty().bind(createTask.messageProperty());
            progressBar.setVisible(true);
            
            // Run task in background thread
            Thread thread = new Thread(createTask);
            thread.setDaemon(true);
            thread.start();
        });
    }
    
    @FXML
    private void exportNewlyCreatedMagic() {
        logger.debug("Starting export of newly created magic");
        
        // Create file chooser for export location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Newly Created Magic");
        fileChooser.setInitialFileName("new_magic");
        
        // Set extension filters
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("Binary Files", "*.bin")
        );
        
        // Set initial directory from user preferences
        var preferences = userPreferencesUseCase.getCurrentPreferences();
        if (preferences.getLastOpenDirectory() != null && 
            java.nio.file.Files.exists(preferences.getLastOpenDirectory()) &&
            java.nio.file.Files.isDirectory(preferences.getLastOpenDirectory())) {
            
            fileChooser.setInitialDirectory(preferences.getLastOpenDirectory().toFile());
        }
        
        // Show save dialog
        File selectedFile = fileChooser.showSaveDialog(getStage());
        if (selectedFile != null) {
            // Update last opened directory preference
            userPreferencesUseCase.updateLastOpenDirectory(selectedFile.getParentFile().toPath());
            
            // Perform export in background task
            performExport(selectedFile);
        }
    }
    
    /**
     * Perform the actual export
     */
    private void performExport(File targetFile) {
        try {
            // Unbind status label to avoid binding conflicts
            statusLabel.textProperty().unbind();
            statusLabel.setText("Exporting newly created magic...");
            
            // Get file name without extension for base filename
            String baseFilename = targetFile.getName();
            int dotIndex = baseFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                baseFilename = baseFilename.substring(0, dotIndex);
            }
            
            // Create export request
            ExportRequestDTO request = ExportRequestDTO.simple(baseFilename, targetFile.getParentFile().toPath());
            
            // Perform export
            ExportResultDTO result = localizedExportUseCase.exportNewlyCreatedMagic(request);
            
            if (result.success()) {
                statusLabel.setText("Export completed successfully - " + result.createdFiles().size() + " files created");
                
                // Show success dialog with details
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Newly created magic exported successfully");
                
                StringBuilder content = new StringBuilder();
                content.append("Files created:\n");
                for (Path file : result.createdFiles()) {
                    content.append("• ").append(file.getFileName()).append("\n");
                }
                if (result.summary() != null) {
                    content.append("\nTotal size: ").append(formatFileSize(result.summary().totalFileSize()));
                }
                
                alert.setContentText(content.toString());
                alert.showAndWait();
                
                logger.info("Export completed successfully: {} files created", result.createdFiles().size());
            } else {
                statusLabel.setText("Export failed");
                String errorMsg = result.errors().isEmpty() ? "Unknown error" : String.join(", ", result.errors());
                showError("Export failed: " + errorMsg, new RuntimeException(errorMsg));
            }
            
        } catch (Exception e) {
            // Ensure status label is unbound before setting text
            statusLabel.textProperty().unbind();
            statusLabel.setText("Export failed");
            showError("Failed to export newly created magic", e);
            logger.error("Export failed to: {}", targetFile.getAbsolutePath(), e);
        }
    }
    
    /**
     * Format file size for display
     */
    private String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }
    
    @FXML
    private void exitApplication() {
        System.exit(0);
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About FF8 Magic Creator");
        alert.setHeaderText("FF8 Magic Creator v1.0.0");
        alert.setContentText("""
            Modern Kernel.bin Editor for Final Fantasy VIII
            
            Built with Java 21 and JavaFX
            Using Hexagonal Architecture
            
            Features:
            • Complete magic data editing
            • Binary preservation for accuracy
            • Modern user interface
            • Real-time validation
            
            Copyright © 2024
            """);
        alert.showAndWait();
    }
    
    private void loadKernelFile(File file) {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Loading kernel file...");
                updateProgress(0, 1);
                
                // Load the kernel file - MagicListModel will be updated automatically via observer pattern
                kernelFileUseCase.loadKernelFile(file.getAbsolutePath());
                updateProgress(1, 1);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    currentFile = file;
                    updateUIState();
                    updateMessage("Loaded kernel file: " + file.getName() + " (magic list updated automatically)");
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    showError("Failed to load kernel file", getException());
                    updateMessage("Failed to load file");
                });
            }
        };
        
        // Bind progress and status
        progressBar.progressProperty().bind(loadTask.progressProperty());
        statusLabel.textProperty().bind(loadTask.messageProperty());
        progressBar.visibleProperty().bind(loadTask.runningProperty());
        
        // Run the task
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void loadMagicBinary(File file) {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Loading magic binary file...");
                updateProgress(0, 1);
                
                // Load the magic binary file - MagicListModel will be updated automatically via observer pattern
                kernelFileUseCase.loadMagicBinary(file.getAbsolutePath());
                updateProgress(1, 1);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    // Note: Don't change currentFile for magic binary - we want to keep the kernel file as current
                    updateUIState(); // Update export menu state since we added new magic
                    updateMessage("Added magic from binary file: " + file.getName() + " (magic list updated automatically)");
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    showError("Failed to load magic binary file", getException());
                    updateMessage("Failed to load magic binary file");
                });
            }
        };
        
        // Bind progress and status
        progressBar.progressProperty().bind(loadTask.progressProperty());
        statusLabel.textProperty().bind(loadTask.messageProperty());
        progressBar.visibleProperty().bind(loadTask.runningProperty());
        
        // Run the task
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void updateUIState() {
        // Export is enabled if there are newly created magic spells
        if (exportMenuItem != null) {
            boolean hasNewlyCreatedMagic = localizedExportUseCase != null && 
                localizedExportUseCase.hasNewlyCreatedMagic();
            exportMenuItem.setDisable(!hasNewlyCreatedMagic);
        }
        
        // Update window title - only if scene is available
        try {
            Stage stage = getStage();
            if (stage != null) {
                String title = "FF8 Magic Creator";
                if (currentFile != null) {
                    title += " - " + currentFile.getName();
                }
                stage.setTitle(title);
            }
        } catch (Exception e) {
            // Ignore - scene might not be ready yet
            logger.debug("Could not update window title: {}", e.getMessage());
        }
    }
    
    private void showError(String message, Throwable exception) {
        logger.error(message, exception);
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(exception != null ? exception.getMessage() : "An unexpected error occurred");
        alert.showAndWait();
    }
    
    private Stage getStage() {
        if (menuBar == null || menuBar.getScene() == null) {
            return null;
        }
        return (Stage) menuBar.getScene().getWindow();
    }
    
    /**
     * Get the magic list model
     */
    public MagicListModel getMagicListModel() {
        return magicListModel;
    }
    
    /**
     * Get the magic editor use case
     */
    public MagicEditorUseCase getMagicEditorUseCase() {
        return magicEditorUseCase;
    }
    
    /**
     * Refresh the UI state (e.g., update export menu state after new magic is created)
     */
    public void refreshUIState() {
        updateUIState();
    }
} 