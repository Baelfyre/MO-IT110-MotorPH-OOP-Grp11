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
    public Payslip processPayrollForEmployee(int empId, PayPeriod period, int processedByUserId) {
        PayrollExecutionResult result = processEmployeePayroll(empId, period, processedByUserId);
        return result.getPayslip();
    }

    /**
     * Processes payroll for all employees in the selected period.
     */
    @Override
    public List<PayrollRunResult> processPayrollForPeriod(PayPeriod period, int processedByUserId) {
        List<PayrollRunResult> results = new ArrayList<>();

        if (period == null) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_BATCH_FAILED",
                    "Payroll batch failed. Period is null."
            );
            return results;
        }

        List<Employee> employees = empRepo.findAll();
        for (Employee employee : employees) {
            if (employee == null) {
                continue;
            }

            PayrollExecutionResult result = processEmployeePayroll(
                    employee.getEmployeeNumber(),
                    period,
                    processedByUserId
            );
            results.add(result.toRunResult());
        }

        logService.recordAction(
                String.valueOf(processedByUserId),
                "PAYROLL_BATCH_DONE",
                "Processed payroll batch for period " + period.toKey()
        );

        return results;
    }

    /**
     * Runs the shared payroll flow for one employee.
     */
    private PayrollExecutionResult processEmployeePayroll(int empId, PayPeriod period, int processedByUserId) {
        if (period == null) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_FAILED",
                    "Payroll failed. Period is null for EmpID=" + empId
            );
            return PayrollExecutionResult.failed(empId, "", "Payroll failed. Period is null.");
        }

        approvalRepo.ensureRowExists(empId, period);

        ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
        if (payrollStatus == ApprovalStatus.APPROVED) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_SKIPPED_ALREADY_APPROVED",
                    "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " Payroll_Status=" + payrollStatus.name()
            );
            return PayrollExecutionResult.failed(
                    empId,
                    "",
                    "Skipped. Payroll already approved (" + payrollStatus.name() + ")."
            );
        }

        ApprovalStatus dtrStatus = approvalRepo.getDtrStatus(empId, period);
        if (dtrStatus != ApprovalStatus.APPROVED) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_SKIPPED_DTR_NOT_APPROVED",
                    "Skipped payroll for EmpID=" + empId + " Period=" + period.toKey() + " DTR_Status=" + dtrStatus.name()
            );
            return PayrollExecutionResult.failed(
                    empId,
                    "",
                    "Skipped. DTR not approved (" + dtrStatus.name() + ")."
            );
        }

        Payslip payslip = payrollService.generatePayslip(empId, period, processedByUserId);
        if (payslip == null) {
            logService.recordAction(
                    String.valueOf(processedByUserId),
                    "PAYROLL_FAILED",
                    "Payroll failed for EmpID=" + empId + " Period=" + period.toKey()
            );
            return PayrollExecutionResult.failed(empId, "", "Payslip generation or save failed.");
        }

        approvalRepo.upsertPayrollApproval(
                empId,
                period,
                processedByUserId,
                ApprovalStatus.APPROVED,
                LocalDateTime.now()
        );

        logService.recordAction(
                String.valueOf(processedByUserId),
                "PAYROLL_OK",
                "Processed payroll for EmpID=" + empId + " TX=" + payslip.getTransactionId()
        );

        return PayrollExecutionResult.success(
                empId,
                payslip,
                "Payslip snapshot saved."
        );
    }

    /**
     * Keeps one employee payroll result in a single object.
     */
    private static final class PayrollExecutionResult {

        private final int employeeId;
        private final Payslip payslip;
        private final boolean success;
        private final String transactionId;
        private final String message;

        /**
         * Stores the payroll execution result.
         */
        private PayrollExecutionResult(int employeeId,
                Payslip payslip,
                boolean success,
                String transactionId,
                String message) {
            this.employeeId = employeeId;
            this.payslip = payslip;
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        /**
         * Builds a successful execution result.
         */
        private static PayrollExecutionResult success(int employeeId, Payslip payslip, String message) {
            return new PayrollExecutionResult(
                    employeeId,
                    payslip,
                    true,
                    payslip == null ? "" : payslip.getTransactionId(),
                    message
            );
        }

        /**
         * Builds a failed execution result.
         */
        private static PayrollExecutionResult failed(int employeeId, String transactionId, String message) {
            return new PayrollExecutionResult(employeeId, null, false, transactionId, message);
        }

        /**
         * Returns the generated payslip.
         */
        private Payslip getPayslip() {
            return payslip;
        }

        /**
         * Converts the result into a batch output row.
         */
        private PayrollRunResult toRunResult() {
            return new PayrollRunResult(employeeId, transactionId, success, message);
        }
    }
}
