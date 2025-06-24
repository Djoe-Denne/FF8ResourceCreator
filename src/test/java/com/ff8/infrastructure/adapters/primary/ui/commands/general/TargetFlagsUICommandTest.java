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
@DisplayName("TargetFlagsUICommand Tests")
class TargetFlagsUICommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private MagicDisplayDTO.TargetInfo targetInfo;
    
    @Mock
    private MagicDisplayDTO.TargetInfo updatedTargetInfo;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private TargetFlagsUICommand command;
    private final Integer magicIndex = 7;

    @BeforeEach
    void setUp() {
        command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_SINGLE, magicIndex);
        
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
        when(currentMagic.targetInfo()).thenReturn(targetInfo);
        when(currentMagic.withTargetInfo(any())).thenReturn(updatedMagic);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize command with valid parameters")
        void shouldInitializeCommandWithValidParameters() {
            // Given & When
            var cmd = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_ENEMY, 15);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(15);
            assertThat(cmd.getDescription()).contains("Update target enemy for magic 15");
        }

        @ParameterizedTest
        @EnumSource(TargetFlagsUICommand.TargetFlagType.class)
        @DisplayName("Should create command for each target flag type")
        void shouldCreateCommandForEachTargetFlagType(TargetFlagsUICommand.TargetFlagType flagType) {
            // Given & When
            var cmd = new TargetFlagsUICommand(magicEditorUseCase, flagType, magicIndex);

            // Then
            assertThat(cmd.getDescription()).contains("Update " + flagType.name().toLowerCase().replace("_", " "));
            assertThat(cmd.getMagicIndex()).isEqualTo(magicIndex);
        }

        @Test
        @DisplayName("Should handle null magic index in description")
        void shouldHandleNullMagicIndexInDescription() {
            // Given & When
            var cmd = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, null);

            // Then
            assertThat(cmd.getDescription()).contains("for magic 0");
            assertThat(cmd.getMagicIndex()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Execute Method Tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Should update TARGET_DEAD flag successfully")
        void shouldUpdateTargetDeadFlagSuccessfully() throws Exception {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, magicIndex);
            when(targetInfo.withDead(true)).thenReturn(updatedTargetInfo);

            // When
            command.execute(true);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(targetInfo).withDead(true);
            verify(currentMagic).withTargetInfo(updatedTargetInfo);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update TARGET_SINGLE flag successfully")
        void shouldUpdateTargetSingleFlagSuccessfully() throws Exception {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_SINGLE, magicIndex);
            when(targetInfo.withSingle(false)).thenReturn(updatedTargetInfo);

            // When
            command.execute(false);

            // Then
            verify(targetInfo).withSingle(false);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update TARGET_ENEMY flag successfully")
        void shouldUpdateTargetEnemyFlagSuccessfully() throws Exception {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_ENEMY, magicIndex);
            when(targetInfo.withEnemy(true)).thenReturn(updatedTargetInfo);

            // When
            command.execute(true);

            // Then
            verify(targetInfo).withEnemy(true);
        }

        @Test
        @DisplayName("Should update TARGET_SINGLE_SIDE flag successfully")
        void shouldUpdateTargetSingleSideFlagSuccessfully() throws Exception {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_SINGLE_SIDE, magicIndex);
            when(targetInfo.withSingleSide(false)).thenReturn(updatedTargetInfo);

            // When
            command.execute(false);

            // Then
            verify(targetInfo).withSingleSide(false);
        }

        @Test
        @DisplayName("Should throw exception when magic index is null")
        void shouldThrowExceptionWhenMagicIndexIsNull() {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, null);

            // When & Then
            assertThatThrownBy(() -> command.execute(true))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot execute command: Magic ID is null");

            verify(magicEditorUseCase, never()).getMagicData(anyInt());
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
            when(targetInfo.withSingle(true)).thenReturn(updatedTargetInfo);
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
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, -1);

            // When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null magic index")
        void shouldRejectNullMagicIndex() {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, null);

            // When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should validate zero magic index")
        void shouldValidateZeroMagicIndex() {
            // Given
            command = new TargetFlagsUICommand(magicEditorUseCase, TargetFlagsUICommand.TargetFlagType.TARGET_DEAD, 0);

            // When
            boolean result = command.validate(true);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @ParameterizedTest
        @EnumSource(TargetFlagsUICommand.TargetFlagType.class)
        @DisplayName("Should include flag type in description")
        void shouldIncludeFlagTypeInDescription(TargetFlagsUICommand.TargetFlagType flagType) {
            // Given
            var cmd = new TargetFlagsUICommand(magicEditorUseCase, flagType, magicIndex);

            // When
            String description = cmd.getDescription();

            // Then
            String expectedFlagName = flagType.name().toLowerCase().replace("_", " ");
            assertThat(description)
                    .contains("Update " + expectedFlagName)
                    .contains("magic " + magicIndex);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full update flow for all flag types")
        void shouldCompleteFullUpdateFlowForAllFlagTypes() throws Exception {
            // Test each flag type
            for (TargetFlagsUICommand.TargetFlagType flagType : TargetFlagsUICommand.TargetFlagType.values()) {
                // Given
                var cmd = new TargetFlagsUICommand(magicEditorUseCase, flagType, magicIndex);
                when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
                
                // Set up mocks for each flag type
                switch (flagType) {
                    case TARGET_DEAD -> when(targetInfo.withDead(true)).thenReturn(updatedTargetInfo);
                    case TARGET_SINGLE -> when(targetInfo.withSingle(true)).thenReturn(updatedTargetInfo);
                    case TARGET_ENEMY -> when(targetInfo.withEnemy(true)).thenReturn(updatedTargetInfo);
                    case TARGET_SINGLE_SIDE -> when(targetInfo.withSingleSide(true)).thenReturn(updatedTargetInfo);
                }

                // When
                cmd.execute(true);

                // Then - Verify complete flow
                verify(magicEditorUseCase, atLeastOnce()).getMagicData(magicIndex);
                verify(currentMagic, atLeastOnce()).targetInfo();
                verify(currentMagic, atLeastOnce()).withTargetInfo(updatedTargetInfo);
                verify(magicEditorUseCase, atLeastOnce()).updateMagicData(magicIndex, updatedMagic);
            }
        }

        @Test
        @DisplayName("Should handle boolean toggle scenarios")
        void shouldHandleBooleanToggleScenarios() throws Exception {
            // Given
            when(targetInfo.withSingle(true)).thenReturn(updatedTargetInfo);
            when(targetInfo.withSingle(false)).thenReturn(updatedTargetInfo);

            // When - First set to true
            command.execute(true);

            // Then
            verify(targetInfo).withSingle(true);

            // When - Then set to false
            command.execute(false);

            // Then
            verify(targetInfo).withSingle(false);
            verify(magicEditorUseCase, times(2)).updateMagicData(magicIndex, updatedMagic);
        }
    }
} 