/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

/**
 * Utility class for validating user input from the GUI.
 * Prevents NumberFormatExceptions and ensures data integrity.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Prevent instantiation
    }

    /** Checks if a string is null or empty. */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Checks if a string can be safely parsed to an Integer. */
    public static boolean isInteger(String value) {
        if (isEmpty(value)) return false;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Checks if a string can be safely parsed to a Double. */
    public static boolean isDouble(String value) {
        if (isEmpty(value)) return false;
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Checks if a string is a valid positive number (useful for salaries/allowances). */
    public static boolean isPositiveDouble(String value) {
        if (!isDouble(value)) return false;
        return Double.parseDouble(value.trim()) >= 0;
    }

    /** Validates SSS format: XX-XXXXXXX-X */
    public static boolean isValidSssFormat(String sss) {
        if (isEmpty(sss)) return true; // Return false here if SSS is strictly required
        return sss.trim().matches("\\d{2}-\\d{7}-\\d{1}");
    }

    /** Validates TIN format: XXX-XXX-XXX-XXX */
    public static boolean isValidTinFormat(String tin) {
        if (isEmpty(tin)) return true; 
        return tin.trim().matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}");
    }
}
