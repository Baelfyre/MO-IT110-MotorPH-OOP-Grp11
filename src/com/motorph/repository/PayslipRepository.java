/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import java.util.List;

/**
 * Contract for storing and reading payslip snapshots.
 *
 * @author ACER
 */
public interface PayslipRepository {

    boolean save(Payslip payslip);

    Payslip findByEmployeeAndPeriod(int empId, PayPeriod period);

    Payslip findLatestByEmployee(int empId);

    List<Payslip> findAllByEmployee(int empId);
}
