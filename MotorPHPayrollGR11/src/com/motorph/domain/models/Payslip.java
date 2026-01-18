/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

/**
 * Represents the final calculated pay for a specific period. Matches the
 * columns in records_payslips CSV.
 *
 * @author ACER
 */
public class Payslip {

    private String transactionId;
    private int employeeId;
    private PayPeriod period;

    // Earnings
    private double basicSalary;
    private double riceAllowance;
    private double phoneAllowance;
    private double clothingAllowance;
    private double overtimePay;    // NEW: Needed for OT calculations
    private double grossIncome;

    // Deductions
    private double lateDeduction;  // NEW: Needed for 8:11 AM grace period rule
    private double sss, philHealth, pagIbig, withholdingTax;
    private double totalDeductions; // NEW: Sum of all deductions

    // Net Result
    private double netPay;

    // --- GETTERS & SETTERS ---
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String id) {
        this.transactionId = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int id) {
        this.employeeId = id;
    }

    public PayPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PayPeriod p) {
        this.period = p;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double v) {
        this.basicSalary = v;
    }

    public double getriceAllowance() {
        return riceAllowance;
    }

    public void setriceAllowance(double v) {
        this.riceAllowance = v;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double v) {
        this.phoneAllowance = v;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double v) {
        this.clothingAllowance = v;
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(double v) {
        this.overtimePay = v;
    }

    public double getGrossIncome() {
        return grossIncome;
    }

    public void setGrossIncome(double v) {
        this.grossIncome = v;
    }

    public double getLateDeduction() {
        return lateDeduction;
    }

    public void setLateDeduction(double v) {
        this.lateDeduction = v;
    }

    public double getSss() {
        return sss;
    }

    public void setSss(double v) {
        this.sss = v;
    }

    public double getPhilHealth() {
        return philHealth;
    }

    public void setPhilHealth(double v) {
        this.philHealth = v;
    }

    public double getPagIbig() {
        return pagIbig;
    }

    public void setPagIbig(double v) {
        this.pagIbig = v;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(double v) {
        this.withholdingTax = v;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double v) {
        this.totalDeductions = v;
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double v) {
        this.netPay = v;
    }
}
