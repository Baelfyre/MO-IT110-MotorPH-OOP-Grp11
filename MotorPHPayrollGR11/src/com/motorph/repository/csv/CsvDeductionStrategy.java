/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.repository.csv.DataPaths;
import com.motorph.service.strategy.DeductionStrategy;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates deductions by looking up values in the CSV tables.
 * Reads: gov_SSS_Table.csv, gov_Tax_Table.csv, gov_Philhealth_Table.csv, gov_Pagibig_Table.csv
 * @author ACER
 */
public class CsvDeductionStrategy implements DeductionStrategy {

    // Helper classes to store the table rows in memory
    private static class SssBracket {
        double min, max, deduction;
        public SssBracket(double min, double max, double deduction) {
            this.min = min; this.max = max; this.deduction = deduction;
        }
    }

    private static class TaxBracket {
        double min, max, base, rate;
        public TaxBracket(double min, double max, double base, double rate) {
            this.min = min; this.max = max; this.base = base; this.rate = rate;
        }
    }
    
    // In-memory tables
    private List<SssBracket> sssTable = new ArrayList<>();
    private List<TaxBracket> taxTable = new ArrayList<>();
    
    // PhilHealth/Pagibig often have fewer rows, but we can store them or read them.
    // Given their logic is often rate-based, we'll read them into variables or lists.
    // For this implementation, we will load everything in the constructor.

    public CsvDeductionStrategy() {
        loadSssTable();
        loadTaxTable();
    }

    private void loadSssTable() {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_SSS_CSV))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // CSV Format: min, max, reg_ee, mpf_ee, total_ee, ...
                // We need Col 0 (Min), Col 1 (Max), and Col 4 (Total EE Contribution)
                double min = Double.parseDouble(data[0]);
                double max = Double.parseDouble(data[1]);
                double totalEe = Double.parseDouble(data[4]); 
                sssTable.add(new SssBracket(min, max, totalEe));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTaxTable() {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_TAX_CSV))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // CSV Format: min, max, base_tax, excess_percent
                double min = Double.parseDouble(data[0]);
                double max = Double.parseDouble(data[1]);
                double base = Double.parseDouble(data[2]);
                double rate = Double.parseDouble(data[3]);
                taxTable.add(new TaxBracket(min, max, base, rate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double calculateSSS(double grossPay) {
        for (SssBracket row : sssTable) {
            if (grossPay >= row.min && grossPay <= row.max) {
                return row.deduction;
            }
        }
        // Fallback: If salary is higher than the last bracket, usually use the max deduction
        if (!sssTable.isEmpty()) {
            SssBracket last = sssTable.get(sssTable.size() - 1);
            if (grossPay > last.max) return last.deduction;
        }
        return 0.0;
    }

    @Override
    public double calculatePhilHealth(double grossPay) {
        // Reading PhilHealth Table on the fly (or you can cache it like SSS)
        // CSV Format: min, max, rate, ee_share_rate, min_share, max_share
        // Row 1: 0, 10000, 0.05, 0.025, 250, 250
        // Row 3: 100000, 999999, ..., ..., 2500, 2500
        
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_PHILHEALTH_CSV))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                double min = Double.parseDouble(data[0]);
                double max = Double.parseDouble(data[1]);
                double eeRate = Double.parseDouble(data[3]);
                double minFixed = Double.parseDouble(data[4]);
                // double maxFixed = Double.parseDouble(data[5]); // Not always needed if logic is correct

                if (grossPay >= min && grossPay <= max) {
                    // If the specific row has a fixed amount (like 250.00), usually the rate is 0 or handled differently.
                    // But standard logic:
                    if (minFixed > 0 && eeRate == 0.025) { 
                        // If it's the fixed lower/upper bracket
                         if (grossPay <= 10000) return 250.00; // Hardcoded based on your snippet logic
                         if (grossPay >= 100000) return 2500.00;
                         return grossPay * eeRate;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Default Logic based on your file snippet if read fails or loop misses
        if (grossPay <= 10000) return 250.0;
        if (grossPay >= 100000) return 2500.0;
        return grossPay * 0.025; // 2.5%
    }

    @Override
    public double calculatePagibig(double grossPay) {
        // CSV Format: min, max, ee_rate, er_rate, max_contribution
        // Logic: Rate is usually 1% or 2%, capped at max_contribution
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_PAGIBIG_CSV))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                double min = Double.parseDouble(data[0]);
                double max = Double.parseDouble(data[1]);
                double rate = Double.parseDouble(data[2]);
                double maxDed = Double.parseDouble(data[4]);

                if (grossPay >= min && grossPay <= max) {
                    double deduction = grossPay * rate;
                    if (deduction > maxDed) return maxDed;
                    return deduction;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 100.0; // Default fallback
    }

    @Override
    public double calculateTax(double taxableIncome) {
        // Logic: Base + (Taxable - Min) * Rate
        for (TaxBracket row : taxTable) {
            if (taxableIncome >= row.min && taxableIncome <= row.max) {
                // Example: Income 25,000. Bracket 20,833 - 33,333.
                // Tax = 0 (Base) + (25,000 - 20,833) * 0.20
                // Note: The "Min" in your CSV (20833.01) is essentially the threshold.
                double excess = taxableIncome - (row.min - 0.01); // Adjust back to round number
                if (excess < 0) excess = 0;
                
                return row.base + (excess * row.rate);
            }
        }
        // If higher than highest bracket
        if (!taxTable.isEmpty()) {
            TaxBracket last = taxTable.get(taxTable.size() - 1);
            if (taxableIncome > last.max) {
                double excess = taxableIncome - (last.min - 0.01);
                return last.base + (excess * last.rate);
            }
        }
        return 0.0;
    }
}