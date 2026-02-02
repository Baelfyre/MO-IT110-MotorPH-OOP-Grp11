/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Computes leave usage in hours for payroll. Dedupe and workday filtering are
 * applied at the service layer.
 *
 * @author ACER
 */
public class LeaveService {

    private final LeaveRepository leaveRepo;

    public LeaveService(LeaveRepository leaveRepo) {
        this.leaveRepo = leaveRepo;
    }

    public double getLeaveHoursUsed(int empId, PayPeriod period) {
        if (period == null) {
            return 0.0;
        }

        List<LeaveRequest> rows = leaveRepo.findByEmployeeAndPeriod(empId, period);

        double totalHours = 0.0;
        Set<String> seenLeaveIds = new HashSet<>();

        for (LeaveRequest r : rows) {
            if (r == null || r.getDate() == null) {
                continue;
            }

            String leaveId = (r.getLeaveId() == null) ? "" : r.getLeaveId().trim();
            if (leaveId.isEmpty()) {
                continue;
            }

            boolean firstOccurrence = seenLeaveIds.add(leaveId);
            if (!firstOccurrence) {
                continue;
            }

            DayOfWeek dow = r.getDate().getDayOfWeek();
            boolean isWorkday = (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY);
            if (!isWorkday) {
                continue;
            }

            totalHours += calculateHours(r.getStartTime(), r.getEndTime());
        }

        return totalHours;
    }

    private double calculateHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return 0.0;
        }
        if (end.isBefore(start)) {
            return 0.0;
        }

        long minutes = Duration.between(start, end).toMinutes();

        // Leave usage is measured as work-hours; long spans exclude a lunch break.
        if (minutes > 240) {
            minutes -= 60;
        }

        double hours = Math.max(0.0, minutes / 60.0);

        // Daily leave consumption is constrained to a standard workday.
        return Math.min(hours, 8.0);
    }

}
