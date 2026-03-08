/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.utils;

import java.util.Locale;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Utility class for field-level input restrictions in Swing forms.
 * Used to limit what the user can type before submit validation.
 */
public final class InputRestrictionUtil {

    private InputRestrictionUtil() {
        // Annotation: Utility class, no instances.
    }

    public static void applyDigitsOnly(JTextField field, int maxLength) {
        applyRestrictedFilter(field, "[0-9]*", maxLength);
    }

    public static void applyDigitsAndDashesOnly(JTextField field, int maxLength) {
        applyRestrictedFilter(field, "[0-9\\-]*", maxLength);
    }

    public static void applyMoneyOnly(JTextField field, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = current.substring(0, offset) + string + current.substring(offset);

                if (next.length() <= maxLength && next.matches("\\d*(\\.\\d{0,2})?")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    text = "";
                }
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = current.substring(0, offset) + text + current.substring(offset + length);

                if (next.length() <= maxLength && next.matches("\\d*(\\.\\d{0,2})?")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    public static void applyGovernmentIdRestrictions(
            JTextField txtSSS,
            JTextField txtPagibig,
            JTextField txtTIN,
            JTextField txtPhilHealth
    ) {
        applyDigitsAndDashesOnly(txtSSS, 12);        // 12-1234567-1
        applyDigitsAndDashesOnly(txtPagibig, 14);    // 1234-5678-9012
        applyDigitsAndDashesOnly(txtTIN, 15);        // 123-456-789-000
        applyDigitsAndDashesOnly(txtPhilHealth, 14); // 12-123456789-1
    }

    public static void applyPhoneRestriction(JTextField txtPhone) {
        applyDigitsAndDashesOnly(txtPhone, 16);
    }

    public static void applyCompensationRestrictions(
            JTextField txtBasicSalary,
            JTextField txtRiceSubsidy,
            JTextField txtPhoneAllowance,
            JTextField txtClothingAllowance
    ) {
        applyMoneyOnly(txtBasicSalary, 12);
        applyMoneyOnly(txtRiceSubsidy, 12);
        applyMoneyOnly(txtPhoneAllowance, 12);
        applyMoneyOnly(txtClothingAllowance, 12);
    }

    public static void bindSalaryComputation(
            JTextField txtBasicSalary,
            JTextField txtGrossSemiMonthly,
            JTextField txtHourlyRate
    ) {
        txtBasicSalary.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                compute();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                compute();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                compute();
            }

            private void compute() {
                String raw = txtBasicSalary.getText().trim();
                if (raw.isEmpty()) {
                    txtGrossSemiMonthly.setText("");
                    txtHourlyRate.setText("");
                    return;
                }

                try {
                    double basicSalary = Double.parseDouble(raw);
                    double grossSemi = basicSalary / 2.0;
                    double hourly = basicSalary / 168.0;

                    txtGrossSemiMonthly.setText(String.format(Locale.US, "%.2f", grossSemi));
                    txtHourlyRate.setText(String.format(Locale.US, "%.2f", hourly));
                } catch (NumberFormatException ex) {
                    txtGrossSemiMonthly.setText("");
                    txtHourlyRate.setText("");
                }
            }
        });
    }

    private static void applyRestrictedFilter(JTextField field, String regex, int maxLength) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) {
                    return;
                }
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = current.substring(0, offset) + string + current.substring(offset);

                if (next.length() <= maxLength && next.matches(regex)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) {
                    text = "";
                }
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = current.substring(0, offset) + text + current.substring(offset + length);

                if (next.length() <= maxLength && next.matches(regex)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
}