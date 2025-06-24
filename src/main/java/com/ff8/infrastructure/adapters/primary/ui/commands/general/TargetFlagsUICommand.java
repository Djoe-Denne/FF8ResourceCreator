package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating target flags in the magic data.
 * Handles Boolean checkbox values for targeting options.
 */
public class TargetFlagsUICommand implements UICommand<Boolean> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final TargetFlagType flagType;
    private final String description;
    private final Integer magicIndex;
    
    public enum TargetFlagType {
        TARGET_DEAD,
        TARGET_SINGLE,
        TARGET_ENEMY,
        TARGET_SINGLE_SIDE
    }
    
    public TargetFlagsUICommand(MagicEditorUseCase magicEditorUseCase, 
                               TargetFlagType flagType,
                               Integer magicIndex) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.flagType = flagType;
        this.magicIndex = magicIndex;
        this.description = String.format("Update %s for magic %d", 
            flagType.name().toLowerCase().replace("_", " "), 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public void execute(Boolean newValue) {
        if (magicIndex == null) {
            throw new IllegalStateException("Cannot execute command: Magic ID is null");
        }
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        MagicDisplayDTO updatedMagic = null;
        var targetInfo = currentMagic.targetInfo();
        switch (flagType) {
            case TARGET_DEAD:
                targetInfo = targetInfo.withDead(newValue);
                break;
            case TARGET_SINGLE:
                targetInfo = targetInfo.withSingle(newValue);
                break;
            case TARGET_ENEMY:
                targetInfo = targetInfo.withEnemy(newValue);
                break;
            case TARGET_SINGLE_SIDE:
                targetInfo = targetInfo.withSingleSide(newValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown target flag type: " + flagType);
        }

        updatedMagic = currentMagic.withTargetInfo(targetInfo);

        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(Boolean newValue) {
        return newValue != null && magicIndex != null && magicIndex >= 0;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex != null ? magicIndex : -1;
    }
} 