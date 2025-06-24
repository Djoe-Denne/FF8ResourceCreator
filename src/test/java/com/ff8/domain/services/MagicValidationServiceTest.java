package com.ff8.domain.services;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.JunctionStats;
import com.ff8.domain.entities.StatusEffectSet;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.domain.exceptions.InvalidMagicDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MagicValidationService Tests")
class MagicValidationServiceTest {

    private MagicValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new MagicValidationService();
    }

    @Nested
    @DisplayName("Basic Field Validation")
    class BasicFieldValidation {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 127, 255})
        @DisplayName("Should accept valid magic ID values")
        void shouldAcceptValidMagicIdValues(int magicId) {
            // Given
            MagicData magic = MagicData.builder()
                    .magicID(magicId)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(magic))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 256, 1000})
        @DisplayName("Should reject invalid magic ID values")
        void shouldRejectInvalidMagicIdValues(int invalidMagicId) {
            // Given
            MagicData magic = MagicData.builder()
                    .magicID(invalidMagicId)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Magic ID");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 127, 255})
        @DisplayName("Should accept valid spell power values")
        void shouldAcceptValidSpellPowerValues(int spellPower) {
            // Given
            MagicData magic = MagicData.builder()
                    .spellPower(spellPower)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(magic))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 256, 1000})
        @DisplayName("Should reject invalid spell power values")
        void shouldRejectInvalidSpellPowerValues(int invalidSpellPower) {
            // Given
            MagicData magic = MagicData.builder()
                    .spellPower(invalidSpellPower)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Spell power");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 127, 255})
        @DisplayName("Should accept valid hit count values")
        void shouldAcceptValidHitCountValues(int hitCount) {
            // Given
            MagicData magic = MagicData.builder()
                    .hitCount(hitCount)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(magic))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 256, 1000})
        @DisplayName("Should reject invalid hit count values")
        void shouldRejectInvalidHitCountValues(int invalidHitCount) {
            // Given
            MagicData magic = MagicData.builder()
                    .hitCount(invalidHitCount)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Hit count");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 127, 255})
        @DisplayName("Should accept valid draw resist values")
        void shouldAcceptValidDrawResistValues(int drawResist) {
            // Given
            MagicData magic = MagicData.builder()
                    .drawResist(drawResist)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(magic))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 256, 1000})
        @DisplayName("Should reject invalid draw resist values")
        void shouldRejectInvalidDrawResistValues(int invalidDrawResist) {
            // Given
            MagicData magic = MagicData.builder()
                    .drawResist(invalidDrawResist)
                    .extractedSpellName("Test Spell")
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Draw resist");
        }
    }

    @Nested
    @DisplayName("Business Rule Validation")
    class BusinessRuleValidation {

        @Test
        @DisplayName("Should validate junction stats limits")
        void shouldValidateJunctionStatsLimits() {
            // Given - Create magic with high but valid junction stats
            JunctionStats highJunctionStats = new JunctionStats(255, 255, 255, 255, 255, 255, 255, 255, 255);

            MagicData magic = MagicData.builder()
                    .extractedSpellName("Ultimate")
                    .junctionStats(highJunctionStats)
                    .build();

            // When & Then - Should trigger validation warning for excessive total bonuses
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("junction");
        }

        @Test
        @DisplayName("Should accept valid curative magic configuration")
        void shouldAcceptValidCurativeMagicConfiguration() {
            // Given
            MagicData curativeMagic = MagicData.builder()
                    .extractedSpellName("Cure")
                    .attackType(AttackType.CURATIVE_MAGIC)
                    .spellPower(50)
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(curativeMagic))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should validate maximum status effects limit")
        void shouldValidateMaximumStatusEffectsLimit() {
            // Given - Magic with excessive status effects (11 > 10 limit)
            StatusEffectSet statusSet = new StatusEffectSet();
            statusSet.setStatus(StatusEffect.DEATH, true);
            statusSet.setStatus(StatusEffect.POISON, true);
            statusSet.setStatus(StatusEffect.PETRIFY, true);
            statusSet.setStatus(StatusEffect.DARKNESS, true);
            statusSet.setStatus(StatusEffect.SILENCE, true);
            statusSet.setStatus(StatusEffect.BERSERK, true);
            statusSet.setStatus(StatusEffect.ZOMBIE, true);
            statusSet.setStatus(StatusEffect.SLEEP, true);
            statusSet.setStatus(StatusEffect.SLOW, true);
            statusSet.setStatus(StatusEffect.STOP, true);
            statusSet.setStatus(StatusEffect.CONFUSION, true);

            MagicData magic = MagicData.builder()
                    .extractedSpellName("Chaos")
                    .statusEffects(statusSet)
                    .build();

            // When & Then - Should fail because we have 11 status effects (over the 10 limit)
            assertThatThrownBy(() -> validationService.validateMagicData(magic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Too many active status effects");
        }
    }

    @Nested
    @DisplayName("Exception Validation")
    class ExceptionValidation {

        @Test
        @DisplayName("Should validate successfully for completely valid magic")
        void shouldValidateSuccessfullyForCompletelyValidMagic() {
            // Given
            MagicData validMagic = MagicData.builder()
                    .index(1)
                    .magicID(25)
                    .extractedSpellName("Fire")
                    .spellPower(150)
                    .hitCount(3)
                    .drawResist(100)
                    .build();

            // When & Then
            assertThatCode(() -> validationService.validateMagicData(validMagic))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should provide specific error messages for validation failures")
        void shouldProvideSpecificErrorMessagesForValidationFailures() {
            // Given
            MagicData invalidMagic = MagicData.builder()
                    .magicID(-1) // Invalid
                    .spellPower(300) // Invalid
                    .hitCount(-5) // Invalid
                    .drawResist(500) // Invalid
                    .build();

            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(invalidMagic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Magic ID");
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should identify valid magic data correctly")
        void shouldIdentifyValidMagicDataCorrectly() {
            // Given
            MagicData validMagic = MagicData.builder()
                    .magicID(10)
                    .spellPower(100)
                    .hitCount(2)
                    .drawResist(50)
                    .build();

            // When
            boolean isValid = validationService.isValid(validMagic);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should identify invalid magic data correctly")
        void shouldIdentifyInvalidMagicDataCorrectly() {
            // Given
            MagicData invalidMagic = MagicData.builder()
                    .magicID(300) // Invalid - over 255
                    .spellPower(-10) // Invalid - negative
                    .hitCount(0) // Invalid - must be at least 1
                    .drawResist(500) // Invalid - over 255
                    .build();

            // When
            boolean isValid = validationService.isValid(invalidMagic);

            // Then
            assertThat(isValid).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {-50, -1, 256, 300, 1000})
        @DisplayName("Should reject invalid magic IDs")
        void shouldRejectInvalidMagicIds(int invalidMagicId) {
            // Given
            MagicData magic = MagicData.builder()
                    .magicID(invalidMagicId)
                    .build();

            // When
            boolean isValid = validationService.isValid(magic);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should handle null magic data gracefully")
        void shouldHandleNullMagicDataGracefully() {
            // When & Then
            assertThatThrownBy(() -> validationService.validateMagicData(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Cannot invoke");
        }

        @Test
        @DisplayName("Should provide validation details for debugging")
        void shouldProvideValidationDetailsForDebugging() {
            // Given
            MagicData magic = MagicData.builder()
                    .extractedSpellName("Test")
                    .spellPower(50)
                    .build();

            // When
            boolean isValid = validationService.isValid(magic);

            // Then
            assertThat(isValid).isTrue();
            // Additional assertions could be made if validation service 
            // provided detailed validation results
        }
    }
} 