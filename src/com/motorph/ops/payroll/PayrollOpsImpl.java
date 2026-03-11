/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.payroll;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.PayrollApprovalRepository;
import com.motorph.service.LogService;
import com.motorph.service.PayrollService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.motorph.domain.models.User;

/**
 * Handles payroll execution flow through the ops layer.
 *
 * Keeps UI calls separate from service and repository access.
 *
 * @author OngoJ.
 */
public class PayrollOpsImpl implements PayrollOps {

    private final PayrollService payrollService;
    private final EmployeeRepository empRepo;
    private final LogService logService;
    private final PayrollApprovalRepository approvalRepo;

    /**
     * Initializes payroll dependencies.
     */
    public PayrollOpsImpl(PayrollService payrollService,
            EmployeeRepository empRepo,
            LogService logService,
            PayrollApprovalRepository approvalRepo) {
        this.payrollService = payrollService;
        this.empRepo = empRepo;
        this.logService = logService;
        this.approvalRepo = approvalRepo;
    }

    /**
     * Resolves the active semi-monthly payroll period.
     */
    @Override
    public PayPeriod resolvePeriod(LocalDate date) {
        return PayPeriod.fromDateSemiMonthly(date);
    }

    /**
     * Loads payroll queue rows for the selected period.
     */
    @Override
    public List<PayrollQueueItem> listEmployeesForPeriod(PayPeriod period) {
        List<Employee> employees = empRepo.findAll();
        List<PayrollQueueItem> out = new ArrayList<>();
        if (period == null) {
            return out;
        }

        for (Employee employee : employees) {
            if (employee == null) {
                continue;
            }

            int empId = employee.getEmployeeNumber();
            approvalRepo.ensureRowExists(empId, period);
            out.add(new PayrollQueueItem(
                    empId,
                    employee.getLastName() + ", " + employee.getFirstName(),
                    approvalRepo.getDtrStatus(empId, period),
                    approvalRepo.getPayrollStatus(empId, period)
            ));
        }
        return out;
    }

    /**
     * Processes payroll for one employee only.
     */
    @Override
    public Payslip processPayrollForEmployee(int empId, PayPeriod period, User currentUser) {
        // 1. BACKEND RBAC VERIFICATION
        if (currentUser == null || !currentUser.hasPermission("CAN_PROCESS_PAYROLL")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to process payroll for EmpID: " + empId
            );
            throw new SecurityException("Access Denied: You do not have permission to process payroll.");
        }

        int processedByUserId = currentUser.getId();

        if (period == null) {
            logService.recordAction(String.valueOf(processedByUserId), "PAYROLL_FAILED", "Payroll failed. Period is null for EmpID=" + empId);
            return null;
        }

        approvalRepo.ensureRowExists(empId, period);

        ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
        if (payrollStatus == ApprovalStatus.APPROVED) {
            logService.recordAction(String.valueOf(processedByUserId), "PAYROLL_SKIPPED_ALREADY_APPROVED", "Skipped payroll for EmpID=" + empId);
            return null;
        }

        ApprovalStatus dtrStatus = approvalRepo.getDtrStatus(empId, period);
        if (dtrStatus != ApprovalStatus.APPROVED) {
            logService.recordAction(String.valueOf(processedByUserId), "PAYROLL_SKIPPED_DTR_NOT_APPROVED", "Skipped payroll for EmpID=" + empId);
            return null;
        }

        Payslip p = payrollService.generatePayslip(empId, period, processedByUserId);

        logService.recordAction(
                String.valueOf(processedByUserId),
                (p != null) ? "PAYROLL_OK" : "PAYROLL_FAILED",
                (p != null) ? ("Processed payroll for EmpID=" + empId) : ("Payroll failed for EmpID=" + empId)
        );

        if (p != null) {
            approvalRepo.upsertPayrollApproval(empId, period, processedByUserId, ApprovalStatus.APPROVED, LocalDateTime.now());
        }

        return p;
    }

    @Override
    public List<PayrollRunResult> processPayrollForPeriod(PayPeriod period, User currentUser) {
        if (currentUser == null || !currentUser.hasPermission("CAN_PROCESS_PAYROLL")) {
            logService.recordAction(
                    currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                    "SECURITY_VIOLATION",
                    "Unauthorized attempt to run batch payroll."
            );
            throw new SecurityException("Access Denied: You do not have permission to process payroll.");
        }

        int processedByUserId = currentUser.getId();
        List<Employee> employees = empRepo.findAll();
        List<PayrollRunResult> results = new ArrayList<>();

        if (period == null) {
            logService.recordAction(String.valueOf(processedByUserId), "PAYROLL_BATCH_FAILED", "Payroll batch failed. Period is null.");
            return results;
        }

        for (Employee e : employees) {
            int empId = e.getEmployeeNumber();
            approvalRepo.ensureRowExists(empId, period);

            ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
            if (payrollStatus == ApprovalStatus.APPROVED) {
                results.add(new PayrollRunResult(empId, "", false, "Skipped. Payroll already approved."));
                continue;
            }

            ApprovalStatus dtrStatus = approvalRepo.getDtrStatus(empId, period);
            if (dtrStatus != ApprovalStatus.APPROVED) {
                results.add(new PayrollRunResult(empId, "", false, "Skipped. DTR not approved."));
                continue;
            }

            Payslip p = payrollService.generatePayslip(empId, period, processedByUserId);

            if (p != null) {
                results.add(new PayrollRunResult(empId, p.getTransactionId(), true, "Payslip snapshot saved."));
                approvalRepo.upsertPayrollApproval(empId, period, processedByUserId, ApprovalStatus.APPROVED, LocalDateTime.now());
            } else {
                results.add(new PayrollRunResult(empId, "", false, "Payslip generation or save failed."));
            }
        }

        logService.recordAction(String.valueOf(processedByUserId), "PAYROLL_BATCH_DONE", "Processed payroll batch for period " + period.toKey());
        return results;
        
    }
}