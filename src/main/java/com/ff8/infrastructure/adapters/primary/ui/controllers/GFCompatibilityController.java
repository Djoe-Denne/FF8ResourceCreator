package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.GF;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.gf.GFCompatibilityUICommand;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the GF Compatibility tab - handles Guardian Force compatibility settings.
 */
public class GFCompatibilityController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GFCompatibilityController.class);
    
    // GF Compatibility Spinners
    @FXML private Spinner<Double> quezacotlSpinner;
    @FXML private Spinner<Double> shivaSpinner;
    @FXML private Spinner<Double> ifritSpinner;
    @FXML private Spinner<Double> sirenSpinner;
    @FXML private Spinner<Double> brothersSpinner;
    @FXML private Spinner<Double> diablosSpinner;
    @FXML private Spinner<Double> carbuncleSpinner;
    @FXML private Spinner<Double> leviathanSpinner;
    @FXML private Spinner<Double> pandemonaSpinner;
    @FXML private Spinner<Double> cerberusSpinner;
    @FXML private Spinner<Double> alexanderSpinner;
    @FXML private Spinner<Double> doomtrainSpinner;
    @FXML private Spinner<Double> bahamutSpinner;
    @FXML private Spinner<Double> cactuarSpinner;
    @FXML private Spinner<Double> tonberrySpinner;
    @FXML private Spinner<Double> edenSpinner;
    
    private final MagicEditorUseCase magicEditorUseCase;
    private MainController mainController;
    private MagicDisplayDTO currentMagic;
    private boolean updatingFromModel = false;
    
    public GFCompatibilityController(ApplicationConfig config) {
        this.magicEditorUseCase = config.getMagicEditorUseCase();
        logger.info("GFCompatibilityController initialized");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSpinners();
        setupChangeListeners();
        
        // Initially disable all controls
        setControlsEnabled(false);
        
        logger.info("GFCompatibilityController UI initialized");
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        
        // Listen to magic selection changes
        mainController.getMagicListModel().selectedMagicProperty().addListener(
            (obs, oldMagic, newMagic) -> loadMagicData(newMagic));
    }
    
    private void setupSpinners() {
        // Setup all GF compatibility spinners with a range that allows decimal values
        setupGFSpinner(quezacotlSpinner);
        setupGFSpinner(shivaSpinner);
        setupGFSpinner(ifritSpinner);
        setupGFSpinner(sirenSpinner);
        setupGFSpinner(brothersSpinner);
        setupGFSpinner(diablosSpinner);
        setupGFSpinner(carbuncleSpinner);
        setupGFSpinner(leviathanSpinner);
        setupGFSpinner(pandemonaSpinner);
        setupGFSpinner(cerberusSpinner);
        setupGFSpinner(alexanderSpinner);
        setupGFSpinner(doomtrainSpinner);
        setupGFSpinner(bahamutSpinner);
        setupGFSpinner(cactuarSpinner);
        setupGFSpinner(tonberrySpinner);
        setupGFSpinner(edenSpinner);
    }
    
    private void setupGFSpinner(Spinner<Double> spinner) {
        // GF compatibility values are typically -10.0 to +10.0 with decimal precision
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10.0, 10.0, 0.0, 0.1));
        spinner.setEditable(true);
        
        // Format to show one decimal place
        spinner.getValueFactory().setConverter(new javafx.util.StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return value != null ? String.format("%.1f", value) : "0.0";
            }
            
            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        });
    }
    
    private void setupChangeListeners() {
        // Add change listeners to all GF compatibility spinners using command pattern
        quezacotlSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.QUEZACOLT), newVal));
        shivaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.SHIVA), newVal));
        ifritSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.IFRIT), newVal));
        sirenSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.SIREN), newVal));
        brothersSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.BROTHERS), newVal));
        diablosSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.DIABLOS), newVal));
        carbuncleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.CARBUNCLE), newVal));
        leviathanSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.LEVIATHAN), newVal));
        pandemonaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.PANDEMONA), newVal));
        cerberusSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.CERBERUS), newVal));
        alexanderSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.ALEXANDER), newVal));
        doomtrainSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.DOOMTRAIN), newVal));
        bahamutSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.BAHAMUT), newVal));
        cactuarSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.CACTUAR), newVal));
        tonberrySpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.TONBERRY), newVal));
        edenSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new GFCompatibilityUICommand(magicEditorUseCase, getCurrentMagicIndex(), GF.EDEN), newVal));
    }
    
    private void loadMagicData(MagicDisplayDTO magic) {
        currentMagic = magic;
        updatingFromModel = true;
        
        try {
            if (magic != null) {
                // Load GF compatibility data
                var gfCompatibility = magic.gfCompatibility();
                
                // Set compatibility values for each GF
                quezacotlSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.QUEZACOLT));
                shivaSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.SHIVA));
                ifritSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.IFRIT));
                sirenSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.SIREN));
                brothersSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.BROTHERS));
                diablosSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.DIABLOS));
                carbuncleSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.CARBUNCLE));
                leviathanSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.LEVIATHAN));
                pandemonaSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.PANDEMONA));
                cerberusSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.CERBERUS));
                alexanderSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.ALEXANDER));
                doomtrainSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.DOOMTRAIN));
                bahamutSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.BAHAMUT));
                cactuarSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.CACTUAR));
                tonberrySpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.TONBERRY));
                edenSpinner.getValueFactory().setValue(gfCompatibility.getCompatibility(GF.EDEN));
                
                setControlsEnabled(true);
                logger.debug("Loaded GF compatibility data for magic: {}", magic.spellName());
            } else {
                clearFields();
                setControlsEnabled(false);
            }
        } finally {
            updatingFromModel = false;
        }
    }
    
    private void clearFields() {
        // Reset all GF compatibility values to 0.0
        quezacotlSpinner.getValueFactory().setValue(0.0);
        shivaSpinner.getValueFactory().setValue(0.0);
        ifritSpinner.getValueFactory().setValue(0.0);
        sirenSpinner.getValueFactory().setValue(0.0);
        brothersSpinner.getValueFactory().setValue(0.0);
        diablosSpinner.getValueFactory().setValue(0.0);
        carbuncleSpinner.getValueFactory().setValue(0.0);
        leviathanSpinner.getValueFactory().setValue(0.0);
        pandemonaSpinner.getValueFactory().setValue(0.0);
        cerberusSpinner.getValueFactory().setValue(0.0);
        alexanderSpinner.getValueFactory().setValue(0.0);
        doomtrainSpinner.getValueFactory().setValue(0.0);
        bahamutSpinner.getValueFactory().setValue(0.0);
        cactuarSpinner.getValueFactory().setValue(0.0);
        tonberrySpinner.getValueFactory().setValue(0.0);
        edenSpinner.getValueFactory().setValue(0.0);
    }
    
    private void setControlsEnabled(boolean enabled) {
        // Enable/disable all GF compatibility spinners
        quezacotlSpinner.setDisable(!enabled);
        shivaSpinner.setDisable(!enabled);
        ifritSpinner.setDisable(!enabled);
        sirenSpinner.setDisable(!enabled);
        brothersSpinner.setDisable(!enabled);
        diablosSpinner.setDisable(!enabled);
        carbuncleSpinner.setDisable(!enabled);
        leviathanSpinner.setDisable(!enabled);
        pandemonaSpinner.setDisable(!enabled);
        cerberusSpinner.setDisable(!enabled);
        alexanderSpinner.setDisable(!enabled);
        doomtrainSpinner.setDisable(!enabled);
        bahamutSpinner.setDisable(!enabled);
        cactuarSpinner.setDisable(!enabled);
        tonberrySpinner.setDisable(!enabled);
        edenSpinner.setDisable(!enabled);
    }
    

    
    /**
     * Execute a UI command for field changes.
     * This follows the Command Pattern for encapsulating user actions.
     * 
     * @param command the command to execute
     * @param newValue the new value from the UI
     */
    private void onFieldChange(UICommand<Double> command, Double newValue) {
        if (updatingFromModel || currentMagic == null) {
            return;
        }
        
        try {
            // Validate the new value first
            if (!command.validate(newValue)) {
                logger.warn("Invalid value {} for command: {}", newValue, command.getDescription());
                showError("Invalid value", "The entered value is not valid for this field.");
                return;
            }
            
            // Execute the command through the domain layer
            // The command will handle validation, use case invocation, and domain updates
            // Observer pattern will handle UI updates automatically
            logger.debug("Executing command: {} with value: {}", command.getDescription(), newValue);
            command.execute(newValue);
            
            // Mark main controller as having changes
            if (mainController != null) {
                mainController.markAsChanged();
            }
            
        } catch (Exception e) {
            logger.error("Error executing command: {} with value: {}", command.getDescription(), newValue, e);
            showError("Failed to save changes", e.getMessage());
        }
    }
    
    /**
     * Get the current magic index for command creation.
     * @return the current magic index (unique identifier), or -1 if no magic is selected
     */
    private int getCurrentMagicIndex() {
        return currentMagic != null ? currentMagic.index() : -1;
    }
    
    private void showError(String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }
} 