/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.ApprovalStatus;
import java.util.List;

/**
 * Represents a weekly/period time record for an employee.
 *
 * @author ACER
 */
public class Timecard {

    private int employeeId;
    private PayPeriod period;
    private List<TimeEntry> entries;
    private ApprovalStatus status;

    public Timecard(int employeeId, PayPeriod period, List<TimeEntry> entries) {
        this.employeeId = employeeId;
        this.period = period;
        this.entries = entries;
        this.status = ApprovalStatus.PENDING; // Default status
    }

    // --- MISSING METHOD FIXED HERE ---
    public int getEmployeeId() {
        return employeeId;
    }
    // ---------------------------------

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public List<TimeEntry> getEntries() {
        return entries;
    }

    public PayPeriod getPeriod() {
        return period;
    }
}
