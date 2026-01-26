/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.Employee;
import java.util.List;

/**
 * The CONTRACT.
 * Defines WHAT we can do, but not HOW we do it.
 */
public interface EmployeeRepository {
    
    // Method 1: Find all employees
    List<Employee> findAll();

    // Method 2: Find one employee by ID
    // (Ensure this is named 'findById', NOT 'findByEmployeeNumber')
    Employee findById(int id);

    // Method 3: Save a new employee
    void create(Employee emp);
}