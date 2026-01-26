/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.Employee;
import com.motorph.domain.enums.Role;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing Employee Profiles. 
 * Handles retrieval, caching, role determination, and hierarchy checks.
 *
 * @author ACER
 */
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    
    // Key: ID (Int), Value: Employee
    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    
    // Key: Supervisor Name (String "Garcia, Manuel III"), Value: List of Subordinates
    private final Map<String, List<Employee>> supervisorMap = new HashMap<>();

    public EmployeeService() {
        this.employeeRepo = new CsvEmployeeRepository();
        refreshCache();
    }

    public void refreshCache() {
        List<Employee> allEmployees = employeeRepo.findAll();
        employeeCache.clear();
        supervisorMap.clear();

        for (Employee emp : allEmployees) {
            employeeCache.put(emp.getId(), emp);

            String supervisorName = emp.getImmediateSupervisor();
            if (supervisorName != null && !supervisorName.trim().isEmpty()) {
                supervisorMap.computeIfAbsent(supervisorName.trim(), k -> new ArrayList<>()).add(emp);
            }
        }
    }

    /**
     * UPDATED LOGIC: 
     * 1. Finds the logged-in user by ID (e.g., 10001).
     * 2. Constructs their full name (e.g., "Garcia, Manuel III").
     * 3. Checks if that NAME exists as a key in the supervisorMap.
     */
    public boolean isSupervisor(String loggedInEmployeeId) {
        try {
            int id = Integer.parseInt(loggedInEmployeeId);
            Employee user = employeeCache.get(id);
            
            if (user != null) {
                // Construct name exactly as it appears in CSV Column 12
                String fullName = user.getLastName() + ", " + user.getFirstName();
                return supervisorMap.containsKey(fullName);
            }
        } catch (NumberFormatException e) {
            // Invalid ID format
        }
        return false;
    }

    public List<Employee> getSubordinates(String supervisorName) {
        return supervisorMap.getOrDefault(supervisorName, new ArrayList<>());
    }

    public Role determineRoleFromPosition(String position) {
        if (position == null) return Role.EMPLOYEE;
        String cleanPosition = position.trim(); 

        // Specific Roles based on CSV data
        if (cleanPosition.contains("HR")) return Role.HR;
        if (cleanPosition.contains("Payroll") || cleanPosition.contains("Finance")) return Role.PAYROLL;
        if (cleanPosition.equals("IT Operations and Systems")) return Role.IT;
        
        return Role.EMPLOYEE;
    }

    public Employee getEmployee(int empId) {
        return employeeCache.get(empId);
    }
}