/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.leave;

import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveRepository;
import com.motorph.service.LeaveCreditsService;
import com.motorph.service.LogService;

import java.time.DayOfWeek;
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

    private static final DateTimeFormatter HM_FMT
            = DateTimeFormatter.ofPattern("HHmm", Locale.US);

    public LeaveOpsImpl(LeaveRepository leaveRepo,
            LeaveCreditsService creditsService,
            LogService logService) {
        this.leaveRepo = leaveRepo;
        this.creditsService = creditsService;
        this.logService = logService;
    }

    @Override
    public boolean requestLeave(int empId, String firstName, String lastName,
            LocalDate date, LocalTime start, LocalTime end) {

        if (date == null || start == null || end == null) {
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_FAILED",
                    "Missing date or time range.");
            return false;
        }

        if (end.isBefore(start)) {
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_FAILED",
                    "End time is earlier than start time.");
            return false;
        }

        if (isWeekend(date)) {
            logService.recordAction(String.valueOf(empId), "LEAVE_REQUEST_IGNORED",
                    "Weekend leave request recorded as invalid workday.");
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

        logService.recordAction(String.valueOf(empId),
                ok ? "LEAVE_REQUEST_RECORDED" : "LEAVE_REQUEST_FAILED",
                ok ? ("Leave row appended: " + leaveId) : "Leave row append failed.");

        return ok;
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
    public boolean syncLeaveTakenYtd(int empId, PayPeriod period) {
        return creditsService.syncLeaveTakenYearToDate(empId, period);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
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
