package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.ports.primary.KernelFileUseCase;
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
import java.util.ResourceBundle;

/**
 * Main controller for the FF8 Magic Editor application.
 * Coordinates the main window and handles file operations.
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private MenuBar menuBar;
    @FXML private MenuItem openMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
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
    
    // UI Models
    private final MagicListModel magicListModel;
    
    // Current file information
    private File currentFile;
    private boolean hasUnsavedChanges = false;
    
    public MainController(ApplicationConfig config) {
        this.kernelFileUseCase = config.getKernelFileUseCase();
        this.magicEditorUseCase = config.getMagicEditorUseCase();
        this.userPreferencesUseCase = config.getUserPreferencesUseCase();
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
     * Set up child controllers after FXML loading
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
    
    private void setupMenuActions() {
        openMenuItem.setOnAction(e -> openFile());
        saveMenuItem.setOnAction(e -> saveFile());
        saveAsMenuItem.setOnAction(e -> saveAsFile());
        exitMenuItem.setOnAction(e -> exitApplication());
        aboutMenuItem.setOnAction(e -> showAbout());
        
        // Initially disable save options
        saveMenuItem.setDisable(true);
        saveAsMenuItem.setDisable(true);
    }
    
    private void setupStatusBar() {
        progressBar.setVisible(false);
        statusLabel.setText("Ready - Please open a kernel.bin file");
    }
    
    @FXML
    private void openFile() {
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
    
    @FXML
    private void saveFile() {
        if (currentFile != null) {
            saveKernelFile(currentFile);
        } else {
            saveAsFile();
        }
    }
    
    @FXML
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Kernel.bin File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Kernel Files", "*.bin")
        );
        
        if (currentFile != null) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
            fileChooser.setInitialFileName(currentFile.getName());
        } else {
            // Set initial directory from user preferences if no current file
            var preferences = userPreferencesUseCase.getCurrentPreferences();
            if (preferences.getLastOpenDirectory() != null && 
                java.nio.file.Files.exists(preferences.getLastOpenDirectory()) &&
                java.nio.file.Files.isDirectory(preferences.getLastOpenDirectory())) {
                
                fileChooser.setInitialDirectory(preferences.getLastOpenDirectory().toFile());
            }
        }
        
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            // Update last opened directory preference
            userPreferencesUseCase.updateLastOpenDirectory(file.getParentFile().toPath());
            saveKernelFile(file);
        }
    }
    
    @FXML
    private void exitApplication() {
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Do you want to save before exiting?");
            
            ButtonType saveAndExit = new ButtonType("Save and Exit");
            ButtonType exitWithoutSaving = new ButtonType("Exit without Saving");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(saveAndExit, exitWithoutSaving, cancel);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == saveAndExit) {
                    saveFile();
                    System.exit(0);
                } else if (response == exitWithoutSaving) {
                    System.exit(0);
                }
                // Cancel - do nothing
            });
        } else {
            System.exit(0);
        }
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
                    hasUnsavedChanges = false;
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
    
    private void saveKernelFile(File file) {
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Saving kernel file...");
                updateProgress(0, 1);
                
                // Save the kernel file
                kernelFileUseCase.saveKernelFile(file.getAbsolutePath());
                updateProgress(1, 1);
                
                javafx.application.Platform.runLater(() -> {
                    currentFile = file;
                    hasUnsavedChanges = false;
                    updateUIState();
                    updateMessage("Saved to " + file.getName());
                });
                
                return null;
            }
            
            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    showError("Failed to save kernel file", getException());
                    updateMessage("Failed to save file");
                });
            }
        };
        
        // Bind progress and status
        progressBar.progressProperty().bind(saveTask.progressProperty());
        statusLabel.textProperty().bind(saveTask.messageProperty());
        progressBar.visibleProperty().bind(saveTask.runningProperty());
        
        // Run the task
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
    
    private void updateUIState() {
        boolean hasFile = currentFile != null;
        
        if (saveMenuItem != null) {
            saveMenuItem.setDisable(!hasFile || !hasUnsavedChanges);
        }
        if (saveAsMenuItem != null) {
            saveAsMenuItem.setDisable(!hasFile);
        }
        
        // Update window title - only if scene is available
        try {
            Stage stage = getStage();
            if (stage != null) {
                String title = "FF8 Magic Creator";
                if (currentFile != null) {
                    title += " - " + currentFile.getName();
                    if (hasUnsavedChanges) {
                        title += " *";
                    }
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
     * Mark that changes have been made
     */
    public void markAsChanged() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateUIState();
        }
    }
    
    /**
     * Get the magic list model
     */
    public MagicListModel getMagicListModel() {
        return magicListModel;
    }
} 