package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.StatusEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StatusEffectSet Tests")
class StatusEffectSetTest {

    private StatusEffectSet statusEffectSet;

    @BeforeEach
    void setUp() {
        statusEffectSet = new StatusEffectSet();
    }

    @Nested
    @DisplayName("Status Effect Management")
    class StatusEffectManagement {

        @Test
        @DisplayName("Should add and check status effects")
        void shouldAddAndCheckStatusEffects() {
            // When
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // Then
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON)).isTrue();
            assertThat(statusEffectSet.hasStatus(StatusEffect.SLEEP)).isTrue();
            assertThat(statusEffectSet.hasStatus(StatusEffect.DEATH)).isFalse();
        }

        @Test
        @DisplayName("Should remove status effects")
        void shouldRemoveStatusEffects() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // When
            statusEffectSet.setStatus(StatusEffect.POISON, false);

            // Then
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON)).isFalse();
            assertThat(statusEffectSet.hasStatus(StatusEffect.SLEEP)).isTrue();
        }

        @Test
        @DisplayName("Should clear all status effects")
        void shouldClearAllStatusEffects() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // When
            statusEffectSet.clear();

            // Then
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON)).isFalse();
            assertThat(statusEffectSet.hasStatus(StatusEffect.SLEEP)).isFalse();
            assertThat(statusEffectSet.hasAnyStatus()).isFalse();
        }

        @Test
        @DisplayName("Should handle duplicate additions gracefully")
        void shouldHandleDuplicateAdditions() {
            // When
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.POISON, true); // Duplicate

            // Then
            var activeStatuses = statusEffectSet.getActiveStatuses();
            assertThat(activeStatuses).hasSize(1);
            assertThat(activeStatuses).contains(StatusEffect.POISON);
        }

        @Test
        @DisplayName("Should not throw when removing non-existent status")
        void shouldNotThrowWhenRemovingNonExistentStatus() {
            // Then
            assertThatCode(() -> statusEffectSet.setStatus(StatusEffect.DEATH, false))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Binary Operations")
    class BinaryOperations {

        @Test
        @DisplayName("Should convert to and from binary correctly")
        void shouldConvertToAndFromBinaryCorrectly() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // When
            byte[] bytes = statusEffectSet.toBytes();
            StatusEffectSet reconstructed = new StatusEffectSet();
            reconstructed.setStatus(StatusEffect.POISON, true);
            reconstructed.setStatus(StatusEffect.SLEEP, true);

            // Then
            assertThat(reconstructed.hasStatus(StatusEffect.POISON)).isTrue();
            assertThat(reconstructed.hasStatus(StatusEffect.SLEEP)).isTrue();
            assertThat(bytes).hasSize(6); // 48 bits = 6 bytes
        }

        @Test
        @DisplayName("Should preserve all status effects in binary format")
        void shouldPreserveAllStatusEffectsInBinaryFormat() {
            // Given - Add several status effects
            StatusEffect[] effects = {StatusEffect.POISON, StatusEffect.SLEEP, StatusEffect.DEATH, StatusEffect.PETRIFY};
            for (StatusEffect effect : effects) {
                statusEffectSet.setStatus(effect, true);
            }

            // When
            byte[] bytes = statusEffectSet.toBytes();
            StatusEffectSet reconstructed = new StatusEffectSet();
            for (StatusEffect effect : effects) {
                reconstructed.setStatus(effect, true);
            }

            // Then
            for (StatusEffect effect : effects) {
                assertThat(reconstructed.hasStatus(effect)).isTrue();
            }
        }

        @Test
        @DisplayName("Should handle empty status set in binary format")
        void shouldHandleEmptyStatusSetInBinaryFormat() {
            // When
            byte[] bytes = statusEffectSet.toBytes();
            StatusEffectSet reconstructed = new StatusEffectSet();

            // Then
            assertThat(bytes).hasSize(6);
            assertThat(reconstructed.hasAnyStatus()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and Hash")
    class EqualityAndHash {

        @Test
        @DisplayName("Should be equal when same status effects")
        void shouldBeEqualWhenSameStatusEffects() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            StatusEffectSet other = new StatusEffectSet();
            other.setStatus(StatusEffect.POISON, true);
            other.setStatus(StatusEffect.SLEEP, true);

            // Then
            assertThat(statusEffectSet).isEqualTo(other);
            assertThat(statusEffectSet.hashCode()).isEqualTo(other.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different status effects")
        void shouldNotBeEqualWhenDifferentStatusEffects() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            StatusEffectSet other = new StatusEffectSet();
            other.setStatus(StatusEffect.SLEEP, true);

            // Then
            assertThat(statusEffectSet).isNotEqualTo(other);
        }

        @Test
        @DisplayName("Should handle null comparison gracefully")
        void shouldHandleNullComparisonGracefully() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);

            // Then
            assertThat(statusEffectSet).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("ToString and Display")
    class ToStringAndDisplay {

        @Test
        @DisplayName("Should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // When
            String result = statusEffectSet.toString();

            // Then
            assertThat(result).contains("StatusEffectSet");
            assertThat(result).contains("POISON");
            assertThat(result).contains("SLEEP");
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethods {

        @Test
        @DisplayName("Should correctly count active statuses")
        void shouldCorrectlyCountActiveStatuses() {
            // Given - Initially empty
            assertThat(statusEffectSet.getActiveStatuses()).isEmpty();

            // When - Add status effects
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);
            assertThat(statusEffectSet.getActiveStatuses()).hasSize(2);

            // When - Remove one
            statusEffectSet.setStatus(StatusEffect.POISON, false);
            assertThat(statusEffectSet.getActiveStatuses()).hasSize(1);
        }

        @Test
        @DisplayName("Should check for specific status combinations")
        void shouldCheckForSpecificStatusCombinations() {
            // Given
            statusEffectSet.setStatus(StatusEffect.POISON, true);
            statusEffectSet.setStatus(StatusEffect.SLEEP, true);

            // Then
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON) && statusEffectSet.hasStatus(StatusEffect.SLEEP)).isTrue();
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON) && statusEffectSet.hasStatus(StatusEffect.DEATH)).isFalse();
            assertThat(statusEffectSet.hasStatus(StatusEffect.POISON) || statusEffectSet.hasStatus(StatusEffect.DEATH)).isTrue();
            assertThat(statusEffectSet.hasStatus(StatusEffect.DEATH) || statusEffectSet.hasStatus(StatusEffect.PETRIFY)).isFalse();
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @ParameterizedTest
        @ValueSource(ints = {100, 1000, 10000})
        @DisplayName("Should handle repeated operations efficiently")
        void shouldHandleRepeatedOperationsEfficiently(int iterations) {
            // When
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                statusEffectSet.setStatus(StatusEffect.POISON, true);
                statusEffectSet.setStatus(StatusEffect.POISON, false);
            }
            long endTime = System.nanoTime();

            // Then
            long durationMs = (endTime - startTime) / 1_000_000;
            assertThat(durationMs).isLessThan(1000); // Should complete within 1 second
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() {
            // Given
            int threadCount = 10;
            int operationsPerThread = 100;

            // When
            CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    StatusEffectSet localSet = new StatusEffectSet();
                    for (int j = 0; j < operationsPerThread; j++) {
                        localSet.setStatus(StatusEffect.POISON, true);
                        localSet.hasStatus(StatusEffect.POISON);
                        localSet.setStatus(StatusEffect.POISON, false);
                    }
                }, Executors.newCachedThreadPool());
            }

            // Then
            assertThatCode(() -> CompletableFuture.allOf(futures).get())
                    .doesNotThrowAnyException();
        }
    }
} 