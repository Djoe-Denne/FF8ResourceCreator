package com.ff8.domain.events;

import com.ff8.application.dto.MagicDisplayDTO;
import lombok.Value;

/**
 * Event fired when magic data has been updated/modified.
 * Contains the updated magic data information.
 */
@Value
public class MagicDataChangeEvent {
    int magicIndex;
    MagicDisplayDTO updatedMagicData;
    String changeType; // "update", "create", "duplicate"
} 