/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.UserAccount;
import com.motorph.repository.UserAccountRepository;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles Create, Read, and Update for User Accounts in CSV.
 * @author ACER
 */
public class CsvUserAccountRepository implements UserAccountRepository {

    @Override
    public UserAccount findByUsername(String username) {
        // (Keep your existing findByUsername code here...)
        // Copy-paste the logic you already have for reading.
        // For brevity, I am showing the NEW methods below.
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LOGIN_CSV))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(","); // Simplified split for readability
                if (data.length >= 6 && data[0].trim().equals(username)) {
                     Role role = determineRole(data[4]);
                     boolean isLocked = data[5].equalsIgnoreCase("Yes");
                     return new UserAccount(data[0], data[1], role, isLocked);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // --- NEW: Create User ---
    @Override
    public void save(UserAccount account, String firstName, String lastName, String dept) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.LOGIN_CSV, true))) {
            // Format: Username,Password,First Name,Last Name,Department,Lock Status
            String line = String.format("%s,%s,%s,%s,%s,%s",
                    account.getUsername(),
                    DataPaths.DEFAULT_HASHED_PASSWORD, 
                    firstName,
                    lastName,
                    dept,
                    "No" // Default is not locked
            );
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- NEW: Reset Password ---
    @Override
    public void updatePassword(String username, String newHashedPassword) {
        List<String> lines = new ArrayList<>();
        
        // 1. Read ALL lines into memory
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LOGIN_CSV))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2 && data[0].equals(username)) {
                    // This is the user! Replace the password (Index 1)
                    data[1] = newHashedPassword;
                    // Rebuild the line
                    lines.add(String.join(",", data));
                } else {
                    lines.add(line); // Keep other lines exactly as they are
                }
            }
        } catch (IOException e) { e.printStackTrace(); }

        // 2. Overwrite the file with the updated list
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.LOGIN_CSV))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Role determineRole(String dept) {
        if (dept.toUpperCase().contains("HR")) return Role.HR;
        if (dept.toUpperCase().contains("PAYROLL")) return Role.PAYROLL;
        if (dept.toUpperCase().contains("IT")) return Role.IT;
        if (dept.toUpperCase().contains("MANAGER")) return Role.MANAGER;
        return Role.EMPLOYEE;
    }
}
