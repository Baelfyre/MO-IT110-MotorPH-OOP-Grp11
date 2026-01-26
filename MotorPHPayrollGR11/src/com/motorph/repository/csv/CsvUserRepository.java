/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.User;
import com.motorph.domain.enums.Role;
import com.motorph.repository.UserRepository;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles Create, Read, and Update for User Accounts in CSV. Implements
 * UserRepository to satisfy the interface contract.
 */
public class CsvUserRepository implements UserRepository {

    // Pointing to your specific hashed file
    private static final String FILE_PATH = "src/com/motorph/resources/data_LogIn_Hashed.csv";

    @Override
    public User findByUsername(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5 && data[1].equalsIgnoreCase(username)) {
                    return new User(
                            Integer.parseInt(data[0]), // id
                            data[1], // username
                            data[2], // passwordHash
                            Role.valueOf(data[3].toUpperCase()),
                            Boolean.parseBoolean(data[4]) // isLocked
                    );
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading hashed login file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void save(User account, String firstName, String lastName, String dept) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            // Appending a new line to the CSV
            out.println(account.toCsvRow());
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    @Override
    public void updatePassword(String username, String newHashedPassword) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[1].equalsIgnoreCase(username)) {
                    // Update only the password hash column (index 2)
                    data[2] = newHashedPassword;
                    line = String.join(",", data);
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading for update: " + e.getMessage());
        }

        // Rewrite the file with the updated data
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String l : lines) {
                out.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error rewriting CSV: " + e.getMessage());
        }

    }

    @Override
    public void updateLockStatus(String username, boolean isLocked) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String targetFile = "src/com/motorph/resources/data_LogIn_Hashed.csv";

        // 1. Read all lines into memory
        try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Check if this is the user we need to update
                if (data.length >= 5 && data[1].equalsIgnoreCase(username)) {
                    // Format: ID[0], Username[1], Password[2], Role[3], isLocked[4]
                    data[4] = String.valueOf(isLocked); // Update the lock status
                    lines.add(String.join(",", data));
                } else {
                    lines.add(line); // Keep other lines exactly as they are
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }

        // 2. Rewrite the whole file with the update
        try (PrintWriter out = new PrintWriter(new FileWriter(targetFile))) {
            for (String l : lines) {
                out.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error updating lock status: " + e.getMessage());
        }
    }
}
