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
    private TimeEntryRepository timeEntryRepo;
    private DeductionStrategy deductionStrategy;
    private PayslipRepository payslipRepo;
    private AuditRepository auditRepo;

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
        Employee emp = empRepo.findByEmployeeNumber(empId);
        if (emp == null) return null;

        List<TimeEntry> entries = timeEntryRepo.findByEmployeeAndPeriod(empId, period);

        double totalLateDeduction = 0.0;
        double totalOvertimePay = 0.0;
        double totalHoursWorked = 0.0;

        for (TimeEntry entry : entries) {
            if (entry.getTimeIn() != null && entry.getTimeOut() != null) {
                // Track total raw hours for the record
                double dailyHours = calculateHours(entry.getTimeIn(), entry.getTimeOut());
                totalHoursWorked += dailyHours;

                // 1. Late Check
                if (entry.getTimeIn().isAfter(GRACE_END)) {
                    long minutesLate = Duration.between(WORK_START, entry.getTimeIn()).toMinutes();
                    totalLateDeduction += minutesLate * (emp.getHourlyRate() / 60.0);
                }

                // 2. Early Out Check
                if (entry.getTimeOut().isBefore(WORK_END)) {
                    long minutesUndertime = Duration.between(entry.getTimeOut(), WORK_END).toMinutes();
                    totalLateDeduction += minutesUndertime * (emp.getHourlyRate() / 60.0);
                }

                // 3. Overtime Check
                if (dailyHours > 8.0) {
                    double otHours = dailyHours - 8.0;
                    totalOvertimePay += otHours * emp.getHourlyRate() * 1.25;
                }
            }
        }

        // Calculate Semi-monthly figures
        double semiMonthlyBasic = emp.getBasicSalary() / 2;
        double rice = emp.getriceAllowance() / 2;
        double phone = emp.getPhoneAllowance() / 2;
        double clothing = emp.getClothingAllowance() / 2;
        double totalAllowances = rice + phone + clothing;

        double grossIncome = (semiMonthlyBasic + totalAllowances + totalOvertimePay) - totalLateDeduction;

        // Deductions
        double sss = deductionStrategy.calculateSSS(grossIncome);
        double ph = deductionStrategy.calculatePhilHealth(grossIncome);
        double pagibig = deductionStrategy.calculatePagibig(grossIncome);
        double totalGovDeductions = sss + ph + pagibig;

        double taxableIncome = grossIncome - totalGovDeductions;
        double tax = deductionStrategy.calculateTax(taxableIncome);

        // Map to Payslip Object
        Payslip p = new Payslip();
        String trxDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        p.setTransactionId("TRX-" + empId + "-" + trxDate);
        
        // Identity & Rates
        p.setEmployeeId(empId);
        p.setLastName(emp.getLastName());
        p.setFirstName(emp.getFirstName());
        p.setHourlyRate(emp.getHourlyRate());
        p.setTotalHoursWorked(totalHoursWorked);
        p.setPeriod(period);

        // Earnings Breakdown
        p.setBasicSalary(semiMonthlyBasic);
        p.setRiceAllowance(rice);
        p.setPhoneAllowance(phone);
        p.setClothingAllowance(clothing);
        p.setOvertimePay(totalOvertimePay);
        p.setGrossIncome(grossIncome);

        // Deductions Breakdown
        p.setLateDeduction(totalLateDeduction);
        p.setSss(sss);
        p.setPhilHealth(ph);
        p.setPagIbig(pagibig);
        p.setWithholdingTax(tax);
        p.setTotalDeductions(totalGovDeductions + tax + totalLateDeduction);

        p.setNetPay(grossIncome - (totalGovDeductions + tax));

        // Save horizontal record
        if (payslipRepo.save(p)) {
            auditRepo.logPayrollChange(String.valueOf(processedByUserId), "Generated Payslip: " + p.getTransactionId());
        }

        return p;
    }

    private double calculateHours(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes > 240) {
            minutes -= 60; // Lunch break deduction
        }
        return Math.max(0, minutes / 60.0);
    }
}
