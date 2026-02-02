/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.test;

import com.motorph.ops.it.ItOps;
import com.motorph.ops.it.ItOpsImpl;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.CsvUserRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.LogService;

import java.io.BufferedReader;
import java.io.FileReader;

public class BackEndItOpsTester {

    public static void main(String[] args) {
        UserRepository userRepo = new CsvUserRepository();
        LogService logService = new LogService();
        ItOps itOps = new ItOpsImpl(userRepo, logService);

        String username = (args.length > 0) ? args[0].trim() : firstUsernameFromLegacyCsv();
        if (username.isEmpty()) {
            System.out.println("No username found. Check " + DataPaths.LOGIN_CSV);
            return;
        }

        int itActorId = 99999; // example IT actor id for logs

        String originalPassword = readLegacyField(username, 1); // Password column
        String originalLock = readLegacyField(username, 5);     // Lock Out Status column ("Yes" or "No")

        System.out.println("Testing IT Ops on username: " + username);
        System.out.println("Original lock: " + originalLock);

        try {
            System.out.println("Locking account...");
            System.out.println("Result: " + itOps.lockAccount(username, itActorId));

            System.out.println("Unlocking account...");
            System.out.println("Result: " + itOps.unlockAccount(username, itActorId));

            if (originalPassword != null && !originalPassword.trim().isEmpty()) {
                System.out.println("Resetting password to default...");
                System.out.println("Result: " + itOps.resetPasswordToDefault(username, itActorId));

                System.out.println("Restoring original password...");
                System.out.println("Result: " + itOps.resetPassword(username, originalPassword, itActorId));
            } else {
                System.out.println("Original password not readable, skipping password restore.");
            }

        } finally {
            // Restore original lock status
            if (originalLock != null) {
                boolean shouldBeLocked = originalLock.trim().equalsIgnoreCase("Yes");
                itOps.setLockStatus(username, shouldBeLocked, itActorId);
            }
            System.out.println("Done. Lock status restored.");
        }
    }

    private static String firstUsernameFromLegacyCsv() {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LOGIN_CSV))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 1) {
                    String u = parts[0].trim();
                    if (!u.isEmpty()) {
                        return u;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String readLegacyField(String username, int fieldIndex) {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LOGIN_CSV))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length > fieldIndex) {
                    if (parts[0].trim().equalsIgnoreCase(username.trim())) {
                        return parts[fieldIndex];
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
