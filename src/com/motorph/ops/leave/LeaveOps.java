/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.motorph.ops.leave;

import com.motorph.domain.models.PayPeriod;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Use-case boundary for leave operations available to all roles.
 *
 * @author ACER
 */
public interface LeaveOps {

    boolean requestLeave(int empId, String firstName, String lastName,
            LocalDate date, LocalTime start, LocalTime end);

    double getLeaveUsedThisPeriod(int empId, PayPeriod period);

    double getLeaveRemainingYtd(int empId, PayPeriod period);

    boolean syncLeaveTakenYtd(int empId, PayPeriod period);
}
