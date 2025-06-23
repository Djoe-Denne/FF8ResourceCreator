package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.GF;
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
        // Add change listeners to all GF compatibility spinners
        quezacotlSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        shivaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        ifritSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        sirenSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        brothersSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        diablosSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        carbuncleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        leviathanSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        pandemonaSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        cerberusSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        alexanderSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        doomtrainSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        bahamutSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        cactuarSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        tonberrySpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        edenSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
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
    
    private void onFieldChanged() {
        if (updatingFromModel || currentMagic == null) {
            return;
        }
        
        try {
            // Validate and save changes
            validateAndSave();
            
            // Mark main controller as having changes
            if (mainController != null) {
                mainController.markAsChanged();
            }
            
        } catch (Exception e) {
            logger.error("Error saving GF compatibility changes", e);
            showError("Failed to save GF compatibility changes", e.getMessage());
        }
    }
    
    private void validateAndSave() {
        // Create updated GF compatibility data and save
        logger.debug("Validating and saving GF compatibility changes for magic");
        
        // For now, just log the changes
        logger.info("GF Compatibility updated - Quezacolt: {}, Shiva: {}, Ifrit: {}", 
            quezacotlSpinner.getValue(), shivaSpinner.getValue(), ifritSpinner.getValue());
    }
    
    private void showError(String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }
} 