/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.service;

import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;

/**
 * Handles security and login logic. Integrated with AuditRepository to track
 * login activity.
 *
 * @author ACER
 */
public class AuthService {

    private final UserRepository userRepo;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 3;
    private User currentUser; // We need this to store who logged in

    // CORRECT CONSTRUCTOR - This fixes the "variable not initialized" error
    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public boolean login(String username, String password) {
        User user = userRepo.findByUsername(username);

        if (user == null) {
            currentUser = null; // failed login does not keep any prior session user
            return false;
        }

        if (user.isLocked()) {
            currentUser = null; // locked users cannot become the active session user
            return false;
        }

        if (user.verifyPassword(password)) {
            this.currentUser = user; // successful login sets the active user
            failedAttempts = 0;
            return true;
        } else {
            failedAttempts++;

            // failed password attempts do not keep any prior session user
            currentUser = null;

            if (failedAttempts >= MAX_ATTEMPTS) {
                userRepo.updateLockStatus(username, true);
            }
            return false;
        }
    }

    // Add this getter so LoginView can pass the user to the Dashboard
    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAccountNowLocked() {
        return failedAttempts >= MAX_ATTEMPTS;
    }
}
