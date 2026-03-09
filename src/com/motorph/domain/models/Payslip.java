package com.motorph.domain.models;

import java.time.LocalDateTime;

/**
 * Payslip snapshot saved per employee and pay period.
 */
public class Payslip {

    private final String transactionId;
    private final int employeeId;
    private final String lastName;
    private final String firstName;
    private final PayPeriod period;

    private final double basicSalary;
    private final double riceAllowance;
    private final double phoneAllowance;
    private final double clothingAllowance;
    private final double grossSemiMonthlyRate;
    private final double hourlyRate;

    private final double totalHoursWorked;
    private final double overtimePay;
    private final double grossIncome;
    private final double lateDeduction;

    private final double sss;
    private final double philHealth;
    private final double pagIbig;
    private final double withholdingTax;

    private final double totalDeductions;
    private final double netPay;

    private final int processedByUserId;
    private final LocalDateTime dateProcessed;
    private final double leaveCreditsSnapshot;
    private final double leaveTakenSnapshot;
    private final double leaveBalanceSnapshot;

    // Annotation: Full constructor used by CSV repositories and payroll computation.
    public Payslip(
            String transactionId,
            int employeeId,
            String lastName,
            String firstName,
            PayPeriod period,
            double basicSalary,
            double riceAllowance,
            double phoneAllowance,
            double clothingAllowance,
            double grossSemiMonthlyRate,
            double hourlyRate,
            double totalHoursWorked,
            double overtimePay,
            double grossIncome,
            double lateDeduction,
            double sss,
            double philHealth,
            double pagIbig,
            double withholdingTax,
            double totalDeductions,
            double netPay,
            int processedByUserId,
            LocalDateTime dateProcessed,
            double leaveCreditsSnapshot,
            double leaveTakenSnapshot,
            double leaveBalanceSnapshot
    ) {
        this.transactionId = safe(transactionId);
        this.employeeId = employeeId;
        this.lastName = safe(lastName);
        this.firstName = safe(firstName);
        this.period = period;

        this.basicSalary = basicSalary;
        this.riceAllowance = riceAllowance;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;

        this.totalHoursWorked = totalHoursWorked;
        this.overtimePay = overtimePay;
        this.grossIncome = grossIncome;
        this.lateDeduction = lateDeduction;

        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.withholdingTax = withholdingTax;

        this.totalDeductions = totalDeductions;
        this.netPay = netPay;

        this.processedByUserId = processedByUserId;
        this.dateProcessed = dateProcessed;
        this.leaveCreditsSnapshot = leaveCreditsSnapshot;
        this.leaveTakenSnapshot = leaveTakenSnapshot;
        this.leaveBalanceSnapshot = leaveBalanceSnapshot;
    }

    public Payslip(
            String transactionId,
            int employeeId,
            String lastName,
            String firstName,
            PayPeriod period,
            double basicSalary,
            double riceAllowance,
            double phoneAllowance,
            double clothingAllowance,
            double grossSemiMonthlyRate,
            double hourlyRate,
            double totalHoursWorked,
            double overtimePay,
            double grossIncome,
            double lateDeduction,
            double sss,
            double philHealth,
            double pagIbig,
            double withholdingTax,
            double totalDeductions,
            double netPay,
            int processedByUserId,
            LocalDateTime dateProcessed
    ) {
        this(transactionId, employeeId, lastName, firstName, period, basicSalary, riceAllowance, phoneAllowance, clothingAllowance, grossSemiMonthlyRate, hourlyRate, totalHoursWorked, overtimePay, grossIncome, lateDeduction, sss, philHealth, pagIbig, withholdingTax, totalDeductions, netPay, processedByUserId, dateProcessed, 0.0, 0.0, 0.0);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public PayPeriod getPeriod() {
        return period;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getRiceAllowance() {
        return riceAllowance;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public double getGrossSemiMonthlyRate() {
        return grossSemiMonthlyRate;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public double getGrossIncome() {
        return grossIncome;
    }

    public double getLateDeduction() {
        return lateDeduction;
    }

    public double getSss() {
        return sss;
    }

    public double getPhilHealth() {
        return philHealth;
    }

    public double getPagIbig() {
        return pagIbig;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getNetPay() {
        return netPay;
    }

    public int getProcessedByUserId() {
        return processedByUserId;
    }

    public LocalDateTime getDateProcessed() {
        return dateProcessed;
    }

    public double getLeaveCreditsSnapshot() {
        return leaveCreditsSnapshot;
    }

    public double getLeaveTakenSnapshot() {
        return leaveTakenSnapshot;
    }

    public double getLeaveBalanceSnapshot() {
        return leaveBalanceSnapshot;
    }

    public String getPayslipNumber() {
        if (period == null) {
            return transactionId;
        }
        return employeeId + "-" + period.getStartDate().getYear() + "-" + String.format(java.util.Locale.US, "%02d", period.getStartDate().getMonthValue()) + "-" + String.format(java.util.Locale.US, "%02d", period.getStartDate().getDayOfMonth());
    }

    public String getEmployeeName() {
        return (firstName + " " + lastName).trim();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
