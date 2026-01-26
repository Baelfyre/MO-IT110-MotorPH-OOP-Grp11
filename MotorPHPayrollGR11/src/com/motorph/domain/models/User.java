/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.Role;
import com.motorph.utils.PasswordUtil;

/**
 * Represents a user's login credentials and access rights. Maps to
 * data_LogIn_Hashed.csv
 *
 * @author ACER
 */
public class User extends BaseEntity {

    private String username;
    private String passwordHash;
    private Role role;
    private boolean isLocked;

    public User(int id, String username, String passwordHash, Role role, boolean isLocked) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isLocked = isLocked;
    }

    public boolean verifyPassword(String inputPass) {
        return PasswordUtil.checkPassword(inputPass, this.passwordHash);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public String toCsvRow() {
        // Standardized format: id,username,passwordHash,role,isLocked
        return id + "," + username + "," + passwordHash + "," + role + "," + isLocked;
    }
}
