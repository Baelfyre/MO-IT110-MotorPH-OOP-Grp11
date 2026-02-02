/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;

/**
 * Master record of an employee containing identity, government IDs, and
 * compensation-related fields. The data structure maps to the employee master
 * dataset and excludes leave balance state, which is handled by a separate
 * LeaveCredits module.
 *
 @author ACER
 */
public class Employee {

    // --- Identity and contact fields ---
    private final int employeeNumber;        // Employee #
    private final String lastName;           // Last Name
    private final String firstName;          // First Name
    private final LocalDate birthday;        // Birthday
    private final String address;            // Address
    private final String phoneNumber;        // Phone Number

    // --- Government identifiers ---
    private final String sssNumber;          // SSS #
    private final String philHealthNumber;   // Philhealth #
    private final String tinNumber;          // TIN #
    private final String pagIbigNumber;      // Pag-ibig #

    // --- Employment details ---
    private final String status;             // Status
    private final String position;           // Position
    private final String immediateSupervisor;// Immediate Supervisor

    // --- Compensation profile ---
    private final double basicSalary;        // Basic Salary
    private final double riceAllowance;      // Rice Subsidy
    private final double phoneAllowance;     // Phone Allowance
    private final double clothingAllowance;  // Clothing Allowance
    private final double grossSemiMonthlyRate; // Gross Semi-monthly Rate
    private final double hourlyRate;         // Hourly Rate

    public Employee(
            int employeeNumber,
            String lastName,
            String firstName,
            LocalDate birthday,
            String address,
            String phoneNumber,
            String sssNumber,
            String philHealthNumber,
            String tinNumber,
            String pagIbigNumber,
            String status,
            String position,
            String immediateSupervisor,
            double basicSalary,
            double riceAllowance,
            double phoneAllowance,
            double clothingAllowance,
            double grossSemiMonthlyRate,
            double hourlyRate
    ) {
        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.sssNumber = sssNumber;
        this.philHealthNumber = philHealthNumber;
        this.tinNumber = tinNumber;
        this.pagIbigNumber = pagIbigNumber;
        this.status = status;
        this.position = position;
        this.immediateSupervisor = immediateSupervisor;
        this.basicSalary = basicSalary;
        this.riceAllowance = riceAllowance;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.grossSemiMonthlyRate = grossSemiMonthlyRate;
        this.hourlyRate = hourlyRate;
    }

    public int getId() {
        return employeeNumber;
    }

    public int getEmployeeNumber() {
        return employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    public String getPhilHealthNumber() {
        return philHealthNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    public String getPagIbigNumber() {
        return pagIbigNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getPosition() {
        return position;
    }

    public String getImmediateSupervisor() {
        return immediateSupervisor;
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

    /**
     * CSV serialization for the employee master dataset. Leave balance fields
     * are intentionally excluded because they are tracked in the LeaveCredits
     * module.
     */
    public String toCsvRow() {
        String dateStr = (birthday != null)
                ? birthday.format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"))
                : "";

        return employeeNumber + ","
                + escape(lastName) + ","
                + escape(firstName) + ","
                + dateStr + ","
                + escape(address) + ","
                + escape(phoneNumber) + ","
                + escape(sssNumber) + ","
                + escape(philHealthNumber) + ","
                + escape(tinNumber) + ","
                + escape(pagIbigNumber) + ","
                + escape(status) + ","
                + escape(position) + ","
                + escape(immediateSupervisor) + ","
                + "\"" + String.format("%,.2f", basicSalary) + "\","
                + "\"" + String.format("%,.2f", riceAllowance) + "\","
                + "\"" + String.format("%,.2f", phoneAllowance) + "\","
                + "\"" + String.format("%,.2f", clothingAllowance) + "\","
                + "\"" + String.format("%,.2f", grossSemiMonthlyRate) + "\","
                + String.format("%.2f", hourlyRate);
    }

    private String escape(String data) {
        if (data == null) {
            return "";
        }
        if (data.contains(",")) {
            return "\"" + data + "\"";
        }
        return data;
    }
}
