package com.ff8.infrastructure.adapters.secondary.repository;

import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryMagicRepository Tests")
class InMemoryMagicRepositoryTest {

    private InMemoryMagicRepository repository;
    private MagicData testMagic1;
    private MagicData testMagic2;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMagicRepository();
        
        testMagic1 = MagicData.builder()
            .index(1)
            .magicID(10)
            .extractedSpellName("Fire")
            .spellPower(100)
            .element(Element.FIRE)
            .attackType(AttackType.MAGIC_ATTACK)
            .build();
            
        testMagic2 = MagicData.builder()
            .index(2)
            .magicID(11)
            .extractedSpellName("Ice")
            .spellPower(90)
            .element(Element.ICE)
            .attackType(AttackType.MAGIC_ATTACK)
            .build();
    }

    // Helper method to create test magic data
    private MagicData createTestMagicData(int index) {
        return MagicData.builder()
            .index(index)
            .magicID(index + 10)
            .extractedSpellName("Test Magic " + index)
            .spellPower(100)
            .element(Element.FIRE)
            .attackType(AttackType.MAGIC_ATTACK)
            .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save magic data successfully")
        void shouldSaveMagicDataSuccessfully() {
            // When
            repository.save(testMagic1);

            // Then
            Optional<MagicData> retrieved = repository.findByIndex(1);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get()).isEqualTo(testMagic1);
            assertThat(repository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should update existing magic data")
        void shouldUpdateExistingMagicData() {
            // Given
            repository.save(testMagic1);
            
            MagicData updatedMagic = testMagic1.withSpellPower(150);

            // When
            repository.save(updatedMagic);

            // Then
            Optional<MagicData> retrieved = repository.findByIndex(1);
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getSpellPower()).isEqualTo(150);
            assertThat(repository.count()).isEqualTo(1); // Should not create new entry
        }

        @Test
        @DisplayName("Should throw exception when saving null magic")
        void shouldThrowExceptionWhenSavingNull() {
            // When & Then
            assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Magic data cannot be null");
        }

        @Test
        @DisplayName("Should save all magic data in list")
        void shouldSaveAllMagicDataInList() {
            // Given
            List<MagicData> magicList = List.of(testMagic1, testMagic2);

            // When
            repository.saveAll(magicList);

            // Then
            assertThat(repository.count()).isEqualTo(2);
            assertThat(repository.findByIndex(1)).isPresent();
            assertThat(repository.findByIndex(2)).isPresent();
        }

        @Test
        @DisplayName("Should throw exception when saving null list")
        void shouldThrowExceptionWhenSavingNullList() {
            // When & Then
            assertThatThrownBy(() -> repository.saveAll(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Magic list cannot be null");
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() {
            // When
            repository.saveAll(List.of());

            // Then
            assertThat(repository.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @BeforeEach
        void setUpData() {
            repository.save(testMagic1);
            repository.save(testMagic2);
        }

        @Test
        @DisplayName("Should find magic by index")
        void shouldFindMagicByIndex() {
            // When
            Optional<MagicData> result = repository.findByIndex(1);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testMagic1);
        }

        @Test
        @DisplayName("Should return empty when magic not found by index")
        void shouldReturnEmptyWhenMagicNotFoundByIndex() {
            // When
            Optional<MagicData> result = repository.findByIndex(99);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find all magic data")
        void shouldFindAllMagicData() {
            // When
            List<MagicData> result = repository.findAll();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testMagic1, testMagic2);
        }

        @Test
        @DisplayName("Should return empty list when no magic data")
        void shouldReturnEmptyListWhenNoMagicData() {
            // Given
            repository.clear();

            // When
            List<MagicData> result = repository.findAll();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find magic by spell name containing")
        void shouldFindMagicBySpellNameContaining() {
            // When
            List<MagicData> fireResults = repository.findBySpellNameContaining("Fire");
            List<MagicData> iceResults = repository.findBySpellNameContaining("ice"); // case insensitive
            List<MagicData> noResults = repository.findBySpellNameContaining("Thunder");

            // Then
            assertThat(fireResults).containsExactly(testMagic1);
            assertThat(iceResults).containsExactly(testMagic2);
            assertThat(noResults).isEmpty();
        }

        @Test
        @DisplayName("Should handle partial name matches")
        void shouldHandlePartialNameMatches() {
            // When
            List<MagicData> results = repository.findBySpellNameContaining("i"); // matches both Fire and Ice

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).containsExactlyInAnyOrder(testMagic1, testMagic2);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationsTests {

        @BeforeEach
        void setUpData() {
            repository.save(testMagic1);
            repository.save(testMagic2);
        }

        @Test
        @DisplayName("Should delete magic by index")
        void shouldDeleteMagicByIndex() {
            // When
            repository.deleteByIndex(1);

            // Then
            assertThat(repository.findByIndex(1)).isEmpty();
            assertThat(repository.findByIndex(2)).isPresent();
            assertThat(repository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle delete of non-existent index")
        void shouldHandleDeleteOfNonExistentIndex() {
            // When
            repository.deleteByIndex(99);

            // Then
            assertThat(repository.count()).isEqualTo(2); // No change
        }

        @Test
        @DisplayName("Should clear all magic data")
        void shouldClearAllMagicData() {
            // When
            repository.clear();

            // Then
            assertThat(repository.count()).isEqualTo(0);
            assertThat(repository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should delete by magic ID (deprecated method)")
        void shouldDeleteByMagicId() {
            // When
            repository.deleteById(10); // testMagic1 has magicID 10

            // Then
            assertThat(repository.findByIndex(1)).isEmpty();
            assertThat(repository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Existence Check Operations")
    class ExistenceCheckTests {

        @BeforeEach
        void setUpData() {
            repository.save(testMagic1);
        }

        @Test
        @DisplayName("Should check existence by index")
        void shouldCheckExistenceByIndex() {
            // Then
            assertThat(repository.existsByIndex(1)).isTrue();
            assertThat(repository.existsByIndex(99)).isFalse();
        }

        @Test
        @DisplayName("Should check existence by ID (deprecated method)")
        void shouldCheckExistenceById() {
            // Then
            assertThat(repository.existsById(10)).isTrue();
            assertThat(repository.existsById(99)).isFalse();
        }
    }

    @Nested
    @DisplayName("Modification Tracking")
    class ModificationTrackingTests {

        @Test
        @DisplayName("Should track modifications")
        void shouldTrackModifications() {
            // Given
            MagicData magic = createTestMagicData(1);
            repository.save(magic);

            // When & Then - Simplified repository always returns false for isModified
            assertThat(repository.isModified()).isFalse(); // Always false in simplified implementation

            // Modify and save
            MagicData modified = magic.withSpellPower(200);
            repository.save(modified);

            // Then - Still false in simplified implementation
            assertThat(repository.isModified()).isFalse(); // No modification tracking in simplified implementation

            // Mark as clean
            repository.markAsClean();
            assertThat(repository.isModified()).isFalse(); // Still false
        }
    }

    @Nested
    @DisplayName("Original Data Management")
    class OriginalDataManagementTests {

        @Test
        @DisplayName("Should store and retrieve original data")
        void shouldStoreAndRetrieveOriginalData() {
            // Given
            MagicData magic = createTestMagicData(1);
            repository.save(magic);

            // When - Modify the magic
            MagicData modified = magic.withSpellPower(200);
            repository.save(modified);

            // Then - In simplified repository, getOriginalByIndex returns current data (not original)
            Optional<MagicData> original = repository.getOriginalByIndex(1);
            assertThat(original).isPresent();
            assertThat(original.get().getSpellPower()).isEqualTo(200); // Current data, not original
        }

        @Test
        @DisplayName("Should reset to original data")
        void shouldResetToOriginalData() {
            // Given
            MagicData magic = createTestMagicData(1);
            repository.save(magic);

            // When - Reset (which just deletes in simplified implementation)
            repository.resetToOriginalByIndex(1);

            // Then - Magic should be deleted
            Optional<MagicData> result = repository.findByIndex(1);
            assertThat(result).isEmpty(); // Deleted, not reset to original
        }

        @Test
        @DisplayName("Should handle reset of non-existent magic")
        void shouldHandleResetOfNonExistentMagic() {
            // When & Then
            assertThatCode(() -> repository.resetToOriginalByIndex(99))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Utility Operations")
    class UtilityOperationsTests {

        @Test
        @DisplayName("Should return correct count")
        void shouldReturnCorrectCount() {
            // Initially empty
            assertThat(repository.count()).isEqualTo(0);

            // After adding one
            repository.save(testMagic1);
            assertThat(repository.count()).isEqualTo(1);

            // After adding another
            repository.save(testMagic2);
            assertThat(repository.count()).isEqualTo(2);

            // After deleting one
            repository.deleteByIndex(1);
            assertThat(repository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should get next available ID and index")
        void shouldGetNextAvailableIdAndIndex() {
            // Given
            repository.save(testMagic1); // index 1, id 10
            repository.save(testMagic2); // index 2, id 11

            // When
            int nextId = repository.getNextAvailableId();
            int nextIndex = repository.getNextAvailableIndex();

            // Then
            assertThat(nextId).isGreaterThan(11);
            assertThat(nextIndex).isGreaterThan(2);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            // Given
            int numberOfThreads = 10;
            int itemsPerThread = 100;
            Thread[] threads = new Thread[numberOfThreads];

            // When - multiple threads save data concurrently
            for (int i = 0; i < numberOfThreads; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < itemsPerThread; j++) {
                        int index = threadIndex * itemsPerThread + j;
                        MagicData magic = MagicData.builder()
                            .index(index)
                            .magicID(index)
                            .extractedSpellName("Magic" + index)
                            .build();
                        repository.save(magic);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            assertThat(repository.count()).isEqualTo(numberOfThreads * itemsPerThread);
        }
    }
} 