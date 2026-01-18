/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.EmployeeProfile;
import com.motorph.repository.EmployeeRepository;

/**
 * Service for managing Employee Profiles. Handles retrieval and validation of
 * employee data.
 *
 * @author ACER
 */
public class EmployeeService {

    private EmployeeRepository employeeRepo;

    // Constructor Injection
    public EmployeeService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    /**
     * Retrieves an employee profile by ID.
     *
     * @param empId The employee number.
     * @return The EmployeeProfile object, or null if not found.
     */
    public EmployeeProfile getEmployee(int empId) {
        if (empId <= 0) {
            return null;
        }
        EmployeeProfile emp = employeeRepo.findByEmployeeNumber(empId);
        if (emp == null) {
            // Optional: log to your CsvAuditRepository here
            System.out.println("Audit: Profile access failed for ID " + empId);
        }
        return emp;
    }

    /**
     * Checks if an employee exists in the system.
     *
     * @param empId The employee number to check.
     * @return true if found, false otherwise.
     */
    public boolean exists(int empId) {
        return getEmployee(empId) != null;
    }

    /**
     * Helper to get the full name format.
     *
     * @param emp The employee profile.
     * @return String "LastName, FirstName"
     */
    public String formatName(EmployeeProfile emp) {
        if (emp == null) {
            return "Unknown";
        }
        return emp.getLastName() + ", " + emp.getFirstName();
    }
}
