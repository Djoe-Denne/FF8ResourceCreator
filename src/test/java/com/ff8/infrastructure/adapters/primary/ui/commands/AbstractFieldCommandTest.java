package com.ff8.infrastructure.adapters.primary.ui.commands;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AbstractFieldCommand Tests")
class AbstractFieldCommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private TestableFieldCommand command;
    private final Integer magicIndex = 3;
    private final String testValue = "newValue";

    @BeforeEach
    void setUp() {
        command = spy(new TestableFieldCommand(
            magicEditorUseCase, 
            magicIndex, 
            TestFieldType.FIELD_ONE
        ));
        
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize field command with valid parameters")
        void shouldInitializeFieldCommandWithValidParameters() {
            // Given & When
            var cmd = new TestableFieldCommand(magicEditorUseCase, 7, TestFieldType.FIELD_TWO);

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(7);
            assertThat(cmd.getDescription()).contains("Update field two");
            assertThat(cmd.fieldType).isEqualTo(TestFieldType.FIELD_TWO);
        }

        @Test
        @DisplayName("Should format field type name in description")
        void shouldFormatFieldTypeNameInDescription() {
            // Given & When
            var cmd = new TestableFieldCommand(magicEditorUseCase, 1, TestFieldType.COMPLEX_FIELD_NAME);

            // Then
            assertThat(cmd.getDescription()).contains("Update complex field name");
        }
    }

    @Nested
    @DisplayName("Update Magic Data Tests")
    class UpdateMagicDataTests {

        @Test
        @DisplayName("Should delegate to updateSpecificField")
        void shouldDelegateToUpdateSpecificField() throws Exception {
            // Given
            when(command.updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_ONE))
                    .thenReturn(updatedMagic);

            // When
            command.execute(testValue);

            // Then
            verify(command).updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_ONE);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should pass correct field type to updateSpecificField")
        void shouldPassCorrectFieldTypeToUpdateSpecificField() throws Exception {
            // Given
            command = spy(new TestableFieldCommand(
                magicEditorUseCase, 
                magicIndex, 
                TestFieldType.FIELD_TWO
            ));
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            when(command.updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_TWO))
                    .thenReturn(updatedMagic);

            // When
            command.execute(testValue);

            // Then
            verify(command).updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_TWO);
        }

        @Test
        @DisplayName("Should propagate exceptions from updateSpecificField")
        void shouldPropagateExceptionsFromUpdateSpecificField() {
            // Given
            when(command.updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_ONE))
                    .thenThrow(new RuntimeException("Field update failed"));

            // When & Then
            assertThatThrownBy(() -> command.execute(testValue))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Field update failed");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should inherit validation from AbstractUICommand")
        void shouldInheritValidationFromAbstractUICommand() {
            // Given & When
            boolean validResult = command.validate(testValue);
            boolean invalidResult = command.validate(null);

            // Then
            assertThat(validResult).isTrue();
            assertThat(invalidResult).isFalse();
        }

        @Test
        @DisplayName("Should inherit execute flow from AbstractUICommand")
        void shouldInheritExecuteFlowFromAbstractUICommand() throws Exception {
            // Given
            when(command.updateSpecificField(any(), any(), any())).thenReturn(updatedMagic);

            // When
            command.execute(testValue);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(command).updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_ONE);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle different field types correctly")
        void shouldHandleDifferentFieldTypesCorrectly() throws Exception {
            // Given
            var cmd1 = spy(new TestableFieldCommand(magicEditorUseCase, magicIndex, TestFieldType.FIELD_ONE));
            var cmd2 = spy(new TestableFieldCommand(magicEditorUseCase, magicIndex, TestFieldType.FIELD_TWO));
            
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            when(cmd1.updateSpecificField(any(), any(), any())).thenReturn(updatedMagic);
            when(cmd2.updateSpecificField(any(), any(), any())).thenReturn(updatedMagic);

            // When
            cmd1.execute(testValue);
            cmd2.execute(testValue);

            // Then
            verify(cmd1).updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_ONE);
            verify(cmd2).updateSpecificField(currentMagic, testValue, TestFieldType.FIELD_TWO);
        }
    }

    /**
     * Test enum for field types.
     */
    private enum TestFieldType {
        FIELD_ONE,
        FIELD_TWO,
        COMPLEX_FIELD_NAME
    }

    /**
     * Testable concrete implementation of AbstractFieldCommand for testing.
     */
    private static class TestableFieldCommand extends AbstractFieldCommand<String, TestFieldType> {

        protected TestableFieldCommand(MagicEditorUseCase magicEditorUseCase,
                                      Integer magicIndex,
                                      TestFieldType fieldType) {
            super(magicEditorUseCase, magicIndex, fieldType);
        }

        @Override
        protected MagicDisplayDTO updateSpecificField(MagicDisplayDTO currentMagic, String newValue, TestFieldType fieldType) {
            // This will be mocked in tests
            return currentMagic;
        }
    }
} 