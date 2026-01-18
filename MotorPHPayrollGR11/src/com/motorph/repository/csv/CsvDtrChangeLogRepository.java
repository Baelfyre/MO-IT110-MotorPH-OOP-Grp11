/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.DtrChangeLogEntry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for accessing DTR Change Logs. Reads from changeLogs_DTR.csv
 *
 * @author ACER
 */
public class CsvDtrChangeLogRepository {

    // Matches format in CSV: "6/30/2025 3:30 AM"
    // M/d/yyyy matches single or double digit months/days
    // h:mm a matches 12-hour format with AM/PM
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    /**
     * Retrieves all DTR change log entries.
     *
     * @return List of DtrChangeLogEntry objects.
     */
    public List<DtrChangeLogEntry> getAllLogs() {
        List<DtrChangeLogEntry> logs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LOG_DTR))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                // Split by comma, ignoring commas inside quotes (standard CSV safety)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Expecting 5 columns: Supervisor, EmpID, Name, Date, Changes
                if (data.length >= 5) {
                    try {
                        String supervisor = data[0].trim();
                        int empId = Integer.parseInt(data[1].trim());
                        String name = data[2].trim();
                        LocalDateTime date = LocalDateTime.parse(data[3].trim(), fmt);
                        String changes = data[4].trim();

                        logs.add(new DtrChangeLogEntry(supervisor, empId, name, date, changes));
                    } catch (Exception e) {
                        // Log parsing error or skip malformed lines
                        System.err.println("Skipping malformed log line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }
}
