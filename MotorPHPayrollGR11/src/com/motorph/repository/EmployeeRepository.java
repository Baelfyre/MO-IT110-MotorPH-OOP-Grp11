/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.EmployeeProfile;

/**
 *
 * @author ACER
 */
public interface EmployeeRepository {
    // Existing method
    EmployeeProfile findByEmployeeNumber(int employeeNumber);
    
    // --- NEW: Add this line ---
    void create(EmployeeProfile emp);
}
