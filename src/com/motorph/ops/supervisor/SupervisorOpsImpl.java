/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.supervisor;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.ops.approval.DtrApprovalOps;
import com.motorph.repository.LeaveRepository;
import com.motorph.repository.PayrollApprovalRepository;
import com.motorph.repository.TimeEntryRepository;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author ACER
 */
public class SupervisorOpsImpl implements SupervisorOps {

    private final EmployeeService employeeService;
    private final TimeEntryRepository timeRepo;
    private final PayrollApprovalRepository approvalRepo;
    private final LeaveRepository leaveRepo;
    private final DtrApprovalOps dtrApprovalOps;
    private final LogService logService;

    public SupervisorOpsImpl(EmployeeService employeeService,
            TimeEntryRepository timeRepo,
            PayrollApprovalRepository approvalRepo,
            LeaveRepository leaveRepo,
            DtrApprovalOps dtrApprovalOps,
            LogService logService) {
        this.employeeService = employeeService;
        this.timeRepo = timeRepo;
        this.approvalRepo = approvalRepo;
        this.leaveRepo = leaveRepo;
        this.dtrApprovalOps = dtrApprovalOps;
        this.logService = logService;
    }

    @Override
    public boolean isDirectReport(int supervisorEmpId, int reportEmpId) {
        Employee supervisor = employeeService.getEmployee(supervisorEmpId);
        Employee report = employeeService.getEmployee(reportEmpId);

        if (supervisor == null || report == null) {
            return false;
        }

        String supervisorDisplay = buildDisplayName(supervisor);
        String reportSupervisor = normalize(report.getImmediateSupervisor());

        return !supervisorDisplay.isEmpty()
                && supervisorDisplay.equalsIgnoreCase(reportSupervisor);
    }

    @Override
    public List<Employee> listDirectReports(int supervisorEmpId) {
        Employee supervisor = employeeService.getEmployee(supervisorEmpId);
        if (supervisor == null) {
            return Collections.emptyList();
        }

        String supervisorDisplay = buildDisplayName(supervisor);
        if (supervisorDisplay.isEmpty()) {
            return Collections.emptyList();
        }

        return employeeService.getSubordinates(supervisorDisplay);
    }

    @Override
    public List<SupervisorDtrSummary> listDirectReportStatuses(int supervisorEmpId, PayPeriod period) {
        if (period == null) {
            return Collections.emptyList();
        }

        List<Employee> reports = listDirectReports(supervisorEmpId);
        List<SupervisorDtrSummary> out = new ArrayList<>();

        for (Employee e : reports) {
            int empId = e.getEmployeeNumber();

            ApprovalStatus dtr = approvalRepo.getDtrStatus(empId, period);
            ApprovalStatus payroll = approvalRepo.getPayrollStatus(empId, period);

            out.add(new SupervisorDtrSummary(
                    empId,
                    e.getLastName() + ", " + e.getFirstName(),
                    dtr,
                    payroll
            ));
        }

        return out;
    }

    @Override
    public List<TimeEntry> viewDirectReportTimeEntries(int supervisorEmpId, int reportEmpId, PayPeriod period) {
        if (period == null) {
            return Collections.emptyList();
        }

        if (!isDirectReport(supervisorEmpId, reportEmpId)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DENIED_VIEW_TIME",
                    "Denied view time entries. Supervisor=" + supervisorEmpId
                    + " Report=" + reportEmpId + " Period=" + period.toKey()
            );
            return Collections.emptyList();
        }

        return timeRepo.findByEmployeeAndPeriod(reportEmpId, period);
    }


    @Override
    public boolean updateDirectReportTimeEntry(int supervisorEmpId, int reportEmpId, java.time.LocalDate date, java.time.LocalTime timeIn, java.time.LocalTime timeOut) {
        if (date == null || timeIn == null || timeOut == null) {
            return false;
        }
        if (!timeOut.isAfter(timeIn)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DTR_EDIT_FAILED",
                    "Denied DTR edit. Invalid time order for Report=" + reportEmpId + " Date=" + date
            );
            return false;
        }
        if (!isDirectReport(supervisorEmpId, reportEmpId)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DTR_EDIT_DENIED",
                    "Denied DTR edit. Supervisor=" + supervisorEmpId + " Report=" + reportEmpId + " Date=" + date
            );
            return false;
        }

        boolean ok = timeRepo.saveEntry(reportEmpId, new TimeEntry(date, timeIn, timeOut));
        logService.recordAction(
                String.valueOf(supervisorEmpId),
                ok ? "SUPERVISOR_DTR_EDIT_OK" : "SUPERVISOR_DTR_EDIT_FAILED",
                "Manual DTR edit. Report=" + reportEmpId + " Date=" + date + " TimeIn=" + timeIn + " TimeOut=" + timeOut
        );
        return ok;
    }

    @Override
    public boolean approveDirectReportDtr(int supervisorEmpId, int reportEmpId, PayPeriod period) {
        if (period == null) {
            return false;
        }

        if (!isDirectReport(supervisorEmpId, reportEmpId)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DENIED_DTR_APPROVE",
                    "Denied DTR approve. Supervisor=" + supervisorEmpId
                    + " Report=" + reportEmpId + " Period=" + period.toKey()
            );
            return false;
        }

        boolean ok = dtrApprovalOps.approveDtr(reportEmpId, period, supervisorEmpId);

        logService.recordAction(
                String.valueOf(supervisorEmpId),
                ok ? "SUPERVISOR_DTR_APPROVED" : "SUPERVISOR_DTR_APPROVE_FAILED",
                "DTR approve attempted. Report=" + reportEmpId + " Period=" + period.toKey()
        );

        return ok;
    }

    @Override
    public boolean rejectDirectReportDtr(int supervisorEmpId, int reportEmpId, PayPeriod period) {
        if (period == null) {
            return false;
        }

        if (!isDirectReport(supervisorEmpId, reportEmpId)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DENIED_DTR_REJECT",
                    "Denied DTR reject. Supervisor=" + supervisorEmpId
                    + " Report=" + reportEmpId + " Period=" + period.toKey()
            );
            return false;
        }

        boolean ok = dtrApprovalOps.rejectDtr(reportEmpId, period, supervisorEmpId);

        logService.recordAction(
                String.valueOf(supervisorEmpId),
                ok ? "SUPERVISOR_DTR_REJECTED" : "SUPERVISOR_DTR_REJECT_FAILED",
                "DTR reject attempted. Report=" + reportEmpId + " Period=" + period.toKey()
        );

        return ok;
    }

    @Override
    public List<LeaveRequest> listDirectReportLeaveRequests(int supervisorEmpId, PayPeriod period) {
        List<Employee> reports = listDirectReports(supervisorEmpId);
        List<LeaveRequest> out = new ArrayList<>();

        for (Employee e : reports) {
            List<LeaveRequest> rows = (period == null)
                    ? leaveRepo.findByEmployee(e.getEmployeeNumber())
                    : leaveRepo.findByEmployeeAndPeriod(e.getEmployeeNumber(), period);

            for (LeaveRequest r : rows) {
                if (r != null && r.getStatus() == LeaveStatus.PENDING) {
                    out.add(r);
                }
            }
        }

        return out;
    }

    @Override
    public boolean decideDirectReportLeave(int supervisorEmpId, int reportEmpId, String leaveId, LeaveStatus status, String note) {
        if (leaveId == null || leaveId.trim().isEmpty() || status == null) {
            return false;
        }

        if (!isDirectReport(supervisorEmpId, reportEmpId)) {
            logService.recordAction(
                    String.valueOf(supervisorEmpId),
                    "SUPERVISOR_DENIED_LEAVE_DECISION",
                    "Denied leave decision. Supervisor=" + supervisorEmpId
                    + " Report=" + reportEmpId + " LeaveId=" + leaveId
            );
            return false;
        }

        String reviewedAt = LocalDateTime.now().toString();
        boolean ok = leaveRepo.updateDecision(reportEmpId, leaveId, status, supervisorEmpId, reviewedAt, note);

        logService.recordAction(
                String.valueOf(supervisorEmpId),
                ok ? "SUPERVISOR_LEAVE_" + status.name() : "SUPERVISOR_LEAVE_DECISION_FAILED",
                "Leave decision attempted. Report=" + reportEmpId + " LeaveId=" + leaveId + " Status=" + status.name()
        );

        return ok;
    }

    private String buildDisplayName(Employee e) {
        return normalize(e.getLastName() + ", " + e.getFirstName());
    }

    private String normalize(String s) {
        return s == null ? "" : s.replace("\"", "").trim();
    }
}
