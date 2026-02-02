/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service.strategy;

import com.motorph.repository.csv.DataPaths;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Implementation of payroll deduction rules for 2025. Reads government tables
 * from CSV files dynamically.
 *
 * @author ACER
 */
public class PayDeductionStrategy implements DeductionStrategy {

    // CSV split regex that preserves commas inside double-quoted values.
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Override
    public double calculateSSS(double grossPay) {
        // Government table rows include a leading ID column at index 0, so min/max start at index 1.
        final int IDX_MIN = 1;
        final int IDX_MAX = 2;
        final int IDX_EE_TOTAL = 5; // Employee share total column after the leading ID column.

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_SSS_CSV))) {
            br.readLine(); // Header row ignored during bracket scan.

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length <= IDX_EE_TOTAL) {
                    continue;
                }

                try {
                    double min = parse(data[IDX_MIN]);

                    // "Over" or blank upper bounds represent an open-ended bracket.
                    double max = (data[IDX_MAX].equalsIgnoreCase("Over") || data[IDX_MAX].isEmpty())
                            ? Double.MAX_VALUE
                            : parse(data[IDX_MAX]);

                    double contribution = parse(data[IDX_EE_TOTAL]);

                    if (grossPay >= min && grossPay < max) {
                        return contribution;
                    }
                } catch (NumberFormatException e) {
                    // Row ignored when numeric parsing fails.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    @Override
    public double calculatePhilHealth(double grossPay) {
        // Government table rows include a leading ID column at index 0, so min/max start at index 1.
        final int IDX_MIN = 1;
        final int IDX_MAX = 2;
        final int IDX_RATE = 4;
        final int IDX_MIN_SHARE = 5;
        final int IDX_MAX_SHARE = 6;

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_PHILHEALTH_CSV))) {
            br.readLine(); // Header row ignored during bracket scan.

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length <= IDX_MAX_SHARE) {
                    continue;
                }

                try {
                    double minSal = parse(data[IDX_MIN]);

                    // "Over" or blank upper bounds represent an open-ended bracket.
                    double maxSal = (data[IDX_MAX].equalsIgnoreCase("Over") || data[IDX_MAX].isEmpty())
                            ? Double.MAX_VALUE
                            : parse(data[IDX_MAX]);

                    double rate = parse(data[IDX_RATE]);
                    double minShare = parse(data[IDX_MIN_SHARE]);
                    double maxShare = parse(data[IDX_MAX_SHARE]);

                    if (grossPay >= minSal && grossPay <= maxSal) {
                        double share = grossPay * rate;

                        if (minShare > 0 && share < minShare) {
                            return minShare;
                        }
                        if (maxShare > 0 && share > maxShare) {
                            return maxShare;
                        }

                        return share;
                    }
                } catch (NumberFormatException e) {
                    // Row ignored when numeric parsing fails.
                }
            }

            // Default value returned when no bracket matches (legacy fallback).
            return 2500.00;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    @Override
    public double calculatePagibig(double grossPay) {
        // Pag-IBIG computed using simplified rates and a cap; the table is not used in the current scope.
        double rate = (grossPay <= 1500) ? 0.01 : 0.02;
        double contribution = grossPay * rate;
        return Math.min(contribution, 100.0);
    }

    @Override
    public double calculateTax(double taxableIncome) {
        // Government table rows include a leading ID column at index 0, so min/max start at index 1.
        final int IDX_MIN = 1;
        final int IDX_MAX = 2;
        final int IDX_BASE_TAX = 3;
        final int IDX_EXCESS_PERCENT = 4;

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_TAX_CSV))) {
            br.readLine(); // Header row ignored during bracket scan.

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length <= IDX_EXCESS_PERCENT) {
                    continue;
                }

                try {
                    double min = parse(data[IDX_MIN]);

                    // "Over" or blank upper bounds represent an open-ended bracket.
                    double max = (data[IDX_MAX].equalsIgnoreCase("Over") || data[IDX_MAX].isEmpty())
                            ? Double.MAX_VALUE
                            : parse(data[IDX_MAX]);

                    double baseTax = parse(data[IDX_BASE_TAX]);
                    double excessPercent = parse(data[IDX_EXCESS_PERCENT]);

                    if (taxableIncome >= min && taxableIncome < max) {
                        return baseTax + ((taxableIncome - min) * excessPercent);
                    }
                } catch (NumberFormatException e) {
                    // Row ignored when numeric parsing fails.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    private double parse(String val) {
        if (val == null || val.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(val.replace("\"", "").replace(",", "").trim());
    }
}
