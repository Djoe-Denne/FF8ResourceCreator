package com.ff8.application.services;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.dto.JunctionStatsDTO;
import com.ff8.application.dto.JunctionElementalDTO;
import com.ff8.application.dto.JunctionStatusDTO;
import com.ff8.application.dto.GFCompatibilityDTO;
import com.ff8.application.mappers.DtoToMagicDataMapper;
import com.ff8.application.mappers.MagicDataToDtoMapper;
import com.ff8.application.ports.primary.MagicEditorUseCase;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.application.ports.secondary.FileSystemPort;
import com.ff8.domain.entities.MagicData;
import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.domain.events.MagicDataChangeEvent;
import com.ff8.domain.exceptions.InvalidMagicDataException;
import com.ff8.domain.observers.Observer;
import com.ff8.domain.services.MagicValidationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MagicEditorService Tests")
class MagicEditorServiceTest {

    @Mock
    private MagicRepository mockRepository;
    
    @Mock
    private MagicValidationService mockValidationService;
    
    @Mock
    private FileSystemPort mockFileSystemPort;
    
    @Mock
    private MagicDataToDtoMapper mockMagicDataToDtoMapper;
    
    @Mock
    private DtoToMagicDataMapper mockDtoToMagicDataMapper;
    
    @Mock
    private Observer<MagicDataChangeEvent> mockObserver;

    private MagicEditorService magicEditorService;
    private MagicData testMagicData;
    private MagicDisplayDTO testMagicDisplayDTO;

    @BeforeEach
    void setUp() {
        magicEditorService = new MagicEditorService(
            mockRepository,
            mockValidationService,
            mockFileSystemPort,
            mockMagicDataToDtoMapper,
            mockDtoToMagicDataMapper
        );

        // Set up test data
        testMagicData = MagicData.builder()
            .index(1)
            .magicID(10)
            .extractedSpellName("Fire")
            .spellPower(100)
            .element(Element.FIRE)
            .attackType(AttackType.MAGIC_ATTACK)
            .build();

        testMagicDisplayDTO = MagicDisplayDTO.builder()
            .index(1)
            .magicID(10)
            .spellName("Fire")
            .spellPower(100)
            .element(Element.FIRE)
            .attackType(AttackType.MAGIC_ATTACK)
            .build();
    }

    @Nested
    @DisplayName("Get Magic Data Tests")
    class GetMagicDataTests {

        @Test
        @DisplayName("Should return magic data when found")
        void shouldReturnMagicDataWhenFound() {
            // Given
            when(mockRepository.findByIndex(1)).thenReturn(Optional.of(testMagicData));
            when(mockMagicDataToDtoMapper.toDto(testMagicData)).thenReturn(testMagicDisplayDTO);

            // When
            Optional<MagicDisplayDTO> result = magicEditorService.getMagicData(1);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testMagicDisplayDTO);
            
            verify(mockRepository).findByIndex(1);
            verify(mockMagicDataToDtoMapper).toDto(testMagicData);
        }

        @Test
        @DisplayName("Should return empty when magic not found")
        void shouldReturnEmptyWhenMagicNotFound() {
            // Given
            when(mockRepository.findByIndex(999)).thenReturn(Optional.empty());

            // When
            Optional<MagicDisplayDTO> result = magicEditorService.getMagicData(999);

            // Then
            assertThat(result).isEmpty();
            
            verify(mockRepository).findByIndex(999);
            verifyNoInteractions(mockMagicDataToDtoMapper);
        }
    }

    @Nested
    @DisplayName("Update Magic Data Tests")
    class UpdateMagicDataTests {

        @Test
        @DisplayName("Should update magic data successfully")
        void shouldUpdateMagicDataSuccessfully() {
            // Given
            MagicDisplayDTO existingMagic = createTestMagicDisplayDTO(1);
            MagicData existingMagicData = createTestMagicData(1);
            MagicDisplayDTO updatedMagic = existingMagic.withSpellPower(200);
            MagicData updatedMagicData = existingMagicData.withSpellPower(200);

            when(mockRepository.findByIndex(1)).thenReturn(Optional.of(existingMagicData));
            when(mockDtoToMagicDataMapper.toDomain(updatedMagic, existingMagicData)).thenReturn(updatedMagicData);
            
            // When & Then
            assertThatCode(() -> magicEditorService.updateMagicData(1, updatedMagic))
                    .doesNotThrowAnyException();

            verify(mockRepository).save(updatedMagicData);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent magic")
        void shouldThrowExceptionWhenUpdatingNonExistentMagic() {
            // Given
            MagicDisplayDTO magic = createTestMagicDisplayDTO(999);
            when(mockRepository.findByIndex(999)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> magicEditorService.updateMagicData(999, magic))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Magic with index 999 not found");
        }

        @Test
        @DisplayName("Should throw exception for invalid magic data during update")
        void shouldThrowExceptionForInvalidMagicDataDuringUpdate() {
            // Given
            MagicDisplayDTO existingMagic = createTestMagicDisplayDTO(1);
            MagicData existingMagicData = createTestMagicData(1);
            MagicDisplayDTO invalidMagic = existingMagic.withSpellPower(-10); // Invalid spell power

            when(mockRepository.findByIndex(1)).thenReturn(Optional.of(existingMagicData));

            // When & Then
            assertThatThrownBy(() -> magicEditorService.updateMagicData(1, invalidMagic))
                    .isInstanceOf(InvalidMagicDataException.class)
                    .hasMessageContaining("Validation failed");
        }
    }

    @Nested
    @DisplayName("Get All Magic Tests")
    class GetAllMagicTests {

        @Test
        @DisplayName("Should return all magic data")
        void shouldReturnAllMagicData() {
            // Given
            List<MagicData> allMagicData = List.of(testMagicData);
            List<MagicDisplayDTO> allDTOs = List.of(testMagicDisplayDTO);

            when(mockRepository.findAll()).thenReturn(allMagicData);
            when(mockMagicDataToDtoMapper.toDtoList(allMagicData)).thenReturn(allDTOs);

            // When
            List<MagicDisplayDTO> result = magicEditorService.getAllMagic();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).contains(testMagicDisplayDTO);
            verify(mockRepository).findAll();
            verify(mockMagicDataToDtoMapper).toDtoList(allMagicData);
        }

        @Test
        @DisplayName("Should return empty list when no magic data")
        void shouldReturnEmptyListWhenNoMagicData() {
            // Given
            List<MagicData> emptyList = List.of();
            List<MagicDisplayDTO> emptyDTOList = List.of();

            when(mockRepository.findAll()).thenReturn(emptyList);
            when(mockMagicDataToDtoMapper.toDtoList(emptyList)).thenReturn(emptyDTOList);

            // When
            List<MagicDisplayDTO> result = magicEditorService.getAllMagic();

            // Then
            assertThat(result).isEmpty();
            verify(mockRepository).findAll();
            verify(mockMagicDataToDtoMapper).toDtoList(emptyList);
        }
    }

    @Nested
    @DisplayName("Magic Creation Tests")
    class MagicCreationTests {

        @Test
        @DisplayName("Should create new magic successfully")
        void shouldCreateNewMagicSuccessfully() {
            // Given
            String spellName = "New Spell";
            MagicData newMagicData = createTestMagicData(100);
            MagicDisplayDTO newMagicDto = createTestMagicDisplayDTO(100);
            
            when(mockRepository.getNextAvailableId()).thenReturn(42);
            when(mockRepository.getNextAvailableIndex()).thenReturn(100);
            when(mockMagicDataToDtoMapper.toDto(any(MagicData.class))).thenReturn(newMagicDto);

            // When
            MagicDisplayDTO result = magicEditorService.createNewMagic(spellName);

            // Then
            assertThat(result).isNotNull();
            verify(mockRepository).save(any(MagicData.class));
        }

        @Test
        @DisplayName("Should duplicate magic successfully")
        void shouldDuplicateMagicSuccessfully() {
            // Given
            int sourceId = 1;
            String newName = "Duplicated Spell";
            MagicData sourceMagic = createTestMagicData(sourceId);
            MagicDisplayDTO duplicatedDto = createTestMagicDisplayDTO(100);
            
            when(mockRepository.findByIndex(sourceId)).thenReturn(Optional.of(sourceMagic));
            when(mockRepository.getNextAvailableIndex()).thenReturn(100);
            when(mockMagicDataToDtoMapper.toDto(any(MagicData.class))).thenReturn(duplicatedDto);

            // When
            MagicDisplayDTO result = magicEditorService.duplicateMagic(sourceId, newName);

            // Then
            assertThat(result).isNotNull();
            verify(mockRepository).save(any(MagicData.class));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return valid result for valid magic data")
        void shouldReturnValidResultForValidMagicData() {
            // Given - Valid magic data with all proper values
            MagicDisplayDTO validMagic = MagicDisplayDTO.builder()
                    .index(1)
                    .magicID(25)
                    .spellName("Fire")
                    .spellPower(100)
                    .element(Element.FIRE)
                    .attackType(AttackType.MAGIC_ATTACK)
                    .hitCount(1)
                    .drawResist(50)
                    .junctionStats(new JunctionStatsDTO(10, 20, 15, 25, 18, 12, 8, 22, 5))
                    .junctionElemental(new JunctionElementalDTO(Element.FIRE, 75, List.of(Element.ICE), 50))
                    .junctionStatus(new JunctionStatusDTO(List.of(StatusEffect.POISON), 80, List.of(StatusEffect.SLEEP), 60))
                    .gfCompatibility(new GFCompatibilityDTO(Map.of()))
                    .build();

            // When
            MagicEditorUseCase.ValidationResult result = magicEditorService.validateMagicData(validMagic);

            // Then
            assertThat(result.isValid()).isTrue();
            assertThat(result.errors()).isEmpty();
        }

        @Test
        @DisplayName("Should return invalid result for invalid magic data")
        void shouldReturnInvalidResultForInvalidMagicData() {
            // Given
            MagicDisplayDTO invalidDTO = testMagicDisplayDTO.withSpellPower(-1);

            // When
            MagicEditorService.ValidationResult result = magicEditorService.validateMagicData(invalidDTO);

            // Then
            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Search and Utility Tests")
    class SearchAndUtilityTests {

        @Test
        @DisplayName("Should search magic by name")
        void shouldSearchMagicByName() {
            // Given
            List<MagicData> searchResults = List.of(testMagicData);
            List<MagicDisplayDTO> searchDTOs = List.of(testMagicDisplayDTO);

            when(mockRepository.findBySpellNameContaining("Fire")).thenReturn(searchResults);
            when(mockMagicDataToDtoMapper.toDtoList(searchResults)).thenReturn(searchDTOs);

            // When
            List<MagicDisplayDTO> result = magicEditorService.searchMagic("Fire");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).contains(testMagicDisplayDTO);
            verify(mockRepository).findBySpellNameContaining("Fire");
        }

        @Test
        @DisplayName("Should return magic count")
        void shouldReturnMagicCount() {
            // Given
            when(mockRepository.count()).thenReturn(50);

            // When
            int count = magicEditorService.getMagicCount();

            // Then
            assertThat(count).isEqualTo(50);
            verify(mockRepository).count();
        }

        @Test
        @DisplayName("Should check for unsaved changes")
        void shouldCheckForUnsavedChanges() {
            // When
            boolean hasChanges = magicEditorService.hasUnsavedChanges();

            // Then
            assertThat(hasChanges).isFalse(); // Default implementation returns false
        }
    }

    // Helper methods for creating test data
    private MagicData createTestMagicData(int index) {
        return MagicData.builder()
                .index(index)
                .magicID(index)
                .extractedSpellName("Test Spell " + index)
                .spellPower(100)
                .hitCount(1)
                .drawResist(50)
                .element(Element.FIRE)
                .attackType(AttackType.MAGIC_ATTACK)
                .build();
    }

    private MagicDisplayDTO createTestMagicDisplayDTO(int index) {
        return MagicDisplayDTO.builder()
                .index(index)
                .magicID(index)
                .spellName("Test Spell " + index)
                .spellPower(100)
                .element(Element.FIRE)
                .attackType(AttackType.MAGIC_ATTACK)
                .hitCount(1)
                .drawResist(50)
                .junctionStats(new JunctionStatsDTO(10, 20, 15, 25, 18, 12, 8, 22, 5))
                .junctionElemental(new JunctionElementalDTO(Element.FIRE, 75, List.of(Element.ICE), 50))
                .junctionStatus(new JunctionStatusDTO(List.of(StatusEffect.POISON), 80, List.of(StatusEffect.SLEEP), 60))
                .gfCompatibility(new GFCompatibilityDTO(Map.of()))
                .build();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() { return valid; }
        public List<String> errors() { return errors; }
        public List<String> warnings() { return warnings; }
    }
} 