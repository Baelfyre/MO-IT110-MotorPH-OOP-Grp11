/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public class EmployeeDirectory {

    private final List<Employee> employees = new ArrayList<>();

    public List<Employee> getAllEmployees() {
        return Collections.unmodifiableList(employees);
    }

    public void addEmployee(Employee employee) {
        if (employee == null) {
            return;
        }
        if (findById(employee.getEmployeeNumber()) != null) {
            throw new IllegalArgumentException("Duplicate employee number.");
        }
        employees.add(employee);
    }

    public Employee findById(int employeeId) {
        for (Employee e : employees) {
            if (e.getEmployeeNumber() == employeeId) {
                return e;
            }
        }
        return null;
    }

    public boolean removeEmployee(int employeeId) {
        Employee e = findById(employeeId);
        if (e == null) {
            return false;
        }
        return employees.remove(e);
    }
}
