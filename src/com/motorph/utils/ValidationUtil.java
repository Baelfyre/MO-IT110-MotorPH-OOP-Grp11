/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating user input from the GUI. Prevents parsing errors
 * and helps keep saved data valid.
 *
 * @author ACER
 */
public final class ValidationUtil {

    public static final int MINIMUM_EMPLOYEE_AGE = 18;

    public static final double[] ALLOWED_RICE_ALLOWANCES = {1500.0};
    public static final double[] ALLOWED_PHONE_ALLOWANCES = {500.0, 800.0, 1000.0, 2000.0};
    public static final double[] ALLOWED_CLOTHING_ALLOWANCES = {500.0, 800.0, 1000.0};

    private ValidationUtil() {
        // Annotation: Utility class, no instances.
    }

    // Annotation: Return true when the value is null, empty, or whitespace only.
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Annotation: Check if the value can be parsed as an integer.
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

    // Annotation: Check if the value can be parsed as a decimal number.
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

    // Annotation: Check if the numeric value is non-negative.
    public static boolean isPositiveDouble(String value) {
        if (!isDouble(value)) {
            return false;
        }

        return Double.parseDouble(value.trim()) >= 0;
    }

    // Annotation: Check if the numeric value is greater than zero.
    public static boolean isStrictlyPositiveDouble(String value) {
        if (!isDouble(value)) {
            return false;
        }

        return Double.parseDouble(value.trim()) > 0;
    }

    // Annotation: Return a trimmed value or an empty string.
    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    // Annotation: Add a required-field message when the value is blank.
    public static void require(List<String> errors, String label, String value) {
        if (errors == null) {
            return;
        }

        if (isEmpty(value)) {
            errors.add(label + " is required.");
        }
    }

    // Annotation: Add a numeric validation message when the value is not a valid decimal.
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

    // Annotation: Check that the date is present and not in the future.
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

    // Annotation: Return the whole-number age based on the current date.
    public static int getAge(LocalDate birthday) {
        if (birthday == null) {
            return -1;
        }

        return Period.between(birthday, LocalDate.now()).getYears();
    }

    // Annotation: Add the shared minimum-age rule for employee records.
    public static void requireMinimumEmployeeAge(List<String> errors, LocalDate birthday) {
        if (errors == null || birthday == null) {
            return;
        }

        if (getAge(birthday) < MINIMUM_EMPLOYEE_AGE) {
            errors.add("Employee must be at least 18 years old.");
        }
    }

    // Annotation: Check whether the value matches one of the allowed amounts.
    public static boolean isAllowedAmount(String value, double... allowedAmounts) {
        if (!isDouble(value)) {
            return false;
        }

        double parsed = Double.parseDouble(value.trim());

        for (double allowed : allowedAmounts) {
            if (Double.compare(parsed, allowed) == 0) {
                return true;
            }
        }

        return false;
    }

    // Annotation: Convert one amount to 2-decimal display.
    public static String formatAmount(double amount) {
        return String.format("%.2f", amount);
    }

    // Annotation: Build a readable list of allowed decimal amounts.
    public static String formatAllowedAmounts(double... amounts) {
        if (amounts == null || amounts.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < amounts.length; i++) {
            builder.append(formatAmount(amounts[i]));

            if (i < amounts.length - 2) {
                builder.append(", ");
            } else if (i == amounts.length - 2) {
                builder.append(", or ");
            }
        }

        return builder.toString();
    }

    // Annotation: Return the shared rice allowance validation message.
    public static String getRiceAllowanceMessage() {
        return "Rice Allowance must be " + formatAllowedAmounts(ALLOWED_RICE_ALLOWANCES) + ".";
    }

    // Annotation: Return the shared phone allowance validation message.
    public static String getPhoneAllowanceMessage() {
        return "Phone Allowance must be one of the allowed amounts: "
                + formatAllowedAmounts(ALLOWED_PHONE_ALLOWANCES) + ".";
    }

    // Annotation: Return the shared clothing allowance validation message.
    public static String getClothingAllowanceMessage() {
        return "Clothing Allowance must be one of the allowed amounts: "
                + formatAllowedAmounts(ALLOWED_CLOTHING_ALLOWANCES) + ".";
    }

    // Annotation: Add required, numeric, and allowed-value checks in one rule.
    public static void requireAllowedAmount(List<String> errors, String label, String value, double... allowedAmounts) {
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

        if (!isAllowedAmount(value, allowedAmounts)) {
            if ("Rice Allowance".equals(label)) {
                errors.add(getRiceAllowanceMessage());
            } else if ("Phone Allowance".equals(label)) {
                errors.add(getPhoneAllowanceMessage());
            } else if ("Clothing Allowance".equals(label)) {
                errors.add(getClothingAllowanceMessage());
            } else {
                errors.add(label + " must be one of the allowed amounts: "
                        + formatAllowedAmounts(allowedAmounts) + ".");
            }
        }
    }

    // Annotation: Validate SSS format as XX-XXXXXXX-X.
    public static boolean isValidSssFormat(String sss) {
        if (isEmpty(sss)) {
            return true;
        }

        return sss.trim().matches("\\d{2}-\\d{7}-\\d{1}");
    }

    // Annotation: Validate TIN format as XXX-XXX-XXX-XXX.
    public static boolean isValidTinFormat(String tin) {
        if (isEmpty(tin)) {
            return true;
        }

        return tin.trim().matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}");
    }

    // Annotation: Validate Pag-IBIG format as XXXX-XXXX-XXXX.
    public static boolean isValidPagIbigFormat(String pagIbig) {
        if (isEmpty(pagIbig)) {
            return true;
        }

        return pagIbig.trim().matches("\\d{4}-\\d{4}-\\d{4}");
    }

    // Annotation: Validate PhilHealth format as XX-XXXXXXXXX-X.
    public static boolean isValidPhilHealthFormat(String philHealth) {
        if (isEmpty(philHealth)) {
            return true;
        }

        return philHealth.trim().matches("\\d{2}-\\d{9}-\\d{1}");
    }

    // Annotation: Validate PH mobile number in 09XX-XXX-XXXX format only.
    public static boolean isValidPhoneFormat(String phone) {
        if (isEmpty(phone)) {
            return true;
        }

        return phone.trim().matches("^09\\d{2}-\\d{3}-\\d{4}$");
    }

    // Annotation: Check if the value contains only numbers, dashes, and spaces.
    public static boolean isNumbersAndDashesOnly(String value) {
        if (isEmpty(value)) {
            return true;
        }

        return value.trim().matches("^[0-9\\-\\s]+$");
    }

    // Annotation: Return a clear password-rule message or null when valid.
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

    // Annotation: Return true when the password follows the shared password rules.
    public static boolean isValidPassword(String password) {
        return getPasswordPolicyMessage(password) == null;
    }

    // Annotation: Aggregate employee-form validation messages for reuse across UI and ops.
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
        requireMinimumEmployeeAge(errors, birthday);
        require(errors, "Position", position);
        require(errors, "Supervisor", supervisor);

        requirePositiveNumber(errors, "Basic Salary", basicSalary, false);
        requireAllowedAmount(errors, "Rice Allowance", riceAllowance, ALLOWED_RICE_ALLOWANCES);
        requireAllowedAmount(errors, "Phone Allowance", phoneAllowance, ALLOWED_PHONE_ALLOWANCES);
        requireAllowedAmount(errors, "Clothing Allowance", clothingAllowance, ALLOWED_CLOTHING_ALLOWANCES);

        if (!isValidPhoneFormat(phone)) {
            errors.add("Phone # must use 09XX-XXX-XXXX format.");
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
