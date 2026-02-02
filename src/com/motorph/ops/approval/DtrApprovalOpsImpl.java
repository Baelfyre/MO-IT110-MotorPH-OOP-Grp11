/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.approval;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.AuditRepository;
import com.motorph.repository.PayrollApprovalRepository;

import java.time.LocalDateTime;

/**
 * Writes DTR approvals into the shared tracker: records_payroll_{empId}.csv
 *
 * @author ACER
 */
public class DtrApprovalOpsImpl implements DtrApprovalOps {

    private final PayrollApprovalRepository approvalRepo;
    private final AuditRepository auditRepo;

    public DtrApprovalOpsImpl(PayrollApprovalRepository approvalRepo, AuditRepository auditRepo) {
        this.approvalRepo = approvalRepo;
        this.auditRepo = auditRepo;
    }

    @Override
    public boolean approveDtr(int empId, PayPeriod period, int approverUserId) {
        if (period == null) {
            return false;
        }

        approvalRepo.ensureRowExists(empId, period);

        ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
        if (payrollStatus == ApprovalStatus.APPROVED) {
            auditRepo.logDtrChange(
                    String.valueOf(approverUserId),
                    "Denied DTR approval (Payroll locked). EmpID=" + empId + " Period=" + period.toKey()
            );
            return false;
        }

        boolean ok = approvalRepo.upsertDtrApproval(
                empId, period, approverUserId, ApprovalStatus.APPROVED, LocalDateTime.now()
        );

        if (ok) {
            auditRepo.logDtrChange(
                    String.valueOf(approverUserId),
                    "Approved DTR for EmpID=" + empId + " Period=" + period.toKey()
            );
        }
        return ok;
    }

    @Override
    public boolean rejectDtr(int empId, PayPeriod period, int approverUserId) {
        if (period == null) {
            return false;
        }

        approvalRepo.ensureRowExists(empId, period);

        ApprovalStatus payrollStatus = approvalRepo.getPayrollStatus(empId, period);
        if (payrollStatus == ApprovalStatus.APPROVED) {
            auditRepo.logDtrChange(
                    String.valueOf(approverUserId),
                    "Denied DTR rejection (Payroll locked). EmpID=" + empId + " Period=" + period.toKey()
            );
            return false;
        }

        boolean ok = approvalRepo.upsertDtrApproval(
                empId, period, approverUserId, ApprovalStatus.REJECTED, LocalDateTime.now()
        );

        if (ok) {
            auditRepo.logDtrChange(
                    String.valueOf(approverUserId),
                    "Rejected DTR for EmpID=" + empId + " Period=" + period.toKey()
            );
        }
        return ok;
    }
}
