/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.LogEntry;
import com.motorph.repository.LogRepository;
import com.motorph.repository.csv.CsvLogRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author ACER
 */
public class LogService {
    private final LogRepository logRepo;

    public LogService() {
        this(new CsvLogRepository());
    }

    // Annotation: Overloaded constructor for dependency injection from the composition root.
    public LogService(LogRepository logRepo) {
        this.logRepo = logRepo;
    }

        public void recordAction(String user, String action, String details) {
        LocalDateTime now = LocalDateTime.now();
        String category = inferCategory(action);

        LogEntry entry = new LogEntry(
                0,
                category,
                now,
                user == null ? "" : user.trim(),
                action == null ? "" : action.trim(),
                details == null ? "" : details.trim()
        );

        logRepo.save(entry);
    }

    // Annotation: Returns all logs in descending order.
    public List<LogEntry> getAllLogs() {
        return logRepo.findAll();
    }


    // Annotation: Returns all logs recorded by one user.
    public List<LogEntry> getLogsForUser(String user) {
        List<LogEntry> out = new ArrayList<>();
        String actor = user == null ? "" : user.trim();

        if (actor.isEmpty()) {
            return out;
        }

        for (LogEntry entry : logRepo.findAll()) {
            if (entry == null) {
                continue;
            }

            String currentUser = entry.getUser() == null ? "" : entry.getUser().trim();
            if (actor.equals(currentUser)) {
                out.add(entry);
            }
        }
        return out;
    }

    // Annotation: Returns logs filtered by one category.
    public List<LogEntry> getLogsByCategory(String category) {
        List<LogEntry> out = new ArrayList<>();
        String target = category == null ? "" : category.trim().toUpperCase(Locale.US);
        for (LogEntry entry : logRepo.findAll()) {
            if (entry == null) {
                continue;
            }
            String current = entry.getCategory() == null ? "" : entry.getCategory().trim().toUpperCase(Locale.US);
            if (current.equals(target)) {
                out.add(entry);
            }
        }
        return out;
    }

    // Annotation: Returns logs whose action starts with or contains the given keys.
    public List<LogEntry> getLogsByActionKeys(String... keys) {
        List<LogEntry> out = new ArrayList<>();
        if (keys == null || keys.length == 0) {
            return out;
        }

        for (LogEntry entry : logRepo.findAll()) {
            if (entry == null) {
                continue;
            }
            String action = entry.getAction() == null ? "" : entry.getAction().trim().toUpperCase(Locale.US);
            for (String key : keys) {
                String probe = key == null ? "" : key.trim().toUpperCase(Locale.US);
                if (!probe.isEmpty() && (action.startsWith(probe) || action.contains(probe))) {
                    out.add(entry);
                    break;
                }
            }
        }
        return out;
    }
    
    public List<LogEntry> getLogsForUserByActionPrefix(String user, String actionPrefix) {
        List<LogEntry> out = new ArrayList<>();

        String actor = user == null ? "" : user.trim();
        String prefix = actionPrefix == null ? "" : actionPrefix.trim().toUpperCase(Locale.US);

        if (actor.isEmpty() || prefix.isEmpty()) {
            return out;
        }

        for (LogEntry entry : logRepo.findAll()) {
            if (entry == null) {
                continue;
            }

            String currentUser = entry.getUser() == null ? "" : entry.getUser().trim();
            String currentAction = entry.getAction() == null ? "" : entry.getAction().trim().toUpperCase(Locale.US);

            if (actor.equals(currentUser) && currentAction.startsWith(prefix)) {
                out.add(entry);
            }
        }

        return out;
    }

    private String inferCategory(String action) {
        String a = action == null ? "" : action.toUpperCase(java.util.Locale.US);
        if (a.startsWith("IT_") || a.contains("PASSWORD") || a.contains("LOCK")) return "IT";
        if (a.startsWith("HR_") || a.contains("EMPLOYEE")) return "HR";
        if (a.contains("PAYROLL") || a.contains("PAYSLIP")) return "PAYROLL";
        if (a.contains("DTR") || a.contains("TIME_") || a.contains("SUPERVISOR")) return "SUPERVISOR";
        if (a.contains("LEAVE")) return "EMPLOYEE";
        return "SYSTEM";
    }
}
