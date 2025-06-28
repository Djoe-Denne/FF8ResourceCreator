package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.IntegerFieldUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.junction.ElementalAttackJunctionUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.junction.ElementalDefenseJunctionUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.junction.JunctionStatUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.junction.StatusEffectAttackJunctionUICommand;
import com.ff8.infrastructure.adapters.primary.ui.commands.junction.StatusEffectDefenseJunctionUICommand;
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
        fireAttackRadio.setUserData(Element.FIRE);
        iceAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        iceAttackRadio.setUserData(Element.ICE);
        thunderAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        thunderAttackRadio.setUserData(Element.THUNDER);
        earthAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        earthAttackRadio.setUserData(Element.EARTH);
        poisonAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        poisonAttackRadio.setUserData(Element.POISON);
        windAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        windAttackRadio.setUserData(Element.WIND);
        waterAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        waterAttackRadio.setUserData(Element.WATER);
        holyAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        holyAttackRadio.setUserData(Element.HOLY);
        nonElementalAttackRadio.setToggleGroup(elementalAttackToggleGroup);
        nonElementalAttackRadio.setUserData(Element.NONE);
    }
    
    private void setupSliders() {
        // Setup elemental attack slider
        elementalAttackSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            elementalAttackLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldUICommand.IntegerFieldType.ELEMENTAL_ATTACK_VALUE, getCurrentMagicIndex()), newVal.intValue());
            }
        });
        
        // Setup elemental defense slider
        elementalDefenseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            elementalDefenseLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldUICommand.IntegerFieldType.ELEMENTAL_DEFENSE_VALUE, getCurrentMagicIndex()), newVal.intValue());
            }
        });
        
        // Setup status attack slider
        statusAttackSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            statusAttackLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldUICommand.IntegerFieldType.STATUS_ATTACK_VALUE, getCurrentMagicIndex()), newVal.intValue());
            }
        });
        
        // Setup status defense slider
        statusDefenseSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = newVal.intValue();
            statusDefenseLabel.setText(percentage + "%");
            if (!updatingFromModel) {
                onFieldChange(new IntegerFieldUICommand(magicEditorUseCase, IntegerFieldUICommand.IntegerFieldType.STATUS_DEFENSE_VALUE, getCurrentMagicIndex()), newVal.intValue());
            }
        });
    }
    
    private void setupChangeListeners() {
        // Junction stat spinners using command pattern
        hpJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.HP), newVal));
        strJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.STR), newVal));
        vitJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.VIT), newVal));
        magJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.MAG), newVal));
        sprJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.SPR), newVal));
        spdJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.SPD), newVal));
        evaJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.EVA), newVal));
        hitJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.HIT), newVal));
        luckJunctionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new JunctionStatUICommand(magicEditorUseCase, getCurrentMagicIndex(), JunctionStatUICommand.JunctionStatType.LUCK), newVal));
        
        // Elemental attack radio buttons
        elementalAttackToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                onFieldChange(new ElementalAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), (Element) newToggle.getToggleGroup().getSelectedToggle().getUserData());
            }
        });
        
        // Elemental defense checkboxes
        fireDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.FIRE));
        iceDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.ICE));
        thunderDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.THUNDER));
        earthDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.EARTH));
        poisonDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.POISON));
        windDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.WIND));
        waterDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.WATER));
        holyDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new ElementalDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), Element.HOLY));
        
        // Status attack checkboxes
        junctionDeathCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DEATH));
        junctionPoisonCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.POISON));
        junctionPetrifyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.PETRIFY));
        junctionDarknessCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DARKNESS));
        junctionSilenceCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SILENCE));
        junctionBerserkCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.BERSERK));
        junctionZombieCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.ZOMBIE));
        junctionSleepCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLEEP));
        junctionSlowCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLOW));
        junctionStopCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.STOP));
        junctionConfusionCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CONFUSION));
        junctionDrainCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectAttackJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DRAIN));
        
        // Status defense checkboxes
        junctionDeathDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DEATH));
        junctionPoisonDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.POISON));
        junctionPetrifyDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.PETRIFY));
        junctionDarknessDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DARKNESS));
        junctionSilenceDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SILENCE));
        junctionBerserkDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.BERSERK));
        junctionZombieDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.ZOMBIE));
        junctionSleepDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLEEP));
        junctionSlowDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.SLOW));
        junctionStopDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.STOP));
        junctionCurseDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CURSE));
        junctionConfusionDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.CONFUSION));
        junctionDrainDefenseCheck.selectedProperty().addListener((obs, oldVal, newVal) -> 
            onFieldChange(new StatusEffectDefenseJunctionUICommand(magicEditorUseCase, getCurrentMagicIndex()), StatusEffect.DRAIN));
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
                
                // Set controls enabled based on whether this is newly created magic
                setControlsForMagic(magic);
                logger.debug("Loaded junction data for magic: {} (newly created: {})", magic.spellName(), magic.isNewlyCreated());
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
        if (enabled) {
            // When enabling, we still need to respect whether magic is newly created
            if (currentMagic != null) {
                setControlsForMagic(currentMagic);
            }
        } else {
            // When disabling completely (no magic selected), disable everything
            setJunctionStatControlsEnabled(false);
            setElementalControlsEnabled(false);
            setStatusControlsEnabled(false);
        }
    }
    
    /**
     * Set control states based on whether the magic is newly created or existing.
     * Newly created magic is fully editable, existing magic is read-only.
     */
    private void setControlsForMagic(MagicDisplayDTO magic) {
        boolean isEditable = magic.isNewlyCreated();
        
        // Apply read-only styling first
        applyReadOnlyStylesToControls(isEditable);
        
        // Junction stat spinners
        setJunctionStatControlsEnabled(isEditable);
        
        // Elemental controls
        setElementalControlsEnabled(isEditable);
        
        // Status controls
        setStatusControlsEnabled(isEditable);
        
        logger.debug("Set junction controls for magic - editable: {}", isEditable);
    }
    
    /**
     * Apply visual styling to indicate read-only state
     */
    private void applyReadOnlyStylesToControls(boolean isEditable) {
        String readOnlyClass = "readonly-field";
        
        // Junction stat spinners
        applySpinnerStyling(isEditable, readOnlyClass,
            hpJunctionSpinner, strJunctionSpinner, vitJunctionSpinner, magJunctionSpinner, sprJunctionSpinner,
            spdJunctionSpinner, evaJunctionSpinner, hitJunctionSpinner, luckJunctionSpinner);
        
        // Sliders
        applySliderStyling(isEditable, readOnlyClass,
            elementalAttackSlider, elementalDefenseSlider, statusAttackSlider, statusDefenseSlider);
    }
    
    /**
     * Apply styling to spinners based on enabled state
     */
    private void applySpinnerStyling(boolean enabled, String readOnlyClass, Spinner<Integer>... spinners) {
        for (Spinner<Integer> spinner : spinners) {
            spinner.getStyleClass().remove(readOnlyClass);
            if (!enabled) {
                spinner.getStyleClass().add(readOnlyClass);
            }
        }
    }
    
    /**
     * Apply styling to sliders based on enabled state
     */
    private void applySliderStyling(boolean enabled, String readOnlyClass, Slider... sliders) {
        for (Slider slider : sliders) {
            slider.getStyleClass().remove(readOnlyClass);
            if (!enabled) {
                slider.getStyleClass().add(readOnlyClass);
            }
        }
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
    
    /**
     * Apply styling to radio buttons based on enabled state
     */
    private void applyRadioButtonStyling(boolean enabled, String readOnlyClass, RadioButton... radioButtons) {
        for (RadioButton radioButton : radioButtons) {
            radioButton.getStyleClass().remove(readOnlyClass);
            if (!enabled) {
                radioButton.getStyleClass().add(readOnlyClass);
            }
        }
    }
    
    /**
     * Enable/disable junction stat controls based on editability
     */
    private void setJunctionStatControlsEnabled(boolean enabled) {
        hpJunctionSpinner.setDisable(!enabled);
        strJunctionSpinner.setDisable(!enabled);
        vitJunctionSpinner.setDisable(!enabled);
        magJunctionSpinner.setDisable(!enabled);
        sprJunctionSpinner.setDisable(!enabled);
        spdJunctionSpinner.setDisable(!enabled);
        evaJunctionSpinner.setDisable(!enabled);
        hitJunctionSpinner.setDisable(!enabled);
        luckJunctionSpinner.setDisable(!enabled);
    }
    
    /**
     * Enable/disable elemental controls based on editability
     */
    private void setElementalControlsEnabled(boolean enabled) {
        // Elemental attack radio buttons
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
        
        // Elemental defense checkboxes
        fireDefenseCheck.setDisable(!enabled);
        iceDefenseCheck.setDisable(!enabled);
        thunderDefenseCheck.setDisable(!enabled);
        earthDefenseCheck.setDisable(!enabled);
        poisonDefenseCheck.setDisable(!enabled);
        windDefenseCheck.setDisable(!enabled);
        waterDefenseCheck.setDisable(!enabled);
        holyDefenseCheck.setDisable(!enabled);
        elementalDefenseSlider.setDisable(!enabled);
        
        // Apply styling
        String readOnlyClass = "readonly-field";
        applyRadioButtonStyling(enabled, readOnlyClass,
            fireAttackRadio, iceAttackRadio, thunderAttackRadio, earthAttackRadio, poisonAttackRadio,
            windAttackRadio, waterAttackRadio, holyAttackRadio, nonElementalAttackRadio);
        
        applyCheckboxStyling(enabled, readOnlyClass,
            fireDefenseCheck, iceDefenseCheck, thunderDefenseCheck, earthDefenseCheck,
            poisonDefenseCheck, windDefenseCheck, waterDefenseCheck, holyDefenseCheck);
    }
    
    /**
     * Enable/disable status controls based on editability
     */
    private void setStatusControlsEnabled(boolean enabled) {
        // Status attack checkboxes
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
        
        // Status defense checkboxes
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
        
        // Apply styling
        String readOnlyClass = "readonly-field";
        applyCheckboxStyling(enabled, readOnlyClass,
            junctionDeathCheck, junctionPoisonCheck, junctionPetrifyCheck, junctionDarknessCheck,
            junctionSilenceCheck, junctionBerserkCheck, junctionZombieCheck, junctionSleepCheck,
            junctionSlowCheck, junctionStopCheck, junctionConfusionCheck, junctionDrainCheck,
            junctionDeathDefenseCheck, junctionPoisonDefenseCheck, junctionPetrifyDefenseCheck,
            junctionDarknessDefenseCheck, junctionSilenceDefenseCheck, junctionBerserkDefenseCheck,
            junctionZombieDefenseCheck, junctionSleepDefenseCheck, junctionSlowDefenseCheck,
            junctionStopDefenseCheck, junctionCurseDefenseCheck, junctionConfusionDefenseCheck,
            junctionDrainDefenseCheck);
    }
    
    /**
     * Execute a UI command for Integer field changes.
     * This follows the Command Pattern for encapsulating user actions.
     */
    private <T> void onFieldChange(UICommand<T> command, T newValue) {
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
     * Get the current magic ID for command creation.
     * @return the current magic ID, or -1 if no magic is selected
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