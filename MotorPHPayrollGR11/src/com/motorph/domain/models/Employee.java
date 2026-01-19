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

    // --- Columns 0-5: Personal Info ---
    private final int employeeNumber;       // Col 0
    private final String lastName;          // Col 1
    private final String firstName;         // Col 2
    private final LocalDate birthday;       // Col 3
    private final String address;           // Col 4
    private final String phoneNumber;       // Col 5

    // --- Columns 6-9: Gov IDs ---
    private final String sssNumber;         // Col 6
    private final String philHealthNumber;  // Col 7
    private final String tinNumber;         // Col 8
    private final String pagIbigNumber;     // Col 9

    // --- Columns 10-12: Job Details ---
    private final String status;            // Col 10
    private final String position;          // Col 11
    private final String immediateSupervisor; // Col 12

    // --- Columns 13-18: Compensation ---
    private final double basicSalary;           // Col 13
    private final double  riceAllowance;         // Col 14
    private final double  phoneAllowance;        // Col 15
    private final double  clothingAllowance;     // Col 16
    private final double  grossSemiMonthlyRate;  // Col 17
    private final double  hourlyRate;            // Col 18
    private final double  leaveCredits;          // Col 19

    // --- CONSTRUCTOR ---
    public Employee(int employeeNumber, String lastName, String firstName, LocalDate birthday,
            String address, String phoneNumber, String sssNumber, String philHealthNumber,
            String tinNumber, String pagIbigNumber, String status, String position,
            String immediateSupervisor, double basicSalary, double riceAllowance,
            double phoneAllowance, double clothingAllowance, double grossSemiMonthlyRate,
            double hourlyRate, double leaveCredits) {
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

    // --- GETTERS (Aligned with your Repository calls) ---
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

    // These specific names fix your errors:
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

    public double getriceAllowance() {
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

    public double leaveCredits() {
        return leaveCredits;
    }
    // Setters can be added if needed for updates
}
