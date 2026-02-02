/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.LogEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * CSV-backed system activity log repository.
 *
 * @author ACER
 */
public class CsvLogRepository {

    private static final String HEADER = "Log_ID,Timestamp,User,Action,Details";
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    public boolean save(LogEntry entry) {
        if (entry == null) {
            return false;
        }

        ensureHeader();

        if (entry.getId() <= 0) {
            entry.setId(nextId());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.SYSTEM_LOG_CSV, true))) {
            bw.newLine();
            bw.write(entry.toCsvRow());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureHeader() {
        try {
            File f = new File(DataPaths.SYSTEM_LOG_CSV);
            if (!f.exists()) {
                writeHeader();
                return;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String firstLine = br.readLine();
                if (firstLine == null || firstLine.trim().isEmpty()) {
                    writeHeader();
                }
            }
        } catch (Exception e) {
            // header creation failure remains non-blocking
        }
    }

    private void writeHeader() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.SYSTEM_LOG_CSV, false))) {
            bw.write(HEADER);
        } catch (Exception e) {
            // file creation failure remains non-blocking
        }
    }

    private int nextId() {
        int last = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.SYSTEM_LOG_CSV))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length > 0) {
                    try {
                        last = Integer.parseInt(data[0].replace("\"", "").trim());
                    } catch (NumberFormatException ignored) {
                        // invalid id rows are ignored
                    }
                }
            }
        } catch (Exception e) {
            return 1;
        }

        return last + 1;
    }
}
