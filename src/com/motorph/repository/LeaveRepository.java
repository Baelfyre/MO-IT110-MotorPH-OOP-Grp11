/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import java.util.List;

/**
 * Leave data access contract for reading and appending leave rows. Hour
 * computation uses the project policy rules in this repository for now.
 *
 * @author ACER
 */
public interface LeaveRepository {

    List<LeaveRequest> findByEmployee(int empId);

    List<LeaveRequest> findByEmployeeAndPeriod(int empId, PayPeriod period);

    double getLeaveHoursUsed(int empId, PayPeriod period);

    boolean create(LeaveRequest request);
}
