/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.payroll;

/**
 * Result record for bulk payroll processing.
 *
 * @author ACER
 */
public class PayrollRunResult {

    private final int employeeId;
    private final String transactionId;
    private final boolean success;
    private final String message;

    public PayrollRunResult(int employeeId, String transactionId, boolean success, String message) {
        this.employeeId = employeeId;
        this.transactionId = transactionId;
        this.success = success;
        this.message = message;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
