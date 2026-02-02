/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a system activity log record.
 *
 * @author ACER
 */
public class LogEntry {

    private static final DateTimeFormatter TS_FMT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    private int id;
    private LocalDateTime timestamp;
    private String user;
    private String action;
    private String details;

    public LogEntry(int id, LocalDateTime timestamp, String user, String action, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toCsvRow() {
        String ts = (timestamp == null) ? "" : timestamp.format(TS_FMT);

        return id + ","
                + escape(ts) + ","
                + escape(user) + ","
                + escape(action) + ","
                + escape(details);
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
