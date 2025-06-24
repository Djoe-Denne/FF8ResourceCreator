package com.ff8.domain.entities;

import com.ff8.domain.entities.enums.AttackType;
import com.ff8.domain.entities.enums.Element;
import com.ff8.domain.entities.enums.StatusEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MagicData Entity Tests")
class MagicDataTest {

    @Nested
    @DisplayName("Creation and Builder Tests")
    class CreationAndBuilderTests {

        @Test
        @DisplayName("Should create magic data with default values")
        void shouldCreateMagicDataWithDefaultValues() {
            // When
            MagicData magic = MagicData.builder().build();

            // Then
            assertThat(magic.getIndex()).isEqualTo(0);
            assertThat(magic.getMagicID()).isEqualTo(0);
            assertThat(magic.getSpellPower()).isEqualTo(0);
            assertThat(magic.getHitCount()).isEqualTo(1);
            assertThat(magic.getAttackType()).isEqualTo(AttackType.NONE);
            assertThat(magic.getElement()).isEqualTo(Element.NONE);
            assertThat(magic.getExtractedSpellName()).isEmpty();
        }

        @Test
        @DisplayName("Should create magic data with custom values")
        void shouldCreateMagicDataWithCustomValues() {
            // When
            MagicData magic = MagicData.builder()
                    .index(10)
                    .magicID(25)
                    .spellPower(150)
                    .element(Element.FIRE)
                    .attackType(AttackType.PHYSICAL_ATTACK)
                    .build();

            // Then
            assertThat(magic.getIndex()).isEqualTo(10);
            assertThat(magic.getMagicID()).isEqualTo(25);
            assertThat(magic.getSpellPower()).isEqualTo(150);
            assertThat(magic.getElement()).isEqualTo(Element.FIRE);
            assertThat(magic.getAttackType()).isEqualTo(AttackType.PHYSICAL_ATTACK);
        }

        @Test
        @DisplayName("Should support toBuilder for modifications")
        void shouldSupportToBuilderForModifications() {
            // Given
            MagicData original = MagicData.builder()
                    .index(1)
                    .magicID(10)
                    .spellPower(100)
                    .build();

            // When
            MagicData modified = original.toBuilder()
                    .spellPower(200)
                    .element(Element.ICE)
                    .build();

            // Then
            assertThat(modified.getIndex()).isEqualTo(1); // Preserved
            assertThat(modified.getMagicID()).isEqualTo(10); // Preserved
            assertThat(modified.getSpellPower()).isEqualTo(200); // Modified
            assertThat(modified.getElement()).isEqualTo(Element.ICE); // Modified
        }

        @Test
        @DisplayName("Should support @With methods for field updates")
        void shouldSupportWithMethodsForFieldUpdates() {
            // Given
            MagicData original = MagicData.builder()
                    .index(1)
                    .spellPower(100)
                    .build();

            // When
            MagicData updated = original.withSpellPower(150);

            // Then
            assertThat(updated.getSpellPower()).isEqualTo(150);
            assertThat(updated.getIndex()).isEqualTo(1); // Other fields preserved
            assertThat(original.getSpellPower()).isEqualTo(100); // Original unchanged
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should identify curative magic correctly")
        void shouldIdentifyCurativeMagicCorrectly() {
            // Given
            MagicData curativeMagic = MagicData.builder()
                    .attackType(AttackType.CURATIVE_MAGIC)
                    .build();

            MagicData offensiveMagic = MagicData.builder()
                    .attackType(AttackType.MAGIC_ATTACK)
                    .spellPower(100)
                    .build();

            // Then
            assertThat(curativeMagic.isCurative()).isTrue();
            assertThat(offensiveMagic.isCurative()).isFalse();
        }

        @Test
        @DisplayName("Should calculate junction bonuses correctly")
        void shouldCalculateJunctionBonusesCorrectly() {
            // Given
            JunctionStats junctionStats = new JunctionStats(50, 30, 25, 40, 35, 20, 15, 45, 10);

            JunctionElemental elementalJunction = new JunctionElemental(
                Element.FIRE, 75, 
                java.util.List.of(Element.ICE, Element.THUNDER), 50
            );

            MagicData magic = MagicData.builder()
                    .junctionStats(junctionStats)
                    .junctionElemental(elementalJunction)
                    .build();

            // Then
            assertThat(magic.hasJunctionBonuses()).isTrue();
            assertThat(magic.getJunctionStats().getHp()).isEqualTo(50);
            assertThat(magic.getJunctionElemental().getAttackElement()).isEqualTo(Element.FIRE);
        }

        @Test
        @DisplayName("Should handle status effects correctly")
        void shouldHandleStatusEffectsCorrectly() {
            // Given
            StatusEffectSet statusEffects = new StatusEffectSet();
            statusEffects.setStatus(StatusEffect.POISON, true);
            statusEffects.setStatus(StatusEffect.SLEEP, true);

            MagicData magic = MagicData.builder()
                    .statusEffects(statusEffects)
                    .build();

            // Then
            assertThat(magic.hasStatusEffects()).isTrue();
            assertThat(magic.getStatusEffects().hasStatus(StatusEffect.POISON)).isTrue();
            assertThat(magic.getStatusEffects().hasStatus(StatusEffect.SLEEP)).isTrue();
        }
    }

    @Nested
    @DisplayName("Equality and Hash Tests")
    class EqualityAndHashTests {

        @Test
        @DisplayName("Should be equal based on index only")
        void shouldBeEqualBasedOnIndexOnly() {
            // Given
            MagicData magic1 = MagicData.builder()
                    .index(5)
                    .magicID(10)
                    .spellPower(100)
                    .build();

            MagicData magic2 = MagicData.builder()
                    .index(5)
                    .magicID(20) // Different ID
                    .spellPower(200) // Different power
                    .build();

            // Then
            assertThat(magic1).isEqualTo(magic2);
            assertThat(magic1.hashCode()).isEqualTo(magic2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different indices")
        void shouldNotBeEqualWithDifferentIndices() {
            // Given
            MagicData magic1 = MagicData.builder().index(1).build();
            MagicData magic2 = MagicData.builder().index(2).build();

            // Then
            assertThat(magic1).isNotEqualTo(magic2);
        }

        @Test
        @DisplayName("Should handle null comparisons gracefully")
        void shouldHandleNullComparisonsGracefully() {
            // Given
            MagicData magic = MagicData.builder().index(1).build();

            // Then
            assertThat(magic).isNotEqualTo(null);
            assertThat(magic).isEqualTo(magic); // Self equality
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should provide meaningful toString representation")
        void shouldProvideMeaningfulToStringRepresentation() {
            // Given
            MagicData magic = MagicData.builder()
                    .index(1)
                    .magicID(25)
                    .extractedSpellName("Fire")
                    .spellPower(150)
                    .element(Element.FIRE)
                    .build();

            // When
            String result = magic.toString();

            // Then
            assertThat(result).contains("MagicData");
            assertThat(result).contains("index=1");
            assertThat(result).contains("magicID=25");
            assertThat(result).contains("Fire");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid magic data")
        void shouldAcceptValidMagicData() {
            // Given
            MagicData validMagic = MagicData.builder()
                    .index(1)
                    .magicID(25)
                    .spellPower(150)
                    .hitCount(3)
                    .drawResist(100)
                    .build();

            // Then - Should not throw any exceptions during creation
            assertThat(validMagic).isNotNull();
            assertThat(validMagic.getSpellPower()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should handle edge case values correctly")
        void shouldHandleEdgeCaseValuesCorrectly() {
            // Given & When
            MagicData edgeCaseMagic = MagicData.builder()
                    .spellPower(255) // Max value
                    .hitCount(1) // Min valid value
                    .drawResist(0) // Min value
                    .build();

            // Then
            assertThat(edgeCaseMagic.getSpellPower()).isEqualTo(255);
            assertThat(edgeCaseMagic.getHitCount()).isEqualTo(1);
            assertThat(edgeCaseMagic.getDrawResist()).isEqualTo(0);
        }
    }
} 