/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.ops.supervisor;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.enums.LeaveStatus;
import java.util.List;

/**
 *
 * @author ACER
 */
public interface SupervisorOps {

    boolean isDirectReport(int supervisorEmpId, int reportEmpId);

    List<Employee> listDirectReports(int supervisorEmpId);

    List<SupervisorDtrSummary> listDirectReportStatuses(int supervisorEmpId, PayPeriod period);

    List<TimeEntry> viewDirectReportTimeEntries(int supervisorEmpId, int reportEmpId, PayPeriod period);

    boolean approveDirectReportDtr(int supervisorEmpId, int reportEmpId, PayPeriod period);

    boolean rejectDirectReportDtr(int supervisorEmpId, int reportEmpId, PayPeriod period);

    List<LeaveRequest> listDirectReportLeaveRequests(int supervisorEmpId, PayPeriod period);

    boolean decideDirectReportLeave(int supervisorEmpId, int reportEmpId, String leaveId, LeaveStatus status, String note);
}
