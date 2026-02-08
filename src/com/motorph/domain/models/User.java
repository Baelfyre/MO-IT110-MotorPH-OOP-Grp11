/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.domain.models;

import com.motorph.domain.enums.Role;

/**
 * Represents a user's login credentials and access rights.
 *
 * Current project mode uses the legacy login CSV (data_Legacy_LogIn.csv), so
 * the stored password is treated as plain text. *
 *
 * @author ACER
 */

public class User extends BaseEntity {

    private final String username;
    private final String password;
    private final Role role;
    private final boolean isLocked;

    public User(int id, String username, String password, Role role, boolean isLocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isLocked = isLocked;
    }

    public boolean verifyPassword(String inputPass) {
        return inputPass != null && inputPass.equals(this.password);
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public String toCsvRow() {
        // Standardized format: id,username,password,role,isLocked
        return id + "," + username + "," + password + "," + role + "," + isLocked;
    }
}
