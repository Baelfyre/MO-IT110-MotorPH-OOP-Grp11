package com.motorph.ops.payroll;

import com.motorph.domain.enums.ApprovalStatus;

/**
 * Read-only row used by the Payroll panel.
 */
public class PayrollQueueItem {

    private final int employeeId;
    private final String employeeName;
    private final ApprovalStatus dtrStatus;
    private final ApprovalStatus payrollStatus;

    public PayrollQueueItem(int employeeId, String employeeName, ApprovalStatus dtrStatus, ApprovalStatus payrollStatus) {
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
