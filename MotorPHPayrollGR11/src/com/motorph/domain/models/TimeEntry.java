/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single daily attendance record. Stores the Date, Time In, and
 * Time Out. Used to calculate total hours worked for a specific day.
 *
 * @author ACER
 */
public class TimeEntry {

    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    /**
     * Constructor for a completed entry.
     *
     * @param date The date of the entry.
     * @param timeIn The time the employee clocked in.
     * @param timeOut The time the employee clocked out (can be null if
     * currently working).
     */
    public TimeEntry(LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(LocalTime timeIn) {
        this.timeIn = timeIn;
    }

    public LocalTime getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(LocalTime timeOut) {
        this.timeOut = timeOut;
    }
}
