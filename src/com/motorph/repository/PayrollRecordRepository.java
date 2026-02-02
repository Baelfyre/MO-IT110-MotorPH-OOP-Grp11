/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.PayPeriod;

/**
 *
 * @author ACER
 */
public interface PayrollRecordRepository {

    boolean ensurePeriodRow(int empId, PayPeriod period, String transactionId);

    boolean updateDtrApproval(int empId, PayPeriod period, int approvedBy, String status);

    boolean updatePayrollApproval(int empId, PayPeriod period, int approvedBy, String status);

    boolean updateComputedTotals(int empId, PayPeriod period,
            double totalHoursWorked, double grossPay, double totalDeductions, double netPay);

    boolean isDtrApproved(int empId, PayPeriod period);

    boolean isPayrollRunApproved(int empId, PayPeriod period);
}
