package com.ff8.infrastructure.adapters.primary.ui.commands.general;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttackFlagsUICommand Tests")
class AttackFlagsUICommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private MagicDisplayDTO.AttackInfo attackInfo;
    
    @Mock
    private MagicDisplayDTO.AttackInfo updatedAttackInfo;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private AttackFlagsUICommand command;
    private final Integer magicIndex = 3;

    @BeforeEach
    void setUp() {
        command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_SHELLED, magicIndex);
        
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
        when(currentMagic.attackInfo()).thenReturn(attackInfo);
        when(currentMagic.withAttackInfo(any())).thenReturn(updatedMagic);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize command with valid parameters")
        void shouldInitializeCommandWithValidParameters() {
            // Given & When
            var cmd = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_REFLECTED, 12);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(12);
            assertThat(cmd.getDescription()).contains("Update attack reflected");
        }

        @ParameterizedTest
        @EnumSource(AttackFlagsUICommand.AttackFlagType.class)
        @DisplayName("Should create command for each attack flag type")
        void shouldCreateCommandForEachAttackFlagType(AttackFlagsUICommand.AttackFlagType flagType) {
            // Given & When
            var cmd = new AttackFlagsUICommand(magicEditorUseCase, flagType, magicIndex);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(magicIndex);
            assertThat(cmd.getDescription()).contains("Update " + flagType.name().toLowerCase().replace("_", " "));
        }
    }

    @Nested
    @DisplayName("Execute Method Tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Should update ATTACK_SHELLED flag successfully")
        void shouldUpdateAttackShelledFlagSuccessfully() throws Exception {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_SHELLED, magicIndex);
            when(attackInfo.withShelled(true)).thenReturn(updatedAttackInfo);

            // When
            command.execute(true);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(attackInfo).withShelled(true);
            verify(currentMagic).withAttackInfo(updatedAttackInfo);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update ATTACK_REFLECTED flag successfully")
        void shouldUpdateAttackReflectedFlagSuccessfully() throws Exception {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_REFLECTED, magicIndex);
            when(attackInfo.withReflected(false)).thenReturn(updatedAttackInfo);

            // When
            command.execute(false);

            // Then
            verify(attackInfo).withReflected(false);
        }

        @Test
        @DisplayName("Should update ATTACK_BREAK_DAMAGE_LIMIT flag successfully")
        void shouldUpdateAttackBreakDamageLimitFlagSuccessfully() throws Exception {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_BREAK_DAMAGE_LIMIT, magicIndex);
            when(attackInfo.withBreakDamageLimit(true)).thenReturn(updatedAttackInfo);

            // When
            command.execute(true);

            // Then
            verify(attackInfo).withBreakDamageLimit(true);
        }

        @Test
        @DisplayName("Should update ATTACK_REVIVE flag successfully")
        void shouldUpdateAttackReviveFlagSuccessfully() throws Exception {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_REVIVE, magicIndex);
            when(attackInfo.withRevive(false)).thenReturn(updatedAttackInfo);

            // When
            command.execute(false);

            // Then
            verify(attackInfo).withRevive(false);
        }

        @Test
        @DisplayName("Should throw exception when magic not found")
        void shouldThrowExceptionWhenMagicNotFound() {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> command.execute(true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Magic not found: " + magicIndex);

            verify(magicEditorUseCase, never()).updateMagicData(anyInt(), any());
        }

        @Test
        @DisplayName("Should propagate exceptions from use case")
        void shouldPropagateExceptionsFromUseCase() throws Exception {
            // Given
            when(attackInfo.withShelled(true)).thenReturn(updatedAttackInfo);
            doThrow(new RuntimeException("Update failed"))
                    .when(magicEditorUseCase).updateMagicData(eq(magicIndex), eq(updatedMagic));

            // When & Then
            assertThatThrownBy(() -> command.execute(true))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Update failed");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate true value with valid magic index")
        void shouldValidateTrueValueWithValidMagicIndex() {
            // Given & When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate false value with valid magic index")
        void shouldValidateFalseValueWithValidMagicIndex() {
            // Given & When
            boolean result = command.validate(false);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject null value")
        void shouldRejectNullValue() {
            // Given & When
            boolean result = command.validate(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject negative magic index")
        void shouldRejectNegativeMagicIndex() {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_SHELLED, -1);

            // When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null magic index")
        void shouldRejectNullMagicIndex() {
            // Given
            command = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_SHELLED, null);

            // When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full update flow for all flag types")
        void shouldCompleteFullUpdateFlowForAllFlagTypes() throws Exception {
            // Test each flag type
            for (AttackFlagsUICommand.AttackFlagType flagType : AttackFlagsUICommand.AttackFlagType.values()) {
                // Given
                var cmd = new AttackFlagsUICommand(magicEditorUseCase, flagType, magicIndex);
                when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
                
                // Set up mocks for each flag type
                switch (flagType) {
                    case ATTACK_SHELLED -> when(attackInfo.withShelled(true)).thenReturn(updatedAttackInfo);
                    case ATTACK_REFLECTED -> when(attackInfo.withReflected(true)).thenReturn(updatedAttackInfo);
                    case ATTACK_BREAK_DAMAGE_LIMIT -> when(attackInfo.withBreakDamageLimit(true)).thenReturn(updatedAttackInfo);
                    case ATTACK_REVIVE -> when(attackInfo.withRevive(true)).thenReturn(updatedAttackInfo);
                }

                // When
                cmd.execute(true);

                // Then - Verify complete flow
                verify(magicEditorUseCase, atLeastOnce()).getMagicData(magicIndex);
                verify(currentMagic, atLeastOnce()).attackInfo();
                verify(currentMagic, atLeastOnce()).withAttackInfo(updatedAttackInfo);
                verify(magicEditorUseCase, atLeastOnce()).updateMagicData(magicIndex, updatedMagic);
            }
        }

        @Test
        @DisplayName("Should handle multiple flag updates independently")
        void shouldHandleMultipleFlagUpdatesIndependently() throws Exception {
            // Given
            var shelledCmd = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_SHELLED, magicIndex);
            var reflectedCmd = new AttackFlagsUICommand(magicEditorUseCase, AttackFlagsUICommand.AttackFlagType.ATTACK_REFLECTED, magicIndex);
            
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            when(attackInfo.withShelled(true)).thenReturn(updatedAttackInfo);
            when(attackInfo.withReflected(false)).thenReturn(updatedAttackInfo);

            // When
            shelledCmd.execute(true);
            reflectedCmd.execute(false);

            // Then
            verify(attackInfo).withShelled(true);
            verify(attackInfo).withReflected(false);
            verify(magicEditorUseCase, times(2)).updateMagicData(magicIndex, updatedMagic);
        }
    }
} 