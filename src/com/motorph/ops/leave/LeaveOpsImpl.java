/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.leave;

import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveRepository;
import com.motorph.service.LeaveCreditsService;
import com.motorph.service.LeaveService;
import com.motorph.service.LogService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Coordinates leave use cases by combining persistence, computed usage, and
 * system logs.
 */
public class LeaveOpsImpl implements LeaveOps {

    private final LeaveRepository leaveRepo;
    private final LeaveCreditsService creditsService;
    private final LogService logService;
    private final LeaveService leaveService;
    private String lastRequestMessage = "";

    private static final DateTimeFormatter HM_FMT = DateTimeFormatter.ofPattern("HHmm", Locale.US);

    public LeaveOpsImpl(LeaveRepository leaveRepo, LeaveCreditsService creditsService, LogService logService) {
        this.leaveRepo = leaveRepo;
        this.creditsService = creditsService;
        this.logService = logService;
        this.leaveService = new LeaveService(leaveRepo);
    }

    @Override
    public boolean requestLeave(int empId, String firstName, String lastName, LocalDate date, LocalTime start, LocalTime end) {
        return requestLeave(empId, firstName, lastName, date, start, end, false);
    }

    @Override
    public boolean requestLeave(int empId, String firstName, String lastName,
            LocalDate date, LocalTime start, LocalTime end,
            boolean allowUnpaidFallback) {

        if (date == null || start == null || end == null) {
            lastRequestMessage = "Missing date or time range.";
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_FAILED", lastRequestMessage);
            return false;
        }

        PayPeriod period = PayPeriod.fromDateSemiMonthly(date);
        double remainingCredits = creditsService.getRemainingCreditsYearToDate(empId, period);

        String validationMessage = leaveService.validateLeaveRequest(
                date, start, end, remainingCredits, allowUnpaidFallback
        );
        if (validationMessage != null) {
            lastRequestMessage = validationMessage;
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_FAILED", validationMessage);
            return false;
        }

        double storedCredits = creditsService.getStoredLeaveCreditsHours(empId);
        if (storedCredits <= 0.0 && !allowUnpaidFallback) {
            lastRequestMessage = "Paid leave credits are 0. Confirm unpaid leave before submission.";
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_NEEDS_UNPAID_CONFIRMATION", lastRequestMessage);
            return false;
        }

        List<LeaveRequest> existingLeaves = leaveRepo.findByEmployee(empId);
        for (LeaveRequest r : existingLeaves) {
            if (r.getDate() != null && r.getDate().equals(date)) {
                if (r.getStatus() == LeaveStatus.PENDING || r.getStatus() == LeaveStatus.APPROVED) {
                    lastRequestMessage = "An active leave request already exists for this date.";
                    logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_DENIED", lastRequestMessage);
                    return false;
                }
            }
        }

        String leaveId = buildLeaveId(empId, date, start);
        LeaveRequest r = new LeaveRequest(leaveId, empId, date, start, end, firstName, lastName);
        boolean ok = leaveRepo.create(r);

        String mode = (storedCredits <= 0.0 && allowUnpaidFallback) ? "unpaid-fallback" : "paid-path";
        lastRequestMessage = ok
                ? ((storedCredits <= 0.0 && allowUnpaidFallback)
                        ? "Leave request recorded through unpaid leave confirmation path."
                        : "Leave request recorded.")
                : "Leave request not recorded.";

        logService.recordAction(
                String.valueOf(empId),
                ok ? "LEAVE_REQUEST_RECORDED" : "LEAVE_REQUEST_FAILED",
                ok ? ("Leave row appended: " + leaveId + " Mode=" + mode) : "Leave row append failed."
        );

        return ok;
    }

    @Override
    public List<LeaveRequest> listLeaveRequests(int empId, PayPeriod period) {
        if (period == null) {
            return leaveRepo.findByEmployee(empId);
        }
        return leaveRepo.findByEmployeeAndPeriod(empId, period);
    }

    @Override
    public double getLeaveUsedThisPeriod(int empId, PayPeriod period) {
        return leaveRepo.getLeaveHoursUsed(empId, period);
    }

    @Override
    public double getLeaveRemainingYtd(int empId, PayPeriod period) {
        return creditsService.getRemainingCreditsYearToDate(empId, period);
    }

    @Override
    public double getStoredLeaveCreditsHours(int empId) {
        return creditsService.getStoredLeaveCreditsHours(empId);
    }

    @Override
    public double calculateLeaveHours(LocalTime start, LocalTime end) {
        return leaveService.calculateHours(start, end);
    }
    
    @Override
    public boolean syncLeaveTakenYtd(int empId, PayPeriod period) {
        return creditsService.syncLeaveTakenYearToDate(empId, period);
    }

    @Override
    public String getLastRequestMessage() {
        return lastRequestMessage == null ? "" : lastRequestMessage;
    }

    private String buildLeaveId(int empId, LocalDate date, LocalTime start) {
        long excelSerial = toExcelSerial(date);
        String timeKey = start.format(HM_FMT);
        // Added a short UUID so a rejected ID doesn't block a new submission ID
        String unique = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return empId + "-" + excelSerial + "-" + timeKey + "-" + unique;
    }

    private long toExcelSerial(LocalDate date) {
        return ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), date);
    }
}
