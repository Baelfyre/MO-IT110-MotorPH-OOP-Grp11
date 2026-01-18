/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.*;
import com.motorph.repository.*;
import com.motorph.service.strategy.DeductionStrategy;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The core engine of the Payroll System. Calculates Gross Pay, Deductions
 * (Late/Gov), and Net Pay. Uses TimeEntryRepository to fetch attendance.
 *
 * @author ACER
 */
public class PayrollService {

    private EmployeeRepository empRepo;
    private TimeEntryRepository timeEntryRepo; // Replaced TimecardRepository
    private DeductionStrategy deductionStrategy;
    private PayslipRepository payslipRepo;
    private AuditRepository auditRepo;

    // Constants
    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final LocalTime WORK_END = LocalTime.of(17, 0);
    private static final LocalTime GRACE_END = LocalTime.of(8, 10);

    public PayrollService(EmployeeRepository empRepo, TimeEntryRepository timeEntryRepo,
            DeductionStrategy deductionStrategy, PayslipRepository payslipRepo,
            AuditRepository auditRepo) {
        this.empRepo = empRepo;
        this.timeEntryRepo = timeEntryRepo;
        this.deductionStrategy = deductionStrategy;
        this.payslipRepo = payslipRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Generates a payslip for a specific employee and period.
     *
     * @param empId The employee to process.
     * @param period The date range (e.g., Jan 1 - Jan 15).
     * @param processedByUserId The ID of the Payroll Officer running this.
     * @return The saved Payslip object.
     */
    public Payslip generatePayslip(int empId, PayPeriod period, int processedByUserId) {
        EmployeeProfile emp = empRepo.findByEmployeeNumber(empId);
        if (emp == null) {
            return null;
        }

        List<TimeEntry> entries = timeEntryRepo.findByEmployeeAndPeriod(empId, period);

        double totalLateDeduction = 0.0; // This will now include Early Out deductions
        double totalOvertimePay = 0.0;

        for (TimeEntry entry : entries) {
            if (entry.getTimeIn() != null && entry.getTimeOut() != null) {

                // 1. LATE IN CHECK (Existing)
                if (entry.getTimeIn().isAfter(GRACE_END)) {
                    long minutesLate = Duration.between(WORK_START, entry.getTimeIn()).toMinutes();
                    totalLateDeduction += minutesLate * (emp.getHourlyRate() / 60.0);
                }

                // 2. EARLY OUT CHECK (NEW Logic)
                // If they leave before 5:00 PM, deduct the remaining minutes
                if (entry.getTimeOut().isBefore(WORK_END)) {
                    long minutesUndertime = Duration.between(entry.getTimeOut(), WORK_END).toMinutes();
                    // We treat Undertime exactly like Lateness (Minute-for-Minute deduction)
                    totalLateDeduction += minutesUndertime * (emp.getHourlyRate() / 60.0);
                }

                // 3. OVERTIME CHECK (Existing)
                double hoursWorked = calculateHours(entry.getTimeIn(), entry.getTimeOut());
                if (hoursWorked > 8.0) {
                    double otHours = hoursWorked - 8.0;
                    totalOvertimePay += otHours * emp.getHourlyRate() * 1.25;
                }
            }
        }

        // 4. Calculate Gross Income
        // Semi-monthly Rate + Allowances + OT - Late Deductions
        double semiMonthlyBasic = emp.getBasicSalary() / 2;
        double allowances = (emp.getriceAllowance() + emp.getPhoneAllowance() + emp.getClothingAllowance()) / 2;

        double grossIncome = (semiMonthlyBasic + allowances + totalOvertimePay) - totalLateDeduction;
        // 5. Calculate Government Deductions
        double sss = deductionStrategy.calculateSSS(grossIncome);
        double ph = deductionStrategy.calculatePhilHealth(grossIncome);
        double pagibig = deductionStrategy.calculatePagibig(grossIncome);
        double totalGovDeductions = sss + ph + pagibig;

        // 6. Calculate Tax
        double taxableIncome = grossIncome - totalGovDeductions;
        double tax = deductionStrategy.calculateTax(taxableIncome);

        // 7. Create Payslip Object
        Payslip p = new Payslip();
        String trxDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        p.setTransactionId("TRX-" + empId + "-" + trxDate);
        p.setEmployeeId(empId);
        p.setPeriod(period);

        p.setBasicSalary(semiMonthlyBasic);
        p.setriceAllowance(emp.getriceAllowance() / 2);
        p.setPhoneAllowance(emp.getPhoneAllowance() / 2);
        p.setClothingAllowance(emp.getClothingAllowance() / 2);

        p.setOvertimePay(totalOvertimePay);
        p.setLateDeduction(totalLateDeduction);
        p.setGrossIncome(grossIncome);

        p.setSss(sss);
        p.setPhilHealth(ph);
        p.setPagIbig(pagibig);
        p.setWithholdingTax(tax);
        p.setTotalDeductions(totalGovDeductions + tax + totalLateDeduction); // Includes Late & Tax

        p.setNetPay(grossIncome - (totalGovDeductions + tax));

        // 8. Save
        if (payslipRepo.save(p)) {
            auditRepo.logPayrollChange(String.valueOf(processedByUserId), "Generated Payslip: " + p.getTransactionId());
        }

        return p;
    }

    /**
     * Helper to calculate raw worked hours (minus 1 hour break).
     */
    private double calculateHours(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes > 240) {
            minutes -= 60; // Deduct 1 hr break if > 4 hours
        }
        return Math.max(0, minutes / 60.0);
    }
}
