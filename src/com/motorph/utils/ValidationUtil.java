/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    // Annotation: Checks if the numeric value is greater than zero.
    public static boolean isStrictlyPositiveDouble(String value) {
        if (!isDouble(value)) {
            return false;
        }
        return Double.parseDouble(value.trim()) > 0;
    }

    // Annotation: Returns a trimmed value or an empty string.
    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    // Annotation: Adds a required-field message when the value is blank.
    public static void require(List<String> errors, String label, String value) {
        if (errors == null) {
            return;
        }
        if (isEmpty(value)) {
            errors.add(label + " is required.");
        }
    }

    // Annotation: Adds a numeric validation message when the value is not a valid decimal.
    public static void requirePositiveNumber(List<String> errors, String label, String value, boolean allowZero) {
        if (errors == null) {
            return;
        }
        if (isEmpty(value)) {
            errors.add(label + " is required.");
            return;
        }
        if (!isDouble(value)) {
            errors.add(label + " must be a valid number.");
            return;
        }
        double parsed = Double.parseDouble(value.trim());
        if (allowZero) {
            if (parsed < 0) {
                errors.add(label + " cannot be negative.");
            }
        } else if (parsed <= 0) {
            errors.add(label + " must be greater than 0.");
        }
    }

    // Annotation: Checks that the date is present and not in the future.
    public static void requirePastOrPresentDate(List<String> errors, String label, LocalDate date) {
        if (errors == null) {
            return;
        }
        if (date == null) {
            errors.add(label + " is required.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            errors.add(label + " cannot be in the future.");
        }
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

    // Annotation: Returns a clear password-rule message or null when valid.
    public static String getPasswordPolicyMessage(String password) {
        if (password == null) {
            return "Password cannot be blank.";
        }

        String p = password.trim();
        if (p.isEmpty()) {
            return "Password cannot be blank.";
        }
        if (p.length() < 8) {
            return "Password must be at least 8 characters.";
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : p.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        if (!hasLetter || !hasDigit) {
            return "Password must contain at least one letter and one number.";
        }

        return null;
    }

    // Annotation: Returns true when the password follows the shared password rules.
    public static boolean isValidPassword(String password) {
        return getPasswordPolicyMessage(password) == null;
    }

    // Annotation: Aggregates employee-form validation messages for reuse across UI and ops.
    public static List<String> validateEmployeeFields(
            String lastName,
            String firstName,
            LocalDate birthday,
            String phone,
            String sss,
            String philHealth,
            String tin,
            String pagIbig,
            String position,
            String supervisor,
            String basicSalary,
            String riceAllowance,
            String phoneAllowance,
            String clothingAllowance
    ) {
        List<String> errors = new ArrayList<>();

        require(errors, "Last Name", lastName);
        require(errors, "First Name", firstName);
        requirePastOrPresentDate(errors, "Birthday", birthday);
        require(errors, "Position", position);
        require(errors, "Supervisor", supervisor);
        requirePositiveNumber(errors, "Basic Salary", basicSalary, false);
        requirePositiveNumber(errors, "Rice Allowance", riceAllowance, true);
        requirePositiveNumber(errors, "Phone Allowance", phoneAllowance, true);
        requirePositiveNumber(errors, "Clothing Allowance", clothingAllowance, true);

        if (!isValidPhoneFormat(phone)) {
            errors.add("Phone # must use 09123456789 or 0912-345-6789 format.");
        }
        if (!isValidSssFormat(sss)) {
            errors.add("SSS # must use XX-XXXXXXX-X format.");
        }
        if (!isValidPhilHealthFormat(philHealth)) {
            errors.add("PhilHealth # must use XX-XXXXXXXXX-X format.");
        }
        if (!isValidTinFormat(tin)) {
            errors.add("TIN # must use XXX-XXX-XXX-XXX format.");
        }
        if (!isValidPagIbigFormat(pagIbig)) {
            errors.add("Pag-IBIG # must use XXXX-XXXX-XXXX format.");
        }

        return errors;
    }
}
