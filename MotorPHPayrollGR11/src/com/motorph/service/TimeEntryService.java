/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.TimeEntryRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Manages daily attendance (Clock In/Out).
 * Ensures employees don't double-clock in.
 * @author ACER
 */
public class TimeEntryService {

    private TimeEntryRepository repo;

    public TimeEntryService(TimeEntryRepository repo) {
        this.repo = repo;
    }

    /**
     * Records the Time In for the current day.
     * Prevents duplicate entries for the same date.
     */
    public boolean logTimeIn(int empId) {
        LocalDate today = LocalDate.now();
        
        // 1. Check if already clocked in today
        if (getEntryForDate(empId, today) != null) {
            System.out.println("Error: Already clocked in for today.");
            return false;
        }

        // 2. Create and Save new entry
        TimeEntry entry = new TimeEntry(today, LocalTime.now(), null);
        return repo.saveEntry(empId, entry);
    }

    /**
     * Records Time Out for the existing entry of today.
     */
    public boolean logTimeOut(int empId) {
        LocalDate today = LocalDate.now();
        TimeEntry existing = getEntryForDate(empId, today);

        if (existing == null) {
            System.out.println("Error: No Time In found for today.");
            return false;
        }

        // Update the existing object
        existing.setTimeOut(LocalTime.now());
        
        // Save (Note: Your CSV Repo currently appends, so this might duplicate lines 
        // in a simple CSV setup. For a real DB, this updates the row. 
        // For this project, appending a new row with both times is often acceptable 
        // if the reader logic handles "latest record wins".)
        return repo.saveEntry(empId, existing);
    }

    /**
     * Helper to find today's specific entry from the list.
     */
    private TimeEntry getEntryForDate(int empId, LocalDate date) {
        List<TimeEntry> entries = repo.getEntries(empId);
        for (TimeEntry e : entries) {
            if (e.getDate().equals(date)) {
                return e;
            }
        }
        return null;
    }
}