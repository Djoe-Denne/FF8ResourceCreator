package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.dto.JunctionElementalDTO;
import com.ff8.application.dto.JunctionStatusDTO;
import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.StatusEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntegerFieldUICommand Tests")
class IntegerFieldUICommandTest {

    @Mock
    private MagicEditorUseCase mockMagicEditorUseCase;

    private MagicDisplayDTO testMagicDisplayDTO;
    private final int TEST_MAGIC_INDEX = 1;

    @BeforeEach
    void setUp() {
        JunctionElementalDTO elementalJunction = new JunctionElementalDTO(
            Element.FIRE, 50, List.of(Element.ICE), 75
        );
        
        JunctionStatusDTO statusJunction = new JunctionStatusDTO(
            List.of(StatusEffect.POISON), 25, 
            List.of(StatusEffect.SLEEP), 50
        );

        testMagicDisplayDTO = MagicDisplayDTO.builder()
            .index(TEST_MAGIC_INDEX)
            .magicID(10)
            .spellName("Fire")
            .spellPower(100)
            .element(Element.FIRE)
            .attackType(AttackType.MAGIC_ATTACK)
            .drawResist(50)
            .hitCount(1)
            .statusAttackEnabler(0)
            .junctionElemental(elementalJunction)
            .junctionStatus(statusJunction)
            .build();
    }

    @Nested
    @DisplayName("Field Type Update Tests")
    class FieldTypeUpdateTests {

        @Test
        @DisplayName("Should update spell power correctly")
        void shouldUpdateSpellPowerCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(150);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.spellPower() == 150));
        }

        @Test
        @DisplayName("Should update draw resist correctly")
        void shouldUpdateDrawResistCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.DRAW_RESIST,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(200);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.drawResist() == 200));
        }

        @Test
        @DisplayName("Should update elemental attack value correctly")
        void shouldUpdateElementalAttackValueCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.ELEMENTAL_ATTACK_VALUE,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(80);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.junctionElemental().attackValue() == 80));
        }

        @Test
        @DisplayName("Should update elemental defense value correctly")
        void shouldUpdateElementalDefenseValueCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.ELEMENTAL_DEFENSE_VALUE,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(90);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.junctionElemental().defenseValue() == 90));
        }

        @Test
        @DisplayName("Should update status attack value correctly")
        void shouldUpdateStatusAttackValueCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.STATUS_ATTACK_VALUE,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(30);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.junctionStatus().attackValue() == 30));
        }

        @Test
        @DisplayName("Should update status defense value correctly")
        void shouldUpdateStatusDefenseValueCorrectly() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.STATUS_DEFENSE_VALUE,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            command.execute(60);

            // Then
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), 
                argThat(dto -> dto.junctionStatus().defenseValue() == 60));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid values in range 0-255")
        void shouldAcceptValidValuesInRange() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            // When & Then
            assertThat(command.validate(0)).isTrue();
            assertThat(command.validate(127)).isTrue();
            assertThat(command.validate(255)).isTrue();
        }

        @Test
        @DisplayName("Should reject values outside range 0-255")
        void shouldRejectValuesOutsideRange() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            // When & Then
            assertThat(command.validate(-1)).isFalse();
            assertThat(command.validate(256)).isFalse();
            assertThat(command.validate(1000)).isFalse();
        }

        @Test
        @DisplayName("Should reject null values")
        void shouldRejectNullValues() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            // When & Then
            assertThat(command.validate(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Command Interface Tests")
    class CommandInterfaceTests {

        @Test
        @DisplayName("Should return correct magic index")
        void shouldReturnCorrectMagicIndex() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            // When
            int magicIndex = command.getMagicIndex();

            // Then
            assertThat(magicIndex).isEqualTo(TEST_MAGIC_INDEX);
        }

        @Test
        @DisplayName("Should provide meaningful description")
        void shouldProvideMeaningfulDescription() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            // When
            String description = command.getDescription();

            // Then
            assertThat(description).isNotNull();
            assertThat(description).isNotEmpty();
            assertThat(description).containsIgnoringCase("spell power");
        }

        @Test
        @DisplayName("Should provide description for different field types")
        void shouldProvideDescriptionForDifferentFieldTypes() {
            // Given
            IntegerFieldUICommand spellPowerCommand = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            IntegerFieldUICommand drawResistCommand = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.DRAW_RESIST,
                    TEST_MAGIC_INDEX
            );

            // When
            String spellPowerDesc = spellPowerCommand.getDescription();
            String drawResistDesc = drawResistCommand.getDescription();

            // Then
            assertThat(spellPowerDesc).containsIgnoringCase("spell power");
            assertThat(drawResistDesc).containsIgnoringCase("draw resist");
            assertThat(spellPowerDesc).isNotEqualTo(drawResistDesc);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow successfully")
        void shouldHandleCompleteWorkflowSuccessfully() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase,
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER,
                    TEST_MAGIC_INDEX
            );

            when(mockMagicEditorUseCase.getMagicData(TEST_MAGIC_INDEX))
                    .thenReturn(Optional.of(testMagicDisplayDTO));

            doNothing().when(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());

            // When
            boolean isValid = command.validate(150);
            
            if (isValid) {
                command.execute(150);
            }

            // Then
            assertThat(isValid).isTrue();
            verify(mockMagicEditorUseCase).getMagicData(TEST_MAGIC_INDEX);
            verify(mockMagicEditorUseCase).updateMagicData(eq(TEST_MAGIC_INDEX), any());
        }

        @Test
        @DisplayName("Should handle magic not found gracefully")
        void shouldHandleMagicNotFoundGracefully() {
            // Given
            IntegerFieldUICommand command = new IntegerFieldUICommand(
                    mockMagicEditorUseCase, 
                    IntegerFieldUICommand.IntegerFieldType.SPELL_POWER, 
                    999
            );
            when(mockMagicEditorUseCase.getMagicData(999)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> command.execute(50))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Magic not found: 999");
        }
    }
} 