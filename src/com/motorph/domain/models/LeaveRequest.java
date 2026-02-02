/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Leave usage row for records_leave_{empId}.csv.
 *
 * Legacy CSV columns (7): Leave_ID, Employee #, Date, Start_Time, End_Time,
 * First Name, Last Name
 *
 * Extended CSV columns (Option B, +4): Status, Reviewed_By, Reviewed_At,
 * Decision_Note
 *
 * @author ACER
 */
public class LeaveRequest {

    private final String leaveId;
    private final int employeeId;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String firstName;
    private final String lastName;

    // Option B fields
    private final LeaveStatus status;
    private final Integer reviewedBy;   // supervisor EmpID, nullable
    private final String reviewedAt;    // text timestamp, blank allowed
    private final String decisionNote;  // blank allowed

    private static final DateTimeFormatter DATE_FMT
            = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);

    private static final DateTimeFormatter TIME_FMT
            = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    // Legacy constructor (kept)
    public LeaveRequest(String leaveId, int employeeId, LocalDate date,
            LocalTime startTime, LocalTime endTime,
            String firstName, String lastName) {
        this(leaveId, employeeId, date, startTime, endTime, firstName, lastName,
                LeaveStatus.PENDING, null, "", "");
    }

    // Option B constructor
    public LeaveRequest(String leaveId, int employeeId, LocalDate date,
            LocalTime startTime, LocalTime endTime,
            String firstName, String lastName,
            LeaveStatus status, Integer reviewedBy,
            String reviewedAt, String decisionNote) {

        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.firstName = firstName;
        this.lastName = lastName;

        this.status = (status == null) ? LeaveStatus.PENDING : status;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = (reviewedAt == null) ? "" : reviewedAt;
        this.decisionNote = (decisionNote == null) ? "" : decisionNote;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    // Always writes 11 columns
    public String toCsvRow() {
        return escape(leaveId) + ","
                + employeeId + ","
                + escape(date == null ? "" : date.format(DATE_FMT)) + ","
                + escape(startTime == null ? "" : startTime.format(TIME_FMT)) + ","
                + escape(endTime == null ? "" : endTime.format(TIME_FMT)) + ","
                + escape(firstName) + ","
                + escape(lastName) + ","
                + escape(status == null ? LeaveStatus.PENDING.name() : status.name()) + ","
                + escape(reviewedBy == null ? "" : String.valueOf(reviewedBy)) + ","
                + escape(reviewedAt) + ","
                + escape(decisionNote);
    }

    private String escape(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
