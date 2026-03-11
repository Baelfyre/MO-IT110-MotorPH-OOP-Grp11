/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV repository for legacy login accounts.
 *
 * Expected CSV format:
 * Username,Password,Employee_ID,Employee_Name,Department,Lock_Out_Status
 */
public class CsvUserRepository implements UserRepository {

    private static final String FILE_PATH = DataPaths.LOGIN_CSV;

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String target = username.trim();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean headerChecked = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (!headerChecked) {
                    headerChecked = true;
                    if (data.length > 0 && "Username".equalsIgnoreCase(data[0].trim())) {
                        continue;
                    }
                }

                if (data.length < 6) {
                    continue;
                }

                String fileUsername = data[0].trim();
                if (!fileUsername.equalsIgnoreCase(target)) {
                    continue;
                }

                String passwordPlain = data[1].trim();
                int id = safeParseInt(data[2].trim(), safeParseInt(fileUsername, 0));
                Role role = determineRoleFromDepartment(data[4].trim());
                boolean isLocked = parseLocked(data[5].trim());

                return new User(id, fileUsername, passwordPlain, role, isLocked);
            }
        } catch (IOException e) {
            System.err.println("Error reading legacy login file: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void save(User account, String firstName, String lastName, String dept) {
        if (account == null) {
            return;
        }

        String username = safeCsv(account.getUsername());
        String password = safeCsv(account.getPassword());
        String employeeId = String.valueOf(account.getId() > 0 ? account.getId() : safeParseInt(username, 0));
        String employeeName = buildEmployeeName(firstName, lastName);
        String lockOut = account.isLocked() ? "Yes" : "No";

        String row = String.join(",",
                username,
                password,
                safeCsv(employeeId),
                safeCsv(employeeName),
                safeCsv(dept),
                lockOut
        );

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
            out.println(row);
        } catch (IOException e) {
            System.err.println("Error saving legacy user: " + e.getMessage());
        }
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        rewriteSingleUserField(username, 1, newPassword == null ? "" : newPassword.trim());
    }

    @Override
    public void updateLockStatus(String username, boolean isLocked) {
        rewriteSingleUserField(username, 5, isLocked ? "Yes" : "No");
    }

    @Override
    public boolean deleteByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        List<String> out = new ArrayList<>();
        boolean removed = false;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean headerHandled = false;

            while ((line = br.readLine()) != null) {
                if (!headerHandled) {
                    headerHandled = true;
                    out.add(line);
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", -1);
                if (data.length < 6) {
                    out.add(line);
                    continue;
                }

                if (data[0].trim().equalsIgnoreCase(username.trim())) {
                    removed = true;
                    continue;
                }

                out.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error deleting legacy user: " + e.getMessage());
            return false;
        }

        if (!removed) {
            return false;
        }

        try (PrintWriter outWriter = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String row : out) {
                outWriter.println(row);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error rewriting legacy login file after delete: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean headerChecked = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (!headerChecked) {
                    headerChecked = true;
                    if (data.length > 0 && "Username".equalsIgnoreCase(data[0].trim())) {
                        continue;
                    }
                }

                if (data.length < 6) {
                    continue;
                }

                String username = data[0].trim();
                String passwordPlain = data[1].trim();
                int id = safeParseInt(data[2].trim(), safeParseInt(username, 0));
                Role role = determineRoleFromDepartment(data[4].trim());
                boolean isLocked = parseLocked(data[5].trim());

                users.add(new User(id, username, passwordPlain, role, isLocked));
            }
        } catch (IOException e) {
            System.err.println("Error reading all legacy users: " + e.getMessage());
        }

        return users;
    }

    private void rewriteSingleUserField(String username, int columnIndex, String newValue) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean headerHandled = false;

            while ((line = br.readLine()) != null) {
                if (!headerHandled) {
                    headerHandled = true;
                    lines.add(line);
                    continue;
                }

                if (line.trim().isEmpty()) {
                    lines.add(line);
                    continue;
                }

                String[] data = line.split(",", -1);
                if (data.length < 6) {
                    lines.add(line);
                    continue;
                }

                if (data[0].trim().equalsIgnoreCase(username.trim())) {
                    data[columnIndex] = newValue;
                    line = String.join(",", data);
                }

                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading legacy login file for update: " + e.getMessage());
            return;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String row : lines) {
                out.println(row);
            }
        } catch (IOException e) {
            System.err.println("Error rewriting legacy login file: " + e.getMessage());
        }
    }

    private boolean parseLocked(String lockStr) {
        return lockStr.equalsIgnoreCase("Yes")
                || lockStr.equalsIgnoreCase("Y")
                || lockStr.equalsIgnoreCase("True");
    }

    private Role determineRoleFromDepartment(String department) {
        if (department == null) {
            return Role.EMPLOYEE;
        }

        String dept = department.trim().toUpperCase();
        if (dept.contains("HR")) {
            return Role.HR;
        }
        if (dept.contains("PAYROLL") || dept.contains("FINANCE") || dept.contains("ACCOUNTING") || dept.contains("CHIEF FINANCE OFFICER")) {
            return Role.PAYROLL;
        }
        if (dept.contains("IT")) {
            return Role.IT;
        }
        if (dept.contains("CHIEF") || dept.contains("CEO") || dept.contains("COO") || dept.contains("CMO") || dept.contains("TEAM LEADER") || dept.contains("MANAGER") || dept.contains("HEAD")) {
            return Role.SUPERVISOR;
        }
        return Role.EMPLOYEE;
    }

    private int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String buildEmployeeName(String firstName, String lastName) {
        String fn = firstName == null ? "" : firstName.trim();
        String ln = lastName == null ? "" : lastName.trim();
        String full = (fn + " " + ln).trim();
        return full.isEmpty() ? "N/A" : full;
    }

    private String safeCsv(String value) {
        return value == null ? "" : value.trim();
    }
}
