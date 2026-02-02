/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.payroll;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;

import java.time.LocalDate;
import java.util.List;

/**
 * Use-case boundary for payroll processing. Payroll processing creates a
 * payslip snapshot for a pay period.
 *
 * @author ACER
 */
public interface PayrollOps {

    PayPeriod resolvePeriod(LocalDate date);

    Payslip processPayrollForEmployee(int empId, PayPeriod period, int processedByUserId);

    List<PayrollRunResult> processPayrollForPeriod(PayPeriod period, int processedByUserId);
}
