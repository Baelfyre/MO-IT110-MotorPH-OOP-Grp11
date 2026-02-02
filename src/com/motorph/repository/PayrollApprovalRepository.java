/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.PayPeriod;
import java.time.LocalDateTime;

/*
 *
 * @author ACER
 */
public interface PayrollApprovalRepository {

    ApprovalStatus getDtrStatus(int empId, PayPeriod period);

    ApprovalStatus getPayrollStatus(int empId, PayPeriod period);

    boolean upsertDtrApproval(int empId, PayPeriod period, int approvedBy, ApprovalStatus status, LocalDateTime approvedAt);

    boolean upsertPayrollApproval(int empId, PayPeriod period, int approvedBy, ApprovalStatus status, LocalDateTime approvedAt);

    boolean ensureRowExists(int empId, PayPeriod period);
}
