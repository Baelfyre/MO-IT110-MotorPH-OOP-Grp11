/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;

/**
 * Represents the master record of an employee. Contains personal details,
 * government IDs, and financial data used for payroll. Maps directly to the
 * records in data_Employee.csv. * Key Responsibilities: - Stores salary and
 * allowance rates. - Links to the immediate supervisor for team structure.
 *
 * * @author ACER
 */
public class Employee {

    // --- Columns 0-5 ---
    private final int employeeNumber;       // 0
    private final String lastName;          // 1
    private final String firstName;         // 2
    private final LocalDate birthday;       // 3
    private final String address;           // 4
    private final String phoneNumber;       // 5

    // --- Columns 6-9 ---
    private final String sssNumber;         // 6
    private final String philHealthNumber;  // 7
    private final String tinNumber;         // 8
    private final String pagIbigNumber;     // 9

    // --- Columns 10-12 ---
    private final String status;            // 10
    private final String position;          // 11
    private final String immediateSupervisor; // 12

    // --- Columns 13-19 ---
    private final double basicSalary;           // 13
    private final double riceAllowance;         // 14
    private final double phoneAllowance;        // 15
    private final double clothingAllowance;     // 16
    private final double grossSemiMonthlyRate;  // 17
    private final double hourlyRate;            // 18
    private final int leaveCredits;             // 19 <--- NEW FIELD

    // --- CONSTRUCTOR (Updated for 20 columns) ---
    public Employee(int employeeNumber, String lastName, String firstName, LocalDate birthday,
            String address, String phoneNumber, String sssNumber, String philHealthNumber,
            String tinNumber, String pagIbigNumber, String status, String position,
            String immediateSupervisor, double basicSalary, double riceAllowance,
            double phoneAllowance, double clothingAllowance, double grossSemiMonthlyRate,
            double hourlyRate, int leaveCredits) {

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
        this.leaveCredits = leaveCredits;
    }

    // --- GETTERS ---
    public int getId() {
        return employeeNumber;
    } // Alias

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

    public int getLeaveCredits() {
        return leaveCredits;
    } // <--- NEW GETTER

    /**
     * Updated CSV serializer to include leaveCredits
     */
    public String toCsvRow() {
        String dateStr = (birthday != null)
                ? birthday.format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy")) : "";

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
                + // Quote numbers to handle commas (e.g. "90,000")
                "\"" + String.format("%,.2f", basicSalary) + "\","
                + "\"" + String.format("%,.2f", riceAllowance) + "\","
                + "\"" + String.format("%,.2f", phoneAllowance) + "\","
                + "\"" + String.format("%,.2f", clothingAllowance) + "\","
                + "\"" + String.format("%,.2f", grossSemiMonthlyRate) + "\","
                + String.format("%.2f", hourlyRate) + ","
                + leaveCredits;
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
