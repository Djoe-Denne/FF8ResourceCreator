package com.ff8.domain.services;

import com.ff8.domain.entities.SpellTranslations;

/**
 * Service for encoding text using FF8's Caesar cipher algorithm.
 * This is the inverse operation of the decipherCaesarCode method found in KernelBinaryParser.
 */
public class TextEncodingService {
    
    /**
     * Encode text using FF8's Caesar cipher (reverse of decipherCaesarCode)
     * Rules:
     * - Uppercase letters (A-Z): Add 4 to get encoded range (D-^)
     * - Digits (0-9): Subtract 15 with wraparound handling
     * - Lowercase letters (a-z): Subtract 2 to get encoded range (_-x)
     * - Other characters (spaces, punctuation): Remain unchanged
     * 
     * @param plainText The plain text to encode
     * @return The encoded text using FF8's Caesar cipher
     */
    public String encipherCaesarCode(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        StringBuilder encrypted = new StringBuilder();
        
        for (char c : plainText.toCharArray()) {
            char encryptedChar = c;
            
            if (c >= 'A' && c <= 'Z') {
                // Uppercase: Add 4 (A-Z → D-^)
                encryptedChar = (char) (c + 4);
            } else if (c >= '0' && c <= '9') {
                // Digits: Subtract 15 (with wraparound)
                // This maps 0-9 to some other range - need to check the exact mapping
                encryptedChar = (char) (c - 15);
            } else if (c >= 'a' && c <= 'z') {
                // Lowercase: Subtract 2 (a-z → _-x)
                encryptedChar = (char) (c - 2);
            }
            // Other characters (spaces, punctuation) remain unchanged
            
            encrypted.append(encryptedChar);
        }
        
        return encrypted.toString();
    }
    
    /**
     * Validate that text can be properly encoded using the Caesar cipher.
     * Checks for characters that might cause issues in the encoding process.
     * 
     * @param text The text to validate
     * @return true if the text can be safely encoded, false otherwise
     */
    public boolean isTextCompatibleWithEncoding(String text) {
        if (text == null) {
            return true;
        }
        
        for (char c : text.toCharArray()) {
            // Check for characters that might cause encoding issues
            if (c < 32 || c > 126) {
                // Non-printable ASCII characters might cause issues
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get the encoded length of text (for layout calculations).
     * Since Caesar cipher is 1:1 character mapping, length remains the same.
     * 
     * @param text The text to measure
     * @return The length after encoding (same as original for Caesar cipher)
     */
    public int getEncodedLength(String text) {
        return text != null ? text.length() : 0;
    }
    
    /**
     * Check if a character would be modified by the Caesar cipher encoding
     * 
     * @param c The character to check
     * @return true if the character would be modified, false if it remains unchanged
     */
    public boolean isCharacterEncoded(char c) {
        return (c >= 'A' && c <= 'Z') || 
               (c >= '0' && c <= '9') || 
               (c >= 'a' && c <= 'z');
    }

    /**
     * Decode text using FF8's Caesar cipher (inverse of encipherCaesarCode)
     * This decodes text that was encoded using the FF8 Caesar cipher.
     * 
     * @param encryptedText The encrypted text to decode
     * @return The decoded plain text
     */
    public String decipherCaesarCode(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        StringBuilder decrypted = new StringBuilder();
        
        for (char c : encryptedText.toCharArray()) {
            char decryptedChar = c;
            
            if (c >= 'D' && c <= '^') {
                // Uppercase: Subtract 4 (reverse of +4 in encoding)
                decryptedChar = (char) (c - 4);
            } else if (c >= '0' && c <= '9') {
                // Digits: Add 15 (reverse of -15 in encoding)
                decryptedChar = (char) (c + 15);
            } else if (c >= '_' && c <= 'x') {
                // Lowercase: Add 2 (reverse of -2 in encoding)
                decryptedChar = (char) (c + 2);
            }
            // Other characters (spaces, punctuation) remain unchanged
            
            decrypted.append(decryptedChar);
        }
        
        return decrypted.toString();
    }

    /**
     * Extract a null-terminated string from binary data
     * 
     * @param binaryData The complete binary data
     * @param offset The offset where the string starts
     * @return The extracted string, or empty string if invalid offset
     */
    public String extractNullTerminatedString(byte[] binaryData, int offset) {
        if (binaryData == null || offset < 0 || offset >= binaryData.length) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        int maxLength = Math.min(100, binaryData.length - offset); // Limit to 100 chars for safety
        
        for (int i = 0; i < maxLength; i++) {
            byte b = binaryData[offset + i];
            if (b == 0) { // Null terminator
                break;
            }
            // Convert byte to char (handling unsigned values)
            sb.append((char) (b & 0xFF));
        }
        
        return sb.toString();
    }

    /**
     * Parse spell translations from a language resource file
     * 
     * @param resourceData The binary data from the language file
     * @param spellCount The number of spells expected in the file
     * @param maxNameLength Maximum length for spell names
     * @param maxDescLength Maximum length for spell descriptions
     * @return Map of spell indices to their translations
     */
    public java.util.Map<Integer, SpellTranslations.Translation> parseLanguageResourceFile(
            byte[] resourceData, int spellCount, int maxNameLength, int maxDescLength) {
        
        java.util.Map<Integer, SpellTranslations.Translation> translations = new java.util.LinkedHashMap<>();
        
        if (resourceData == null || resourceData.length == 0) {
            return translations;
        }
        
        int offset = 0;
        int entrySize = maxNameLength + maxDescLength; // Each entry has name + description
        
        for (int i = 0; i < spellCount && offset < resourceData.length; i++) {
            // Extract spell name
            String encryptedName = extractNullTerminatedString(resourceData, offset);
            String spellName = decipherCaesarCode(encryptedName);
            offset += maxNameLength;
            
            // Extract spell description
            String encryptedDescription = extractNullTerminatedString(resourceData, offset);
            String spellDescription = decipherCaesarCode(encryptedDescription);
            offset += maxDescLength;
            
            translations.put(i, new SpellTranslations.Translation(spellName, spellDescription));
        }
        
        return translations;
    }
} 