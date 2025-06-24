package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.infrastructure.adapters.primary.ui.commands.UICommand;
import com.ff8.application.dto.MagicDisplayDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Command for updating attack flags in the magic data.
 * Handles Boolean checkbox values for attack options.
 */
@RequiredArgsConstructor
@Getter
public class AttackFlagsUICommand implements UICommand<Boolean> {
    
    private final MagicEditorUseCase magicEditorUseCase;
    private final AttackFlagType flagType;
    private final Integer magicIndex;
    
    public enum AttackFlagType {
        ATTACK_SHELLED,
        ATTACK_REFLECTED,
        ATTACK_BREAK_DAMAGE_LIMIT,
        ATTACK_REVIVE
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
        var attackInfo = currentMagic.attackInfo();
        switch (flagType) {
            case ATTACK_SHELLED:
                attackInfo = attackInfo.withShelled(newValue);
                break;
            case ATTACK_REFLECTED:
                attackInfo = attackInfo.withReflected(newValue);
                break;
            case ATTACK_BREAK_DAMAGE_LIMIT:
                attackInfo = attackInfo.withBreakDamageLimit(newValue);
                break;
            case ATTACK_REVIVE:
                attackInfo = attackInfo.withRevive(newValue);
                break;
            default:
                throw new IllegalArgumentException("Unknown attack flag type: " + flagType);
        }

        updatedMagic = currentMagic.withAttackInfo(attackInfo);

        magicEditorUseCase.updateMagicData(magicIndex, updatedMagic);
    }
    
    @Override
    public boolean validate(Boolean newValue) {
        return newValue != null && magicIndex != null && magicIndex >= 0;
    }
    
    @Override
    public String getDescription() {
        return String.format("Update %s for magic %d", 
            flagType.name().toLowerCase().replace("_", " "), 
            magicIndex != null ? magicIndex : 0);
    }
    
    @Override
    public int getMagicIndex() {
        return magicIndex != null ? magicIndex : -1;
    }
} 