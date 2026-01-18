/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.HolidayRepository;
import java.time.Duration;
import java.time.LocalTime;

/**
 * Service for calculating work hours and attendance rules. Implements the
 * 10-minute grace period rule (8:00 - 8:10 AM).
 *
 * @author ACER
 */
public class TimeCardService {

    private HolidayRepository holidayRepo;

    // Standard Business Hours
    private static final LocalTime WORK_START = LocalTime.of(8, 0); // 8:00 AM
    private static final LocalTime GRACE_END = LocalTime.of(8, 10); // 8:10 AM

    public TimeCardService(HolidayRepository holidayRepo) {
        this.holidayRepo = holidayRepo;
    }

    /**
     * Calculates the total payable hours for a single day. Applies Grace Period
     * and Lunch Break rules.
     *
     * @param entry The daily time record.
     * @return Total hours worked (e.g., 8.0 or 7.5).
     */
    public double calculateDailyHours(TimeEntry entry) {
        if (entry.getTimeIn() == null || entry.getTimeOut() == null) {
            return 0.0;
        }

        LocalTime in = entry.getTimeIn();
        LocalTime out = entry.getTimeOut();

        // --- GRACE PERIOD LOGIC ---
        // If logged in between 8:00 and 8:10, count it as 8:00 (No Deduction)
        if (in.isAfter(WORK_START.minusMinutes(1)) && in.isBefore(GRACE_END.plusSeconds(1))) {
            in = WORK_START;
        }
        // If logged in before 8:00, usually we count from 8:00 unless OT is approved.
        // For this milestone, we'll snap early logins to 8:00 to avoid unintended OT.
        if (in.isBefore(WORK_START)) {
            in = WORK_START;
        }

        // --- DURATION CALCULATION ---
        long minutes = Duration.between(in, out).toMinutes();

        // Deduct 1 Hour Lunch Break if worked more than 4 hours
        // (Standard DOLE/Labor Code practice)
        if (minutes > 240) { // > 4 hours
            minutes -= 60;
        }

        // Convert to hours (e.g., 480 mins / 60 = 8.0 hours)
        // Ensure non-negative
        return Math.max(0, minutes / 60.0);
    }
}
