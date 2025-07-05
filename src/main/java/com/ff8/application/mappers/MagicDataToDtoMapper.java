package com.ff8.application.mappers;

import com.ff8.application.dto.*;
import com.ff8.domain.entities.*;
import com.ff8.domain.entities.enums.AttackFlag;
import com.ff8.domain.entities.enums.StatusEffect;
import com.ff8.domain.entities.enums.TargetFlag;

import java.util.List;

/**
 * Application layer mapper for converting domain entities to Data Transfer Objects.
 * 
 * <p>This mapper class is responsible for converting {@link MagicData} domain entities
 * to {@link MagicDisplayDTO} objects suitable for consumption by the presentation layer.
 * It implements the Mapper pattern as part of the application layer in the hexagonal
 * architecture, serving as a bridge between the domain and presentation concerns.</p>
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li><strong>Entity-to-DTO Conversion:</strong> Transforms complex domain objects into flat DTOs</li>
 *   <li><strong>Data Flattening:</strong> Converts nested domain structures into presentation-friendly formats</li>
 *   <li><strong>Type Safety:</strong> Ensures proper type mapping between domain and presentation layers</li>
 *   <li><strong>Null Safety:</strong> Handles null values gracefully with appropriate defaults</li>
 * </ul>
 * 
 * <p>Mapping strategy:</p>
 * <ul>
 *   <li><strong>Direct Mapping:</strong> Simple field-to-field mapping for basic properties</li>
 *   <li><strong>Structural Mapping:</strong> Converts complex objects (flags, stats) to structured DTOs</li>
 *   <li><strong>List Extraction:</strong> Extracts active elements from sets and bit flags</li>
 *   <li><strong>Metadata Calculation:</strong> Computes derived properties for UI convenience</li>
 * </ul>
 * 
 * <p>The mapper handles the following complex domain structures:</p>
 * <ul>
 *   <li><strong>Target Information:</strong> {@link TargetFlags} → {@link MagicDisplayDTO.TargetInfo}</li>
 *   <li><strong>Attack Information:</strong> {@link AttackFlags} → {@link MagicDisplayDTO.AttackInfo}</li>
 *   <li><strong>Status Effects:</strong> {@link StatusEffectSet} → Active status lists</li>
 *   <li><strong>Junction Data:</strong> Complex junction structures → Flattened DTOs</li>
 *   <li><strong>GF Compatibility:</strong> {@link GFCompatibilitySet} → {@link GFCompatibilityDTO}</li>
 * </ul>
 * 
 * <p>Special handling for newly created magic:</p>
 * <ul>
 *   <li>Debug logging for development and troubleshooting</li>
 *   <li>Proper metadata flags for UI differentiation</li>
 *   <li>Translation support for multi-language displays</li>
 * </ul>
 * 
 * @author FF8 Magic Creator Team
 * @version 1.0
 * @since 1.0
 */
public class MagicDataToDtoMapper {
    
    /**
     * Converts a single MagicData domain entity to a MagicDisplayDTO.
     * 
     * <p>This method performs comprehensive mapping of a {@link MagicData} entity
     * to a {@link MagicDisplayDTO}, handling all nested structures and complex
     * mappings required for proper presentation layer consumption.</p>
     * 
     * <p>Mapping process:</p>
     * <ul>
     *   <li>Maps all basic properties (ID, name, power, element, etc.)</li>
     *   <li>Converts complex flag structures to structured DTOs</li>
     *   <li>Extracts active status effects into lists</li>
     *   <li>Maps junction data to flattened DTOs</li>
     *   <li>Calculates derived metadata for UI convenience</li>
     * </ul>
     * 
     * <p>The method includes special handling for newly created magic spells,
     * providing debug logging to assist with development and troubleshooting.</p>
     * 
     * @param magic The MagicData domain entity to convert, may be null
     * @return The converted MagicDisplayDTO, or null if input is null
     */
    public MagicDisplayDTO toDto(MagicData magic) {
        if (magic == null) {
            return null;
        }
        
        // Debug logging for newly created magic
        if (magic.isNewlyCreated()) {
            System.out.println("DEBUG: Converting newly created magic to DTO: " + magic.getExtractedSpellName() + " (index: " + magic.getIndex() + ")");
        }
        
        return new MagicDisplayDTO(
            magic.getIndex(),
            magic.getMagicID(),
            magic.getExtractedSpellName(),
            magic.getExtractedSpellDescription(),
            magic.getTranslations(), // Include translations
            magic.getSpellPower(),
            magic.getElement(),
            magic.getAttackType(),
            magic.getDrawResist(),
            magic.getHitCount(),
            magic.getStatusAttackEnabler(),
            // TargetInfo
            mapTargetInfo(magic.getTargetInfo()),
            // AttackInfo
            mapAttackInfo(magic.getAttackFlags()),
            // Active status effects
            magic.getStatusEffects().getActiveStatuses(),
            // Junction DTOs
            mapJunctionStats(magic.getJunctionStats()),
            mapJunctionElemental(magic.getJunctionElemental()),
            mapJunctionStatus(magic.getJunctionStatus()),
            mapGFCompatibility(magic.getGfCompatibility()),
            // Metadata
            magic.hasStatusEffects(),
            magic.hasJunctionBonuses(),
            magic.isCurative(),
            false, // not modified initially
            magic.isNewlyCreated()
        );
    }
    
    /**
     * Converts a list of MagicData entities to MagicDisplayDTOs.
     * 
     * <p>This method provides batch conversion of multiple {@link MagicData} entities
     * to their corresponding DTOs. It handles null inputs gracefully and maintains
     * the order of the input list.</p>
     * 
     * <p>This method is commonly used when:</p>
     * <ul>
     *   <li>Loading all magic data for UI display</li>
     *   <li>Filtering and displaying subsets of magic data</li>
     *   <li>Preparing data for export or serialization</li>
     * </ul>
     * 
     * @param magicDataList The list of MagicData entities to convert, may be null
     * @return List of converted MagicDisplayDTOs, empty list if input is null
     */
    public List<MagicDisplayDTO> toDtoList(List<MagicData> magicDataList) {
        if (magicDataList == null) {
            return List.of();
        }
        
        return magicDataList.stream()
            .map(this::toDto)
            .toList();
    }
    
    /**
     * Maps TargetFlags domain object to TargetInfo DTO.
     * 
     * <p>This method converts the complex {@link TargetFlags} domain object into
     * a flattened {@link MagicDisplayDTO.TargetInfo} DTO suitable for UI consumption.
     * It extracts both boolean flags and active flag indices for comprehensive
     * presentation layer support.</p>
     * 
     * <p>Mapping includes:</p>
     * <ul>
     *   <li><strong>Boolean flags:</strong> Direct mapping of primary target characteristics</li>
     *   <li><strong>Active flags:</strong> List of bit indices for detailed flag information</li>
     *   <li><strong>Null safety:</strong> Provides default values for null input</li>
     * </ul>
     * 
     * @param targetFlags The TargetFlags domain object to convert, may be null
     * @return TargetInfo DTO with mapped values, or default values if input is null
     */
    private MagicDisplayDTO.TargetInfo mapTargetInfo(TargetFlags targetFlags) {
        if (targetFlags == null) {
            return new MagicDisplayDTO.TargetInfo(false, false, false, false, List.of());
        }
        
        return new MagicDisplayDTO.TargetInfo(
            targetFlags.isDead(),
            targetFlags.isSingle(),
            targetFlags.isEnemy(),
            targetFlags.isSingleSide(),
            targetFlags.getActiveFlags().stream().map(TargetFlag::getBitIndex).toList()
        );
    }
    
    /**
     * Maps AttackFlags domain object to AttackInfo DTO.
     * 
     * <p>This method converts the complex {@link AttackFlags} domain object into
     * a flattened {@link MagicDisplayDTO.AttackInfo} DTO suitable for UI consumption.
     * It extracts both boolean flags and active flag indices for comprehensive
     * attack behavior representation.</p>
     * 
     * <p>Mapping includes:</p>
     * <ul>
     *   <li><strong>Boolean flags:</strong> Direct mapping of primary attack characteristics</li>
     *   <li><strong>Active flags:</strong> List of bit indices for detailed flag information</li>
     *   <li><strong>Null safety:</strong> Provides default values for null input</li>
     * </ul>
     * 
     * @param attackFlags The AttackFlags domain object to convert, may be null
     * @return AttackInfo DTO with mapped values, or default values if input is null
     */
    private MagicDisplayDTO.AttackInfo mapAttackInfo(AttackFlags attackFlags) {
        if (attackFlags == null) {
            return new MagicDisplayDTO.AttackInfo(false, false, false, false, List.of());
        }
        
        return new MagicDisplayDTO.AttackInfo(
            attackFlags.isShelled(),
            attackFlags.isReflected(),
            attackFlags.isBreakDamageLimit(),
            attackFlags.isRevive(),
            attackFlags.getActiveFlags().stream().map(AttackFlag::getBitIndex).toList()
        );
    }
    
    /**
     * Maps JunctionStats domain object to JunctionStatsDTO.
     * 
     * <p>This method converts the {@link JunctionStats} domain object containing
     * character stat bonuses into a flattened {@link JunctionStatsDTO} suitable
     * for UI display and manipulation.</p>
     * 
     * <p>The mapping includes all nine character statistics:</p>
     * <ul>
     *   <li><strong>HP:</strong> Hit Points bonus</li>
     *   <li><strong>STR:</strong> Strength bonus</li>
     *   <li><strong>VIT:</strong> Vitality bonus</li>
     *   <li><strong>MAG:</strong> Magic bonus</li>
     *   <li><strong>SPR:</strong> Spirit bonus</li>
     *   <li><strong>SPD:</strong> Speed bonus</li>
     *   <li><strong>EVA:</strong> Evasion bonus</li>
     *   <li><strong>HIT:</strong> Hit accuracy bonus</li>
     *   <li><strong>LCK:</strong> Luck bonus</li>
     * </ul>
     * 
     * @param stats The JunctionStats domain object to convert, may be null
     * @return JunctionStatsDTO with mapped values, or empty DTO if input is null
     */
    private JunctionStatsDTO mapJunctionStats(JunctionStats stats) {
        if (stats == null) {
            return JunctionStatsDTO.empty();
        }
        
        return new JunctionStatsDTO(
            stats.getHp(),
            stats.getStr(),
            stats.getVit(),
            stats.getMag(),
            stats.getSpr(),
            stats.getSpd(),
            stats.getEva(),
            stats.getHit(),
            stats.getLuck()
        );
    }
    
    /**
     * Maps JunctionElemental domain object to JunctionElementalDTO.
     * 
     * <p>This method converts the {@link JunctionElemental} domain object containing
     * elemental attack and defense properties into a flattened {@link JunctionElementalDTO}
     * suitable for UI display and manipulation.</p>
     * 
     * <p>The mapping includes:</p>
     * <ul>
     *   <li><strong>Attack Element:</strong> The element added to attacks when junctioned</li>
     *   <li><strong>Attack Value:</strong> The strength of the elemental attack bonus</li>
     *   <li><strong>Defense Elements:</strong> List of elements this magic provides defense against</li>
     *   <li><strong>Defense Value:</strong> The strength of the elemental defense bonus</li>
     * </ul>
     * 
     * @param elemental The JunctionElemental domain object to convert, may be null
     * @return JunctionElementalDTO with mapped values, or empty DTO if input is null
     */
    private JunctionElementalDTO mapJunctionElemental(JunctionElemental elemental) {
        if (elemental == null) {
            return JunctionElementalDTO.empty();
        }
        
        return new JunctionElementalDTO(
            elemental.getAttackElement(),
            elemental.getAttackValue(),
            elemental.getDefenseElements(),
            elemental.getDefenseValue()
        );
    }
    
    /**
     * Maps JunctionStatusEffects domain object to JunctionStatusDTO.
     * 
     * <p>This method converts the {@link JunctionStatusEffects} domain object containing
     * status effect attack and defense properties into a flattened {@link JunctionStatusDTO}
     * suitable for UI display and manipulation.</p>
     * 
     * <p>The mapping includes:</p>
     * <ul>
     *   <li><strong>Attack Statuses:</strong> List of status effects added to attacks</li>
     *   <li><strong>Attack Value:</strong> The strength/probability of status attack</li>
     *   <li><strong>Defense Statuses:</strong> List of status effects defended against</li>
     *   <li><strong>Defense Value:</strong> The strength of status defense</li>
     * </ul>
     * 
     * @param status The JunctionStatusEffects domain object to convert, may be null
     * @return JunctionStatusDTO with mapped values, or empty DTO if input is null
     */
    private JunctionStatusDTO mapJunctionStatus(JunctionStatusEffects status) {
        if (status == null) {
            return JunctionStatusDTO.empty();
        }
        
        return new JunctionStatusDTO(
            status.getAttackStatuses().getActiveStatuses(),
            status.getAttackValue(),
            status.getDefenseStatuses().getActiveStatuses(),
            status.getDefenseValue()
        );
    }
    
    /**
     * Maps GFCompatibilitySet domain object to GFCompatibilityDTO.
     * 
     * <p>This method converts the {@link GFCompatibilitySet} domain object containing
     * Guardian Force compatibility information into a flattened {@link GFCompatibilityDTO}
     * suitable for UI display and manipulation.</p>
     * 
     * <p>The mapping includes:</p>
     * <ul>
     *   <li><strong>Compatibility Map:</strong> Complete mapping of all 16 Guardian Forces</li>
     *   <li><strong>Default Values:</strong> Provides default compatibility for null input</li>
     *   <li><strong>Comprehensive Coverage:</strong> Ensures all GFs are represented</li>
     * </ul>
     * 
     * @param compatibility The GFCompatibilitySet domain object to convert, may be null
     * @return GFCompatibilityDTO with mapped values, or default compatibility if input is null
     */
    private GFCompatibilityDTO mapGFCompatibility(GFCompatibilitySet compatibility) {
        if (compatibility == null) {
            return GFCompatibilityDTO.createDefault();
        }
        
        return new GFCompatibilityDTO(compatibility.getAllCompatibilities());
    }
} 