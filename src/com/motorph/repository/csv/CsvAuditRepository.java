/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.repository.AuditRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSV-backed audit logger. Appends records to changeLogs_records.csv using a
 * timestamp-based Audit_ID.
 *
 * @author ACER
 */
public class CsvAuditRepository implements AuditRepository {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Extracts transaction IDs like: TX-10001-240601-240615
    private static final Pattern TX_PATTERN = Pattern.compile("(TX-\\d+-\\d{6}-\\d{6})");

    @Override
    public boolean logPayrollChange(String performedBy, String details) {
        return logChange("PAYROLL", performedBy, details);
    }

    @Override
    public boolean logDtrChange(String performedBy, String details) {
        return logChange("DTR", performedBy, details);
    }

    @Override
    public boolean logLeaveChange(String performedBy, String details) {
        return logChange("LEAVE", performedBy, details);
    }

    private boolean logChange(String targetTable, String performedBy, String details) {
        ensureHeader();

        String auditId = "AUD-" + System.currentTimeMillis();
        String recordId = extractTransactionId(details);
        String timestamp = LocalDateTime.now().format(TS_FMT);

        String oldValue = "";
        String newValue = details == null ? "" : details;

        String row
                = escape(auditId) + ","
                + escape(targetTable) + ","
                + escape(recordId) + ","
                + escape(performedBy) + ","
                + escape(timestamp) + ","
                + escape(oldValue) + ","
                + escape(newValue);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.AUDIT_LOG_CSV, true))) {
            bw.newLine();
            bw.write(row);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureHeader() {
        try {
            File f = new File(DataPaths.AUDIT_LOG_CSV);
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
            // Header creation failures are ignored; append will fail safely if path is invalid.
        }
    }

    private void writeHeader() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.AUDIT_LOG_CSV, false))) {
            bw.write("Audit_ID,Target_Table,Record_ID,Performed_By,Timestamp,Old_Value,New_Value");
        } catch (Exception e) {
            // File creation failures are ignored.
        }
    }

    private String extractTransactionId(String details) {
        if (details == null) {
            return "";
        }
        Matcher m = TX_PATTERN.matcher(details);
        return m.find() ? m.group(1) : "";
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
