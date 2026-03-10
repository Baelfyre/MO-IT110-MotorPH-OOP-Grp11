/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.payroll;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;

import java.time.LocalDate;
import java.util.List;

public interface PayrollOps {

    PayPeriod resolvePeriod(LocalDate date);

    Payslip processPayrollForEmployee(int empId, PayPeriod period, User currentUser);

    List<PayrollRunResult> processPayrollForPeriod(PayPeriod period, User currentUser);
}
