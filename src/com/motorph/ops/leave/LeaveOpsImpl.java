/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.leave;

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
import java.util.Locale;

/**
 * Coordinates leave use cases by combining persistence, computed usage, and
 * system logs.
 *
 * @author ACER
 */
public class LeaveOpsImpl implements LeaveOps {

    private final LeaveRepository leaveRepo;
    private final LeaveCreditsService creditsService;
    private final LogService logService;
    private final LeaveService leaveService;
    private String lastRequestMessage = "";

    private static final DateTimeFormatter HM_FMT
            = DateTimeFormatter.ofPattern("HHmm", Locale.US);

    public LeaveOpsImpl(LeaveRepository leaveRepo,
            LeaveCreditsService creditsService,
            LogService logService) {
        this.leaveRepo = leaveRepo;
        this.creditsService = creditsService;
        this.logService = logService;
        this.leaveService = new LeaveService(leaveRepo);
    }

    @Override
    public boolean requestLeave(int empId, String firstName, String lastName,
            LocalDate date, LocalTime start, LocalTime end) {
        return requestLeave(empId, firstName, lastName, date, start, end, false);
    }

    @Override
    public boolean requestLeave(int empId, String firstName, String lastName,
            LocalDate date, LocalTime start, LocalTime end, boolean allowUnpaidFallback) {

        String validationMessage = leaveService.validateLeaveRequest(date, start, end);
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

        String leaveId = buildLeaveId(empId, date, start);
        LeaveRequest r = new LeaveRequest(
                leaveId,
                empId,
                date,
                start,
                end,
                firstName,
                lastName
        );

        boolean ok = leaveRepo.create(r);

        String mode = (storedCredits <= 0.0 && allowUnpaidFallback) ? "unpaid-fallback" : "paid-path";
        lastRequestMessage = ok
                ? (storedCredits <= 0.0 && allowUnpaidFallback
                        ? "Leave request recorded through unpaid leave confirmation path."
                        : "Leave request recorded.")
                : "Leave request not recorded.";

        logService.recordAction(String.valueOf(empId),
                ok ? "LEAVE_REQUEST_RECORDED" : "LEAVE_REQUEST_FAILED",
                (ok ? ("Leave row appended: " + leaveId + " Mode=" + mode) : "Leave row append failed."));

        return ok;
    }

    @Override
    public java.util.List<LeaveRequest> listLeaveRequests(int empId, PayPeriod period) {
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
        return empId + "-" + excelSerial + "-" + timeKey;
    }

    /**
     * Base date 1899-12-30 matches Excel 1900 date system behavior for modern
     * dates.
     */
    private long toExcelSerial(LocalDate date) {
        return ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), date);
    }
}
