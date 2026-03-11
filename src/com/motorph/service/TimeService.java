package com.motorph.service;

import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.TimeEntryRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for daily attendance.
 *
 * @author ACER
 */
public class TimeService {

    // Minimum minutes considered a reasonable shift before raising a warning.
    private static final long MIN_WORK_MINUTES_FOR_WARNING = 60;

    private final TimeEntryRepository repo;

    public TimeService(TimeEntryRepository repo) {
        this.repo = repo;
    }

    public boolean logTimeIn(int empId) {
        LocalDate today = LocalDate.now();

        if (!isWorkday(today)) {
            return false;
        }

        TimeEntry existing = getEntryForDate(empId, today);

        // A time-in already exists for today.
        if (existing != null && existing.getTimeIn() != null) {
            return false;
        }

        TimeEntry entry = new TimeEntry(today, LocalTime.now(), null);
        return repo.saveEntry(empId, entry);
    }

    public boolean logTimeOut(int empId) {
        LocalDate today = LocalDate.now();

        if (!isWorkday(today)) {
            return false;
        }

        TimeEntry existing = getEntryForDate(empId, today);

        // No time-in recorded yet.
        if (existing == null || existing.getTimeIn() == null) {
            return false;
        }

        // A time-out was already recorded.
        if (existing.getTimeOut() != null) {
            return false;
        }

        existing.setTimeOut(LocalTime.now());
        return repo.saveEntry(empId, existing);
    }

    public List<TimeEntry> getEntries(int empId) {
        return repo.getEntries(empId);
    }

    /**
     * Returns true if today's recorded work duration for the employee is
     * considered short (for example, zero to less than or equal to one hour),
     * based on the latest time-in and time-out stored in the repository.
     */
    public boolean isWorkedHoursShort(int empId) {
        LocalDate today = LocalDate.now();
        List<TimeEntry> entries = repo.getEntries(empId);

        LocalTime timeIn = null;
        LocalTime timeOut = null;

        for (TimeEntry entry : entries) {
            if (entry.getDate() != null && entry.getDate().equals(today)) {
                timeIn = entry.getTimeIn();
                timeOut = entry.getTimeOut();
                break;
            }
        }

        if (timeIn == null || timeOut == null) {
            return false;
        }

        long minutes = Duration.between(timeIn, timeOut).toMinutes();
        // Treat zero or any positive duration up to the threshold as "short".
        return minutes <= MIN_WORK_MINUTES_FOR_WARNING;
    }

    private TimeEntry getEntryForDate(int empId, LocalDate date) {
        List<TimeEntry> entries = repo.getEntries(empId);
        for (TimeEntry e : entries) {
            if (e.getDate() != null && e.getDate().equals(date)) {
                return e;
            }
        }
        return null;
    }

    // Annotation: Computes worked hours using the shared attendance rule.
    public double calculateWorkedHours(TimeEntry entry) {
        if (entry == null || entry.getTimeIn() == null || entry.getTimeOut() == null) {
            return 0.0;
        }

        long minutes = Duration.between(entry.getTimeIn(), entry.getTimeOut()).toMinutes();
        if (minutes < 0) {
            return 0.0;
        }
        if (minutes > 240) {
            minutes -= 60;
        }
        return Math.max(0.0, minutes / 60.0);
    }

    // Annotation: Flags unusually short worked durations for review.
    public boolean isWorkedDurationTooShort(TimeEntry entry) {
        return calculateWorkedHours(entry) > 0.0 && calculateWorkedHours(entry) < MIN_VALID_WORK_HOURS;
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
