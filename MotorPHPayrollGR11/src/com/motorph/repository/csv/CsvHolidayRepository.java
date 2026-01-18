/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.Holiday;
import com.motorph.repository.HolidayRepository;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of HolidayRepository using CSV storage. Reads
 * data_HolidayCalendar.csv to check for pay rate adjustments.
 *
 * @author ACER
 */
public class CsvHolidayRepository implements HolidayRepository {

    // Matches format in CSV (e.g., "1/1/2024" or "01/01/2024")
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public Holiday findByDate(LocalDate date) {
        // Ensure the path is correct in DataPaths class
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.HOLIDAY_CSV))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                // 1. Safe Split (Handles commas inside quotes)
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // CSV Format: Date, Holiday Name, Type
                if (data.length >= 3) {
                    try {
                        String dateStr = data[0].trim().replace("\"", "");
                        LocalDate hDate = LocalDate.parse(dateStr, fmt);

                        // Check if this row matches the date we are looking for
                        if (hDate.equals(date)) {
                            String name = data[1].trim().replace("\"", "");
                            String type = data[2].trim().replace("\"", "");

                            return new Holiday(hDate, name, type);
                        }
                    } catch (Exception e) {
                        // Skip lines with invalid dates (logs error silently to avoid spamming console)
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // No holiday found for this date
    }
}
