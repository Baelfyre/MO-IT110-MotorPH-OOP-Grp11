/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

/**
 * Utility class for validating user input from the GUI. Prevents parsing errors
 * and helps keep saved data valid.
 *
 * @author ACER
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Annotation: Utility class, no instances.
    }

    // Annotation: Returns true when the value is null, empty, or whitespace only.
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Annotation: Checks if the value can be parsed as an integer.
    public static boolean isInteger(String value) {
        if (isEmpty(value)) {
            return false;
        }

        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Annotation: Checks if the value can be parsed as a decimal number.
    public static boolean isDouble(String value) {
        if (isEmpty(value)) {
            return false;
        }

        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Annotation: Checks if the value is a valid non-negative decimal number.
    public static boolean isPositiveDouble(String value) {
        if (!isDouble(value)) {
            return false;
        }

        return Double.parseDouble(value.trim()) >= 0;
    }

    // Annotation: Validates SSS format as XX-XXXXXXX-X.
    public static boolean isValidSssFormat(String sss) {
        if (isEmpty(sss)) {
            return true;
        }

        return sss.trim().matches("\\d{2}-\\d{7}-\\d{1}");
    }

    // Annotation: Validates TIN format as XXX-XXX-XXX-XXX.
    public static boolean isValidTinFormat(String tin) {
        if (isEmpty(tin)) {
            return true;
        }

        return tin.trim().matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}");
    }

    // Annotation: Validates Pag-IBIG format as XXXX-XXXX-XXXX.
    public static boolean isValidPagIbigFormat(String pagIbig) {
        if (isEmpty(pagIbig)) {
            return true;
        }

        return pagIbig.trim().matches("\\d{4}-\\d{4}-\\d{4}");
    }

    // Annotation: Validates PhilHealth format as XX-XXXXXXXXX-X.
    public static boolean isValidPhilHealthFormat(String philHealth) {
        if (isEmpty(philHealth)) {
            return true;
        }

        return philHealth.trim().matches("\\d{2}-\\d{9}-\\d{1}");
    }

    // Annotation: Validates PH mobile number as 09123456789 or 0912-345-6789.
    public static boolean isValidPhoneFormat(String phone) {
        if (isEmpty(phone)) {
            return true;
        }

        String value = phone.trim();
        return value.matches("^09\\d{9}$") || value.matches("^09\\d{2}-\\d{3}-\\d{4}$");
    }

    // Annotation: Checks if the value contains only numbers, dashes, and spaces.
    public static boolean isNumbersAndDashesOnly(String value) {
        if (isEmpty(value)) {
            return true;
        }

        return value.trim().matches("^[0-9\\-\\s]+$");
    }
}
