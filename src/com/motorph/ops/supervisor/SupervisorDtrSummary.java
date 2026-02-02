/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.supervisor;

import com.motorph.domain.enums.ApprovalStatus;

/**
 *
 * @author ACER
 */
public class SupervisorDtrSummary {
    private final int employeeId;
    private final String employeeName;
    private final ApprovalStatus dtrStatus;
    private final ApprovalStatus payrollStatus;

    public SupervisorDtrSummary(int employeeId, String employeeName,
                                ApprovalStatus dtrStatus, ApprovalStatus payrollStatus) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.dtrStatus = dtrStatus;
        this.payrollStatus = payrollStatus;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public ApprovalStatus getDtrStatus() {
        return dtrStatus;
    }

    public ApprovalStatus getPayrollStatus() {
        return payrollStatus;
    }
}
