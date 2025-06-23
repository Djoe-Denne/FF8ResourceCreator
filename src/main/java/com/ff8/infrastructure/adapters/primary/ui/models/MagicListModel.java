package com.ff8.infrastructure.adapters.primary.ui.models;

import com.ff8.application.dto.MagicDisplayDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * Observable model for magic list state management.
 * Provides data binding for the magic list view and filtering capabilities.
 */
public class MagicListModel {
    private static final Logger logger = LoggerFactory.getLogger(MagicListModel.class);
    
    // Observable properties
    private final ObservableList<MagicDisplayDTO> allMagic = FXCollections.observableArrayList();
    private final FilteredList<MagicDisplayDTO> filteredMagic = new FilteredList<>(allMagic);
    private final SortedList<MagicDisplayDTO> sortedMagic = new SortedList<>(filteredMagic);
    
    private final ObjectProperty<MagicDisplayDTO> selectedMagic = new SimpleObjectProperty<>();
    private final StringProperty searchText = new SimpleStringProperty("");
    private final BooleanProperty hasData = new SimpleBooleanProperty(false);
    
    public MagicListModel() {
        setupBindings();
        logger.info("MagicListModel initialized");
    }
    
    private void setupBindings() {
        // Update hasData property when magic list changes
        hasData.bind(javafx.beans.binding.Bindings.isEmpty(allMagic).not());
        
        // Set up search filtering
        searchText.addListener((observable, oldValue, newValue) -> updateFilter());
        
        // Clear selection when filter changes
        filteredMagic.addListener((javafx.collections.ListChangeListener<MagicDisplayDTO>) change -> {
            if (selectedMagic.get() != null && !filteredMagic.contains(selectedMagic.get())) {
                selectedMagic.set(null);
            }
        });
    }
    
    /**
     * Set the complete list of magic spells
     */
    public void setMagicList(List<MagicDisplayDTO> magicList) {
        logger.info("Setting magic list with {} spells", magicList.size());
        for (int i = 0; i < Math.min(5, magicList.size()); i++) {
            MagicDisplayDTO magic = magicList.get(i);
            logger.info("Magic {}: index={}, magicID={}, name='{}'", 
                       i, magic.index(), magic.magicID(), magic.spellName());
        }
        if (magicList.size() > 5) {
            logger.info("... and {} more entries", magicList.size() - 5);
        }
        
        allMagic.setAll(magicList);
        logger.info("Magic list updated with {} spells, filtered list has {} entries", 
                   magicList.size(), filteredMagic.size());
        
        // Clear selection and search
        selectedMagic.set(null);
        searchText.set("");
    }
    
    /**
     * Update a single magic entry by index (unique identifier)
     */
    public void updateMagic(MagicDisplayDTO updatedMagic) {
        int index = -1;
        for (int i = 0; i < allMagic.size(); i++) {
            if (allMagic.get(i).index() == updatedMagic.index()) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            allMagic.set(index, updatedMagic);
            logger.debug("Updated magic at list position {}, kernel index {}: {}", 
                        index, updatedMagic.index(), updatedMagic.spellName());
        } else {
            logger.warn("Could not find magic with kernel index {} to update", updatedMagic.index());
        }
    }
    
    /**
     * Clear all magic data
     */
    public void clearMagic() {
        allMagic.clear();
        selectedMagic.set(null);
        searchText.set("");
        logger.info("Magic list cleared");
    }
    
    private void updateFilter() {
        String search = searchText.get();
        if (search == null || search.trim().isEmpty()) {
            filteredMagic.setPredicate(null);
        } else {
            String lowerCaseSearch = search.toLowerCase().trim();
            filteredMagic.setPredicate(createSearchPredicate(lowerCaseSearch));
        }
    }
    
    private Predicate<MagicDisplayDTO> createSearchPredicate(String searchTerm) {
        return magic -> {
            // Search in spell name
            if (magic.spellName().toLowerCase().contains(searchTerm)) {
                return true;
            }
            
            // Search in kernel index
            if (String.valueOf(magic.index()).contains(searchTerm)) {
                return true;
            }
            
            // Search in magic ID (as string)
            if (String.valueOf(magic.magicID()).contains(searchTerm)) {
                return true;
            }
            
            // Search in element
            if (magic.element().toString().toLowerCase().contains(searchTerm)) {
                return true;
            }
            
            // Search in attack type
            if (magic.attackType().toString().toLowerCase().contains(searchTerm)) {
                return true;
            }
            
            return false;
        };
    }
    
    /**
     * Select magic by kernel index (unique identifier)
     */
    public void selectMagicByIndex(int kernelIndex) {
        MagicDisplayDTO found = allMagic.stream()
            .filter(magic -> magic.index() == kernelIndex)
            .findFirst()
            .orElse(null);
        
        selectedMagic.set(found);
        if (found != null) {
            logger.debug("Selected magic by kernel index {}: {}", kernelIndex, found.spellName());
        }
    }
    
    /**
     * Select magic by ID (may not be unique - selects first occurrence)
     * @deprecated Use selectMagicByIndex instead for unique identification
     */
    @Deprecated
    public void selectMagicById(int magicId) {
        MagicDisplayDTO found = allMagic.stream()
            .filter(magic -> magic.magicID() == magicId)
            .findFirst()
            .orElse(null);
        
        selectedMagic.set(found);
        if (found != null) {
            logger.debug("Selected magic by ID {} (first occurrence): {}", magicId, found.spellName());
            logger.warn("selectMagicById is deprecated - magic IDs may not be unique. Use selectMagicByIndex instead.");
        }
    }
    
    // Property getters
    public ObservableList<MagicDisplayDTO> getSortedMagic() {
        return sortedMagic;
    }
    
    public ObjectProperty<MagicDisplayDTO> selectedMagicProperty() {
        return selectedMagic;
    }
    
    public MagicDisplayDTO getSelectedMagic() {
        return selectedMagic.get();
    }
    
    public void setSelectedMagic(MagicDisplayDTO magic) {
        selectedMagic.set(magic);
    }
    
    public StringProperty searchTextProperty() {
        return searchText;
    }
    
    public String getSearchText() {
        return searchText.get();
    }
    
    public void setSearchText(String text) {
        searchText.set(text);
    }
    
    public BooleanProperty hasDataProperty() {
        return hasData;
    }
    
    public boolean hasData() {
        return hasData.get();
    }
    
    /**
     * Get the total count of magic spells
     */
    public int getTotalCount() {
        return allMagic.size();
    }
    
    /**
     * Get the filtered count of magic spells
     */
    public int getFilteredCount() {
        return filteredMagic.size();
    }
} 