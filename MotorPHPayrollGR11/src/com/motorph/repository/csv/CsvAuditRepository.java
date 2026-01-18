/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.AuditLogEntry;
import com.motorph.repository.AuditRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of the AuditRepository.
 * Writes security and transaction logs to specific CSV files based on the action type.
 * Aligned with DataPaths.java structure.
 * @author ACER
 */
public class CsvAuditRepository implements AuditRepository {

    // Format the date so it looks clean in the CSV (e.g., "2022-09-01 13:45:00")
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void save(AuditLogEntry entry) {
        // Use the Generic Fallback for unspecified logs
        writeLog(DataPaths.AUDIT_LOG_CSV, entry.getUser(), "GENERIC_LOG", entry.getDetails());
    }

    @Override
    public void logPayrollChange(String user, String details) {
        writeLog(DataPaths.LOG_PAYROLL, user, "PAYROLL_REGEN", details);
    }

    @Override
    public void logEmpDataChange(String user, String details) {
        writeLog(DataPaths.LOG_EMP_DATA, user, "PROFILE_UPDATE", details);
    }

    @Override
    public void logDtrChange(String user, String details) {
        writeLog(DataPaths.LOG_DTR, user, "DTR_OVERRIDE", details);
    }

    // Helper method to handle the actual file writing
    private void writeLog(String path, String user, String action, String details) {
        File file = new File(path);
        
        // Check if file is new so we can add a Header Row
        boolean isNewFile = !file.exists() || file.length() == 0;

        // Safety: Create the 'data' folder if it doesn't exist
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) { // true = append mode
            
            // Write Header if this is the first time writing to the file
            if (isNewFile) {
                pw.println("Timestamp,User,Action,Details");
            }

            // Clean up details to prevent CSV breakage (replace commas with semicolons in the message)
            String safeDetails = (details != null) ? details.replace(",", ";") : "";
            String timestamp = LocalDateTime.now().format(FMT);

            pw.println(timestamp + "," + user + "," + action + "," + safeDetails);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}