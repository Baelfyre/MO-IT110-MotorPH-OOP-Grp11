/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.Role;
import com.motorph.utils.PasswordUtil;

/**
 * Represents a user's login credentials and access rights. Maps to
 * data_LogIn.csv
 *
 * @author ACER
 */
public class User {
    
    private String username;
    private String passwordHash; 
    private Role role;
    private boolean isLocked;

    public User(String username, String passwordHash, Role role, boolean isLocked) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isLocked = isLocked;
    }

    /**
     * Uses the PasswordUtil to verify credentials securely.
     */
    public boolean verifyPassword(String inputPass) {
        // Delegate logic to the Utility (OOP Encapsulation)
        return PasswordUtil.checkPassword(inputPass, this.passwordHash);
    }
    
    // Getters
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean isLocked() { return isLocked; }
}
