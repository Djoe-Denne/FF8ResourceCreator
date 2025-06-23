package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the General tab - handles basic magic properties editing.
 */
public class GeneralTabController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GeneralTabController.class);
    
    // General section
    @FXML private ComboBox<String> magicIdComboBox;
    @FXML private TextField spellNameField;
    @FXML private TextField spellDescriptionField;
    @FXML private ComboBox<Element> elementComboBox;
    @FXML private ComboBox<AttackType> attackTypeComboBox;
    @FXML private Spinner<Integer> spellPowerSpinner;
    @FXML private Spinner<Integer> drawResistSpinner;
    @FXML private Spinner<Integer> hitCountSpinner;
    @FXML private Button damageFormulaButton;
    @FXML private Button damageChartButton;
    
    // Target Info checkboxes
    @FXML private CheckBox targetDeadCheckBox;
    @FXML private CheckBox targetUnknown1CheckBox;
    @FXML private CheckBox targetUnknown2CheckBox;
    @FXML private CheckBox targetSingleSideCheckBox;
    @FXML private CheckBox targetSingleCheckBox;
    @FXML private CheckBox targetUnknown3CheckBox;
    @FXML private CheckBox targetEnemyCheckBox;
    @FXML private CheckBox targetUnknown4CheckBox;
    
    // Status Attack
    @FXML private Spinner<Integer> statusAttackSpinner;
    @FXML private CheckBox statusSleepCheckBox;
    @FXML private CheckBox statusHasteCheckBox;
    @FXML private CheckBox statusSlowCheckBox;
    @FXML private CheckBox statusStopCheckBox;
    @FXML private CheckBox statusRegenCheckBox;
    @FXML private CheckBox statusProtectCheckBox;
    @FXML private CheckBox statusShellCheckBox;
    @FXML private CheckBox statusReflectCheckBox;
    @FXML private CheckBox statusAuraCheckBox;
    @FXML private CheckBox statusCurseCheckBox;
    @FXML private CheckBox statusDoomCheckBox;
    @FXML private CheckBox statusInvincibleCheckBox;
    @FXML private CheckBox statusFloatCheckBox;
    @FXML private CheckBox statusPetrifyingCheckBox;
    @FXML private CheckBox statusConfusionCheckBox;
    @FXML private CheckBox statusDrainCheckBox;
    @FXML private CheckBox statusEjectCheckBox;
    @FXML private CheckBox statusDoubleCheckBox;
    @FXML private CheckBox statusTripleCheckBox;
    @FXML private CheckBox statusDefendCheckBox;
    @FXML private CheckBox statusUnknown1CheckBox;
    @FXML private CheckBox statusUnknown2CheckBox;
    @FXML private CheckBox statusChargedCheckBox;
    @FXML private CheckBox statusBackAttackCheckBox;
    @FXML private CheckBox statusVit0CheckBox;
    @FXML private CheckBox statusAngelWingCheckBox;
    @FXML private CheckBox statusUnknown3CheckBox;
    @FXML private CheckBox statusUnknown4CheckBox;
    @FXML private CheckBox statusUnknown5CheckBox;
    @FXML private CheckBox statusUnknown6CheckBox;
    @FXML private CheckBox statusHasMagicCheckBox;
    @FXML private CheckBox statusSummonGFCheckBox;
    @FXML private CheckBox statusDeathCheckBox;
    @FXML private CheckBox statusPoisonCheckBox;
    @FXML private CheckBox statusPetrifyCheckBox;
    @FXML private CheckBox statusDarknessCheckBox;
    @FXML private CheckBox statusSilenceCheckBox;
    @FXML private CheckBox statusBerserkCheckBox;
    @FXML private CheckBox statusZombieCheckBox;
    @FXML private CheckBox statusUnknown7CheckBox;
    
    // Attack Flags
    @FXML private CheckBox attackShelledCheckBox;
    @FXML private CheckBox attackUnknown1CheckBox;
    @FXML private CheckBox attackUnknown2CheckBox;
    @FXML private CheckBox attackBreakDamageLimitCheckBox;
    @FXML private CheckBox attackReflectedCheckBox;
    @FXML private CheckBox attackUnknown3CheckBox;
    @FXML private CheckBox attackUnknown4CheckBox;
    @FXML private CheckBox attackReviveCheckBox;
    
    private final MagicEditorUseCase magicEditorUseCase;
    private MainController mainController;
    private MagicDisplayDTO currentMagic;
    private boolean updatingFromModel = false;
    
    public GeneralTabController(ApplicationConfig config) {
        this.magicEditorUseCase = config.getMagicEditorUseCase();
        logger.info("GeneralTabController initialized");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSpinners();
        setupComboBoxes();
        setupChangeListeners();
        
        // Initially disable all controls
        setControlsEnabled(false);
        
        logger.info("GeneralTabController UI initialized");
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        
        // Listen to magic selection changes
        mainController.getMagicListModel().selectedMagicProperty().addListener(
            (obs, oldMagic, newMagic) -> loadMagicData(newMagic));
    }
    
    private void setupSpinners() {
        // Spell power spinner
        spellPowerSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        
        // Draw resist spinner
        drawResistSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
        
        // Hit count spinner
        hitCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, 1));
        
        // Status attack spinner
        statusAttackSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 0));
    }
    
    private void setupComboBoxes() {
        // Magic ID combo box with spell names
        populateMagicIdComboBox();
        
        // Element combo box
        elementComboBox.getItems().setAll(Element.values());
        elementComboBox.setConverter(new StringConverter<Element>() {
            @Override
            public String toString(Element element) {
                return element != null ? element.getDisplayName() : "";
            }
            
            @Override
            public Element fromString(String string) {
                for (Element element : Element.values()) {
                    if (element.getDisplayName().equals(string)) {
                        return element;
                    }
                }
                return null;
            }
        });
        
        // Attack type combo box
        attackTypeComboBox.getItems().setAll(AttackType.values());
        attackTypeComboBox.setConverter(new StringConverter<AttackType>() {
            @Override
            public String toString(AttackType type) {
                return type != null ? type.getDisplayName() : "";
            }
            
            @Override
            public AttackType fromString(String string) {
                for (AttackType type : AttackType.values()) {
                    if (type.getDisplayName().equals(string)) {
                        return type;
                    }
                }
                return null;
            }
        });
    }
    
    private void populateMagicIdComboBox() {
        try {
            // Load magic ID list from use case
            var magicIdList = magicEditorUseCase.getMagicIdList();
            magicIdComboBox.getItems().addAll(magicIdList);
            logger.info("Loaded {} magic IDs from resource file", magicIdList.size());
        } catch (Exception e) {
            logger.error("Failed to load magic ID list", e);
            // Fallback to sample data
            magicIdComboBox.getItems().addAll(
                "103 - Blizzara", "104 - Blizzaga", "105 - Sleep", "106 - Blind",
                "107 - Silence", "108 - Berserk", "109 - Bio", "110 - Esuna"
            );
        }
    }
    
    private void setupChangeListeners() {
        // Add change listeners to all controls
        spellPowerSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        drawResistSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        hitCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusAttackSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        elementComboBox.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        attackTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        magicIdComboBox.valueProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Target checkboxes
        targetDeadCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        targetSingleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        targetEnemyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        targetSingleSideCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Status effect checkboxes
        statusSleepCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusHasteCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusSlowCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusStopCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusRegenCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusProtectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusShellCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusReflectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusAuraCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusCurseCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDoomCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusInvincibleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusFloatCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusPetrifyingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusConfusionCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDrainCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusEjectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDoubleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusTripleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDefendCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusChargedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusBackAttackCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusVit0CheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusAngelWingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusHasMagicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusSummonGFCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDeathCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusPoisonCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusPetrifyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusDarknessCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusSilenceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusBerserkCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        statusZombieCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        
        // Attack flag checkboxes
        attackShelledCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        attackReflectedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        attackBreakDamageLimitCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
        attackReviveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> onFieldChanged());
    }
    
    private void loadMagicData(MagicDisplayDTO magic) {
        currentMagic = magic;
        updatingFromModel = true;
        
        try {
            if (magic != null) {
                // Populate basic fields
                magicIdComboBox.setValue(magic.magicID() + " - " + magic.spellName());
                spellNameField.setText(magic.spellName());
                spellDescriptionField.setText(magic.spellDescription());
                spellPowerSpinner.getValueFactory().setValue(magic.spellPower());
                drawResistSpinner.getValueFactory().setValue(magic.drawResist());
                hitCountSpinner.getValueFactory().setValue(magic.hitCount());
                statusAttackSpinner.getValueFactory().setValue(magic.statusAttackEnabler());
                elementComboBox.setValue(magic.element());
                attackTypeComboBox.setValue(magic.attackType());
                
                // Load target info
                targetDeadCheckBox.setSelected(magic.targetInfo().dead());
                targetSingleCheckBox.setSelected(magic.targetInfo().single());
                targetEnemyCheckBox.setSelected(magic.targetInfo().enemy());
                targetSingleSideCheckBox.setSelected(magic.targetInfo().singleSide());
                
                // Load status effects
                var statusEffects = magic.activeStatusEffects();
                statusSleepCheckBox.setSelected(statusEffects.contains(StatusEffect.SLEEP));
                statusHasteCheckBox.setSelected(statusEffects.contains(StatusEffect.HASTE));
                statusSlowCheckBox.setSelected(statusEffects.contains(StatusEffect.SLOW));
                statusStopCheckBox.setSelected(statusEffects.contains(StatusEffect.STOP));
                statusRegenCheckBox.setSelected(statusEffects.contains(StatusEffect.REGEN));
                statusProtectCheckBox.setSelected(statusEffects.contains(StatusEffect.PROTECT));
                statusShellCheckBox.setSelected(statusEffects.contains(StatusEffect.SHELL));
                statusReflectCheckBox.setSelected(statusEffects.contains(StatusEffect.REFLECT));
                statusAuraCheckBox.setSelected(statusEffects.contains(StatusEffect.AURA));
                statusCurseCheckBox.setSelected(statusEffects.contains(StatusEffect.CURSE));
                statusDoomCheckBox.setSelected(statusEffects.contains(StatusEffect.DOOM));
                statusInvincibleCheckBox.setSelected(statusEffects.contains(StatusEffect.INVINCIBLE));
                statusFloatCheckBox.setSelected(statusEffects.contains(StatusEffect.FLOAT));
                statusPetrifyingCheckBox.setSelected(statusEffects.contains(StatusEffect.PETRIFYING));
                statusConfusionCheckBox.setSelected(statusEffects.contains(StatusEffect.CONFUSION));
                statusDrainCheckBox.setSelected(statusEffects.contains(StatusEffect.DRAIN));
                statusEjectCheckBox.setSelected(statusEffects.contains(StatusEffect.EJECT));
                statusDoubleCheckBox.setSelected(statusEffects.contains(StatusEffect.DOUBLE));
                statusTripleCheckBox.setSelected(statusEffects.contains(StatusEffect.TRIPLE));
                statusDefendCheckBox.setSelected(statusEffects.contains(StatusEffect.DEFEND));
                statusChargedCheckBox.setSelected(statusEffects.contains(StatusEffect.CHARGED));
                statusBackAttackCheckBox.setSelected(statusEffects.contains(StatusEffect.BACK_ATTACK));
                statusVit0CheckBox.setSelected(statusEffects.contains(StatusEffect.VIT_0));
                statusAngelWingCheckBox.setSelected(statusEffects.contains(StatusEffect.ANGEL_WING));
                statusHasMagicCheckBox.setSelected(statusEffects.contains(StatusEffect.HAS_MAGIC));
                statusSummonGFCheckBox.setSelected(statusEffects.contains(StatusEffect.SUMMON_GF));
                statusDeathCheckBox.setSelected(statusEffects.contains(StatusEffect.DEATH));
                statusPoisonCheckBox.setSelected(statusEffects.contains(StatusEffect.POISON));
                statusPetrifyCheckBox.setSelected(statusEffects.contains(StatusEffect.PETRIFY));
                statusDarknessCheckBox.setSelected(statusEffects.contains(StatusEffect.DARKNESS));
                statusSilenceCheckBox.setSelected(statusEffects.contains(StatusEffect.SILENCE));
                statusBerserkCheckBox.setSelected(statusEffects.contains(StatusEffect.BERSERK));
                statusZombieCheckBox.setSelected(statusEffects.contains(StatusEffect.ZOMBIE));
                
                // Load attack flags
                attackShelledCheckBox.setSelected(magic.attackInfo().shelled());
                attackReflectedCheckBox.setSelected(magic.attackInfo().reflected());
                attackBreakDamageLimitCheckBox.setSelected(magic.attackInfo().breakDamageLimit());
                attackReviveCheckBox.setSelected(magic.attackInfo().revive());
                
                setControlsEnabled(true);
                logger.debug("Loaded magic data: {}", magic.spellName());
            } else {
                clearFields();
                setControlsEnabled(false);
            }
        } finally {
            updatingFromModel = false;
        }
    }
    
    private void clearFields() {
        magicIdComboBox.setValue(null);
        spellNameField.clear();
        spellDescriptionField.clear();
        spellPowerSpinner.getValueFactory().setValue(0);
        drawResistSpinner.getValueFactory().setValue(0);
        hitCountSpinner.getValueFactory().setValue(1);
        statusAttackSpinner.getValueFactory().setValue(0);
        elementComboBox.setValue(null);
        attackTypeComboBox.setValue(null);
        
        // Clear all checkboxes
        clearAllCheckboxes();
    }
    
    private void clearAllCheckboxes() {
        // Target checkboxes
        targetDeadCheckBox.setSelected(false);
        targetSingleCheckBox.setSelected(false);
        targetEnemyCheckBox.setSelected(false);
        targetSingleSideCheckBox.setSelected(false);
        
        // Status checkboxes
        statusSleepCheckBox.setSelected(false);
        statusHasteCheckBox.setSelected(false);
        statusSlowCheckBox.setSelected(false);
        statusStopCheckBox.setSelected(false);
        statusRegenCheckBox.setSelected(false);
        statusProtectCheckBox.setSelected(false);
        statusShellCheckBox.setSelected(false);
        statusReflectCheckBox.setSelected(false);
        statusAuraCheckBox.setSelected(false);
        statusCurseCheckBox.setSelected(false);
        statusDoomCheckBox.setSelected(false);
        statusInvincibleCheckBox.setSelected(false);
        statusFloatCheckBox.setSelected(false);
        statusPetrifyingCheckBox.setSelected(false);
        statusConfusionCheckBox.setSelected(false);
        statusDrainCheckBox.setSelected(false);
        statusEjectCheckBox.setSelected(false);
        statusDoubleCheckBox.setSelected(false);
        statusTripleCheckBox.setSelected(false);
        statusDefendCheckBox.setSelected(false);
        statusChargedCheckBox.setSelected(false);
        statusBackAttackCheckBox.setSelected(false);
        statusVit0CheckBox.setSelected(false);
        statusAngelWingCheckBox.setSelected(false);
        statusHasMagicCheckBox.setSelected(false);
        statusSummonGFCheckBox.setSelected(false);
        statusDeathCheckBox.setSelected(false);
        statusPoisonCheckBox.setSelected(false);
        statusPetrifyCheckBox.setSelected(false);
        statusDarknessCheckBox.setSelected(false);
        statusSilenceCheckBox.setSelected(false);
        statusBerserkCheckBox.setSelected(false);
        statusZombieCheckBox.setSelected(false);
        
        // Attack flag checkboxes
        attackShelledCheckBox.setSelected(false);
        attackReflectedCheckBox.setSelected(false);
        attackBreakDamageLimitCheckBox.setSelected(false);
        attackReviveCheckBox.setSelected(false);
    }
    
    private void setControlsEnabled(boolean enabled) {
        magicIdComboBox.setDisable(!enabled);
        spellPowerSpinner.setDisable(!enabled);
        drawResistSpinner.setDisable(!enabled);
        hitCountSpinner.setDisable(!enabled);
        statusAttackSpinner.setDisable(!enabled);
        elementComboBox.setDisable(!enabled);
        attackTypeComboBox.setDisable(!enabled);
        damageFormulaButton.setDisable(!enabled);
        damageChartButton.setDisable(!enabled);
        
        // Target checkboxes
        targetDeadCheckBox.setDisable(!enabled);
        targetSingleCheckBox.setDisable(!enabled);
        targetEnemyCheckBox.setDisable(!enabled);
        targetSingleSideCheckBox.setDisable(!enabled);
        
        // Status checkboxes  
        statusSleepCheckBox.setDisable(!enabled);
        statusHasteCheckBox.setDisable(!enabled);
        statusSlowCheckBox.setDisable(!enabled);
        statusStopCheckBox.setDisable(!enabled);
        statusRegenCheckBox.setDisable(!enabled);
        statusProtectCheckBox.setDisable(!enabled);
        statusShellCheckBox.setDisable(!enabled);
        statusReflectCheckBox.setDisable(!enabled);
        statusAuraCheckBox.setDisable(!enabled);
        statusCurseCheckBox.setDisable(!enabled);
        statusDoomCheckBox.setDisable(!enabled);
        statusInvincibleCheckBox.setDisable(!enabled);
        statusFloatCheckBox.setDisable(!enabled);
        statusPetrifyingCheckBox.setDisable(!enabled);
        statusConfusionCheckBox.setDisable(!enabled);
        statusDrainCheckBox.setDisable(!enabled);
        statusEjectCheckBox.setDisable(!enabled);
        statusDoubleCheckBox.setDisable(!enabled);
        statusTripleCheckBox.setDisable(!enabled);
        statusDefendCheckBox.setDisable(!enabled);
        statusChargedCheckBox.setDisable(!enabled);
        statusBackAttackCheckBox.setDisable(!enabled);
        statusVit0CheckBox.setDisable(!enabled);
        statusAngelWingCheckBox.setDisable(!enabled);
        statusHasMagicCheckBox.setDisable(!enabled);
        statusSummonGFCheckBox.setDisable(!enabled);
        statusDeathCheckBox.setDisable(!enabled);
        statusPoisonCheckBox.setDisable(!enabled);
        statusPetrifyCheckBox.setDisable(!enabled);
        statusDarknessCheckBox.setDisable(!enabled);
        statusSilenceCheckBox.setDisable(!enabled);
        statusBerserkCheckBox.setDisable(!enabled);
        statusZombieCheckBox.setDisable(!enabled);
        
        // Attack flag checkboxes
        attackShelledCheckBox.setDisable(!enabled);
        attackReflectedCheckBox.setDisable(!enabled);
        attackBreakDamageLimitCheckBox.setDisable(!enabled);
        attackReviveCheckBox.setDisable(!enabled);
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
            logger.error("Error saving magic changes", e);
            showError("Failed to save changes", e.getMessage());
        }
    }
    
    private void validateAndSave() {
        if (elementComboBox.getValue() == null) {
            throw new IllegalArgumentException("Element must be selected");
        }
        
        if (attackTypeComboBox.getValue() == null) {
            throw new IllegalArgumentException("Attack type must be selected");
        }
        
        // Create updated magic data and save
        logger.debug("Validating and saving changes for magic");
        
        // For now, just log the changes
        logger.info("Magic updated - Power: {}, Element: {}, Draw Resist: {}", 
            spellPowerSpinner.getValue(), elementComboBox.getValue(), drawResistSpinner.getValue());
    }
    
    private void showError(String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    @FXML
    private void onDamageFormulaClick() {
        // TODO: Implement damage formula dialog
        logger.info("Damage Formula button clicked");
    }
    
    @FXML
    private void onDamageChartClick() {
        // TODO: Implement damage chart dialog
        logger.info("Damage Chart button clicked");
    }
} 