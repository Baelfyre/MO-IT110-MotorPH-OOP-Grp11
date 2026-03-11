/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.payslip;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;
import java.util.List;

public interface PayslipOps {
    Payslip viewPayslipForPeriod(int empId, PayPeriod period, User currentUser);
    Payslip viewLatestPayslip(int empId, User currentUser);
    List<Payslip> listPayslipHistory(int empId, User currentUser);
}
