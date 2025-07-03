package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.infrastructure.adapters.primary.ui.models.MagicListModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
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
        
        // Set refresh callback to trigger table refresh when new magic is added
        model.setRefreshCallback(() -> {
            logger.info("Triggering table refresh from model callback");
            Platform.runLater(() -> magicTable.refresh());
        });
        
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
        
        // Set row factory to apply styling for newly created magic and add context menu
        magicTable.setRowFactory(tv -> {
            TableRow<MagicDisplayDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(MagicDisplayDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    // Remove existing style classes
                    getStyleClass().removeAll("newly-created");
                    
                    if (item != null && !empty) {
                        // Apply styling for newly created magic
                        if (item.isNewlyCreated()) {
                            getStyleClass().add("newly-created");
                            logger.info("Applied 'newly-created' CSS class to magic: {} (index: {})", 
                                       item.spellName(), item.index());
                        }
                    }
                }
            };
            
            // Add context menu to non-empty rows
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    ContextMenu contextMenu = createContextMenu(newItem);
                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
            
            return row;
        });
    }
    
    /**
     * Create context menu for a magic item
     */
    private ContextMenu createContextMenu(MagicDisplayDTO magic) {
        ContextMenu contextMenu = new ContextMenu();
        
        // Copy menu item
        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setOnAction(e -> copyMagic(magic));
        
        contextMenu.getItems().add(copyMenuItem);
        
        return contextMenu;
    }
    
    /**
     * Copy/duplicate the specified magic
     */
    private void copyMagic(MagicDisplayDTO magic) {
        if (mainController == null) {
            logger.error("Cannot copy magic: MainController is null");
            showError("Copy Error", "Unable to access magic editor functionality.");
            return;
        }
        
        try {
            // Create a dialog to get the new spell name
            TextInputDialog dialog = new TextInputDialog(magic.spellName() + " Copy");
            dialog.setTitle("Copy Magic Spell");
            dialog.setHeaderText("Create a copy of '" + magic.spellName() + "'");
            dialog.setContentText("Enter name for the copied spell:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String newName = result.get().trim();
                
                // Call the duplicate magic method through the main controller
                var magicEditorUseCase = mainController.getMagicEditorUseCase();
                if (magicEditorUseCase != null) {
                    MagicDisplayDTO duplicatedMagic = magicEditorUseCase.duplicateMagic(magic.index(), newName);
                    
                    logger.info("Successfully copied magic '{}' (index: {}) to '{}' (index: {})", 
                               magic.spellName(), magic.index(), 
                               duplicatedMagic.spellName(), duplicatedMagic.index());
                    
                    // Update UI state to enable export menu since we now have newly created magic
                    mainController.refreshUIState();
                    
                    // Select the newly created magic
                    Platform.runLater(() -> {
                        if (magicListModel != null) {
                            magicListModel.setSelectedMagic(duplicatedMagic);
                        }
                    });
                } else {
                    logger.error("Cannot copy magic: MagicEditorUseCase is null");
                    showError("Copy Error", "Unable to access magic editor functionality.");
                }
            }
        } catch (Exception e) {
            logger.error("Error copying magic: {}", e.getMessage(), e);
            showError("Copy Error", "Failed to copy magic: " + e.getMessage());
        }
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        
        // Update count label and refresh table when list changes
        magicListModel.getSortedMagic().addListener((javafx.collections.ListChangeListener<MagicDisplayDTO>) change -> {
            updateCountLabel();
            // Force table refresh to ensure row styling is applied
            javafx.application.Platform.runLater(() -> magicTable.refresh());
        });
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
        logger.info("Refreshing magic table display");
        magicTable.refresh();
    }
} 