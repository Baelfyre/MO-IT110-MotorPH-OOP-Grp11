/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.Employee;
import com.motorph.repository.EmployeeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing employee master records. Handles retrieval, caching,
 * role determination, and team hierarchy checks.
 *
 * @author ACER
 */
public class EmployeeService {

    private final EmployeeRepository employeeRepo;

    // Key: Employee ID, Value: Employee
    private final Map<Integer, Employee> employeeCache = new HashMap<>();

    // Key: Supervisor display name (example: "Garcia, Manuel III"), Value: direct subordinates
    private final Map<String, List<Employee>> supervisorMap = new HashMap<>();

    public EmployeeService(EmployeeRepository employeeRepo) {
        this.employeeRepo = employeeRepo;
        refreshCache();
    }

    public final void refreshCache() {
        List<Employee> allEmployees = employeeRepo.findAll();

        employeeCache.clear();
        supervisorMap.clear();

        for (Employee emp : allEmployees) {
            employeeCache.put(emp.getId(), emp);

            String supervisorName = normalizeSupervisorName(emp.getImmediateSupervisor());
            if (!supervisorName.isEmpty()) {
                supervisorMap
                        .computeIfAbsent(supervisorName, k -> new ArrayList<>())
                        .add(emp);
            }
        }
    }

    public Employee getEmployee(int empId) {
        return employeeCache.get(empId);
    }

    public boolean isSupervisor(String loggedInEmployeeId) {
        try {
            return isSupervisor(Integer.parseInt(loggedInEmployeeId));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isSupervisor(int loggedInEmployeeId) {
        Employee user = employeeCache.get(loggedInEmployeeId);
        if (user == null) {
            return false;
        }

        String fullName = user.getLastName() + ", " + user.getFirstName();
        return supervisorMap.containsKey(fullName);
    }

    public List<Employee> getSubordinates(String supervisorName) {
        String key = normalizeSupervisorName(supervisorName);
        List<Employee> subs = supervisorMap.getOrDefault(key, new ArrayList<>());
        return new ArrayList<>(subs);
    }
    public Role determineRoleFromPosition(String position) {
        if (position == null) {
            return Role.EMPLOYEE;
        }

        String p = position.trim().toUpperCase();

        if (p.contains("HR")) {
            return Role.HR;
        }
        if (p.contains("PAYROLL") || p.contains("FINANCE") || p.contains("ACCOUNTING") || p.contains("ACCOUNT MANAGER") || p.contains("ACCOUNT TEAM LEADER") || p.contains("CHIEF FINANCE OFFICER")) {
            return Role.PAYROLL;
        }
        if (p.contains("IT")) {
            return Role.IT;
        }
        if (p.contains("CHIEF") || p.contains("CEO") || p.contains("COO") || p.contains("CMO") || p.contains("TEAM LEADER") || p.contains("MANAGER") || p.contains("HEAD")) {
            return Role.SUPERVISOR;
        }

        return Role.EMPLOYEE;
    }

    private String normalizeSupervisorName(String supervisorName) {
        if (supervisorName == null) {
            return "";
        }
        String s = supervisorName.trim();

        if (s.isEmpty()) {
            return "";
        }

        // Placeholder values treated as "no supervisor"
        String upper = s.toUpperCase();
        if (upper.equals("N/A") || upper.equals("NA")) {
            return "";
        }

        return s;
    }
}
