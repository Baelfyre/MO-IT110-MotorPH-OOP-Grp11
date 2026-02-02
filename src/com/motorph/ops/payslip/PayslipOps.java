/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.payslip;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import java.util.List;

/**
 * Use-case layer for Payslip viewing. All roles can view payslips since all
 * roles are employees.
 *
 * @author ACER
 */
public interface PayslipOps {

    Payslip viewPayslipForPeriod(int empId, PayPeriod period);

    Payslip viewLatestPayslip(int empId);

    List<Payslip> listPayslipHistory(int empId);
}
