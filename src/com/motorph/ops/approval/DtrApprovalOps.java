/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.approval;

import com.motorph.domain.models.PayPeriod;

/**
 *
 * @author ACER
 */
public interface DtrApprovalOps {

    boolean approveDtr(int empId, PayPeriod period, int approverUserId);

    boolean rejectDtr(int empId, PayPeriod period, int approverUserId);
}
