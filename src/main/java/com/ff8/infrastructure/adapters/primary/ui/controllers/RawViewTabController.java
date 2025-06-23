package com.ff8.infrastructure.adapters.primary.ui.controllers;

import com.ff8.application.dto.MagicDisplayDTO;
import com.ff8.application.dto.RawViewDTO;
import com.ff8.application.ports.primary.RawDataViewUseCase;
import com.ff8.infrastructure.config.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Raw View tab - displays magic data as hex values with offsets.
 * Shows a table with offset, type, name, and hex value columns.
 */
public class RawViewTabController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(RawViewTabController.class);
    
    @FXML private TableView<RawFieldTableEntry> rawDataTable;
    @FXML private TableColumn<RawFieldTableEntry, String> offsetColumn;
    @FXML private TableColumn<RawFieldTableEntry, String> typeColumn;
    @FXML private TableColumn<RawFieldTableEntry, String> nameColumn;
    @FXML private TableColumn<RawFieldTableEntry, String> valueColumn;
    @FXML private Label statusLabel;
    
    private final RawDataViewUseCase rawDataViewUseCase;
    private MainController mainController;
    private MagicDisplayDTO currentMagic;
    private boolean updatingFromModel = false;
    
    private final ObservableList<RawFieldTableEntry> rawDataItems = FXCollections.observableArrayList();
    
    public RawViewTabController(ApplicationConfig config) {
        this.rawDataViewUseCase = config.getRawDataViewUseCase();
        logger.info("RawViewTabController initialized");
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        clearFields();
        
        logger.info("RawViewTabController UI initialized");
    }
    
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        
        // Listen to magic selection changes
        mainController.getMagicListModel().selectedMagicProperty().addListener(
            (obs, oldMagic, newMagic) -> loadMagicData(newMagic));
    }
    
    private void setupTable() {
        // Configure table columns
        offsetColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        
        // Set column widths
        offsetColumn.setPrefWidth(100);
        typeColumn.setPrefWidth(80);
        nameColumn.setPrefWidth(200);
        valueColumn.setPrefWidth(100);
        
        // Set table properties
        rawDataTable.setItems(rawDataItems);
        rawDataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rawDataTable.setRowFactory(tv -> {
            TableRow<RawFieldTableEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showFieldDetails(row.getItem());
                }
            });
            return row;
        });
        
        // Set initial status
        statusLabel.setText("No magic data selected");
    }
    
    private void loadMagicData(MagicDisplayDTO magic) {
        currentMagic = magic;
        updatingFromModel = true;
        
        try {
            if (magic != null) {
                logger.debug("Loading raw view for magic: {}", magic.spellName());
                
                // Get raw view data from use case
                var rawViewOptional = rawDataViewUseCase.getRawView(magic.magicID());
                
                if (rawViewOptional.isPresent()) {
                    RawViewDTO rawView = rawViewOptional.get();
                    
                    // Clear existing items
                    rawDataItems.clear();
                    
                    // Add all fields to table
                    for (RawViewDTO.RawFieldEntry field : rawView.fields()) {
                        rawDataItems.add(new RawFieldTableEntry(
                            field.offset(),
                            field.type(),
                            field.name(),
                            field.value()
                        ));
                    }
                    
                    statusLabel.setText(String.format("Showing raw data for: %s (ID: %d) - %d fields", 
                            magic.spellName(), magic.magicID(), rawView.fields().size()));
                    
                    logger.debug("Loaded {} raw fields for magic: {}", rawView.fields().size(), magic.spellName());
                } else {
                    clearFields();
                    statusLabel.setText("Failed to load raw data for magic: " + magic.spellName());
                    logger.warn("Failed to load raw view for magic ID: {}", magic.magicID());
                }
            } else {
                clearFields();
                statusLabel.setText("No magic data selected");
            }
        } finally {
            updatingFromModel = false;
        }
    }
    
    private void clearFields() {
        rawDataItems.clear();
        statusLabel.setText("No magic data selected");
    }
    
    private void showFieldDetails(RawFieldTableEntry field) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Field Details");
        alert.setHeaderText("Raw Data Field Information");
        
        String content = String.format(
            "Offset: %s\nType: %s\nName: %s\nValue: %s\nDecimal: %d\nBinary: %s",
            field.getOffset(),
            field.getType(),
            field.getName(),
            field.getValue(),
            parseHexValue(field.getValue()),
            Integer.toBinaryString(parseHexValue(field.getValue()))
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private int parseHexValue(String hexValue) {
        try {
            return Integer.parseInt(hexValue, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * JavaFX property class for table binding.
     * Uses JavaFX properties for data binding to table columns.
     */
    public static class RawFieldTableEntry {
        private final String offset;
        private final String type;
        private final String name;
        private final String value;
        
        public RawFieldTableEntry(String offset, String type, String name, String value) {
            this.offset = offset;
            this.type = type;
            this.name = name;
            this.value = value;
        }
        
        public String getOffset() { return offset; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getValue() { return value; }
    }
} 