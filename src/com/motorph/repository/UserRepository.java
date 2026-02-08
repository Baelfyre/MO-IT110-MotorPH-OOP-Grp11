/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.motorph.repository;

import com.motorph.domain.models.User;

/**
 * Interface for accessing user authentication data. Used by AuthService to
 * verify credentials and determine user roles (e.g., HR, Payroll, Employee).
 *
 * @author ACER
 */
public interface UserRepository {

    User findByUsername(String username);

    void save(User account, String firstName, String lastName, String dept);

    // Legacy mode: password is stored as plain text in data_Legacy_LogIn.csv.
    void updatePassword(String username, String newPassword);

    void updateLockStatus(String username, boolean isLocked);

    boolean deleteByUsername(String username);
}
