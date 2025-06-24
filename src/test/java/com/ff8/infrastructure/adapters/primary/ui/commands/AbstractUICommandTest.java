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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AbstractUICommand Tests")
class AbstractUICommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private TestableCommand command;
    private final Integer magicIndex = 5;
    private final String testValue = "testValue";

    @BeforeEach
    void setUp() {
        command = new TestableCommand(magicEditorUseCase, magicIndex, "Test Operation");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize command with valid parameters")
        void shouldInitializeCommandWithValidParameters() {
            // Given & When
            var cmd = new TestableCommand(magicEditorUseCase, 10, "Update Test");

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(10);
            assertThat(cmd.getDescription()).isEqualTo("Update Test for magic 10");
        }

        @Test
        @DisplayName("Should handle null magic index")
        void shouldHandleNullMagicIndex() {
            // Given & When
            var cmd = new TestableCommand(magicEditorUseCase, null, "Update Test");

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(-1);
            assertThat(cmd.getDescription()).contains("for magic 0");
        }

        @Test
        @DisplayName("Should require non-null magic editor use case")
        void shouldRequireNonNullMagicEditorUseCase() {
            // Given & When & Then
            // Note: Constructor doesn't validate null, but will fail on first use
            var cmd = new TestableCommand(null, magicIndex, "Test");
            assertThatThrownBy(() -> cmd.execute(testValue))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Execute Method Tests")
    class ExecuteMethodTests {

        @Test
        @DisplayName("Should successfully execute command with valid data")
        void shouldExecuteCommandSuccessfully() throws Exception {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            
            // Create a spy to mock the updateMagicData method
            TestableCommand spyCommand = spy(command);
            doReturn(updatedMagic).when(spyCommand).updateMagicData(currentMagic, testValue);

            // When
            spyCommand.execute(testValue);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
            verify(spyCommand).updateMagicData(currentMagic, testValue);
        }

        @Test
        @DisplayName("Should throw exception when magic not found")
        void shouldThrowExceptionWhenMagicNotFound() {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> command.execute(testValue))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Magic not found: " + magicIndex);

            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(magicEditorUseCase, never()).updateMagicData(anyInt(), any());
        }

        @Test
        @DisplayName("Should throw exception when magic index is null")
        void shouldThrowExceptionWhenMagicIndexIsNull() {
            // Given
            command = new TestableCommand(magicEditorUseCase, null, "Test");

            // When & Then
            assertThatThrownBy(() -> command.execute(testValue))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot execute command: Magic ID is null");
        }

        @Test
        @DisplayName("Should validate before execution")
        void shouldValidateBeforeExecution() {
            // Given
            command = spy(command);
            doReturn(false).when(command).validate(testValue);

            // When & Then
            assertThatThrownBy(() -> command.execute(testValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid value for command execution: " + testValue);

            verify(command).validate(testValue);
            verify(magicEditorUseCase, never()).getMagicData(anyInt());
        }

        @Test
        @DisplayName("Should propagate exceptions from updateMagicData")
        void shouldPropagateExceptionsFromUpdateMagicData() throws Exception {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            
            TestableCommand spyCommand = spy(command);
            doThrow(new RuntimeException("Update failed"))
                    .when(spyCommand).updateMagicData(currentMagic, testValue);

            // When & Then
            assertThatThrownBy(() -> spyCommand.execute(testValue))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Update failed");
        }

        @Test
        @DisplayName("Should propagate exceptions from use case update")
        void shouldPropagateExceptionsFromUseCaseUpdate() throws Exception {
            // Given
            when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
            
            TestableCommand spyCommand = spy(command);
            doReturn(updatedMagic).when(spyCommand).updateMagicData(currentMagic, testValue);
            doThrow(new RuntimeException("Save failed"))
                    .when(magicEditorUseCase).updateMagicData(eq(magicIndex), eq(updatedMagic));

            // When & Then
            assertThatThrownBy(() -> spyCommand.execute(testValue))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Save failed");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate non-null value with valid magic index")
        void shouldValidateNonNullValueWithValidMagicIndex() {
            // Given & When
            boolean result = command.validate(testValue);

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
            command = new TestableCommand(magicEditorUseCase, -1, "Test");

            // When
            boolean result = command.validate(testValue);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null magic index")
        void shouldRejectNullMagicIndex() {
            // Given
            command = new TestableCommand(magicEditorUseCase, null, "Test");

            // When
            boolean result = command.validate(testValue);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Description and Index Tests")
    class DescriptionAndIndexTests {

        @Test
        @DisplayName("Should return correct description")
        void shouldReturnCorrectDescription() {
            // Given & When
            String description = command.getDescription();

            // Then
            assertThat(description).isEqualTo("Test Operation for magic " + magicIndex);
        }

        @Test
        @DisplayName("Should return correct magic index")
        void shouldReturnCorrectMagicIndex() {
            // Given & When
            int index = command.getMagicIndex();

            // Then
            assertThat(index).isEqualTo(magicIndex);
        }

        @Test
        @DisplayName("Should return -1 for null magic index")
        void shouldReturnMinusOneForNullMagicIndex() {
            // Given
            command = new TestableCommand(magicEditorUseCase, null, "Test");

            // When
            int index = command.getMagicIndex();

            // Then
            assertThat(index).isEqualTo(-1);
        }
    }

    /**
     * Testable concrete implementation of AbstractUICommand for testing.
     */
    private static class TestableCommand extends AbstractUICommand<String> {

        protected TestableCommand(MagicEditorUseCase magicEditorUseCase,
                                 Integer magicIndex,
                                 String operationDescription) {
            super(magicEditorUseCase, magicIndex, operationDescription);
        }

        @Override
        protected MagicDisplayDTO updateMagicData(MagicDisplayDTO currentMagic, String newValue) {
            // This will be mocked in tests
            return currentMagic;
        }
    }
} 