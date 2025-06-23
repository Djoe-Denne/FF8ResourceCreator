package com.ff8.infrastructure.adapters.primary.ui.components;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.UnaryOperator;

/**
 * Enhanced numeric spinner with improved validation and user experience.
 * Provides better handling of numeric input and prevents invalid values.
 */
public class NumericSpinner extends Spinner<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(NumericSpinner.class);
    
    private final int minValue;
    private final int maxValue;
    
    public NumericSpinner(int minValue, int maxValue, int initialValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        
        // Set up value factory
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initialValue);
        setValueFactory(valueFactory);
        
        // Set up text formatter for validation
        setupTextFormatter();
        
        // Make editable
        setEditable(true);
        
        logger.debug("Created NumericSpinner with range [{}, {}], initial value: {}", 
                    minValue, maxValue, initialValue);
    }
    
    private void setupTextFormatter() {
        // Create a text formatter that only allows valid numeric input
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            
            // Allow empty text (will be handled by converter)
            if (newText.isEmpty()) {
                return change;
            }
            
            try {
                int value = Integer.parseInt(newText);
                
                // Allow typing even if temporarily out of range
                // The spinner will clamp to valid range when focus is lost
                if (newText.length() <= String.valueOf(maxValue).length()) {
                    return change;
                }
                
                return null; // Reject change
                
            } catch (NumberFormatException e) {
                return null; // Reject non-numeric input
            }
        };
        
        // Create converter that handles empty strings gracefully
        IntegerStringConverter converter = new IntegerStringConverter() {
            @Override
            public Integer fromString(String value) {
                if (value == null || value.trim().isEmpty()) {
                    return minValue;
                }
                
                try {
                    int intValue = Integer.parseInt(value.trim());
                    // Clamp to valid range
                    return Math.max(minValue, Math.min(maxValue, intValue));
                } catch (NumberFormatException e) {
                    return minValue;
                }
            }
        };
        
        TextFormatter<Integer> textFormatter = new TextFormatter<>(converter, minValue, filter);
        getEditor().setTextFormatter(textFormatter);
        
        // Bind the text formatter value to the spinner value
        textFormatter.valueProperty().bindBidirectional(getValueFactory().valueProperty());
        
        // Add focus listener to validate on focus loss
        getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Validate and clamp value when focus is lost
                Integer currentValue = getValue();
                if (currentValue == null) {
                    getValueFactory().setValue(minValue);
                } else if (currentValue < minValue || currentValue > maxValue) {
                    getValueFactory().setValue(Math.max(minValue, Math.min(maxValue, currentValue)));
                }
            }
        });
    }
    
    /**
     * Get the minimum allowed value
     */
    public int getMinValue() {
        return minValue;
    }
    
    /**
     * Get the maximum allowed value
     */
    public int getMaxValue() {
        return maxValue;
    }
    
    /**
     * Check if the current value is valid
     */
    public boolean isValid() {
        Integer value = getValue();
        return value != null && value >= minValue && value <= maxValue;
    }
    
    /**
     * Reset to minimum value
     */
    public void reset() {
        getValueFactory().setValue(minValue);
    }
    
    /**
     * Set value with validation
     */
    public void setValidatedValue(int value) {
        getValueFactory().setValue(Math.max(minValue, Math.min(maxValue, value)));
    }
} 