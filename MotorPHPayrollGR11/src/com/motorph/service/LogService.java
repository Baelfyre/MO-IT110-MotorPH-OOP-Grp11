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
            0, // ID auto-generated or managed by repo
            LocalDateTime.now(),
            user,
            action,
            details
        );
        logRepo.save(entry);
    }
    
}
