package com.ff8.domain.events;

import com.ff8.application.dto.MagicDisplayDTO;

import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

/**
 * Event fired when magic data has been updated/modified.
 * Contains the updated magic data information.
 */
@Data
@EqualsAndHashCode(of = {"magicIndex", "updatedMagicData", "changeType"})
public class MagicDataChangeEvent {
    private final int magicIndex;
    private final MagicDisplayDTO updatedMagicData;
    private final String changeType; // "update", "create", "duplicate"
} 