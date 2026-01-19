package com.motorph.test;

import com.motorph.domain.models.*;
import com.motorph.repository.csv.*;
import com.motorph.service.*;
import com.motorph.service.strategy.DeductionStrategy;
import com.motorph.service.strategy.PayDeductionStrategy;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * CONSOLE TEST RUNNER 
 * Verifies Backend Logic and displays detailed Attendance and Payroll Breakdown.
 */
public class BackEndTester {

    public static void main(String[] args) {
        System.out.println("=== MOTORPH SYSTEM BACKEND TEST ===");

        // 1. Setup Dependencies
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();
        CsvTimeRepository timeRepo = new CsvTimeRepository();
        CsvPayslipRepository payslipRepo = new CsvPayslipRepository();
        CsvAuditRepository auditRepo = new CsvAuditRepository();
        DeductionStrategy strategy = new PayDeductionStrategy();

        PayrollService payrollService = new PayrollService(
                empRepo, timeRepo, strategy, payslipRepo, auditRepo
        );

        // 2. Define Test Data (Employee 10001 - Manuel Garcia)
        int employeeId = 10002;
        LocalDate start = LocalDate.of(2024, 7, 1);
        LocalDate end = LocalDate.of(2024, 7, 30);
        PayPeriod period = new PayPeriod(start, end);

        System.out.println("Testing Attendance and Calculation for Emp ID: " + employeeId);
        System.out.println("Period: " + start + " to " + end);
        System.out.println("--------------------------------------------------");

        // 3. Display Attendance Logs and Duration
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

        // 4. Run Calculation (Passing 10001 as the Admin processing this)
        Payslip result = payrollService.generatePayslip(employeeId, period, 10001);

        // 5. Detailed Calculation Results
        if (result != null) {
            System.out.println("\n--- EMPLOYEE IDENTITY ---");
            System.out.println("Name         : " + result.getLastName() + ", " + result.getFirstName());
            System.out.println("Employee ID  : " + result.getEmployeeId());
            
            System.out.println("\n--- WORK DURATION & RATES ---");
            System.out.println("Hourly Rate  : " + String.format("%.2f", result.getHourlyRate()));
            System.out.println("Total Hours  : " + String.format("%.2f", result.getTotalHoursWorked()));
            
            System.out.println("\n--- EARNINGS BREAKDOWN ---");
            System.out.println("Basic (Semi) : " + String.format("%.2f", result.getBasicSalary()));
            System.out.println("Rice Allow.  : " + String.format("%.2f", result.getRiceAllowance()));
            System.out.println("Phone Allow. : " + String.format("%.2f", result.getPhoneAllowance()));
            System.out.println("Cloth Allow. : " + String.format("%.2f", result.getClothingAllowance()));
            System.out.println("Overtime Pay : " + String.format("%.2f", result.getOvertimePay()));
            System.out.println("--------------------------");
            System.out.println("GROSS INCOME : " + String.format("%.2f", result.getGrossIncome()));

            System.out.println("\n--- DEDUCTIONS ---");
            System.out.println("Late/Under   : " + String.format("%.2f", result.getLateDeduction()));
            System.out.println("SSS          : " + String.format("%.2f", result.getSss()));
            System.out.println("PhilHealth   : " + String.format("%.2f", result.getPhilHealth()));
            System.out.println("Pag-IBIG     : " + String.format("%.2f", result.getPagIbig()));
            System.out.println("Withholding  : " + String.format("%.2f", result.getWithholdingTax()));
            System.out.println("--------------------------");
            System.out.println("TOTAL DEDUCT : " + String.format("%.2f", result.getTotalDeductions()));

            System.out.println("\n--- NET RESULT ---");
            System.out.println("NET PAY      : " + String.format("%.2f", result.getNetPay()));
            System.out.println("--------------------------");
            System.out.println("Status: Saved to records_payslips.csv");
        } else {
            System.out.println("\nError: Could not generate payslip.");
        }
    }
}