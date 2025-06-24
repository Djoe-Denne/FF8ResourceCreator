package com.ff8.infrastructure.adapters.primary.ui.commands.junction;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for updating junction stat values.
 * Encapsulates the user action of changing a junction stat
 * and coordinates its execution through the domain layer.
 */
public class JunctionStatUICommand implements UICommand<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(JunctionStatUICommand.class);
    
    public enum JunctionStatType {
        HP, STR, VIT, MAG, SPR, SPD, EVA, HIT, LUCK
    }
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final int magicIndex;
    private final JunctionStatType statType;
    
    public JunctionStatUICommand(MagicEditorUseCase magicEditorUseCase, int magicIndex, JunctionStatType statType) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.statType = statType;
    }
    
    @Override
    public void execute(Integer newValue) throws Exception {
        logger.debug("Executing junction stat update: {} = {} for magic ID {}", statType, newValue, magicIndex);
        
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        
        // Update the specific junction stat
        var currentStats = currentMagic.junctionStats();
        var updatedStats = switch (statType) {
            case HP -> currentStats.withHp(newValue);
            case STR -> currentStats.withStr(newValue);
            case VIT -> currentStats.withVit(newValue);
            case MAG -> currentStats.withMag(newValue);
            case SPR -> currentStats.withSpr(newValue);
            case SPD -> currentStats.withSpd(newValue);
            case EVA -> currentStats.withEva(newValue);
            case HIT -> currentStats.withHit(newValue);
            case LUCK -> currentStats.withLuck(newValue);
        };
        
        // Create updated magic DTO with new junction stats
        var updatedMagic = currentMagic.withJunctionStats(updatedStats);
        
        // Save through use case
        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
        
        logger.info("Updated junction {} to {} for magic ID {}", statType, newValue, magicIndex);
    }
    
    @Override
    public boolean validate(Integer newValue) {
        if (newValue == null) {
            return false;
        }
        
        // Junction stats are typically between 0 and 255
        return newValue >= 0 && newValue <= 255;
    }
    
    @Override
    public String getDescription() {
        return String.format("Update junction %s for magic ID %d", statType, magicIndex);
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex;
    }
} 