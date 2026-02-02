/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.*;
import com.motorph.repository.*;
import com.motorph.service.strategy.DeductionStrategy;
import java.time.Duration;
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

    private final EmployeeRepository empRepo;
    private final TimeEntryRepository timeEntryRepo;
    private final DeductionStrategy deductionStrategy;
    private final PayslipRepository payslipRepo;
    private final AuditRepository auditRepo;

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

    public Payslip generatePayslip(int empId, PayPeriod period, int processedByUserId) {

        // Repository lookup uses the contract method name.
        Employee emp = empRepo.findById(empId);
        if (emp == null || period == null) {
            return null;
        }

        List<TimeEntry> entries = timeEntryRepo.findByEmployeeAndPeriod(empId, period);

        double totalLateDeduction = 0.0;
        double totalOvertimePay = 0.0;
        double totalHoursWorked = 0.0;

        for (TimeEntry entry : entries) {
            if (entry.getTimeIn() == null || entry.getTimeOut() == null) {
                continue;
            }

            double dailyHours = calculateHours(entry.getTimeIn(), entry.getTimeOut());
            totalHoursWorked += dailyHours;

            // Grace window treats arrivals up to 08:10 as on-time for late deduction.
            if (entry.getTimeIn().isAfter(GRACE_END)) {
                long minutesLate = Duration.between(WORK_START, entry.getTimeIn()).toMinutes();
                totalLateDeduction += minutesLate * (emp.getHourlyRate() / 60.0);
            }

            // Undertime is tracked when Time Out occurs before 17:00.
            if (entry.getTimeOut().isBefore(WORK_END)) {
                long minutesUndertime = Duration.between(entry.getTimeOut(), WORK_END).toMinutes();
                totalLateDeduction += minutesUndertime * (emp.getHourlyRate() / 60.0);
            }

            // Overtime pay is computed beyond 8 hours per day.
            if (dailyHours > 8.0) {
                double otHours = dailyHours - 8.0;
                totalOvertimePay += otHours * emp.getHourlyRate() * 1.25;
            }
        }

        // Semi-monthly baseline earnings.
        double semiMonthlyBasic = emp.getBasicSalary() / 2.0;
        double rice = emp.getRiceAllowance() / 2.0;
        double phone = emp.getPhoneAllowance() / 2.0;
        double clothing = emp.getClothingAllowance() / 2.0;
        double totalAllowances = rice + phone + clothing;

        // Earnings before deductions.
        double grossEarnings = semiMonthlyBasic + totalAllowances + totalOvertimePay;

        // Pay base after time-based deductions.
        double payAfterTimeDeduction = Math.max(0.0, grossEarnings - totalLateDeduction);

        // Mandatory contributions are computed before withholding tax in the simplified model.
        double sss = deductionStrategy.calculateSSS(payAfterTimeDeduction);
        double ph = deductionStrategy.calculatePhilHealth(payAfterTimeDeduction);
        double pagibig = deductionStrategy.calculatePagibig(payAfterTimeDeduction);
        double totalGovDeductions = sss + ph + pagibig;

        // Taxable income is net of mandatory deductions.
        double taxableIncome = Math.max(0.0, payAfterTimeDeduction - totalGovDeductions);
        double tax = deductionStrategy.calculateTax(taxableIncome);

        // Full deductions include time-based + mandatory + withholding tax.
        double totalDeductions = totalLateDeduction + totalGovDeductions + tax;

        // Net pay stays consistent with the equation: GrossIncome - TotalDeductions.
        double netPay = Math.max(0.0, grossEarnings - totalDeductions);

        Payslip p = new Payslip();

        // Deterministic transaction id based on employee and pay period.
        String txId = buildTransactionId(empId, period);
        p.setTransactionId(txId);

        // Identity and period metadata.
        p.setEmployeeId(empId);
        p.setLastName(emp.getLastName());
        p.setFirstName(emp.getFirstName());
        p.setHourlyRate(emp.getHourlyRate());
        p.setTotalHoursWorked(totalHoursWorked);
        p.setPeriod(period);

        // Earnings breakdown.
        p.setBasicSalary(semiMonthlyBasic);
        p.setRiceAllowance(rice);
        p.setPhoneAllowance(phone);
        p.setClothingAllowance(clothing);
        p.setOvertimePay(totalOvertimePay);
        p.setGrossIncome(grossEarnings);

        // Deductions breakdown.
        p.setLateDeduction(totalLateDeduction);
        p.setSss(sss);
        p.setPhilHealth(ph);
        p.setPagIbig(pagibig);
        p.setWithholdingTax(tax);
        p.setTotalDeductions(totalDeductions);

        // Net pay.
        p.setNetPay(netPay);

        // Processing metadata stays in the snapshot.
        p.setProcessedByUserId(processedByUserId);

        if (payslipRepo.save(p)) {
            auditRepo.logPayrollChange(String.valueOf(processedByUserId),
                    "Generated Payslip: " + p.getTransactionId());
            return p;
        }

        return null;
    }

    private double calculateHours(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes > 240) {
            minutes -= 60; // Lunch break deduction
        }
        return Math.max(0, minutes / 60.0);
    }

    private static final DateTimeFormatter TX_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    private String buildTransactionId(int empId, PayPeriod period) {
        String start = period.getStartDate().format(TX_DATE_FMT);
        String end = period.getEndDate().format(TX_DATE_FMT);
        return "TX-" + empId + "-" + start + "-" + end;
    }

}
