package com.motorph;

import com.motorph.domain.models.*;
import com.motorph.repository.csv.*;
import com.motorph.service.*;
import com.motorph.service.strategy.DeductionStrategy;
import com.motorph.service.strategy.DeductionStrategy2025;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * CONSOLE TEST RUNNER 
 * Verifies Backend Logic and displays detailed Attendance/Work Duration.
 */
public class BackEndTester {

    public static void main(String[] args) {
        System.out.println("=== MOTORPH SYSTEM BACKEND TEST ===");

        // 1. Setup Dependencies
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();
        CsvTimeEntryRepository timeRepo = new CsvTimeEntryRepository();
        CsvPayslipRepository payslipRepo = new CsvPayslipRepository();
        CsvAuditRepository auditRepo = new CsvAuditRepository();
        DeductionStrategy strategy = new DeductionStrategy2025();

        PayrollService payrollService = new PayrollService(
                empRepo, timeRepo, strategy, payslipRepo, auditRepo
        );

        // 2. Define Test Data
        int employeeId = 10001;
        LocalDate start = LocalDate.of(2024, 6, 1);
        LocalDate end = LocalDate.of(2024, 6, 30);
        PayPeriod period = new PayPeriod(start, end);

        System.out.println("Testing Attendance and Calculation for Emp ID: " + employeeId);
        System.out.println("Period: " + start + " to " + end);
        System.out.println("--------------------------------------------------");

        // 3. NEW: Display Attendance Logs and Duration
        List<TimeEntry> entries = timeRepo.findByEmployeeAndPeriod(employeeId, period);
        
        if (entries.isEmpty()) {
            System.out.println("No DTR entries found for this period.");
        } else {
            System.out.println(String.format("%-12s | %-10s | %-10s | %-10s", "Date", "In", "Out", "Duration"));
            System.out.println("--------------------------------------------------");
            
            for (TimeEntry entry : entries) {
                String timeIn = (entry.getTimeIn() != null) ? entry.getTimeIn().toString() : "MISSING";
                String timeOut = (entry.getTimeOut() != null) ? entry.getTimeOut().toString() : "MISSING";
                String durationStr = "0h 0m";

                if (entry.getTimeIn() != null && entry.getTimeOut() != null) {
                    Duration duration = Duration.between(entry.getTimeIn(), entry.getTimeOut());
                    long hours = duration.toHours();
                    long mins = duration.toMinutesPart();
                    durationStr = hours + "h " + mins + "m";
                }

                System.out.println(String.format("%-12s | %-10s | %-10s | %-10s", 
                        entry.getDate(), timeIn, timeOut, durationStr));
            }
        }

        // 4. Run Calculation
        Payslip result = payrollService.generatePayslip(employeeId, period, 10001);

        // 5. Print Financial Results
        if (result != null) {
            System.out.println("\n--- FINAL CALCULATION RESULT ---");
            System.out.println("Basic Salary : " + String.format("%.2f", result.getBasicSalary()));
            System.out.println("Gross Income : " + String.format("%.2f", result.getGrossIncome()));
            System.out.println("--------------------------");
            System.out.println("SSS          : " + String.format("%.2f", result.getSss()));
            System.out.println("PhilHealth   : " + String.format("%.2f", result.getPhilHealth()));
            System.out.println("Pag-IBIG     : " + String.format("%.2f", result.getPagIbig()));
            System.out.println("Tax          : " + String.format("%.2f", result.getWithholdingTax()));
            System.out.println("--------------------------");
            System.out.println("NET PAY      : " + String.format("%.2f", result.getNetPay()));
            System.out.println("--------------------------");
            System.out.println("Status: Saved to records_payslips.csv");
        } else {
            System.out.println("\nError: Could not generate payslip.");
        }
    }
}