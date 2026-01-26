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

    // Regex to split by comma ONLY if not inside double quotes
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Override
    public double calculateSSS(double grossPay) {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_SSS_CSV))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length >= 5) {
                    try {
                        double min = parse(data[0]);
                        double max = (data[1].equalsIgnoreCase("Over") || data[1].isEmpty())
                                ? Double.MAX_VALUE : parse(data[1]);
                        double contribution = parse(data[4]); // Total EE Contribution

                        if (grossPay >= min && grossPay < max) {
                            return contribution;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double calculatePhilHealth(double grossPay) {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_PHILHEALTH_CSV))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length >= 6) {
                    double minSal = parse(data[0]);
                    double maxSal = (data[1].equalsIgnoreCase("Over") || data[1].isEmpty())
                            ? Double.MAX_VALUE : parse(data[1]);
                    double rate = parse(data[3]);
                    double minShare = parse(data[4]);
                    double maxShare = parse(data[5]);

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
                }
            }
            return 2500.00; // Standard fallback for 2025 cap
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double calculatePagibig(double grossPay) {
        double rate = (grossPay <= 1500) ? 0.01 : 0.02;
        double contribution = grossPay * rate;
        return Math.min(contribution, 100.0); // Capped at 100
    }

    @Override
    public double calculateTax(double taxableIncome) {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_TAX_CSV))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length >= 4) {
                    try {
                        double min = parse(data[0]);
                        double max = (data[1].equalsIgnoreCase("Over") || data[1].isEmpty())
                                ? Double.MAX_VALUE : parse(data[1]);
                        double baseTax = parse(data[2]);
                        double excessPercent = parse(data[3]);

                        if (taxableIncome >= min && taxableIncome < max) {
                            return baseTax + ((taxableIncome - min) * excessPercent);
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Helper to safely clean and parse numbers from CSV fields.
     */
    private double parse(String val) {
        if (val == null || val.trim().isEmpty()) {
            return 0.0;
        }
        // Remove quotes and commas (e.g., "24,750" becomes 24750.0)
        return Double.parseDouble(val.replace("\"", "").replace(",", "").trim());
    }
}
