package com.ff8.infrastructure.adapters.primary.ui.commands.junction;

import com.ff8.application.dto.JunctionStatsDTO;
import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
@DisplayName("JunctionStatUICommand Tests")
class JunctionStatUICommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private JunctionStatsDTO currentStats;
    
    @Mock
    private JunctionStatsDTO updatedStats;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private JunctionStatUICommand command;
    private final int magicIndex = 10;

    @BeforeEach
    void setUp() {
        command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.HP);
        
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
        when(currentMagic.junctionStats()).thenReturn(currentStats);
        when(currentMagic.withJunctionStats(any())).thenReturn(updatedMagic);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize command with valid parameters")
        void shouldInitializeCommandWithValidParameters() {
            // Given & When
            var cmd = new JunctionStatUICommand(magicEditorUseCase, 15, JunctionStatUICommand.JunctionStatType.STR);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(15);
            assertThat(cmd.getDescription()).contains("Update junction STR for magic ID 15");
        }

        @ParameterizedTest
        @EnumSource(JunctionStatUICommand.JunctionStatType.class)
        @DisplayName("Should create command for each stat type")
        void shouldCreateCommandForEachStatType(JunctionStatUICommand.JunctionStatType statType) {
            // Given & When
            var cmd = new JunctionStatUICommand(magicEditorUseCase, magicIndex, statType);

            // Then
            assertThat(cmd.getDescription()).contains("Update junction " + statType);
            assertThat(cmd.getMagicIndex()).isEqualTo(magicIndex);
        }
    }

    @Nested
    @DisplayName("Execute Method Tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Should update HP stat successfully")
        void shouldUpdateHPStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.HP);
            when(currentStats.withHp(100)).thenReturn(updatedStats);

            // When
            command.execute(100);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(currentStats).withHp(100);
            verify(currentMagic).withJunctionStats(updatedStats);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update STR stat successfully")
        void shouldUpdateSTRStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.STR);
            when(currentStats.withStr(50)).thenReturn(updatedStats);

            // When
            command.execute(50);

            // Then
            verify(currentStats).withStr(50);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update VIT stat successfully")
        void shouldUpdateVITStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.VIT);
            when(currentStats.withVit(75)).thenReturn(updatedStats);

            // When
            command.execute(75);

            // Then
            verify(currentStats).withVit(75);
        }

        @Test
        @DisplayName("Should update MAG stat successfully")
        void shouldUpdateMAGStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.MAG);
            when(currentStats.withMag(200)).thenReturn(updatedStats);

            // When
            command.execute(200);

            // Then
            verify(currentStats).withMag(200);
        }

        @Test
        @DisplayName("Should update SPR stat successfully")
        void shouldUpdateSPRStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.SPR);
            when(currentStats.withSpr(90)).thenReturn(updatedStats);

            // When
            command.execute(90);

            // Then
            verify(currentStats).withSpr(90);
        }

        @Test
        @DisplayName("Should update SPD stat successfully")
        void shouldUpdateSPDStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.SPD);
            when(currentStats.withSpd(120)).thenReturn(updatedStats);

            // When
            command.execute(120);

            // Then
            verify(currentStats).withSpd(120);
        }

        @Test
        @DisplayName("Should update EVA stat successfully")
        void shouldUpdateEVAStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.EVA);
            when(currentStats.withEva(30)).thenReturn(updatedStats);

            // When
            command.execute(30);

            // Then
            verify(currentStats).withEva(30);
        }

        @Test
        @DisplayName("Should update HIT stat successfully")
        void shouldUpdateHITStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.HIT);
            when(currentStats.withHit(255)).thenReturn(updatedStats);

            // When
            command.execute(255);

            // Then
            verify(currentStats).withHit(255);
        }

        @Test
        @DisplayName("Should update LUCK stat successfully")
        void shouldUpdateLUCKStatSuccessfully() throws Exception {
            // Given
            command = new JunctionStatUICommand(magicEditorUseCase, magicIndex, JunctionStatUICommand.JunctionStatType.LUCK);
            when(currentStats.withLuck(100)).thenReturn(updatedStats);

            // When
            command.execute(100);

            // Then
            verify(currentStats).withLuck(100);
        }

        @Test
        @DisplayName("Should throw exception when magic not found")
        void shouldThrowExceptionWhenMagicNotFound() {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> command.execute(100))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Magic not found: " + magicIndex);

            verify(magicEditorUseCase, never()).updateMagicData(anyInt(), any());
        }

        @Test
        @DisplayName("Should propagate exceptions from use case")
        void shouldPropagateExceptionsFromUseCase() throws Exception {
            // Given
            when(currentStats.withHp(100)).thenReturn(updatedStats);
            doThrow(new RuntimeException("Update failed"))
                    .when(magicEditorUseCase).updateMagicData(eq(magicIndex), any());

            // When & Then
            assertThatThrownBy(() -> command.execute(100))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Update failed");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100, 200, 255})
        @DisplayName("Should validate values within range")
        void shouldValidateValuesWithinRange(int value) {
            // Given & When
            boolean result = command.validate(value);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, 256, 300, 1000})
        @DisplayName("Should reject values outside range")
        void shouldRejectValuesOutsideRange(int value) {
            // Given & When
            boolean result = command.validate(value);

            // Then
            assertThat(result).isFalse();
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
        @DisplayName("Should validate boundary values")
        void shouldValidateBoundaryValues() {
            // Given & When & Then
            assertThat(command.validate(0)).isTrue();
            assertThat(command.validate(255)).isTrue();
            assertThat(command.validate(-1)).isFalse();
            assertThat(command.validate(256)).isFalse();
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @ParameterizedTest
        @EnumSource(JunctionStatUICommand.JunctionStatType.class)
        @DisplayName("Should include stat type in description")
        void shouldIncludeStatTypeInDescription(JunctionStatUICommand.JunctionStatType statType) {
            // Given
            var cmd = new JunctionStatUICommand(magicEditorUseCase, magicIndex, statType);

            // When
            String description = cmd.getDescription();

            // Then
            assertThat(description)
                    .contains("Update junction " + statType)
                    .contains("magic ID " + magicIndex);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should complete full update flow")
        void shouldCompleteFullUpdateFlow() throws Exception {
            // Given
            when(currentStats.withHp(150)).thenReturn(updatedStats);

            // When
            command.execute(150);

            // Then - Verify complete flow
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(currentMagic).junctionStats();
            verify(currentStats).withHp(150);
            verify(currentMagic).withJunctionStats(updatedStats);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should handle edge case with minimum value")
        void shouldHandleEdgeCaseWithMinimumValue() throws Exception {
            // Given
            when(currentStats.withHp(0)).thenReturn(updatedStats);

            // When
            command.execute(0);

            // Then
            verify(currentStats).withHp(0);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should handle edge case with maximum value")
        void shouldHandleEdgeCaseWithMaximumValue() throws Exception {
            // Given
            when(currentStats.withHp(255)).thenReturn(updatedStats);

            // When
            command.execute(255);

            // Then
            verify(currentStats).withHp(255);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }
    }
} 