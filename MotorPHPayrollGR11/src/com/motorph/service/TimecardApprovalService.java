/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.Timecard;
import com.motorph.domain.models.UserAccount;
import com.motorph.domain.enums.Role;
import com.motorph.repository.AuditRepository;
import java.time.LocalDateTime;

/**
 * Service for handling the approval workflow of Timecards. Used by Managers to
 * Approve/Reject employee DTRs.
 *
 * @author ACER
 */
public class TimecardApprovalService {

    private AuditRepository auditRepo;

    public TimecardApprovalService(AuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    /**
     * Approves a specific timecard.
     *
     * @param timecard The timecard to approve.
     * @param approver The UserAccount of the manager performing the action.
     * @return true if successful, false if unauthorized.
     */
    public boolean approveTimecard(Timecard timecard, UserAccount approver) {
        // 1. Validation: Only Managers or Payroll can approve
        if (approver.getRole() != Role.MANAGER && approver.getRole() != Role.PAYROLL) {
            System.out.println("Access Denied: Only Managers can approve timecards.");
            return false;
        }

        // 2. Change Status
        timecard.setStatus(ApprovalStatus.APPROVED);

        // 3. Log the Action (Auditing)
        String details = "Approved Timecard for Emp ID: " + timecard.getEmployeeId();
        auditRepo.logDtrChange(approver.getUsername(), details);

        return true;
    }

    /**
     * Rejects a timecard.
     *
     * @param timecard The timecard to reject.
     * @param approver The manager rejecting it.
     */
    public void rejectTimecard(Timecard timecard, UserAccount approver) {
        timecard.setStatus(ApprovalStatus.REJECTED);

        String details = "Rejected Timecard for Emp ID: " + timecard.getEmployeeId();
        auditRepo.logDtrChange(approver.getUsername(), details);
    }
}
