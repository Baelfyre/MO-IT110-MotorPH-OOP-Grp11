/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.test;

import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.CsvUserRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * BACKEND-ONLY AUTH TEST (NO UI) Tests: 1) Correct login succeeds 2) 3 wrong
 * attempts locks the user 3) Locked user cannot login even with correct
 * password
 *
 * It restores the CSV after the test so you don't keep accounts locked.
 *
 * @author ACER
 */
public class BackEndAuthTester {

    // Defaults based on your data/data_Legacy_LogIn.csv sample row
    // You can change these if you want to test a different user
    private static final String DEFAULT_USERNAME = "10000";
    private static final String DEFAULT_PASSWORD = "sAdmin";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== MOTORPH BACKEND AUTH TEST (LEGACY MODE) ===");

        String username = (args.length >= 1 && !args[0].trim().isEmpty()) ? args[0].trim() : DEFAULT_USERNAME;
        String password = (args.length >= 2 && !args[1].trim().isEmpty()) ? args[1].trim() : DEFAULT_PASSWORD;

        Path csvPath = resolveCsvPath(DataPaths.LOGIN_CSV);

        if (csvPath == null || !Files.exists(csvPath)) {
            System.out.println("FAIL | Cannot find LOGIN_CSV. Checked: " + DataPaths.LOGIN_CSV);
            System.out.println("Fix: Ensure DataPaths.LOGIN_CSV points to your legacy file path (example: ./data/data_Legacy_LogIn.csv).");
            return;
        }

        String originalCsv = null;

        try {
            originalCsv = Files.readString(csvPath, StandardCharsets.UTF_8);

            UserRepository userRepo = new CsvUserRepository();

            // Always reset lock first (test should start clean)
            userRepo.updateLockStatus(username, false);

            runTests(userRepo, username, password);

        } catch (Exception e) {
            System.out.println("ERROR | Test runner encountered an exception: " + e.getMessage());
        } finally {
            // Restore CSV to its original content (so the lockout test doesn't permanently lock anyone)
            if (originalCsv != null) {
                try {
                    Files.writeString(csvPath, originalCsv, StandardCharsets.UTF_8);
                    System.out.println("INFO | Restored login CSV to original state.");
                } catch (Exception e) {
                    System.out.println("WARN | Could not restore login CSV: " + e.getMessage());
                }
            }

            System.out.println();
            System.out.println("=== SUMMARY ===");
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
        }
    }

    private static void runTests(UserRepository userRepo, String username, String password) {

        // T1: Correct login succeeds
        AuthService auth1 = new AuthService(userRepo);
        boolean okLogin = auth1.login(username, password);
        check(okLogin, "T1 Correct login succeeds");
        check(auth1.getCurrentUser() != null, "T1.1 Current user is set after successful login");

        // T2: Three failed attempts locks the account
        AuthService auth2 = new AuthService(userRepo);

        boolean fail1 = auth2.login(username, "WRONG_PASSWORD_1");
        boolean fail2 = auth2.login(username, "WRONG_PASSWORD_2");
        boolean fail3 = auth2.login(username, "WRONG_PASSWORD_3");

        check(!fail1, "T2 Wrong password attempt #1 fails");
        check(!fail2, "T2 Wrong password attempt #2 fails");
        check(!fail3, "T2 Wrong password attempt #3 fails (lock should trigger)");

        User userAfter = userRepo.findByUsername(username);
        check(userAfter != null, "T2.1 User can be reloaded after lock attempts");
        check(userAfter != null && userAfter.isLocked(), "T2.2 User is locked after 3 failed attempts");

        // T3: Locked user cannot login even with correct password
        AuthService auth3 = new AuthService(userRepo);
        boolean lockedLogin = auth3.login(username, password);
        check(!lockedLogin, "T3 Locked user cannot login even with correct password");
        check(auth3.getCurrentUser() == null, "T3.1 Current user stays null when locked");
    }

    private static void check(boolean condition, String label) {
        if (condition) {
            passed++;
            System.out.println("PASS | " + label);
        } else {
            failed++;
            System.out.println("FAIL | " + label);
        }
    }

    private static Path resolveCsvPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }

        // If it starts with "./", Paths.get still works, but this makes it safer in some run configs
        Path p1 = Paths.get(rawPath);
        if (Files.exists(p1)) {
            return p1;
        }

        String cleaned = rawPath.replaceFirst("^\\./", "");
        Path p2 = Paths.get(cleaned);
        if (Files.exists(p2)) {
            return p2;
        }

        return p1; // return best guess even if not found (caller checks exists)
    }
}
