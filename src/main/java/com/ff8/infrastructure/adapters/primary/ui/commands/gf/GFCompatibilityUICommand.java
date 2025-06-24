package com.ff8.infrastructure.adapters.primary.ui.commands.gf;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.GF;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for updating GF compatibility values.
 * Encapsulates the user action of changing a GF compatibility value
 * and coordinates its execution through the domain layer.
 */
public class GFCompatibilityUICommand implements UICommand<Double> {
    private static final Logger logger = LoggerFactory.getLogger(GFCompatibilityUICommand.class);
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final int magicIndex;
    private final GF gf;
    
    public GFCompatibilityUICommand(MagicEditorUseCase magicEditorUseCase, int magicIndex, GF gf) {
        this.magicEditorUseCase = magicEditorUseCase;
        this.magicIndex = magicIndex;
        this.gf = gf;
    }
    
    @Override
    public void execute(Double newValue) throws Exception {
        logger.debug("Executing GF compatibility update: {} = {} for magic ID {}", gf, newValue, magicIndex);
        
        // Get current magic data
        var currentMagic = magicEditorUseCase.getMagicData(magicIndex)
                .orElseThrow(() -> new IllegalStateException("Magic not found: " + magicIndex));
        
        // Convert double value (UI) to raw integer value (domain)
        // UI shows -10.0 to +10.0, domain stores as display value 0-20
        int displayValue = (int) Math.round(Math.max(0, newValue * 10.0));
        
        // Update the specific GF compatibility using display value
        var updatedCompatibility = currentMagic.gfCompatibility().withDisplayValue(gf, displayValue);
        
        // Create updated magic DTO with new compatibility
        var updatedMagic = currentMagic.withGfCompatibility(updatedCompatibility);
        
        // Save through use case
        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
        
        logger.info("Updated {} compatibility to {} (display: {}) for magic ID {}", gf, newValue, displayValue, magicIndex);
    }
    
    @Override
    public boolean validate(Double newValue) {
        if (newValue == null) {
            return false;
        }
        
        // GF compatibility values are typically between -10.0 and +10.0
        return newValue >= -10.0 && newValue <= 10.0;
    }
    
    @Override
    public String getDescription() {
        return String.format("Update %s compatibility for magic ID %d", gf, magicIndex);
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex;
    }
} 