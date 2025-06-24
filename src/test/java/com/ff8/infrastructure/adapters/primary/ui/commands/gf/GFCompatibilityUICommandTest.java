package com.ff8.infrastructure.adapters.primary.ui.commands.gf;

import com.ff8.application.dto.GFCompatibilityDTO;
import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.GF;
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
@DisplayName("GFCompatibilityUICommand Tests")
class GFCompatibilityUICommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private GFCompatibilityDTO currentCompatibility;
    
    @Mock
    private GFCompatibilityDTO updatedCompatibility;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private GFCompatibilityUICommand command;
    private final int magicIndex = 5;
    private final GF testGF = GF.IFRIT;

    @BeforeEach
    void setUp() {
        command = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, testGF);
        
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
        when(currentMagic.gfCompatibility()).thenReturn(currentCompatibility);
        when(currentMagic.withGfCompatibility(any())).thenReturn(updatedMagic);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize command with valid parameters")
        void shouldInitializeCommandWithValidParameters() {
            // Given & When
            var cmd = new GFCompatibilityUICommand(magicEditorUseCase, 12, GF.SHIVA);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(12);
            assertThat(cmd.getDescription()).contains("Update SHIVA compatibility for magic ID 12");
        }

        @ParameterizedTest
        @EnumSource(GF.class)
        @DisplayName("Should create command for each GF")
        void shouldCreateCommandForEachGF(GF gf) {
            // Given & When
            var cmd = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, gf);

            // Then
            assertThat(cmd.getDescription()).contains("Update " + gf + " compatibility");
            assertThat(cmd.getMagicIndex()).isEqualTo(magicIndex);
        }
    }

    @Nested
    @DisplayName("Execute Method Tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Should update GF compatibility successfully with positive value")
        void shouldUpdateGFCompatibilitySuccessfullyWithPositiveValue() throws Exception {
            // Given
            double uiValue = 5.0;
            int expectedDisplayValue = 50; // 5.0 * 10 = 50
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
            verify(currentMagic).withGfCompatibility(updatedCompatibility);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should update GF compatibility successfully with negative value")
        void shouldUpdateGFCompatibilitySuccessfullyWithNegativeValue() throws Exception {
            // Given
            double uiValue = -3.5;
            int expectedDisplayValue = 0; // Math.max(0, -3.5 * 10) = 0
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should handle zero value correctly")
        void shouldHandleZeroValueCorrectly() throws Exception {
            // Given
            double uiValue = 0.0;
            int expectedDisplayValue = 0;
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should handle maximum positive value")
        void shouldHandleMaximumPositiveValue() throws Exception {
            // Given
            double uiValue = 10.0;
            int expectedDisplayValue = 100; // 10.0 * 10 = 100
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should handle decimal values")
        void shouldHandleDecimalValues() throws Exception {
            // Given
            double uiValue = 2.7;
            int expectedDisplayValue = 27; // Math.round(2.7 * 10) = 27
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should handle different GF types")
        void shouldHandleDifferentGFTypes() throws Exception {
            // Given
            var shivaCommand = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, GF.SHIVA);
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            when(currentCompatibility.withDisplayValue(GF.SHIVA, 50)).thenReturn(updatedCompatibility);

            // When
            shivaCommand.execute(5.0);

            // Then
            verify(currentCompatibility).withDisplayValue(GF.SHIVA, 50);
        }

        @Test
        @DisplayName("Should throw exception when magic not found")
        void shouldThrowExceptionWhenMagicNotFound() {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> command.execute(5.0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Magic not found: " + magicIndex);

            verify(magicEditorUseCase, never()).updateMagicData(anyInt(), any());
        }

        @Test
        @DisplayName("Should propagate exceptions from use case")
        void shouldPropagateExceptionsFromUseCase() throws Exception {
            // Given
            when(currentCompatibility.withDisplayValue(eq(testGF), anyInt())).thenReturn(updatedCompatibility);
            doThrow(new RuntimeException("Update failed"))
                    .when(magicEditorUseCase).updateMagicData(eq(magicIndex), eq(updatedMagic));

            // When & Then
            assertThatThrownBy(() -> command.execute(5.0))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Update failed");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @ParameterizedTest
        @ValueSource(doubles = {-10.0, -5.5, 0.0, 2.5, 10.0})
        @DisplayName("Should validate values within range")
        void shouldValidateValuesWithinRange(double value) {
            // Given & When
            boolean result = command.validate(value);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {-10.1, -15.0, 10.1, 20.0})
        @DisplayName("Should reject values outside range")
        void shouldRejectValuesOutsideRange(double value) {
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
            assertThat(command.validate(-10.0)).isTrue();
            assertThat(command.validate(10.0)).isTrue();
            assertThat(command.validate(-10.1)).isFalse();
            assertThat(command.validate(10.1)).isFalse();
        }

        @Test
        @DisplayName("Should validate precision values")
        void shouldValidatePrecisionValues() {
            // Given & When & Then
            assertThat(command.validate(-9.99)).isTrue();
            assertThat(command.validate(9.99)).isTrue();
            assertThat(command.validate(0.1)).isTrue();
            assertThat(command.validate(-0.1)).isTrue();
        }
    }

    @Nested
    @DisplayName("Value Conversion Tests")
    class ValueConversionTests {

        @Test
        @DisplayName("Should convert positive UI value to display value correctly")
        void shouldConvertPositiveUIValueToDisplayValueCorrectly() throws Exception {
            // Given
            double uiValue = 7.3;
            int expectedDisplayValue = 73; // Math.round(7.3 * 10)
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should handle negative values by converting to zero")
        void shouldHandleNegativeValuesByConvertingToZero() throws Exception {
            // Given
            double uiValue = -5.0;
            int expectedDisplayValue = 0; // Math.max(0, -5.0 * 10)
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }

        @Test
        @DisplayName("Should round fractional display values")
        void shouldRoundFractionalDisplayValues() throws Exception {
            // Given
            double uiValue = 1.26; // Should round to 13 (1.26 * 10 = 12.6, rounded = 13)
            int expectedDisplayValue = 13;
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @ParameterizedTest
        @EnumSource(GF.class)
        @DisplayName("Should include GF name in description")
        void shouldIncludeGFNameInDescription(GF gf) {
            // Given
            var cmd = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, gf);

            // When
            String description = cmd.getDescription();

            // Then
            assertThat(description)
                    .contains("Update " + gf + " compatibility")
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
            double uiValue = 3.5;
            int expectedDisplayValue = 35;
            when(currentCompatibility.withDisplayValue(testGF, expectedDisplayValue)).thenReturn(updatedCompatibility);

            // When
            command.execute(uiValue);

            // Then - Verify complete flow
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(currentMagic).gfCompatibility();
            verify(currentCompatibility).withDisplayValue(testGF, expectedDisplayValue);
            verify(currentMagic).withGfCompatibility(updatedCompatibility);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should handle multiple GF updates independently")
        void shouldHandleMultipleGFUpdatesIndependently() throws Exception {
            // Given
            var ifritCommand = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, GF.IFRIT);
            var shivaCommand = new GFCompatibilityUICommand(magicEditorUseCase, magicIndex, GF.SHIVA);
            
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            when(currentCompatibility.withDisplayValue(GF.IFRIT, 50)).thenReturn(updatedCompatibility);
            when(currentCompatibility.withDisplayValue(GF.SHIVA, 30)).thenReturn(updatedCompatibility);

            // When
            ifritCommand.execute(5.0);
            shivaCommand.execute(3.0);

            // Then
            verify(currentCompatibility).withDisplayValue(GF.IFRIT, 50);
            verify(currentCompatibility).withDisplayValue(GF.SHIVA, 30);
            verify(magicEditorUseCase, times(2)).updateMagicData(magicIndex, updatedMagic);
        }
    }
} 