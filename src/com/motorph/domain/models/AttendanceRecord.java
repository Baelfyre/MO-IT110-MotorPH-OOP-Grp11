/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.ApprovalStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author OngoJ.
 */
public class AttendanceRecord {

    private int employeeId;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;

    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    public AttendanceRecord() {
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("employeeId must be > 0.");
        }
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date cannot be a future date.");
        }
        this.date = date;
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
        if (timeIn != null && timeOut != null && timeOut.isBefore(timeIn)) {
            throw new IllegalArgumentException("Time Out must be after Time In.");
        }
        this.timeOut = timeOut;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = (approvalStatus == null) ? ApprovalStatus.PENDING : approvalStatus;
    }

    public double calculateHoursWorked() {
        if (timeIn == null || timeOut == null) {
            return 0.0;
        }
        if (timeOut.isBefore(timeIn)) {
            return 0.0;
        }
        long minutes = Duration.between(timeIn, timeOut).toMinutes();
        return Math.max(0.0, minutes / 60.0);
    }
}
