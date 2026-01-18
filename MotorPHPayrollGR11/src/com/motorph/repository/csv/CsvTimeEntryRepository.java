/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.repository.TimeEntryRepository;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of TimeEntryRepository using CSV. Reads/Writes specific
 * employee DTR files (records_dtr_{id}.csv). Aligned with CsvFileInitializer
 * headers: Date,TimeIn,TimeOut
 *
 * @author ACER
 */
public class CsvTimeEntryRepository implements TimeEntryRepository {

    // DATE FORMAT: Matches your input (e.g., 6/3/2024)
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M/d/yyyy");

    // TIME FORMAT: Matches "8:30 AM" or "5:00 PM"
    // If your data uses 24-hour format (17:00), change this to "H:mm"
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");

    @Override
    public boolean saveEntry(int empId, TimeEntry entry) {
        String filename = "records_dtr_" + empId + ".csv";
        // Ensure the path uses the correct folder from DataPaths
        File file = new File(DataPaths.DTR_FOLDER + filename);

        // Auto-create folder/file if missing
        if (!file.exists()) {
            CsvFileInitializer.initializeEmployeeFiles(empId);
        }

        // CSV Structure: Date,TimeIn,TimeOut
        String record = String.format("%s,%s,%s",
                entry.getDate().format(dateFmt),
                (entry.getTimeIn() != null ? entry.getTimeIn().format(timeFmt) : ""),
                (entry.getTimeOut() != null ? entry.getTimeOut().format(timeFmt) : "")
        );

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            pw.println(record);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<TimeEntry> getEntries(int empId) {
        // Fetch all records (Period is null)
        return readFromFile(empId, null);
    }

    @Override
    public List<TimeEntry> findByEmployeeAndPeriod(int empId, PayPeriod period) {
        // Fetch records only within the period
        return readFromFile(empId, period);
    }

    /**
     * Shared helper to parse the CSV file. Aligned to: [0]Date, [1]TimeIn,
     * [2]TimeOut
     */
    private List<TimeEntry> readFromFile(int empId, PayPeriod period) {
        List<TimeEntry> entries = new ArrayList<>();
        String filename = "records_dtr_" + empId + ".csv";
        File file = new File(DataPaths.DTR_FOLDER + filename);

        if (!file.exists()) {
            return entries; // Return empty list if no file exists
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip Header (Date,TimeIn,TimeOut)

            while ((line = br.readLine()) != null) {
                // Robust split to handle empty columns (e.g., missing TimeOut)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                if (data.length >= 4) { // EmpID, Date, TimeIn, TimeOut
                    try {
                        // data[0] = EmpID (ignore)
                        LocalDate date = LocalDate.parse(clean(data[1]), dateFmt);

                        if (period == null || period.includes(date)) {
                            LocalTime in = parseTime(data[2]);
                            LocalTime out = parseTime(data[3]); // safe even if blank (parseTime returns null)

                            // Optional but recommended: require TimeIn to be valid
                            if (in != null) {
                                entries.add(new TimeEntry(date, in, out));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Skipping invalid DTR line for Emp " + empId + ": " + line);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    // --- HELPERS ---
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(clean(timeStr), timeFmt);
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String input) {
        return input.trim().replace("\"", "");
    }
}
