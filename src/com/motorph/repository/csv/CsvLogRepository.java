/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.LogEntry;
import com.motorph.repository.LogRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * CSV-backed system activity log repository.
 *
 * @author ACER
 */
public class CsvLogRepository implements LogRepository {

    private static final String HEADER = "Log_ID,LogCategory,Timestamp,User,Action,Details";
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter LEGACY_TS_FMT = DateTimeFormatter.ofPattern("M/d/yyyy H:mm", Locale.US);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
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

    @Override
    public List<LogEntry> findAll() {
        ensureHeader();
        List<LogEntry> out = new ArrayList<>();
        File file = new File(DataPaths.SYSTEM_LOG_CSV);
        if (!file.exists()) {
            return out;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) {
                return out;
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length < 6) {
                    continue;
                }

                int id = parseInt(clean(data[0]), 0);
                String category = clean(data[1]);
                LocalDateTime timestamp = parseTimestamp(clean(data[2]));
                String user = clean(data[3]);
                String action = clean(data[4]);
                String details = clean(data[5]);

                out.add(new LogEntry(id, category, timestamp, user, action, details));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        out.sort(Comparator.comparing(LogEntry::getId).reversed());
        return out;
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
                        last = Integer.parseInt(clean(data[0]));
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

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1).replace("\"\"", "\"");
        }
        return v;
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

        private LocalDateTime parseTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.MIN;
        }

        String value = raw.trim();

        try {
            return LocalDateTime.parse(value, TS_FMT);
        } catch (Exception ignored) {
            // try legacy format next
        }

        try {
            return LocalDateTime.parse(value, LEGACY_TS_FMT);
        } catch (Exception ignored) {
            // fall through
        }

        return LocalDateTime.MIN;
    }
}
