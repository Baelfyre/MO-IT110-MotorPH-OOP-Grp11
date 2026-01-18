/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.AuditLogEntry;

/**
 *
 * @author ACER
 */
public interface AuditRepository {

    // Generic save
    void save(AuditLogEntry entry);

    // Specific Log Methods (This fixes your error!)
    void logPayrollChange(String user, String details);

    void logEmpDataChange(String user, String details);

    void logDtrChange(String user, String details);
}
