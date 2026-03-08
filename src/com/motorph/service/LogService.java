/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.LogEntry;
import com.motorph.repository.csv.CsvLogRepository;
import java.time.LocalDateTime;

/**
 *
 * @author ACER
 */
public class LogService {
    private final CsvLogRepository logRepo = new CsvLogRepository();

    public void recordAction(String user, String action, String details) {
        LogEntry entry = new LogEntry(
            0,
            inferCategory(action),
            LocalDateTime.now(),
            user,
            action,
            details
        );
        logRepo.save(entry);
    }
    

    private String inferCategory(String action) {
        String a = action == null ? "" : action.toUpperCase(java.util.Locale.US);
        if (a.startsWith("IT_") || a.contains("PASSWORD") || a.contains("LOCK")) return "IT";
        if (a.startsWith("HR_") || a.contains("EMPLOYEE")) return "HR";
        if (a.contains("PAYROLL") || a.contains("PAYSLIP")) return "PAYROLL";
        if (a.contains("DTR") || a.contains("TIME_")) return "SUPERVISOR";
        if (a.contains("LEAVE")) return "EMPLOYEE";
        return "SYSTEM";
    }
}
