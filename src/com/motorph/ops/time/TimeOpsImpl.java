/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.time;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.TimeEntryRepository;
import com.motorph.service.LogService;
import com.motorph.service.TimeService;

import java.util.List;

/**
 * Coordinates attendance use cases and system logs.
 *
 * @author ACER
 */
public class TimeOpsImpl implements TimeOps {

    private final TimeService timeService;
    private final TimeEntryRepository timeRepo;
    private final LogService logService;

    public TimeOpsImpl(TimeService timeService, TimeEntryRepository timeRepo, LogService logService) {
        this.timeService = timeService;
        this.timeRepo = timeRepo;
        this.logService = logService;
    }

    @Override
    public boolean clockIn(int empId) {
        boolean ok = timeService.logTimeIn(empId);

        logService.recordAction(
                String.valueOf(empId),
                ok ? "TIME_IN_OK" : "TIME_IN_DENIED",
                ok ? "Time-in recorded." : "Time-in rejected by rule."
        );

        return ok;
    }

    @Override
    public boolean clockOut(int empId) {
        boolean ok = timeService.logTimeOut(empId);

        logService.recordAction(
                String.valueOf(empId),
                ok ? "TIME_OUT_OK" : "TIME_OUT_DENIED",
                ok ? "Time-out recorded." : "Time-out rejected by rule."
        );

        return ok;
    }

    @Override
    public List<TimeEntry> viewMyTimeEntries(int empId) {
        return timeRepo.getEntries(empId);
    }

    @Override
    public List<TimeEntry> viewMyTimeEntriesForPeriod(int empId, PayPeriod period) {
        return timeRepo.findByEmployeeAndPeriod(empId, period);
    }
}
