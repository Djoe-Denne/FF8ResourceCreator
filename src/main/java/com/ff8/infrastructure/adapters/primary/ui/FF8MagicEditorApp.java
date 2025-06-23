package com.ff8.infrastructure.adapters.primary.ui;

import com.ff8.application.ports.primary.UserPreferencesUseCase;
import com.ff8.domain.entities.UserPreferences;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX application class for FF8 Magic Editor.
 * Serves as the primary adapter in the hexagonal architecture.
 */
public class FF8MagicEditorApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(FF8MagicEditorApp.class);
    
    private ApplicationConfig applicationConfig;
    private UserPreferencesUseCase userPreferencesUseCase;
    private Stage primaryStage;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        // Initialize application configuration
        applicationConfig = ApplicationConfig.getInstance();
        applicationConfig.validateConfiguration();
        
        // Get user preferences use case
        userPreferencesUseCase = applicationConfig.getUserPreferencesUseCase();
        
        logger.info("JavaFX application initialized");
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        
        try {
            logger.info("Starting JavaFX application");
            
            // Load user preferences
            UserPreferences preferences = userPreferencesUseCase.loadPreferences();
            logger.info("Loaded user preferences: {}", preferences);
            
            // Load main window FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
            
            // Set up dependency injection for controllers
            loader.setControllerFactory(controllerClass -> {
                try {
                    var constructor = controllerClass.getDeclaredConstructor(ApplicationConfig.class);
                    return constructor.newInstance(applicationConfig);
                } catch (Exception e) {
                    logger.error("Failed to create controller: " + controllerClass.getName(), e);
                    throw new RuntimeException("Controller creation failed", e);
                }
            });
            
            Scene scene = new Scene(loader.load());
            
            // Load CSS styles
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            // Set up primary stage
            primaryStage.setTitle("FF8 Magic Creator - Modern Kernel.bin Editor");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Apply window settings from preferences
            userPreferencesUseCase.applyWindowSettings(primaryStage, preferences);
            
            // Set application icon
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icons/app-icon.png")));
            } catch (Exception e) {
                logger.warn("Could not load application icon", e);
            }
            
            // Set up window state change listeners to save preferences
            setupWindowStateListeners(primaryStage);
            
            primaryStage.show();
            
            logger.info("JavaFX application started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start JavaFX application", e);
            throw e;
        }
    }
    
    @Override
    public void stop() throws Exception {
        // Save current window state before closing
        if (primaryStage != null && userPreferencesUseCase != null) {
            try {
                var windowSettings = userPreferencesUseCase.captureWindowSettings(primaryStage);
                var currentPreferences = userPreferencesUseCase.getCurrentPreferences();
                var updatedPreferences = currentPreferences.withWindowSettings(windowSettings);
                
                var saveResult = userPreferencesUseCase.savePreferences(updatedPreferences);
                if (saveResult.isSuccess()) {
                    logger.info("Saved window state on application exit");
                } else if (saveResult instanceof UserPreferencesUseCase.SaveResult.Failure failure) {
                    logger.warn("Failed to save window state on exit: {}", failure.message());
                }
            } catch (Exception e) {
                logger.warn("Error saving window state on exit", e);
            }
        }
        
        super.stop();
        logger.info("JavaFX application stopping");
    }
    
    /**
     * Set up listeners to automatically save window state changes
     */
    private void setupWindowStateListeners(Stage stage) {
        // Debounce rapid changes to avoid excessive file writes
        final long SAVE_DELAY_MS = 1000; // 1 second delay
        final java.util.Timer timer = new java.util.Timer(true); // daemon thread
        final java.util.concurrent.atomic.AtomicReference<java.util.TimerTask> pendingTask = 
            new java.util.concurrent.atomic.AtomicReference<>();
        
        Runnable saveWindowState = () -> {
            // Cancel any pending save task
            java.util.TimerTask existing = pendingTask.getAndSet(null);
            if (existing != null) {
                existing.cancel();
            }
            
            // Schedule new save task
            java.util.TimerTask newTask = new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        var windowSettings = userPreferencesUseCase.captureWindowSettings(stage);
                        var currentPreferences = userPreferencesUseCase.getCurrentPreferences();
                        var updatedPreferences = currentPreferences.withWindowSettings(windowSettings);
                        
                        var saveResult = userPreferencesUseCase.savePreferences(updatedPreferences);
                        if (saveResult.isFailure() && saveResult instanceof UserPreferencesUseCase.SaveResult.Failure failure) {
                            logger.debug("Failed to auto-save window state: {}", failure.message());
                        }
                    } catch (Exception e) {
                        logger.debug("Error auto-saving window state", e);
                    }
                }
            };
            
            pendingTask.set(newTask);
            timer.schedule(newTask, SAVE_DELAY_MS);
        };
        
        // Listen to window state changes
        stage.widthProperty().addListener((obs, oldVal, newVal) -> saveWindowState.run());
        stage.heightProperty().addListener((obs, oldVal, newVal) -> saveWindowState.run());
        stage.xProperty().addListener((obs, oldVal, newVal) -> saveWindowState.run());
        stage.yProperty().addListener((obs, oldVal, newVal) -> saveWindowState.run());
        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> saveWindowState.run());
        
        logger.debug("Window state listeners configured with {}ms debounce delay", SAVE_DELAY_MS);
    }
    
    /**
     * Launch the JavaFX application
     */
    public static void launchApp(String[] args) {
        launch(args);
    }
} 