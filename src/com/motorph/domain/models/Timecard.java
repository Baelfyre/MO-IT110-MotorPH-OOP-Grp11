/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.ApprovalStatus;

/**
 * Represents an approval wrapper for an employee's DTR coverage.
 *
 * @author ACER
 */
public class Timecard {

    private int employeeId;
    private ApprovalStatus status;

    public Timecard(int employeeId) {
        this.employeeId = employeeId;
        this.status = ApprovalStatus.PENDING;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
