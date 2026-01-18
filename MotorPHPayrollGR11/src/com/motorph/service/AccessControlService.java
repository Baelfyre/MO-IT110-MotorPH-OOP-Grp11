/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.UserAccount;

/**
 * Security Service. Decides which screens a user is allowed to open based on
 * their Role.
 *
 * @author ACER
 */
public class AccessControlService {

    public boolean checkAccess(UserAccount user, String viewName) {
        if (user == null) {
            return false;
        }

        Role role = user.getRole();

        // 1. PAYROLL / HR Dashboard
        if (viewName.equals("PayrollDashboardView")) {
            return role == Role.PAYROLL || role == Role.HR;
        }

        // 2. MANAGER Dashboard
        if (viewName.equals("ManagerDashboardView")) {
            return role == Role.MANAGER;
        }

        // 3. IT Dashboard (Strict access)
        if (viewName.equals("ItDashboardView")) {
            return role == Role.IT;
        }

        // 4. EMPLOYEE Portal (Self-Service)
        if (viewName.equals("EmployeePortalView")) {
            // FIXED: Just check for EMPLOYEE. 
            // Your logic assigns Role.EMPLOYEE to both Regular and Probationary staff.
            return role == Role.EMPLOYEE;
        }

        return false; // Access Denied for unknown views
    }
}
