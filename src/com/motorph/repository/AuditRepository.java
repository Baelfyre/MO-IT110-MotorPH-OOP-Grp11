/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

/**
 * Contract for audit logging. Records immutable change history for
 * payroll-related actions.
 *
 * @author ACER
 */
public interface AuditRepository {

    boolean logPayrollChange(String performedBy, String details);

    boolean logDtrChange(String performedBy, String details);

    boolean logLeaveChange(String performedBy, String details);
}
