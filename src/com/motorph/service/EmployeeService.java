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
import java.util.Locale;
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

    // Key: Normalized supervisor display name, Value: direct subordinates
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
            if (emp == null) {
                continue;
            }

            employeeCache.put(emp.getId(), emp);

            String supervisorName = normalizeSupervisorName(emp.getImmediateSupervisor());
            String selfName = normalizeEmployeeDisplayName(emp);
            if (!supervisorName.isEmpty() && !supervisorName.equals(selfName)) {
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

        return supervisorMap.containsKey(normalizeEmployeeDisplayName(user));
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

        String p = position.trim().toUpperCase(Locale.US);

        if (p.contains("IT")) {
            return Role.IT;
        }
        if (p.contains("HR")) {
            return Role.HR;
        }
        if (p.contains("PAYROLL") || p.contains("FINANCE") || p.contains("ACCOUNTING")
                || p.contains("ACCOUNT MANAGER") || p.contains("ACCOUNT TEAM LEADER")
                || p.contains("CHIEF FINANCE OFFICER")) {
            return Role.PAYROLL;
        }
        if (isLeadershipPosition(p)) {
            return Role.SUPERVISOR;
        }

        return Role.EMPLOYEE;
    }

    // Annotation: Returns a consistent display name used in hierarchy matching.
    public String buildEmployeeDisplayName(Employee employee) {
        if (employee == null) {
            return "";
        }
        String last = employee.getLastName() == null ? "" : employee.getLastName().trim();
        String first = employee.getFirstName() == null ? "" : employee.getFirstName().trim();
        return (last + ", " + first).trim();
    }

    // Annotation: Normalizes supervisor names so CSV punctuation differences still map to the correct team lead.
    public String normalizeSupervisorName(String supervisorName) {
        if (supervisorName == null) {
            return "";
        }
        String s = supervisorName.trim();

        if (s.isEmpty()) {
            return "";
        }

        String upper = s.toUpperCase(Locale.US);
        if (upper.equals("N/A") || upper.equals("NA") || upper.equals("NONE")) {
            return "";
        }

        return normalizeNameToken(s);
    }

    private String normalizeEmployeeDisplayName(Employee employee) {
        return normalizeNameToken(buildEmployeeDisplayName(employee));
    }

    private boolean isLeadershipPosition(String positionUpper) {
        return positionUpper.contains("CHIEF")
                || positionUpper.contains("CEO")
                || positionUpper.contains("COO")
                || positionUpper.contains("CMO")
                || positionUpper.contains("TEAM LEADER")
                || positionUpper.contains("MANAGER")
                || positionUpper.contains("HEAD");
    }

    private String normalizeNameToken(String raw) {
        if (raw == null) {
            return "";
        }
        String token = raw.toUpperCase(Locale.US).replaceAll("[^A-Z0-9]", "");
        return token == null ? "" : token.trim();
    }
}
