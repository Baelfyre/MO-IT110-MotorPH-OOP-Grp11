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

/**
 * Coordinates payroll processing, logging, and bulk execution.
 *
 * DTR approval and payroll approval are tracked in the shared records_payroll
 * CSV.
 *
 * @author ACER
 */
public class PayrollOpsImpl implements PayrollOps {

    private final PayrollService payrollService;
    private final EmployeeRepository empRepo;
    private final LogService logService;
    private final PayrollApprovalRepository approvalRepo;

    public PayrollOpsImpl(PayrollService payrollService,
            EmployeeRepository empRepo,
            LogService logService,
            PayrollApprovalRepository approvalRepo) {
        this.payrollService = payrollService;
        this.empRepo = empRepo;
        this.logService = logService;
        this.approvalRepo = approvalRepo;
    }

    @Override
    public PayPeriod resolvePeriod(LocalDate date) {
        return PayPeriod.fromDateSemiMonthly(date);
    }

    @Override
    public Payslip processPayrollForEmployee(int empId, PayPeriod period, int processedByUserId) {
        if (period == null) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_FAILED",
                    "Payroll failed. Period is null for EmpID=" + empId
            );
            return null;
        }

        // Shared approval tracker row exists for this empId + period.
        approvalRepo.ensureRowExists(empId, period);

        // Prevent duplicate payroll runs once already approved for the same period.
        ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
        if (payrollStatus == ApprovalStatus.APPROVED) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_SKIPPED_ALREADY_APPROVED",
                    "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " Payroll_Status=" + payrollStatus.name()
            );
            return null;
        }

        // DTR approval gate applied before payroll execution.
        ApprovalStatus dtrStatus = approvalRepo.getDtrStatus(empId, period);
        if (dtrStatus != ApprovalStatus.APPROVED) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_SKIPPED_DTR_NOT_APPROVED",
                    "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " DTR_Status=" + dtrStatus.name()
            );
            return null;
        }

        Payslip p = payrollService.generatePayslip(empId, period, processedByUserId);

        logService.recordAction(
                String.valueOf(processedByUserId),
                (p != null) ? "PAYROLL_OK" : "PAYROLL_FAILED",
                (p != null)
                        ? ("Processed payroll for EmpID=" + empId + " TX=" + p.getTransactionId())
                        : ("Payroll failed for EmpID=" + empId + " Period=" + period.toKey())
        );

        // Payroll approval is recorded after a successful payslip snapshot.
        if (p != null) {
            approvalRepo.upsertPayrollApproval(
                    empId,
                    period,
                    processedByUserId,
                    ApprovalStatus.APPROVED,
                    LocalDateTime.now()
            );
        }

        return p;
    }

    @Override
    public List<PayrollRunResult> processPayrollForPeriod(PayPeriod period, int processedByUserId) {
        List<Employee> employees = empRepo.findAll();
        List<PayrollRunResult> results = new ArrayList<>();

        if (period == null) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_BATCH_FAILED",
                    "Payroll batch failed. Period is null."
            );
            return results;
        }

        for (Employee e : employees) {
            int empId = e.getEmployeeNumber();

            // Shared approval tracker row exists for this empId + period.
            approvalRepo.ensureRowExists(empId, period);

            ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
            if (payrollStatus == ApprovalStatus.APPROVED) {
                results.add(new PayrollRunResult(
                        empId,
                        "",
                        false,
                        "Skipped. Payroll already approved (" + payrollStatus.name() + ")."
                ));

                logService.recordAction(
                        String.valueOf(processedByUserId),
                        "PAYROLL_SKIPPED_ALREADY_APPROVED",
                        "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " Payroll_Status=" + payrollStatus.name()
                );
                continue;
            }

            ApprovalStatus dtrStatus = approvalRepo.getDtrStatus(empId, period);
            if (dtrStatus != ApprovalStatus.APPROVED) {
                results.add(new PayrollRunResult(
                        empId,
                        "",
                        false,
                        "Skipped. DTR not approved (" + dtrStatus.name() + ")."
                ));

                logService.recordAction(
                        String.valueOf(processedByUserId),
                        "PAYROLL_SKIPPED_DTR_NOT_APPROVED",
                        "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " DTR_Status=" + dtrStatus.name()
                );
                continue;
            }

            Payslip p = payrollService.generatePayslip(empId, period, processedByUserId);

            if (p != null) {
                results.add(new PayrollRunResult(empId, p.getTransactionId(), true, "Payslip snapshot saved."));

                approvalRepo.upsertPayrollApproval(
                        empId,
                        period,
                        processedByUserId,
                        ApprovalStatus.APPROVED,
                        LocalDateTime.now()
                );
            } else {
                results.add(new PayrollRunResult(empId, "", false, "Payslip generation or save failed."));
            }
        }

        logService.recordAction(
                String.valueOf(processedByUserId),
                "PAYROLL_BATCH_DONE",
                "Processed payroll batch for period " + period.toKey()
        );

        return results;
    }
}
