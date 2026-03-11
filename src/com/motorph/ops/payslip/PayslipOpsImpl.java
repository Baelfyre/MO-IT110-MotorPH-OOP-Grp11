/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.payslip;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;
import com.motorph.repository.PayslipRepository;
import com.motorph.service.LogService;
import java.util.List;

public class PayslipOpsImpl implements PayslipOps {

    private final PayslipRepository payslipRepo;
    private final LogService logService; 

    public PayslipOpsImpl(PayslipRepository payslipRepo) {
        this.payslipRepo = payslipRepo;
        this.logService = null; 
    }

    public PayslipOpsImpl(PayslipRepository payslipRepo, LogService logService) {
        this.payslipRepo = payslipRepo;
        this.logService = logService; 
    }

    // --- CENTRALIZED RBAC CHECK ---
    private void verifyAccess(int targetEmpId, User currentUser, String actionName) {
        if (currentUser == null) {
            throw new SecurityException("Access Denied: You must be logged in.");
        }
        
        boolean isOwnPayslip = (currentUser.getId() == targetEmpId);
        boolean isPayrollAdmin = currentUser.hasPermission("CAN_GENERATE_PAYSLIP");
        
        if (!isOwnPayslip && !isPayrollAdmin) {
            if (logService != null) {
                logService.recordAction(String.valueOf(currentUser.getId()), "SECURITY_VIOLATION", "Unauthorized " + actionName + " attempt for EmpID: " + targetEmpId);
            }
            throw new SecurityException("Access Denied: You can only view your own payslips.");
        }
    }

    @Override
    public Payslip viewPayslipForPeriod(int empId, PayPeriod period, User currentUser) {
        verifyAccess(empId, currentUser, "viewPayslipForPeriod");
        
        Payslip p = payslipRepo.findByEmployeeAndPeriod(empId, period);

        if (logService != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()), // Changed to currentUser.getId() for accurate logging
                    "PAYSLIP_VIEW_PERIOD",
                    "Viewed payslip for EmpID=" + empId + " Period=" + (period != null ? period.toKey() : "null")
            );
        }
        return p;
    }

    @Override
    public Payslip viewLatestPayslip(int empId, User currentUser) {
        verifyAccess(empId, currentUser, "viewLatestPayslip");

        Payslip p = payslipRepo.findLatestByEmployee(empId);

        if (logService != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()), 
                    "PAYSLIP_VIEW_LATEST",
                    "Viewed latest payslip for EmpID=" + empId
            );
        }
        return p;
    }

    @Override
    public List<Payslip> listPayslipHistory(int empId, User currentUser) {
        verifyAccess(empId, currentUser, "listPayslipHistory");

        List<Payslip> list = payslipRepo.findAllByEmployee(empId);

        if (logService != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()), 
                    "PAYSLIP_VIEW_HISTORY",
                    "Viewed payslip history for EmpID=" + empId + " Count=" + (list != null ? list.size() : 0)
            );
        }
        return list;
    }
}
