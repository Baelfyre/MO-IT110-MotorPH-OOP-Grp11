/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDateTime;

/**
 * Represents a log entry for changes made to Daily Time Records (DTR). Maps to
 * data in changeLogs_DTR.csv
 *
 * @author ACER
 */
public class DtrChangeLogEntry {

    private String supervisor;
    private int empId;
    private String empName;
    private LocalDateTime timestamp;
    private String changes; // Description of the change (e.g., "Manual DTR entry...")

    // Constructor
    public DtrChangeLogEntry(String supervisor, int empId, String empName, LocalDateTime timestamp, String changes) {
        this.supervisor = supervisor;
        this.empId = empId;
        this.empName = empName;
        this.timestamp = timestamp;
        this.changes = changes;
    }

    // --- Getters and Setters ---
    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    // Optional: Helper for CSV string format if needed for saving
    @Override
    public String toString() {
        return supervisor + "," + empId + "," + empName + "," + timestamp + "," + changes;
    }
}
