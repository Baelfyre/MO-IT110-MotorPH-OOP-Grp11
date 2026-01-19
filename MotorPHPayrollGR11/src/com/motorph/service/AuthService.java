/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.AuditLogEntry;
import com.motorph.domain.models.User;
import com.motorph.repository.AuditRepository;
import java.time.LocalDateTime;
import com.motorph.repository.UserRepository;

/**
 * Handles security and login logic. Integrated with AuditRepository to track
 * login activity.
 *
 * @author ACER
 */
public class AuthService {

    private final UserRepository userRepo;
    private final AuditRepository auditRepo; // Add this

    // Update Constructor to accept AuditRepository
    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
    }

    /**
     * Verifies credentials and logs the attempt.
     *
     * @return User if successful, null if failed.
     */
    public User authenticate(String username, String password) {
        User user = userRepo.findByUsername(username);

        // 1. Check if User Exists
        if (user == null) {
            log(username, "LOGIN_FAILED", "User ID not found");
            return null;
        }

        // 2. Check Password
        if (user.verifyPassword(password)) {
            // 3. Check Lock Status
            if (user.isLocked()) {
                System.out.println("Login attempted on locked account: " + username);
                log(username, "LOGIN_DENIED", "Account is Locked");
                return null;
            }

            // SUCCESS
            log(username, "LOGIN_SUCCESS", "User logged in successfully");
            return user;
        } else {
            // WRONG PASSWORD
            log(username, "LOGIN_FAILED", "Invalid Password");
            return null;
        }
    }

    // Helper to keep code clean
    private void log(String user, String action, String details) {
        if (auditRepo != null) {
            auditRepo.save(new AuditLogEntry(LocalDateTime.now(), user, action, details));
        }
    }
}
