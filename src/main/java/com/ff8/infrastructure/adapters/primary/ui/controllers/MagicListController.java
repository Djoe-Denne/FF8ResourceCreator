package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.infrastructure.adapters.primary.ui.models.MagicListModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the magic list view.
 * Handles magic selection and search functionality.
 */
public class MagicListController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MagicListController.class);
    
    @FXML private TextField searchField;
    @FXML private TableView<MagicDisplayDTO> magicTable;
    @FXML private TableColumn<MagicDisplayDTO, Integer> indexColumn;
    @FXML private TableColumn<MagicDisplayDTO, Integer> idColumn;
    @FXML private TableColumn<MagicDisplayDTO, String> nameColumn;
    @FXML private TableColumn<MagicDisplayDTO, Integer> powerColumn;
    @FXML private TableColumn<MagicDisplayDTO, String> elementColumn;
    @FXML private TableColumn<MagicDisplayDTO, String> typeColumn;
    @FXML private Label countLabel;
    
    private MagicListModel magicListModel;
    private MainController mainController;
    
    // Default constructor for FXML
    public MagicListController() {
        logger.info("MagicListController created");
    }
    
    // Constructor with ApplicationConfig for dependency injection
    public MagicListController(com.ff8.infrastructure.config.ApplicationConfig config) {
        this();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupSearchField();
        
        logger.info("MagicListController initialized");
    }
    
    public void setMagicListModel(MagicListModel model) {
        this.magicListModel = model;
        logger.info("MagicListController: Setting magic list model");
        setupBindings();
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    private void setupTableColumns() {
        // Configure table columns - use callback functions for record accessor methods
        indexColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().index()).asObject());
        idColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().magicID()).asObject());
        nameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().spellName()));
        powerColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().spellPower()).asObject());
        elementColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().element().getDisplayName()));
        typeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().attackType().getDisplayName()));
        
        // Set column widths
        indexColumn.setPrefWidth(50);
        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(150);
        powerColumn.setPrefWidth(80);
        elementColumn.setPrefWidth(100);
        typeColumn.setPrefWidth(100);
        
        // Set selection mode
        magicTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Handle selection changes
        magicTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (magicListModel != null) {
                magicListModel.setSelectedMagic(newSelection);
            }
        });
    }
    
    private void setupSearchField() {
        searchField.setPromptText("Search magic by name, ID, element, or type...");
    }
    
    private void setupBindings() {
        if (magicListModel == null) return;
        
        // Bind table data
        logger.info("MagicListController: Binding table data, sorted magic list size: {}", 
                   magicListModel.getSortedMagic().size());
        magicTable.setItems(magicListModel.getSortedMagic());
        
        // Bind search field
        searchField.textProperty().bindBidirectional(magicListModel.searchTextProperty());
        
        // Bind selection
        magicListModel.selectedMagicProperty().addListener((obs, oldMagic, newMagic) -> {
            if (newMagic != null && magicTable.getSelectionModel().getSelectedItem() != newMagic) {
                magicTable.getSelectionModel().select(newMagic);
                magicTable.scrollTo(newMagic);
            }
        });
        
        // Update count label
        magicListModel.getSortedMagic().addListener((javafx.collections.ListChangeListener<MagicDisplayDTO>) change -> 
            updateCountLabel());
        updateCountLabel();
        
        logger.info("MagicListController bindings established");
    }
    
    private void updateCountLabel() {
        if (magicListModel != null) {
            int filtered = magicListModel.getFilteredCount();
            int total = magicListModel.getTotalCount();
            
            if (filtered == total) {
                countLabel.setText(String.format("%d spells", total));
            } else {
                countLabel.setText(String.format("%d of %d spells", filtered, total));
            }
        } else {
            countLabel.setText("No data");
        }
    }
    
    @FXML
    private void clearSearch() {
        if (magicListModel != null) {
            magicListModel.setSearchText("");
        }
    }
    
    /**
     * Refresh the table display
     */
    public void refresh() {
        magicTable.refresh();
    }
} 