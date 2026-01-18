/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.EmployeeProfile;
import com.motorph.domain.models.UserAccount;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.UserAccountRepository;
import com.motorph.repository.csv.CsvFileInitializer;
import com.motorph.repository.csv.DataPaths;
import com.motorph.utils.PasswordUtil;

/**
 * Service for IT operations on User Accounts. Handles unlocking accounts and
 * password resets.
 *
 * @author ACER
 */
/**
 * Orchestrates creating new users and resetting credentials.
 *
 * @author ACER
 */
public class UserManagementService {

    private final EmployeeRepository empRepo;
    private final UserAccountRepository userRepo;

    public UserManagementService(EmployeeRepository empRepo, UserAccountRepository userRepo) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
    }

    /**
     * FULL ONBOARDING: 1. Saves Employee Data (Address, Phone, etc.) 2. Creates
     * Login Account (Username = EmpID, Password = Default) 3. Generates blank
     * DTR and Payslip CSV files.
     */
    public void createNewEmployee(EmployeeProfile emp, String department) {
        // 1. Save Profile
        empRepo.create(emp); 

        // 2. Create Login Account
        Role role = Role.EMPLOYEE;
        if (department.contains("HR")) {
            role = Role.HR;
        }
        // ... (add other role checks)

        UserAccount newAccount = new UserAccount(
                String.valueOf(emp.getEmployeeNumber()), // Username is ID
                DataPaths.DEFAULT_HASHED_PASSWORD, // Default: ph12345
                role,
                false // Not locked
        );

        userRepo.save(newAccount, emp.getFirstName(), emp.getLastName(), department);

        // 3. Initialize Files
        CsvFileInitializer.initializeEmployeeFiles(emp.getEmployeeNumber());

        System.out.println("User " + emp.getEmployeeNumber() + " onboarded successfully.");
    }

    /**
     * RESET PASSWORD: Resets a user's password back to 'ph12345'.
     */
    public void resetPasswordToDefault(String username) {
        userRepo.updatePassword(username, DataPaths.DEFAULT_HASHED_PASSWORD);
        System.out.println("Password for " + username + " has been reset.");
    }

    /**
     * CHANGE PASSWORD: User sets their own password.
     */
    public void changePassword(String username, String newPlainPassword) {
        String newHash = PasswordUtil.hashPassword(newPlainPassword);
        userRepo.updatePassword(username, newHash);
    }
}
