package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Junction tab - handles junction bonuses editing.
 */
public class JunctionTabController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(JunctionTabController.class);
    
    // Junction Stats
    @FXML private Spinner<Integer> hpJunctionSpinner;
    @FXML private Spinner<Integer> strJunctionSpinner;
    @FXML private Spinner<Integer> vitJunctionSpinner;
    @FXML private Spinner<Integer> magJunctionSpinner;
    @FXML private Spinner<Integer> sprJunctionSpinner;
    @FXML private Spinner<Integer> spdJunctionSpinner;
    @FXML private Spinner<Integer> evaJunctionSpinner;
    @FXML private Spinner<Integer> hitJunctionSpinner;
    @FXML private Spinner<Integer> luckJunctionSpinner;
    
    // Elemental Attack (Radio buttons)
    @FXML private RadioButton fireAttackRadio;
    @FXML private RadioButton iceAttackRadio;
    @FXML private RadioButton thunderAttackRadio;
    @FXML private RadioButton earthAttackRadio;
    @FXML private RadioButton poisonAttackRadio;
    @FXML private RadioButton windAttackRadio;
    @FXML private RadioButton waterAttackRadio;
    @FXML private RadioButton holyAttackRadio;
    @FXML private RadioButton nonElementalAttackRadio;
    @FXML private Slider elementalAttackSlider;
    @FXML private Label elementalAttackLabel;
    
    // Elemental Defense (Checkboxes)
    @FXML private CheckBox fireDefenseCheck;
    @FXML private CheckBox iceDefenseCheck;
    @FXML private CheckBox thunderDefenseCheck;
    @FXML private CheckBox earthDefenseCheck;
    @FXML private CheckBox poisonDefenseCheck;
    @FXML private CheckBox windDefenseCheck;
    @FXML private CheckBox waterDefenseCheck;
    @FXML private CheckBox holyDefenseCheck;
    @FXML private Slider elementalDefenseSlider;
    @FXML private Label elementalDefenseLabel;
    
    // Status Attack
    @FXML private CheckBox junctionDeathCheck;
    @FXML private CheckBox junctionPoisonCheck;
    @FXML private CheckBox junctionPetrifyCheck;
    @FXML private CheckBox junctionDarknessCheck;
    @FXML private CheckBox junctionSilenceCheck;
    @FXML private CheckBox junctionBerserkCheck;
    @FXML private CheckBox junctionZombieCheck;
    @FXML private CheckBox junctionSleepCheck;
    @FXML private CheckBox junctionSlowCheck;
    @FXML private CheckBox junctionStopCheck;
    @FXML private CheckBox junctionConfusionCheck;
    @FXML private CheckBox junctionDrainCheck;
    @FXML private Slider statusAttackSlider;
    @FXML private Label statusAttackLabel;
    
    // Status Defense
    @FXML private CheckBox junctionDeathDefenseCheck;
    @FXML private CheckBox junctionPoisonDefenseCheck;
    @FXML private CheckBox junctionPetrifyDefenseCheck;
    @FXML private CheckBox junctionDarknessDefenseCheck;
    @FXML private CheckBox junctionSilenceDefenseCheck;
    @FXML private CheckBox junctionBerserkDefenseCheck;
    @FXML private CheckBox junctionZombieDefenseCheck;
    @FXML private CheckBox junctionSleepDefenseCheck;
    @FXML private CheckBox junctionSlowDefenseCheck;
    @FXML private CheckBox junctionStopDefenseCheck;
    @FXML private CheckBox junctionCurseDefenseCheck;
    @FXML private CheckBox junctionConfusionDefenseCheck;
    @FXML private CheckBox junctionDrainDefenseCheck;
    @FXML private Slider statusDefenseSlider;
    @FXML private Label statusDefenseLabel;
    
    private final MagicEditorUseCase magicEditorUseCase;
    private MainController mainController;
    private MagicDisplayDTO currentMagic;
    private boolean updatingFromModel = false;
    
    private ToggleGroup elementalAttackToggleGroup;
    
    public JunctionTabController(ApplicationConfig config) {
        this.magicEditorUseCase = config.getMagicEditorUseCase();
        logger.info("JunctionTabController initialized");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSpinners();
        setupToggleGroups();
        setupSliders();
        setupChangeListeners();
        
        // Initially disable all controls
        setControlsEnabled(false);
        
        logger.info("JunctionTabController UI initialized");
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        
        // Listen to magic selection changes
        mainController.getMagicListModel().selectedMagicProperty().addListener(
            (obs, oldMagic, newMagic) -> loadMagicData(newMagic));
    }
    
    private void setupSpinners() {
        // Junction stat spinners
        hpJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        strJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        vitJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        magJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        sprJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        spdJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        evaJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        hitJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        luckJunctionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
    }
    
    private void setupToggleGroups() {
        // Create toggle group for elemental attack radio buttons
        elementalAttackToggleGroup = new ToggleGroup();
        fireAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        iceAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        thunderAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        earthAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        poisonAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        windAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        waterAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        holyAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        nonElementalAttackRadio.setToggleGroup(elementalAttackToggleGroup);
    }
    
    private void setupSliders() {
        // Setup elemental attack slider
        elementalAttackSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            elementalAttackLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChanged();
            }
        });
        
        // Setup elemental defense slider
        elementalDefenseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            elementalDefenseLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChanged();
            }
        });
        
        // Setup status attack slider
        statusAttackSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            statusAttackLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChanged();
            }
        });
        
        // Setup status defense slider
        statusDefenseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            statusDefenseLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChanged();
            }
        });
    }
    
    private void setupChangeListeners() {
        // Junction stat spinners
        hpJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        strJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        vitJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        magJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        sprJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        spdJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        evaJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        hitJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        luckJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Elemental attack radio buttons
        elementalAttackToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> onFieldChanged());
        
        // Elemental defense checkboxes
        fireDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        iceDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        thunderDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        earthDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        poisonDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        windDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        waterDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        holyDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Status attack checkboxes
        junctionDeathCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionPoisonCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionPetrifyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionDarknessCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSilenceCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionBerserkCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionZombieCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSleepCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSlowCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionStopCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionConfusionCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionDrainCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Status defense checkboxes
        junctionDeathDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionPoisonDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionPetrifyDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionDarknessDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSilenceDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionBerserkDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionZombieDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSleepDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionSlowDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionStopDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionCurseDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionConfusionDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        junctionDrainDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
    }
    
    private void loadMagicData(MagicDisplayDTO magic) {
        currentMagic = magic;
        updatingFromModel = true;
        
        try {
            if (magic != null) {
                // Load junction stats
                var junctionStats = magic.junctionStats();
                hpJunctionSpinner.getValueFactory().setValue(junctionStats.hp());
                strJunctionSpinner.getValueFactory().setValue(junctionStats.str());
                vitJunctionSpinner.getValueFactory().setValue(junctionStats.vit());
                magJunctionSpinner.getValueFactory().setValue(junctionStats.mag());
                sprJunctionSpinner.getValueFactory().setValue(junctionStats.spr());
                spdJunctionSpinner.getValueFactory().setValue(junctionStats.spd());
                evaJunctionSpinner.getValueFactory().setValue(junctionStats.eva());
                hitJunctionSpinner.getValueFactory().setValue(junctionStats.hit());
                luckJunctionSpinner.getValueFactory().setValue(junctionStats.luck());
                
                // Load elemental junction data
                var elementalJunction = magic.junctionElemental();
                
                // Set elemental attack radio button
                clearElementalAttackRadios();
                switch (elementalJunction.attackElement()) {
                    case FIRE -> fireAttackRadio.setSelected(true);
                    case ICE -> iceAttackRadio.setSelected(true);
                    case THUNDER -> thunderAttackRadio.setSelected(true);
                    case EARTH -> earthAttackRadio.setSelected(true);
                    case POISON -> poisonAttackRadio.setSelected(true);
                    case WIND -> windAttackRadio.setSelected(true);
                    case WATER -> waterAttackRadio.setSelected(true);
                    case HOLY -> holyAttackRadio.setSelected(true);
                    case NONE -> nonElementalAttackRadio.setSelected(true);
                }
                elementalAttackSlider.setValue(elementalJunction.attackValue());
                
                // Set elemental defense checkboxes
                var defenseElements = elementalJunction.defenseElements();
                fireDefenseCheck.setSelected(defenseElements.contains(Element.FIRE));
                iceDefenseCheck.setSelected(defenseElements.contains(Element.ICE));
                thunderDefenseCheck.setSelected(defenseElements.contains(Element.THUNDER));
                earthDefenseCheck.setSelected(defenseElements.contains(Element.EARTH));
                poisonDefenseCheck.setSelected(defenseElements.contains(Element.POISON));
                windDefenseCheck.setSelected(defenseElements.contains(Element.WIND));
                waterDefenseCheck.setSelected(defenseElements.contains(Element.WATER));
                holyDefenseCheck.setSelected(defenseElements.contains(Element.HOLY));
                elementalDefenseSlider.setValue(elementalJunction.defenseValue());
                
                // Load status junction data
                var statusJunction = magic.junctionStatus();
                
                // Set status attack checkboxes
                var attackStatuses = statusJunction.attackStatuses();
                junctionDeathCheck.setSelected(attackStatuses.contains(StatusEffect.DEATH));
                junctionPoisonCheck.setSelected(attackStatuses.contains(StatusEffect.POISON));
                junctionPetrifyCheck.setSelected(attackStatuses.contains(StatusEffect.PETRIFY));
                junctionDarknessCheck.setSelected(attackStatuses.contains(StatusEffect.DARKNESS));
                junctionSilenceCheck.setSelected(attackStatuses.contains(StatusEffect.SILENCE));
                junctionBerserkCheck.setSelected(attackStatuses.contains(StatusEffect.BERSERK));
                junctionZombieCheck.setSelected(attackStatuses.contains(StatusEffect.ZOMBIE));
                junctionSleepCheck.setSelected(attackStatuses.contains(StatusEffect.SLEEP));
                junctionSlowCheck.setSelected(attackStatuses.contains(StatusEffect.SLOW));
                junctionStopCheck.setSelected(attackStatuses.contains(StatusEffect.STOP));
                junctionConfusionCheck.setSelected(attackStatuses.contains(StatusEffect.CONFUSION));
                junctionDrainCheck.setSelected(attackStatuses.contains(StatusEffect.DRAIN));
                statusAttackSlider.setValue(statusJunction.attackValue());
                
                // Set status defense checkboxes
                var defenseStatuses = statusJunction.defenseStatuses();
                junctionDeathDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.DEATH));
                junctionPoisonDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.POISON));
                junctionPetrifyDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.PETRIFY));
                junctionDarknessDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.DARKNESS));
                junctionSilenceDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.SILENCE));
                junctionBerserkDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.BERSERK));
                junctionZombieDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.ZOMBIE));
                junctionSleepDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.SLEEP));
                junctionSlowDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.SLOW));
                junctionStopDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.STOP));
                junctionCurseDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.CURSE));
                junctionConfusionDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.CONFUSION));
                junctionDrainDefenseCheck.setSelected(defenseStatuses.contains(StatusEffect.DRAIN));
                statusDefenseSlider.setValue(statusJunction.defenseValue());
                
                setControlsEnabled(true);
                logger.debug("Loaded junction data for magic: {}", magic.spellName());
            } else {
                clearFields();
                setControlsEnabled(false);
            }
        } finally {
            updatingFromModel = false;
        }
    }
    
    private void clearElementalAttackRadios() {
        elementalAttackToggleGroup.selectToggle(null);
    }
    
    private void clearFields() {
        // Clear junction stats
        hpJunctionSpinner.getValueFactory().setValue(0);
        strJunctionSpinner.getValueFactory().setValue(0);
        vitJunctionSpinner.getValueFactory().setValue(0);
        magJunctionSpinner.getValueFactory().setValue(0);
        sprJunctionSpinner.getValueFactory().setValue(0);
        spdJunctionSpinner.getValueFactory().setValue(0);
        evaJunctionSpinner.getValueFactory().setValue(0);
        hitJunctionSpinner.getValueFactory().setValue(0);
        luckJunctionSpinner.getValueFactory().setValue(0);
        
        // Clear elemental attack
        clearElementalAttackRadios();
        elementalAttackSlider.setValue(0);
        
        // Clear elemental defense
        fireDefenseCheck.setSelected(false);
        iceDefenseCheck.setSelected(false);
        thunderDefenseCheck.setSelected(false);
        earthDefenseCheck.setSelected(false);
        poisonDefenseCheck.setSelected(false);
        windDefenseCheck.setSelected(false);
        waterDefenseCheck.setSelected(false);
        holyDefenseCheck.setSelected(false);
        elementalDefenseSlider.setValue(0);
        
        // Clear status attack
        junctionDeathCheck.setSelected(false);
        junctionPoisonCheck.setSelected(false);
        junctionPetrifyCheck.setSelected(false);
        junctionDarknessCheck.setSelected(false);
        junctionSilenceCheck.setSelected(false);
        junctionBerserkCheck.setSelected(false);
        junctionZombieCheck.setSelected(false);
        junctionSleepCheck.setSelected(false);
        junctionSlowCheck.setSelected(false);
        junctionStopCheck.setSelected(false);
        junctionConfusionCheck.setSelected(false);
        junctionDrainCheck.setSelected(false);
        statusAttackSlider.setValue(0);
        
        // Clear status defense
        junctionDeathDefenseCheck.setSelected(false);
        junctionPoisonDefenseCheck.setSelected(false);
        junctionPetrifyDefenseCheck.setSelected(false);
        junctionDarknessDefenseCheck.setSelected(false);
        junctionSilenceDefenseCheck.setSelected(false);
        junctionBerserkDefenseCheck.setSelected(false);
        junctionZombieDefenseCheck.setSelected(false);
        junctionSleepDefenseCheck.setSelected(false);
        junctionSlowDefenseCheck.setSelected(false);
        junctionStopDefenseCheck.setSelected(false);
        junctionCurseDefenseCheck.setSelected(false);
        junctionConfusionDefenseCheck.setSelected(false);
        junctionDrainDefenseCheck.setSelected(false);
        statusDefenseSlider.setValue(0);
    }
    
    private void setControlsEnabled(boolean enabled) {
        // Junction stat spinners
        hpJunctionSpinner.setDisable(!enabled);
        strJunctionSpinner.setDisable(!enabled);
        vitJunctionSpinner.setDisable(!enabled);
        magJunctionSpinner.setDisable(!enabled);
        sprJunctionSpinner.setDisable(!enabled);
        spdJunctionSpinner.setDisable(!enabled);
        evaJunctionSpinner.setDisable(!enabled);
        hitJunctionSpinner.setDisable(!enabled);
        luckJunctionSpinner.setDisable(!enabled);
        
        // Elemental attack
        fireAttackRadio.setDisable(!enabled);
        iceAttackRadio.setDisable(!enabled);
        thunderAttackRadio.setDisable(!enabled);
        earthAttackRadio.setDisable(!enabled);
        poisonAttackRadio.setDisable(!enabled);
        windAttackRadio.setDisable(!enabled);
        waterAttackRadio.setDisable(!enabled);
        holyAttackRadio.setDisable(!enabled);
        nonElementalAttackRadio.setDisable(!enabled);
        elementalAttackSlider.setDisable(!enabled);
        
        // Elemental defense
        fireDefenseCheck.setDisable(!enabled);
        iceDefenseCheck.setDisable(!enabled);
        thunderDefenseCheck.setDisable(!enabled);
        earthDefenseCheck.setDisable(!enabled);
        poisonDefenseCheck.setDisable(!enabled);
        windDefenseCheck.setDisable(!enabled);
        waterDefenseCheck.setDisable(!enabled);
        holyDefenseCheck.setDisable(!enabled);
        elementalDefenseSlider.setDisable(!enabled);
        
        // Status attack
        junctionDeathCheck.setDisable(!enabled);
        junctionPoisonCheck.setDisable(!enabled);
        junctionPetrifyCheck.setDisable(!enabled);
        junctionDarknessCheck.setDisable(!enabled);
        junctionSilenceCheck.setDisable(!enabled);
        junctionBerserkCheck.setDisable(!enabled);
        junctionZombieCheck.setDisable(!enabled);
        junctionSleepCheck.setDisable(!enabled);
        junctionSlowCheck.setDisable(!enabled);
        junctionStopCheck.setDisable(!enabled);
        junctionConfusionCheck.setDisable(!enabled);
        junctionDrainCheck.setDisable(!enabled);
        statusAttackSlider.setDisable(!enabled);
        
        // Status defense
        junctionDeathDefenseCheck.setDisable(!enabled);
        junctionPoisonDefenseCheck.setDisable(!enabled);
        junctionPetrifyDefenseCheck.setDisable(!enabled);
        junctionDarknessDefenseCheck.setDisable(!enabled);
        junctionSilenceDefenseCheck.setDisable(!enabled);
        junctionBerserkDefenseCheck.setDisable(!enabled);
        junctionZombieDefenseCheck.setDisable(!enabled);
        junctionSleepDefenseCheck.setDisable(!enabled);
        junctionSlowDefenseCheck.setDisable(!enabled);
        junctionStopDefenseCheck.setDisable(!enabled);
        junctionCurseDefenseCheck.setDisable(!enabled);
        junctionConfusionDefenseCheck.setDisable(!enabled);
        junctionDrainDefenseCheck.setDisable(!enabled);
        statusDefenseSlider.setDisable(!enabled);
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
            logger.error("Error saving junction changes", e);
            showError("Failed to save junction changes", e.getMessage());
        }
    }
    
    private void validateAndSave() {
        // Create updated junction data and save
        logger.debug("Validating and saving junction changes for magic");
        
        // For now, just log the changes
        logger.info("Junction updated - HP: {}, STR: {}, VIT: {}", 
            hpJunctionSpinner.getValue(), strJunctionSpinner.getValue(), vitJunctionSpinner.getValue());
    }
    
    private void showError(String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }
} 