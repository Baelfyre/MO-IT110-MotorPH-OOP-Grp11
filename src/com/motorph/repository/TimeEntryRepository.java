/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import java.util.List;

/**
 * Interface for accessing daily attendance records.
 *
 * @author ACER
 */
public interface TimeEntryRepository {

    // Used for Manual Entry (Optional but good to have)
    boolean saveEntry(int empId, TimeEntry entry);

    // Used for listing all history
    List<TimeEntry> getEntries(int empId);

    // REQUIRED: Used by PayrollService to filter specifically for the pay period
    List<TimeEntry> findByEmployeeAndPeriod(int empId, PayPeriod period);
}
