package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.infrastructure.adapters.primary.ui.commands.AbstractFieldCommand;
import com.ff8.application.dto.MagicDisplayDTO;

/**
 * Command for updating attack flags in the magic data.
 * Handles Boolean checkbox values for attack options.
 */
public class AttackFlagsUICommand extends AbstractFieldCommand<Boolean, AttackFlagsUICommand.AttackFlagType> {
    
    public enum AttackFlagType {
        ATTACK_SHELLED,
        ATTACK_REFLECTED,
        ATTACK_BREAK_DAMAGE_LIMIT,
        ATTACK_REVIVE
    }
    
    public AttackFlagsUICommand(MagicEditorUseCase magicEditorUseCase, 
                               AttackFlagType flagType,
                               Integer magicIndex) {
        super(magicEditorUseCase, magicIndex, flagType);
    }
    
    @Override
    protected MagicDisplayDTO updateSpecificField(MagicDisplayDTO currentMagic, Boolean newValue, AttackFlagType fieldType) {
        var attackInfo = currentMagic.attackInfo();
        
        switch (fieldType) {
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
                throw new IllegalArgumentException("Unknown attack flag type: " + fieldType);
        }

        return currentMagic.withAttackInfo(attackInfo);
    }
} 