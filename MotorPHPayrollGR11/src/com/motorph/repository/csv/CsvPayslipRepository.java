/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.Payslip;
import com.motorph.repository.PayslipRepository;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Implementation that saves calculated Payslips to a CSV file. Saves to
 * specific employee files: records_payslip_{id}.csv
 *
 * @author ACER
 */
public class CsvPayslipRepository implements PayslipRepository {

    // Helper: Construct filename "records_payslip_10001.csv"
    private File getFileForEmployee(int empId) {
        // Ensure DataPaths.PAYSLIP_FOLDER ends with a slash in DataPaths.java
        return new File(DataPaths.PAYSLIP_FOLDER + "records_payslip_" + empId + ".csv");
    }

    @Override
    public boolean save(Payslip p) {
        File file = getFileForEmployee(p.getEmployeeId());

        // Safety: Auto-Create folder if missing
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        boolean isNewFile = !file.exists();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) { // true = append mode
            if (isNewFile) {
                // WRITE HEADER
                bw.write("TransactionID,PeriodStart,PeriodEnd,GrossIncome,TotalDeductions,NetPay");
                bw.newLine();
            }

            // Extract dates safely (prevent crash if period is null)
            String start = (p.getPeriod() != null) ? p.getPeriod().getStartDate().toString() : "";
            String end = (p.getPeriod() != null) ? p.getPeriod().getEndDate().toString() : "";

            // Simple CSV Line Generation
            String line = String.format("%s,%s,%s,%.2f,%.2f,%.2f",
                    p.getTransactionId(),
                    start,
                    end,
                    p.getGrossIncome(),
                    p.getTotalDeductions(),
                    p.getNetPay()
            );

            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
