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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AbstractToggleListCommand Tests")
class AbstractToggleListCommandTest {

    @Mock
    private MagicEditorUseCase magicEditorUseCase;
    
    @Mock
    private MagicDisplayDTO currentMagic;
    
    @Mock
    private MagicDisplayDTO updatedMagic;

    private TestableToggleListCommand command;
    private final Integer magicIndex = 2;
    private final String toggleValue = "testItem";

    @BeforeEach
    void setUp() {
        command = spy(new TestableToggleListCommand(magicEditorUseCase, magicIndex, "Toggle Test"));
        when(magicEditorUseCase.getMagicData(magicIndex)).thenReturn(Optional.of(currentMagic));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize toggle list command with valid parameters")
        void shouldInitializeToggleListCommandWithValidParameters() {
            // Given & When
            var cmd = new TestableToggleListCommand(magicEditorUseCase, 8, "Toggle Elements");

            // Then
            assertThat(cmd.getMagicIndex()).isEqualTo(8);
            assertThat(cmd.getDescription()).contains("Toggle Elements for magic 8");
        }
    }

    @Nested
    @DisplayName("Toggle Functionality Tests")
    class ToggleFunctionalityTests {

        @Test
        @DisplayName("Should add item to empty list")
        void shouldAddItemToEmptyList() throws Exception {
            // Given
            List<String> emptyList = new ArrayList<>();
            when(command.getCurrentList(currentMagic)).thenReturn(emptyList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            verify(command).getCurrentList(currentMagic);
            verify(command).updateMagicWithNewList(eq(currentMagic), argThat(list -> 
                list.size() == 1 && list.contains(toggleValue)
            ));
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }

        @Test
        @DisplayName("Should add item to list when not present")
        void shouldAddItemToListWhenNotPresent() throws Exception {
            // Given
            List<String> existingList = new ArrayList<>(Arrays.asList("item1", "item2"));
            when(command.getCurrentList(currentMagic)).thenReturn(existingList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            verify(command).updateMagicWithNewList(eq(currentMagic), argThat(list -> 
                list.size() == 3 && 
                list.contains("item1") && 
                list.contains("item2") && 
                list.contains(toggleValue)
            ));
        }

        @Test
        @DisplayName("Should remove item from list when present")
        void shouldRemoveItemFromListWhenPresent() throws Exception {
            // Given
            List<String> existingList = new ArrayList<>(Arrays.asList("item1", toggleValue, "item2"));
            when(command.getCurrentList(currentMagic)).thenReturn(existingList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            verify(command).updateMagicWithNewList(eq(currentMagic), argThat(list -> 
                list.size() == 2 && 
                list.contains("item1") && 
                list.contains("item2") && 
                !list.contains(toggleValue)
            ));
        }

        @Test
        @DisplayName("Should handle toggling the same item multiple times")
        void shouldHandleTogglingTheSameItemMultipleTimes() throws Exception {
            // Given
            List<String> initialList = new ArrayList<>(Arrays.asList("item1"));
            when(command.getCurrentList(currentMagic)).thenReturn(initialList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When - First toggle (add)
            command.execute(toggleValue);

            // Then
            verify(command).updateMagicWithNewList(eq(currentMagic), argThat(list -> 
                list.contains(toggleValue)
            ));

            // Given - Reset for second toggle
            List<String> listWithToggleValue = new ArrayList<>(Arrays.asList("item1", toggleValue));
            when(command.getCurrentList(currentMagic)).thenReturn(listWithToggleValue);

            // When - Second toggle (remove)
            command.execute(toggleValue);

            // Then
            verify(command, times(2)).updateMagicWithNewList(eq(currentMagic), any());
        }
    }

    @Nested
    @DisplayName("List Immutability Tests")
    class ListImmutabilityTests {

        @Test
        @DisplayName("Should not modify original list")
        void shouldNotModifyOriginalList() throws Exception {
            // Given
            List<String> originalList = new ArrayList<>(Arrays.asList("item1", "item2"));
            List<String> originalListCopy = new ArrayList<>(originalList);
            when(command.getCurrentList(currentMagic)).thenReturn(originalList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            assertThat(originalList).isEqualTo(originalListCopy);
        }

        @Test
        @DisplayName("Should create new list copy for modification")
        void shouldCreateNewListCopyForModification() throws Exception {
            // Given
            List<String> originalList = new ArrayList<>(Arrays.asList("item1"));
            when(command.getCurrentList(currentMagic)).thenReturn(originalList);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            verify(command).updateMagicWithNewList(eq(currentMagic), argThat(list -> 
                list != originalList && // Different instance
                list.contains("item1") && 
                list.contains(toggleValue)
            ));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate exceptions from getCurrentList")
        void shouldPropagateExceptionsFromGetCurrentList() {
            // Given
            when(command.getCurrentList(currentMagic))
                    .thenThrow(new RuntimeException("Get list failed"));

            // When & Then
            assertThatThrownBy(() -> command.execute(toggleValue))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Get list failed");
        }

        @Test
        @DisplayName("Should propagate exceptions from updateMagicWithNewList")
        void shouldPropagateExceptionsFromUpdateMagicWithNewList() {
            // Given
            List<String> list = new ArrayList<>();
            when(command.getCurrentList(currentMagic)).thenReturn(list);
            when(command.updateMagicWithNewList(eq(currentMagic), any()))
                    .thenThrow(new RuntimeException("Update magic failed"));

            // When & Then
            assertThatThrownBy(() -> command.execute(toggleValue))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Update magic failed");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should inherit validation from AbstractUICommand")
        void shouldInheritValidationFromAbstractUICommand() {
            // Given & When
            boolean validResult = command.validate(toggleValue);
            boolean invalidResult = command.validate(null);

            // Then
            assertThat(validResult).isTrue();
            assertThat(invalidResult).isFalse();
        }

        @Test
        @DisplayName("Should follow template method pattern")
        void shouldFollowTemplateMethodPattern() throws Exception {
            // Given
            List<String> list = new ArrayList<>();
            when(command.getCurrentList(currentMagic)).thenReturn(list);
            when(command.updateMagicWithNewList(eq(currentMagic), any())).thenReturn(updatedMagic);

            // When
            command.execute(toggleValue);

            // Then
            verify(magicEditorUseCase).getMagicData(magicIndex);
            verify(command).getCurrentList(currentMagic);
            verify(command).updateMagicWithNewList(eq(currentMagic), any());
            verify(magicEditorUseCase).updateMagicData(magicIndex, updatedMagic);
        }
    }

    /**
     * Testable concrete implementation of AbstractToggleListCommand for testing.
     */
    private static class TestableToggleListCommand extends AbstractToggleListCommand<String> {

        protected TestableToggleListCommand(MagicEditorUseCase magicEditorUseCase,
                                           Integer magicIndex,
                                           String operationDescription) {
            super(magicEditorUseCase, magicIndex, operationDescription);
        }

        @Override
        protected List<String> getCurrentList(MagicDisplayDTO currentMagic) {
            // This will be mocked in tests
            return new ArrayList<>();
        }

        @Override
        protected MagicDisplayDTO updateMagicWithNewList(MagicDisplayDTO currentMagic, List<String> newList) {
            // This will be mocked in tests
            return currentMagic;
        }
    }
} 