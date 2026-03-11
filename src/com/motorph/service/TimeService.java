package com.motorph.service;

import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.TimeEntryRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business logic for daily attendance.
 *
 * @author ACER
 */
public class TimeService {

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

    private TimeEntry getEntryForDate(int empId, LocalDate date) {
        List<TimeEntry> entries = repo.getEntries(empId);
        for (TimeEntry e : entries) {
            if (e.getDate() != null && e.getDate().equals(date)) {
                return e;
            }
        }
        return null;
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}
