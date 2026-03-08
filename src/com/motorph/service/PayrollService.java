/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.Compensation;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.AuditRepository;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.PayslipRepository;
import com.motorph.repository.TimeEntryRepository;
import com.motorph.service.strategy.DeductionStrategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author OngoJ.
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

    private static final DateTimeFormatter TX_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    public PayrollService(EmployeeRepository empRepo,
            TimeEntryRepository timeEntryRepo,
            DeductionStrategy deductionStrategy,
            PayslipRepository payslipRepo,
            AuditRepository auditRepo) {
        this.empRepo = empRepo;
        this.timeEntryRepo = timeEntryRepo;
        this.deductionStrategy = deductionStrategy;
        this.payslipRepo = payslipRepo;
        this.auditRepo = auditRepo;
    }

    // Annotation: Generates and saves payslip for employee and pay period.
    public Payslip generatePayslip(int empId, PayPeriod period, int processedByUserId) {
        if (empId <= 0 || period == null) {
            return null;
        }

        Employee emp = empRepo.findById(empId);
        if (emp == null) {
            return null;
        }

        Compensation comp = emp.getCompensation();
        if (comp == null) {
            return null;
        }

        Payslip payslip = computePayslip(emp, period, comp, processedByUserId, 0.0);
        if (payslip == null) {
            return null;
        }

        boolean saved = payslipRepo.save(payslip);
        if (!saved) {
            return null;
        }

        auditRepo.logPayrollChange(String.valueOf(processedByUserId), "Generated payslip for EmpID=" + empId + " period " + period.toKey());
        return payslip;
    }

    // Annotation: Compatibility overload for older callers.
    public Payslip generatePayslip(int empId, PayPeriod period) {
        return generatePayslip(empId, period, 0);
    }

    // Annotation: Overload used to demonstrate polymorphism with optional bonus amount.
    public Payslip generatePayslip(int empId, PayPeriod period, double bonusAmount) {
        if (empId <= 0 || period == null) {
            return null;
        }
        Employee emp = empRepo.findById(empId);
        if (emp == null || emp.getCompensation() == null) {
            return null;
        }
        Payslip payslip = computePayslip(emp, period, emp.getCompensation(), 0, bonusAmount);
        if (payslip == null || !payslipRepo.save(payslip)) {
            return null;
        }
        return payslip;
    }

    private Payslip computePayslip(Employee emp, PayPeriod period, Compensation comp, int processedByUserId, double bonusAmount) {
        int empId = emp.getEmployeeNumber();
        List<TimeEntry> entries = timeEntryRepo.findByEmployeeAndPeriod(empId, period);

        double totalLateDeduction = 0.0;
        double totalOvertimePay = 0.0;
        double totalHoursWorked = 0.0;

        for (TimeEntry entry : entries) {
            if (entry == null || entry.getTimeIn() == null || entry.getTimeOut() == null) {
                continue;
            }

            double dailyHours = calculateHours(entry.getTimeIn(), entry.getTimeOut());
            totalHoursWorked += dailyHours;

            if (entry.getTimeIn().isAfter(GRACE_END)) {
                long minutesLate = Duration.between(WORK_START, entry.getTimeIn()).toMinutes();
                totalLateDeduction += minutesLate * (comp.getHourlyRate() / 60.0);
            }

            if (entry.getTimeOut().isBefore(WORK_END)) {
                long minutesUndertime = Duration.between(entry.getTimeOut(), WORK_END).toMinutes();
                totalLateDeduction += minutesUndertime * (comp.getHourlyRate() / 60.0);
            }

            if (dailyHours > 8.0) {
                double otHours = dailyHours - 8.0;
                totalOvertimePay += otHours * comp.getHourlyRate() * 1.25;
            }
        }

        double semiMonthlyBasic = comp.getBasicSalary() / 2.0;
        double rice = comp.getRiceSubsidy() / 2.0;
        double phone = comp.getPhoneAllowance() / 2.0;
        double clothing = comp.getClothingAllowance() / 2.0;

        double gross = semiMonthlyBasic + rice + phone + clothing + totalOvertimePay + Math.max(0.0, bonusAmount);

        double payAfterTimeDeduction = Math.max(0.0, gross - totalLateDeduction);

        double sss = deductionStrategy.calculateSSS(payAfterTimeDeduction);
        double ph = deductionStrategy.calculatePhilHealth(payAfterTimeDeduction);
        double pagibig = deductionStrategy.calculatePagibig(payAfterTimeDeduction);
        double totalGov = sss + ph + pagibig;

        double taxableIncome = Math.max(0.0, payAfterTimeDeduction - totalGov);
        double tax = deductionStrategy.calculateTax(taxableIncome);

        double totalDeductions = totalLateDeduction + totalGov + tax;
        double net = Math.max(0.0, gross - totalDeductions);

        String txId = buildTransactionId(empId, period);

        com.motorph.domain.models.LeaveCredits lc = new com.motorph.repository.csv.CsvLeaveCreditsRepository().findByEmpId(empId);
        double leaveCredits = lc == null ? 0.0 : lc.getLeaveCreditsHours();
        double leaveTaken = lc == null ? 0.0 : lc.getLeaveTakenHours();
        double leaveBalance = lc == null ? 0.0 : lc.getRemainingHours();

        return new Payslip(
                txId,
                empId,
                emp.getLastName(),
                emp.getFirstName(),
                period,
                comp.getBasicSalary(),
                comp.getRiceSubsidy(),
                comp.getPhoneAllowance(),
                comp.getClothingAllowance(),
                comp.getGrossSemiMonthlyRate(),
                comp.getHourlyRate(),
                totalHoursWorked,
                totalOvertimePay,
                gross,
                totalLateDeduction,
                sss,
                ph,
                pagibig,
                tax,
                totalDeductions,
                net,
                processedByUserId,
                LocalDateTime.now(),
                leaveCredits,
                leaveTaken,
                leaveBalance
        );
    }

    // Annotation: Deterministic TX ID for duplicate prevention.
    private String buildTransactionId(int empId, PayPeriod period) {
        String start = period.getStartDate().format(TX_DATE_FMT);
        String end = period.getEndDate().format(TX_DATE_FMT);
        return "TX-" + empId + "-" + start + "-" + end;
    }

    private double calculateHours(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes > 240) {
            minutes -= 60;
        }
        return Math.max(0.0, minutes / 60.0);
    }
}
