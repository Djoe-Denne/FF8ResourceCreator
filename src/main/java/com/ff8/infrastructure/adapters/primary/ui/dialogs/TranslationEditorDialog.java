package com.ff8.infrastructure.adapters.primary.ui.dialogs;

import com.ff8.domain.entities.SpellTranslations;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dialog for editing spell translations in multiple languages.
 * Supports adding/removing languages while enforcing English as the primary language.
 */
public class TranslationEditorDialog {
    private static final Logger logger = LoggerFactory.getLogger(TranslationEditorDialog.class);
    
    private Stage dialog;
    private TableView<TranslationRow> translationTable;
    private ObservableList<TranslationRow> translationData;
    private SpellTranslations result;
    private boolean confirmed = false;
    
    /**
     * Data model for a single translation row in the table
     */
    public static class TranslationRow {
        private final SimpleStringProperty language;
        private final SimpleStringProperty name;
        private final SimpleStringProperty description;
        private final boolean isEnglish;
        
        public TranslationRow(String language, String name, String description) {
            this.language = new SimpleStringProperty(language);
            this.name = new SimpleStringProperty(name);
            this.description = new SimpleStringProperty(description);
            this.isEnglish = "English".equals(language);
        }
        
        public String getLanguage() { return language.get(); }
        public void setLanguage(String value) { 
            if (!isEnglish) { // Don't allow changing English language name
                language.set(value); 
            }
        }
        public SimpleStringProperty languageProperty() { return language; }
        
        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public SimpleStringProperty nameProperty() { return name; }
        
        public String getDescription() { return description.get(); }
        public void setDescription(String value) { description.set(value); }
        public SimpleStringProperty descriptionProperty() { return description; }
        
        public boolean isEnglish() { return isEnglish; }
    }
    
    /**
     * Create and show the translation editor dialog
     */
    public static Optional<SpellTranslations> showDialog(Stage owner, SpellTranslations translations, String fieldName) {
        TranslationEditorDialog dialog = new TranslationEditorDialog();
        return dialog.showAndWait(owner, translations, fieldName);
    }
    
    private Optional<SpellTranslations> showAndWait(Stage owner, SpellTranslations translations, String fieldName) {
        createDialog(owner, fieldName);
        loadTranslations(translations);
        
        dialog.showAndWait();
        
        if (confirmed) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
    
    private void createDialog(Stage owner, String fieldName) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Edit " + fieldName + " Translations");
        dialog.setResizable(true);
        
        // Create the main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create the table
        createTranslationTable();
        
        // Create the button panel
        HBox buttonPanel = createButtonPanel();
        
        // Create info label
        Label infoLabel = new Label("English translation is required and cannot be removed.");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666;");
        
        VBox centerPanel = new VBox(10);
        centerPanel.getChildren().addAll(infoLabel, translationTable);
        
        root.setCenter(centerPanel);
        root.setBottom(buttonPanel);
        
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add("/css/application.css");
        dialog.setScene(scene);
        
        // Set minimum size
        dialog.setMinWidth(500);
        dialog.setMinHeight(300);
        
        logger.debug("Created translation editor dialog for field: {}", fieldName);
    }
    
    private void createTranslationTable() {
        translationTable = new TableView<>();
        translationTable.setEditable(true);
        translationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Language column
        TableColumn<TranslationRow, String> languageColumn = new TableColumn<>("Language");
        languageColumn.setCellValueFactory(cellData -> cellData.getValue().languageProperty());
        languageColumn.setCellFactory(column -> new TextFieldTableCell<TranslationRow, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getTableRow() != null) {
                    TranslationRow row = getTableRow().getItem();
                    if (row != null && row.isEnglish()) {
                        setEditable(false);
                        setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666666;");
                    } else {
                        setEditable(true);
                        setStyle("");
                    }
                }
            }
        });
        languageColumn.setOnEditCommit(event -> {
            TranslationRow row = event.getRowValue();
            if (!row.isEnglish()) {
                String newValue = event.getNewValue();
                if (newValue != null && !newValue.trim().isEmpty() && !languageExists(newValue.trim(), row)) {
                    row.setLanguage(newValue.trim());
                } else {
                    // Revert to old value if invalid
                    translationTable.refresh();
                    if (newValue != null && languageExists(newValue.trim(), row)) {
                        showError("Language Already Exists", "A translation for '" + newValue.trim() + "' already exists.");
                    }
                }
            }
        });
        languageColumn.setMinWidth(120);
        languageColumn.setPrefWidth(150);
        
        // Name column
        TableColumn<TranslationRow, String> nameColumn = new TableColumn<>("Spell Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));
        nameColumn.setMinWidth(150);
        
        // Description column
        TableColumn<TranslationRow, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setOnEditCommit(event -> event.getRowValue().setDescription(event.getNewValue()));
        descriptionColumn.setMinWidth(200);
        
        translationTable.getColumns().addAll(languageColumn, nameColumn, descriptionColumn);
        
        translationData = FXCollections.observableArrayList();
        translationTable.setItems(translationData);
        
        // Selection handler for remove button state
        translationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // This will be used to enable/disable the remove button
        });
    }
    
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(10, 0, 0, 0));
        
        // Add row button
        Button addButton = new Button("Add Language");
        addButton.setOnAction(e -> addNewLanguage());
        
        // Remove row button
        Button removeButton = new Button("Remove Language");
        removeButton.setOnAction(e -> removeSelectedLanguage());
        
        // Create a boolean binding for the disable property
        javafx.beans.binding.BooleanBinding disableBinding = javafx.beans.binding.Bindings.createBooleanBinding(() -> {
            TranslationRow selected = translationTable.getSelectionModel().getSelectedItem();
            return selected == null || selected.isEnglish();
        }, translationTable.getSelectionModel().selectedItemProperty());
        
        removeButton.disableProperty().bind(disableBinding);
        
        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // OK and Cancel buttons
        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> confirmChanges());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonPanel.getChildren().addAll(addButton, removeButton, spacer, okButton, cancelButton);
        
        return buttonPanel;
    }
    
    private void loadTranslations(SpellTranslations translations) {
        translationData.clear();
        
        for (Map.Entry<String, SpellTranslations.Translation> entry : translations.getAllTranslations().entrySet()) {
            String language = entry.getKey();
            SpellTranslations.Translation translation = entry.getValue();
            translationData.add(new TranslationRow(language, translation.getName(), translation.getDescription()));
        }
        
        logger.debug("Loaded {} translations into editor", translationData.size());
    }
    
    private void addNewLanguage() {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Add Language");
        inputDialog.setHeaderText("Enter Language Name");
        inputDialog.setContentText("Language:");
        
        Optional<String> result = inputDialog.showAndWait();
        result.ifPresent(language -> {
            String trimmedLanguage = language.trim();
            if (!trimmedLanguage.isEmpty() && !languageExists(trimmedLanguage, null)) {
                translationData.add(new TranslationRow(trimmedLanguage, "", ""));
                logger.debug("Added new language: {}", trimmedLanguage);
            } else if (languageExists(trimmedLanguage, null)) {
                showError("Language Already Exists", "A translation for '" + trimmedLanguage + "' already exists.");
            }
        });
    }
    
    private void removeSelectedLanguage() {
        TranslationRow selected = translationTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isEnglish()) {
            translationData.remove(selected);
            logger.debug("Removed language: {}", selected.getLanguage());
        }
    }
    
    private boolean languageExists(String language, TranslationRow excludeRow) {
        return translationData.stream()
            .filter(row -> row != excludeRow)
            .anyMatch(row -> row.getLanguage().equalsIgnoreCase(language));
    }
    
    private void confirmChanges() {
        try {
            // Build the result translations
            Map<String, SpellTranslations.Translation> translationMap = new LinkedHashMap<>();
            
            for (TranslationRow row : translationData) {
                String language = row.getLanguage().trim();
                if (!language.isEmpty()) {
                    translationMap.put(language, new SpellTranslations.Translation(
                        row.getName() != null ? row.getName() : "",
                        row.getDescription() != null ? row.getDescription() : ""
                    ));
                }
            }
            
            // Ensure English exists
            if (!translationMap.containsKey("English")) {
                translationMap.put("English", new SpellTranslations.Translation("", ""));
            }
            
            result = new SpellTranslations(translationMap);
            confirmed = true;
            dialog.close();
            
            logger.debug("Confirmed translation changes with {} languages", translationMap.size());
            
        } catch (Exception e) {
            logger.error("Error saving translations", e);
            showError("Save Error", "Failed to save translations: " + e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 