/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.time.LocalDateTime;

/**
 * Represents a security or transaction log entry. Captures WHO did WHAT and
 * WHEN.
 *
 * @author ACER
 */
public class AuditLogEntry {

    private LocalDateTime timestamp;
    private String user;
    private String action;
    private String details;

    // This constructor must exist for AuthService to work
    public AuditLogEntry(LocalDateTime timestamp, String user, String action, String details) {
        this.timestamp = timestamp;
        this.user = user;
        this.action = action;
        this.details = details;
    }

    // Getters
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
}
