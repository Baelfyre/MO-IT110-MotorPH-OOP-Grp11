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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation that saves calculated Payslips to a CSV file. Saves to
 * specific employee files: records_payslip_{id}.csv
 *
 * @author ACER
 */
public class CsvPayslipRepository implements PayslipRepository {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean save(Payslip p) {
        // Filename format: records_payslips_{EmpID}_{Period}.csv
        String periodRange = p.getPeriod().getStartDate().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" +
                             p.getPeriod().getEndDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
        
        File file = new File(DataPaths.PAYSLIP_FOLDER + "records_payslips_" + p.getEmployeeId() + "_" + periodRange + ".csv");

        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        boolean isNewFile = !file.exists();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                bw.write("EmployeeID,LastName,FirstName,PayPeriodStart,PayPeriodEnd,BasicSalary," +
                         "RiceAllowance,PhoneAllowance,ClothingAllowance,GrossSemiMonthlyRate,HourlyRate," +
                         "TotalHoursWorked,GrossIncome,SSS,PhilHealth,Pagibig,WithholdingTax," +
                         "TotalDeductions,NetPay,ProcessedBy,DateProcessed");
                bw.newLine();
            }

            // Formatting the detailed row
            String line = String.format("%d,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s",
                p.getEmployeeId(),
                p.getLastName(),
                p.getFirstName(),
                p.getPeriod().getStartDate().format(DATE_FMT),
                p.getPeriod().getEndDate().format(DATE_FMT),
                p.getBasicSalary(),
                p.getRiceAllowance(),
                p.getPhoneAllowance(),
                p.getClothingAllowance(),
                p.getGrossSemiMonthlyRate(),
                p.getHourlyRate(),
                p.getTotalHoursWorked(),
                p.getGrossIncome(),
                p.getSss(),
                p.getPhilHealth(),
                p.getPagIbig(),
                p.getWithholdingTax(),
                p.getTotalDeductions(),
                p.getNetPay(),
                "Admin", // Can be replaced with session user ID
                LocalDateTime.now().format(TIME_FMT)
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
