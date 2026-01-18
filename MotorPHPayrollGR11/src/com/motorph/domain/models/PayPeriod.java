/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a payroll cycle (Start Date to End Date). Used to group
 * TimeEntries and define the coverage of a Payslip.
 *
 * @author ACER
 */
public class PayPeriod {

    private LocalDate startDate;
    private LocalDate endDate;

    public PayPeriod(LocalDate startDate, LocalDate endDate) {
        // Validation: End date should not be before Start date
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before Start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Checks if a specific date falls within this pay period.
     *
     * @param date The date to check.
     * @return true if the date is between startDate and endDate (inclusive).
     */
    public boolean includes(LocalDate date) {
        return (date.isEqual(startDate) || date.isAfter(startDate))
                && (date.isEqual(endDate) || date.isBefore(endDate));
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return startDate.format(fmt) + " - " + endDate.format(fmt);
    }
}
