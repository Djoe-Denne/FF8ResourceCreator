package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.SpellTranslations;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.IntegerFieldUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.IntegerFieldUICommand.IntegerFieldType;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.AttackFlagsUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.AttackTypeFieldUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.ElementFieldUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.StatusEffectAttackUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.TargetFlagsUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.AttackFlagsUICommand.AttackFlagType;
import com.ff8.infrastructure.adapters.primary.ui.commands.general.TargetFlagsUICommand.TargetFlagType;
import com.ff8.infrastructure.adapters.primary.ui.dialogs.TranslationEditorDialog;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the General tab - handles basic magic properties editing.
 */
public class GeneralTabController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GeneralTabController.class);
    
    // General section
    @FXML private Spinner<Integer> magicIdSpinner;
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
        // Magic ID spinner (0-345 based on FF8 magic limit)
        magicIdSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 345, 0));
        
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
    

    
    private void setupChangeListeners() {
        // Integer field spinners using command pattern
        magicIdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldType.MAGIC_ID, getCurrentMagicIndex()), newVal));
        spellPowerSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldType.SPELL_POWER, getCurrentMagicIndex()), newVal));
        drawResistSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldType.DRAW_RESIST, getCurrentMagicIndex()), newVal));
        hitCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldType.HIT_COUNT, getCurrentMagicIndex()), newVal));
        statusAttackSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldType.STATUS_ATTACK, getCurrentMagicIndex()), newVal));
        
        // Element and enum fields using command pattern
        elementComboBox.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementFieldUICommand(magicEditorUseCase, getCurrentMagicIndex()), newVal));
        attackTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new AttackTypeFieldUICommand(magicEditorUseCase, getCurrentMagicIndex()), newVal));
        
        // Target checkboxes using command pattern
        targetDeadCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new TargetFlagsUICommand(magicEditorUseCase, TargetFlagType.TARGET_DEAD, getCurrentMagicIndex()), newVal));
        targetSingleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new TargetFlagsUICommand(magicEditorUseCase, TargetFlagType.TARGET_SINGLE, getCurrentMagicIndex()), newVal));
        targetEnemyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new TargetFlagsUICommand(magicEditorUseCase, TargetFlagType.TARGET_ENEMY, getCurrentMagicIndex()), newVal));
        targetSingleSideCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new TargetFlagsUICommand(magicEditorUseCase, TargetFlagType.TARGET_SINGLE_SIDE, getCurrentMagicIndex()), newVal));
        
        // Status effect checkboxes using command pattern
        statusSleepCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLEEP));
        statusHasteCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.HASTE));
        statusSlowCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLOW));
        statusStopCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.STOP));
        statusRegenCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.REGEN));
        statusProtectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.PROTECT));
        statusShellCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SHELL));
        statusReflectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.REFLECT));
        statusAuraCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.AURA));
        statusCurseCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CURSE));
        statusDoomCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DOOM));
        statusInvincibleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.INVINCIBLE));
        statusFloatCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.FLOAT));
        statusPetrifyingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.PETRIFYING));
        statusConfusionCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CONFUSION));
        statusDrainCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DRAIN));
        statusEjectCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.EJECT));
        statusDoubleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DOUBLE));
        statusTripleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.TRIPLE));
        statusDefendCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DEFEND));
        statusChargedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CHARGED));
        statusBackAttackCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.BACK_ATTACK));
        statusVit0CheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.VIT_0));
        statusAngelWingCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.ANGEL_WING));
        statusHasMagicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.HAS_MAGIC));
        statusSummonGFCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SUMMON_GF));
        statusDeathCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DEATH));
        statusPoisonCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.POISON));
        statusPetrifyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.PETRIFY));
        statusDarknessCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DARKNESS));
        statusSilenceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SILENCE));
        statusBerserkCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.BERSERK));
        statusZombieCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.ZOMBIE));
        
        // Attack flag checkboxes using command pattern
        attackShelledCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new AttackFlagsUICommand(magicEditorUseCase, AttackFlagType.ATTACK_SHELLED, getCurrentMagicIndex()), newVal));
        attackReflectedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new AttackFlagsUICommand(magicEditorUseCase, AttackFlagType.ATTACK_REFLECTED, getCurrentMagicIndex()), newVal));
        attackBreakDamageLimitCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new AttackFlagsUICommand(magicEditorUseCase, AttackFlagType.ATTACK_BREAK_DAMAGE_LIMIT, getCurrentMagicIndex()), newVal));
        attackReviveCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new AttackFlagsUICommand(magicEditorUseCase, AttackFlagType.ATTACK_REVIVE, getCurrentMagicIndex()), newVal));
    }
    
    private void loadMagicData(MagicDisplayDTO magic) {
        currentMagic = magic;
        updatingFromModel = true;
        
        try {
            if (magic != null) {
                // Populate basic fields
                magicIdSpinner.getValueFactory().setValue(magic.magicID());
                
                // Display current spell name and description (from translations or extracted data)
                if (magic.translations() != null) {
                    spellNameField.setText(magic.translations().getEnglishName());
                    spellDescriptionField.setText(magic.translations().getEnglishDescription());
                } else {
                    spellNameField.setText(magic.spellName());
                    spellDescriptionField.setText(magic.spellDescription());
                }
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
                
                // Set controls enabled based on whether this is newly created magic
                setControlsForMagic(magic);
                logger.debug("Loaded magic data: {} (newly created: {})", magic.spellName(), magic.isNewlyCreated());
            } else {
                clearFields();
                setControlsEnabled(false);
            }
        } finally {
            updatingFromModel = false;
        }
    }
    
    /**
     * Set control states based on whether the magic is newly created or existing.
     * Newly created magic is fully editable, existing magic is read-only.
     */
    private void setControlsForMagic(MagicDisplayDTO magic) {
        boolean isEditable = magic.isNewlyCreated();
        
        // Basic controls - Magic ID is always editable (can change IDs)
        magicIdSpinner.setDisable(false); // Always allow ID changes
        elementComboBox.setDisable(!isEditable);
        attackTypeComboBox.setDisable(!isEditable);
        
        // Spinners
        spellPowerSpinner.setDisable(!isEditable);
        drawResistSpinner.setDisable(!isEditable);
        hitCountSpinner.setDisable(!isEditable);
        statusAttackSpinner.setDisable(!isEditable);
        
        // Buttons
        damageFormulaButton.setDisable(!isEditable);
        damageChartButton.setDisable(!isEditable);
        
        // Apply read-only styling
        applyReadOnlyStylesToControls(isEditable);
        
        // Target checkboxes
        setTargetControlsEnabled(isEditable);
        
        // Status checkboxes  
        setStatusControlsEnabled(isEditable);
        
        // Attack flag checkboxes
        setAttackFlagControlsEnabled(isEditable);
        
        logger.debug("Set controls for magic - editable: {}", isEditable);
    }
    
    /**
     * Apply visual styling to indicate read-only state
     */
    private void applyReadOnlyStylesToControls(boolean isEditable) {
        String readOnlyClass = "readonly-field";
        
        // Remove existing readonly class first
        elementComboBox.getStyleClass().remove(readOnlyClass);
        attackTypeComboBox.getStyleClass().remove(readOnlyClass);
        spellPowerSpinner.getStyleClass().remove(readOnlyClass);
        drawResistSpinner.getStyleClass().remove(readOnlyClass);
        hitCountSpinner.getStyleClass().remove(readOnlyClass);
        statusAttackSpinner.getStyleClass().remove(readOnlyClass);
        
        // Add readonly class if not editable
        if (!isEditable) {
            elementComboBox.getStyleClass().add(readOnlyClass);
            attackTypeComboBox.getStyleClass().add(readOnlyClass);
            spellPowerSpinner.getStyleClass().add(readOnlyClass);
            drawResistSpinner.getStyleClass().add(readOnlyClass);
            hitCountSpinner.getStyleClass().add(readOnlyClass);
            statusAttackSpinner.getStyleClass().add(readOnlyClass);
        }
    }
    
    /**
     * Enable/disable target controls based on editability
     */
    private void setTargetControlsEnabled(boolean enabled) {
        targetDeadCheckBox.setDisable(!enabled);
        targetSingleCheckBox.setDisable(!enabled);
        targetEnemyCheckBox.setDisable(!enabled);
        targetSingleSideCheckBox.setDisable(!enabled);
        targetUnknown1CheckBox.setDisable(!enabled);
        targetUnknown2CheckBox.setDisable(!enabled);
        targetUnknown3CheckBox.setDisable(!enabled);
        targetUnknown4CheckBox.setDisable(!enabled);
        
        // Apply styling
        String readOnlyClass = "readonly-field";
        applyCheckboxStyling(enabled, readOnlyClass, 
            targetDeadCheckBox, targetSingleCheckBox, targetEnemyCheckBox, targetSingleSideCheckBox,
            targetUnknown1CheckBox, targetUnknown2CheckBox, targetUnknown3CheckBox, targetUnknown4CheckBox);
    }
    
    /**
     * Enable/disable status effect controls based on editability
     */
    private void setStatusControlsEnabled(boolean enabled) {
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
        statusUnknown1CheckBox.setDisable(!enabled);
        statusUnknown2CheckBox.setDisable(!enabled);
        statusUnknown3CheckBox.setDisable(!enabled);
        statusUnknown4CheckBox.setDisable(!enabled);
        statusUnknown5CheckBox.setDisable(!enabled);
        statusUnknown6CheckBox.setDisable(!enabled);
        statusUnknown7CheckBox.setDisable(!enabled);
        
        // Apply styling
        String readOnlyClass = "readonly-field";
        applyCheckboxStyling(enabled, readOnlyClass,
            statusSleepCheckBox, statusHasteCheckBox, statusSlowCheckBox, statusStopCheckBox, statusRegenCheckBox,
            statusProtectCheckBox, statusShellCheckBox, statusReflectCheckBox, statusAuraCheckBox, statusCurseCheckBox,
            statusDoomCheckBox, statusInvincibleCheckBox, statusFloatCheckBox, statusPetrifyingCheckBox, statusConfusionCheckBox,
            statusDrainCheckBox, statusEjectCheckBox, statusDoubleCheckBox, statusTripleCheckBox, statusDefendCheckBox,
            statusChargedCheckBox, statusBackAttackCheckBox, statusVit0CheckBox, statusAngelWingCheckBox,
            statusHasMagicCheckBox, statusSummonGFCheckBox, statusDeathCheckBox, statusPoisonCheckBox, statusPetrifyCheckBox,
            statusDarknessCheckBox, statusSilenceCheckBox, statusBerserkCheckBox, statusZombieCheckBox,
            statusUnknown1CheckBox, statusUnknown2CheckBox, statusUnknown3CheckBox, statusUnknown4CheckBox,
            statusUnknown5CheckBox, statusUnknown6CheckBox, statusUnknown7CheckBox);
    }
    
    /**
     * Enable/disable attack flag controls based on editability
     */
    private void setAttackFlagControlsEnabled(boolean enabled) {
        attackShelledCheckBox.setDisable(!enabled);
        attackReflectedCheckBox.setDisable(!enabled);
        attackBreakDamageLimitCheckBox.setDisable(!enabled);
        attackReviveCheckBox.setDisable(!enabled);
        attackUnknown1CheckBox.setDisable(!enabled);
        attackUnknown2CheckBox.setDisable(!enabled);
        attackUnknown3CheckBox.setDisable(!enabled);
        attackUnknown4CheckBox.setDisable(!enabled);
        
        // Apply styling
        String readOnlyClass = "readonly-field";
        applyCheckboxStyling(enabled, readOnlyClass,
            attackShelledCheckBox, attackReflectedCheckBox, attackBreakDamageLimitCheckBox, attackReviveCheckBox,
            attackUnknown1CheckBox, attackUnknown2CheckBox, attackUnknown3CheckBox, attackUnknown4CheckBox);
    }
    
    /**
     * Apply styling to checkboxes based on enabled state
     */
    private void applyCheckboxStyling(boolean enabled, String readOnlyClass, CheckBox... checkboxes) {
        for (CheckBox checkbox : checkboxes) {
            checkbox.getStyleClass().remove(readOnlyClass);
            if (!enabled) {
                checkbox.getStyleClass().add(readOnlyClass);
            }
        }
    }
    
    private void clearFields() {
        magicIdSpinner.getValueFactory().setValue(0);
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
        magicIdSpinner.setDisable(!enabled);
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
    
    /**
     * Execute a UI command for Integer field changes.
     * This follows the Command Pattern for encapsulating user actions.
     */
    private void onFieldChange(UICommand<Integer> command, Integer newValue) {
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
            logger.debug("Executing command: {} with value: {}", command.getDescription(), newValue);
            command.execute(newValue);
            
        } catch (Exception e) {
            logger.error("Error executing command: {} with value: {}", command.getDescription(), newValue, e);
            showError("Failed to save changes", e.getMessage());
        }
    }
    
    /**
     * Execute a UI command for Element field changes.
     * This follows the Command Pattern for encapsulating user actions.
     */
    private void onFieldChange(UICommand<Element> command, Element newValue) {
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
            logger.debug("Executing command: {} with value: {}", command.getDescription(), newValue);
            command.execute(newValue);
            
        } catch (Exception e) {
            logger.error("Error executing command: {} with value: {}", command.getDescription(), newValue, e);
            showError("Failed to save changes", e.getMessage());
        }
    }
    
    /**
     * Execute a UI command for Boolean field changes.
     * This follows the Command Pattern for encapsulating user actions.
     */
    private void onFieldChange(UICommand<Boolean> command, Boolean newValue) {
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
            logger.debug("Executing command: {} with value: {}", command.getDescription(), newValue);
            command.execute(newValue);
            
        } catch (Exception e) {
            logger.error("Error executing command: {} with value: {}", command.getDescription(), newValue, e);
            showError("Failed to save changes", e.getMessage());
        }
    }
    
    /**
     * Execute a UI command for Enum field changes (AttackType).
     * This follows the Command Pattern for encapsulating user actions.
     */
    private <T extends Enum<T>> void onFieldChange(UICommand<T> command, T newValue) {
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
            logger.debug("Executing command: {} with value: {}", command.getDescription(), newValue);
            command.execute(newValue);
            
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
    
    /**
     * Handle click on spell name field to open translation editor
     */
    @FXML
    private void onSpellNameClicked(MouseEvent event) {
        if (currentMagic == null) {
            return;
        }
        
        try {
            Stage stage = (Stage) spellNameField.getScene().getWindow();
            SpellTranslations currentTranslations = currentMagic.translations() != null 
                ? currentMagic.translations() 
                : new SpellTranslations(currentMagic.spellName(), currentMagic.spellDescription());
            
            Optional<SpellTranslations> result = TranslationEditorDialog.showDialog(
                stage, currentTranslations, "Spell Name");
            
            if (result.isPresent()) {
                updateMagicTranslations(result.get());
                logger.debug("Updated spell name translations");
            }
            
        } catch (Exception e) {
            logger.error("Error opening spell name translation editor", e);
            showError("Translation Editor Error", "Failed to open translation editor: " + e.getMessage());
        }
    }
    
    /**
     * Handle click on spell description field to open translation editor
     */
    @FXML
    private void onSpellDescriptionClicked(MouseEvent event) {
        if (currentMagic == null) {
            return;
        }
        
        try {
            Stage stage = (Stage) spellDescriptionField.getScene().getWindow();
            SpellTranslations currentTranslations = currentMagic.translations() != null 
                ? currentMagic.translations() 
                : new SpellTranslations(currentMagic.spellName(), currentMagic.spellDescription());
            
            Optional<SpellTranslations> result = TranslationEditorDialog.showDialog(
                stage, currentTranslations, "Spell Description");
            
            if (result.isPresent()) {
                updateMagicTranslations(result.get());
                logger.debug("Updated spell description translations");
            }
            
        } catch (Exception e) {
            logger.error("Error opening spell description translation editor", e);
            showError("Translation Editor Error", "Failed to open translation editor: " + e.getMessage());
        }
    }
    
    /**
     * Update the magic translations and refresh the UI
     */
    private void updateMagicTranslations(SpellTranslations newTranslations) {
        try {
            // Update the magic data through the use case
            magicEditorUseCase.updateMagicTranslations(getCurrentMagicIndex(), newTranslations);
            
            // Update the current DTO to reflect the changes
            if (currentMagic != null) {
                MagicDisplayDTO updatedMagic = currentMagic.withTranslations(newTranslations);
                
                // Update the display fields
                spellNameField.setText(newTranslations.getEnglishName());
                spellDescriptionField.setText(newTranslations.getEnglishDescription());
                
                logger.debug("Successfully updated translations for magic index: {}", getCurrentMagicIndex());
            }
            
        } catch (Exception e) {
            logger.error("Error updating magic translations", e);
            showError("Translation Update Error", "Failed to update translations: " + e.getMessage());
        }
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