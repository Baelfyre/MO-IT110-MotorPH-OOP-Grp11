/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.payslip;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.repository.PayslipRepository;
import com.motorph.service.LogService;
import java.util.List;

/**
 * Implementation of payslip use cases using the repository.
 *
 * @author ACER
 */
public class PayslipOpsImpl implements PayslipOps {

    private final PayslipRepository payslipRepo;
    private final LogService logService; // optional, may be null

    public PayslipOpsImpl(PayslipRepository payslipRepo) {
        this.payslipRepo = payslipRepo;
        this.logService = null; // logging not enabled in this constructor
    }

    public PayslipOpsImpl(PayslipRepository payslipRepo, LogService logService) {
        this.payslipRepo = payslipRepo;
        this.logService = logService; // logging enabled
    }

    @Override
    public Payslip viewPayslipForPeriod(int empId, PayPeriod period) {
        Payslip p = payslipRepo.findByEmployeeAndPeriod(empId, period);

        // Records a system log entry when logging is available.
        if (logService != null) {
            logService.recordAction(
                    String.valueOf(empId),
                    "PAYSLIP_VIEW_PERIOD",
                    "Viewed payslip for EmpID=" + empId + " Period=" + (period != null ? period.toKey() : "null")
            );
        }

        return p;
    }

    @Override
    public Payslip viewLatestPayslip(int empId) {
        Payslip p = payslipRepo.findLatestByEmployee(empId);

        // Records a system log entry when logging is available.
        if (logService != null) {
            logService.recordAction(
                    String.valueOf(empId),
                    "PAYSLIP_VIEW_LATEST",
                    "Viewed latest payslip for EmpID=" + empId
            );
        }

        return p;
    }

    @Override
    public List<Payslip> listPayslipHistory(int empId) {
        List<Payslip> list = payslipRepo.findAllByEmployee(empId);

        // Records a system log entry when logging is available.
        if (logService != null) {
            logService.recordAction(
                    String.valueOf(empId),
                    "PAYSLIP_VIEW_HISTORY",
                    "Viewed payslip history for EmpID=" + empId + " Count=" + (list != null ? list.size() : 0)
            );
        }

        return list;
    }
}
