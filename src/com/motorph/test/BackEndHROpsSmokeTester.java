/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.test;

/**
 *
 * @author ACER
 */
import com.motorph.domain.enums.Role;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;

import com.motorph.ops.hr.HROps;
import com.motorph.ops.hr.HROpsImpl;

import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import com.motorph.repository.csv.CsvUserRepository;
import com.motorph.repository.csv.DataPaths;

import com.motorph.service.AuthService;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;
import java.time.LocalDate;

public class BackEndHROpsSmokeTester {

    private static int pass = 0;
    private static int fail = 0;

    private static final int TEST_PERFORMED_BY = 10000;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("HR OPS SMOKE TEST: CREATE EMPLOYEE + AUTO LOGIN");
        System.out.println("==================================================");

        ensureDataFolderExists();

        // Backups
        FileBackup empBackup = backupFile(DataPaths.EMPLOYEE_CSV);
        FileBackup loginBackup = backupFile(DataPaths.LOGIN_CSV);
        FileBackup sysLogBackup = backupFile(DataPaths.SYSTEM_LOG_CSV);

        try {
            // Dependencies
            EmployeeRepository empRepo = new CsvEmployeeRepository();
            UserRepository userRepo = new CsvUserRepository();
            EmployeeService employeeService = new EmployeeService(empRepo);
            LogService logService = new LogService();

            // IMPORTANT:
            // This constructor matches the updated HROpsImpl that injects userRepo.
            // If compilation fails here, the local HROpsImpl constructor has not been updated.
            HROps hrOps = new HROpsImpl(empRepo, employeeService, userRepo, logService);

            AuthService authService = new AuthService(userRepo);

            // Baseline counts
            int empRowsBefore = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsBefore = countDataRows(DataPaths.LOGIN_CSV);
            int logRowsBefore = countDataRows(DataPaths.SYSTEM_LOG_CSV);

            // Pick a safe test empId not used in employee or login files
            int empId1 = findAvailableEmpId(empRepo, userRepo, 99001);

            // ----------------------------
            // ST-01: Create employee + login
            // ----------------------------
            section("ST-01 CREATE EMPLOYEE + AUTO LOGIN");

            Employee e1 = buildTestEmployee(empId1, "Regular", "HR Manager");

            boolean created = hrOps.createEmployee(e1, TEST_PERFORMED_BY);
            assertTrue("createEmployee returns true", created);

            Employee createdEmp = hrOps.getEmployee(empId1);
            assertNotNull("employee exists in repository", createdEmp);

            User createdUser = userRepo.findByUsername(String.valueOf(empId1));
            assertNotNull("login exists in login CSV", createdUser);

            if (createdUser != null) {
                assertEquals("username equals EmpID", String.valueOf(empId1), createdUser.getUsername());
                assertEquals("password equals default password", DataPaths.DEFAULT_PASSWORD, getPasswordUnsafe(createdUser));
                assertTrue("lock status default is not locked", !createdUser.isLocked());
                assertEquals("role derived from position contains HR", Role.HR, createdUser.getRole());
            }

            int empRowsAfterCreate = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsAfterCreate = countDataRows(DataPaths.LOGIN_CSV);
            int logRowsAfterCreate = countDataRows(DataPaths.SYSTEM_LOG_CSV);

            assertEquals("employee CSV +1 row", empRowsBefore + 1, empRowsAfterCreate);
            assertEquals("login CSV +1 row", loginRowsBefore + 1, loginRowsAfterCreate);
            assertTrue("system logs row increased (>= +1)", logRowsAfterCreate >= logRowsBefore + 1);

            // ----------------------------
            // ST-02: Login works using EmpID + default password
            // ----------------------------
            section("ST-02 LOGIN CHECK");

            boolean loginOk = authService.login(String.valueOf(empId1), DataPaths.DEFAULT_PASSWORD);
            assertTrue("AuthService.login returns true", loginOk);

            // ----------------------------
            // ST-03: Duplicate EmpID blocked
            // ----------------------------
            section("ST-03 DUPLICATE EMPID BLOCK");

            int empRowsBeforeDup = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsBeforeDup = countDataRows(DataPaths.LOGIN_CSV);

            boolean createdDup = hrOps.createEmployee(e1, TEST_PERFORMED_BY);
            assertTrue("duplicate createEmployee returns false", !createdDup);

            int empRowsAfterDup = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsAfterDup = countDataRows(DataPaths.LOGIN_CSV);

            assertEquals("employee CSV unchanged on duplicate EmpID", empRowsBeforeDup, empRowsAfterDup);
            assertEquals("login CSV unchanged on duplicate EmpID", loginRowsBeforeDup, loginRowsAfterDup);

            // ----------------------------
            // ST-04: Duplicate username blocked before employee write
            // ----------------------------
            section("ST-04 DUPLICATE USERNAME BLOCK (PRE-CHECK)");

            int empId2 = findAvailableEmpId(empRepo, userRepo, empId1 + 1);

            int empRowsBeforeUdup = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsBeforeUdup = countDataRows(DataPaths.LOGIN_CSV);

            // Manually seed a login row to force username duplicate
            appendLegacyLoginRow(empId2, "Seed", "User", "HR Manager");

            int loginRowsAfterSeed = countDataRows(DataPaths.LOGIN_CSV);
            assertEquals("login CSV +1 due to manual seed", loginRowsBeforeUdup + 1, loginRowsAfterSeed);

            Employee e2 = buildTestEmployee(empId2, "Regular", "HR Manager");
            boolean created2 = hrOps.createEmployee(e2, TEST_PERFORMED_BY);
            assertTrue("createEmployee returns false when username already exists", !created2);

            int empRowsAfterUdup = countDataRows(DataPaths.EMPLOYEE_CSV);
            int loginRowsAfterUdup = countDataRows(DataPaths.LOGIN_CSV);

            assertEquals("employee CSV unchanged (blocked before employee write)", empRowsBeforeUdup, empRowsAfterUdup);
            assertEquals("login CSV unchanged beyond seed row", loginRowsAfterSeed, loginRowsAfterUdup);

            // ----------------------------
            // ST-05: Cache check
            // ----------------------------
            section("ST-05 CACHE CHECK");

            Employee cached = employeeService.getEmployee(empId1);
            assertNotNull("employeeService cache contains new employee", cached);

        } catch (Exception ex) {
            System.out.println("[FATAL] " + ex.getMessage());
            ex.printStackTrace();
            fail++;
        } finally {
            // Restore backups
            restoreFile(empBackup);
            restoreFile(loginBackup);
            restoreFile(sysLogBackup);

            System.out.println();
            System.out.println("==================================================");
            System.out.println("SMOKE TEST SUMMARY");
            System.out.println("PASS: " + pass);
            System.out.println("FAIL: " + fail);
            System.out.println("==================================================");
        }
    }

    // ----------------------------
    // Employee builder
    // ----------------------------
    private static Employee buildTestEmployee(int empId, String status, String position) {
        return new Employee(
                empId,
                "Smoke",
                "Test",
                LocalDate.of(2000, 1, 1),
                "N/A",
                "0000000000",
                "SSS-TEST",
                "PH-TEST",
                "TIN-TEST",
                "PAGIBIG-TEST",
                status,
                position,
                "N/A",
                0.00,
                0.00,
                0.00,
                0.00,
                0.00,
                0.00
        );
    }

    // ----------------------------
    // Utilities
    // ----------------------------
    private static void ensureDataFolderExists() {
        try {
            Files.createDirectories(Paths.get("./data"));
        } catch (Exception ignored) {
        }
    }

    private static int countDataRows(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            return 0;
        }

        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            boolean first = true;
            String line;
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                count++;
            }
        } catch (Exception e) {
            return 0;
        }
        return count;
    }

    private static int findAvailableEmpId(EmployeeRepository empRepo, UserRepository userRepo, int start) {
        int id = start;
        while (true) {
            boolean empExists = (empRepo.findById(id) != null);
            boolean userExists = (userRepo.findByUsername(String.valueOf(id)) != null);
            if (!empExists && !userExists) {
                return id;
            }
            id++;
        }
    }

    private static void appendLegacyLoginRow(int empId, String first, String last, String dept) {
        String row = String.join(",",
                String.valueOf(empId),
                "SeedPass123",
                first,
                last,
                dept,
                "No"
        );

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.LOGIN_CSV, true))) {
            bw.newLine();
            bw.write(row);
        } catch (Exception ignored) {
        }
    }

    // CsvUserRepository stores password in User.passwordHash, but no getter exists.
    // This helper keeps testing simple without modifying domain models.
    private static String getPasswordUnsafe(User u) {
        try {
            java.lang.reflect.Field f = User.class.getDeclaredField("passwordHash");
            f.setAccessible(true);
            Object val = f.get(u);
            return val == null ? "" : val.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // ----------------------------
    // Assertions
    // ----------------------------
    private static void section(String title) {
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println(title);
        System.out.println("--------------------------------------------------");
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            pass++;
            System.out.println("[PASS] " + name);
        } else {
            fail++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void assertNotNull(String name, Object obj) {
        assertTrue(name, obj != null);
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (!ok) {
            System.out.println("       Expected: " + expected);
            System.out.println("       Actual  : " + actual);
        }
        assertTrue(name, ok);
    }

    // ----------------------------
    // Backup/Restore
    // ----------------------------
    private static class FileBackup {

        final Path original;
        final Path backup;
        final boolean existed;

        FileBackup(Path original, Path backup, boolean existed) {
            this.original = original;
            this.backup = backup;
            this.existed = existed;
        }
    }

    private static FileBackup backupFile(String originalPath) {
        Path original = Paths.get(originalPath);
        boolean existed = Files.exists(original);

        Path backup = Paths.get(originalPath + ".bak_hr_smoketest");
        try {
            if (existed) {
                Files.copy(original, backup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[BACKUP] " + originalPath + " -> " + backup);
            } else {
                System.out.println("[BACKUP] Skipped (missing): " + originalPath);
            }
        } catch (Exception e) {
            System.out.println("[WARN] Backup failed for: " + originalPath + " | " + e.getMessage());
        }
        return new FileBackup(original, backup, existed);
    }

    private static void restoreFile(FileBackup fb) {
        if (fb == null) {
            return;
        }

        try {
            if (fb.existed && Files.exists(fb.backup)) {
                Files.copy(fb.backup, fb.original, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(fb.backup);
                System.out.println("[RESTORE] Restored: " + fb.original);
            } else {
                if (!fb.existed) {
                    Files.deleteIfExists(fb.original);
                }
                Files.deleteIfExists(fb.backup);
            }
        } catch (Exception e) {
            System.out.println("[WARN] Restore failed for: " + fb.original + " | " + e.getMessage());
        }
    }
}
