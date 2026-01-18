/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to ensure employee record files exist. Creates blank templates for
 * DTR, Payslips, and Payroll if files are missing.
 *
 * @author ACER
 */
public class CsvFileInitializer {

    // --- Define Headers ---
    // 1. DTR: Date, In, Out
    private static final String DTR_HEADER = "Date,TimeIn,TimeOut";

    // 2. Payslip: Simple Summary for the employee
    private static final String PAYSLIP_HEADER = "TransactionID,PeriodStart,PeriodEnd,GrossIncome,TotalDeductions,NetPay";

    // 3. Payroll: Detailed breakdown for accounting (Matches the PayPeriod Calculation)
    private static final String PAYROLL_HEADER = "PeriodStart,PeriodEnd,BasicSalary,Overtime,Allowances,GrossIncome,SSS,PhilHealth,PagIbig,Tax,TotalDeductions,NetPay";

    /**
     * Checks if DTR, Payslip, and Payroll files exist for an employee. If not,
     * creates them with the correct headers.
     *
     * @param empId The Employee ID (e.g. 10001)
     */
    public static void initializeEmployeeFiles(int empId) {
        // 1. Create DTR File
        createFileIfNotExists(DataPaths.DTR_FOLDER, "records_dtr_" + empId + ".csv", DTR_HEADER);

        // 2. Create Payslip File
        createFileIfNotExists(DataPaths.PAYSLIP_FOLDER, "records_payslip_" + empId + ".csv", PAYSLIP_HEADER);

        // 3. Create Payroll Record File
        createFileIfNotExists(DataPaths.PAYROLL_FOLDER, "records_payroll_" + empId + ".csv", PAYROLL_HEADER);
    }

    private static void createFileIfNotExists(String folderPath, String fileName, String header) {
        File folder = new File(folderPath);

        // Create directory if it doesn't exist
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);

        // Only create if file is missing (don't overwrite existing data!)
        if (!file.exists()) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(header);
                fw.write(System.lineSeparator()); // Add new line
                System.out.println("Created template: " + file.getPath());
            } catch (IOException e) {
                System.err.println("Error creating file " + fileName + ": " + e.getMessage());
            }
        }
    }
}
