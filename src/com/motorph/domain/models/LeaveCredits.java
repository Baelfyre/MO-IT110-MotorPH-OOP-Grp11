/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

/**
 * Leave credits record expressed in hours. Represents total credits and
 * cumulative leave taken.
 *
 * @author ACER
 */
public class LeaveCredits {

    private final int employeeNumber;
    private final String lastName;
    private final String firstName;

    private final double leaveCreditsHours;
    private final double leaveTakenHours;

    public LeaveCredits(int employeeNumber, String lastName, String firstName,
            double leaveCreditsHours, double leaveTakenHours) {
        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.leaveCreditsHours = leaveCreditsHours;
        this.leaveTakenHours = leaveTakenHours;
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

    public double getLeaveCreditsHours() {
        return leaveCreditsHours;
    }

    public double getLeaveTakenHours() {
        return leaveTakenHours;
    }

    public double getRemainingHours() {
        return Math.max(0.0, leaveCreditsHours - leaveTakenHours);
    }
}
