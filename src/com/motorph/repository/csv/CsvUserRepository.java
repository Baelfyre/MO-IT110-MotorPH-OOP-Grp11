/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy login CSV repository.
 *
 * Expected CSV format (6 columns): Username,Password,First Name, Last
 * Name,Department,Lock Out Status
 *
 * Source of path: DataPaths.LOGIN_CSV Source: DataPaths.java LOGIN_CSV constant
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

                // Skip header if present
                if (!headerChecked) {
                    headerChecked = true;
                    if (data.length > 0 && "Username".equalsIgnoreCase(data[0].trim())) {
                        continue;
                    }
                }

                // Legacy requires 6 columns
                if (data.length < 6) {
                    continue;
                }

                String fileUsername = data[0].trim();
                if (!fileUsername.equalsIgnoreCase(target)) {
                    continue;
                }

                String passwordPlain = data[1].trim();
                String department = data[4].trim();
                String lockStr = data[5].trim();

                boolean isLocked = lockStr.equalsIgnoreCase("Yes")
                        || lockStr.equalsIgnoreCase("Y")
                        || lockStr.equalsIgnoreCase("True");

                Role role = determineRoleFromDepartment(department);

                int id = safeParseInt(fileUsername, 0);

                // Note: User constructor still uses "passwordHash" field name, but in legacy mode it holds plain text
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

        // We reuse the existing User.toCsvRow() to extract username and password without needing extra getters.
        // User.toCsvRow() format is: id,username,passwordHash,role,isLocked
        String[] parts = account.toCsvRow().split(",", -1);
        if (parts.length < 5) {
            System.err.println("Cannot save user. Invalid User.toCsvRow() format.");
            return;
        }

        String username = parts[1].trim();
        String password = parts[2].trim();
        String lockOut = account.isLocked() ? "Yes" : "No";

        // Legacy CSV row format:
        // Username,Password,First Name, Last Name,Department,Lock Out Status
        String row = String.join(",",
                username,
                password,
                safeCsv(firstName),
                safeCsv(lastName),
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
    public void updatePassword(String username, String newPasswordPlain) {
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
                    lines.add(line); // keep header as is
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
                    // Legacy password column is index 1
                    data[1] = newPasswordPlain == null ? "" : newPasswordPlain.trim();
                    line = String.join(",", data);
                }

                lines.add(line);
            }

        } catch (IOException e) {
            System.err.println("Error reading legacy login file for password update: " + e.getMessage());
            return;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String l : lines) {
                out.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error rewriting legacy login file for password update: " + e.getMessage());
        }
    }

    @Override
    public void updateLockStatus(String username, boolean isLocked) {
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
                    lines.add(line); // keep header as is
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
                    // Legacy lock status column is index 5: Yes / No
                    data[5] = isLocked ? "Yes" : "No";
                    line = String.join(",", data);
                }

                lines.add(line);
            }

        } catch (IOException e) {
            System.err.println("Error reading legacy login file for lock update: " + e.getMessage());
            return;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String l : lines) {
                out.println(l);
            }
        } catch (IOException e) {
            System.err.println("Error rewriting legacy login file for lock update: " + e.getMessage());
        }
    }

    private Role determineRoleFromDepartment(String department) {
        if (department == null) {
            return Role.EMPLOYEE;
        }

        String d = department.trim().toUpperCase();

        // Matches your existing role intent for HR and Payroll related departments.
        // Source reference for similar mapping: EmployeeService.determineRoleFromPosition(...)
        if (d.contains("HR")) {
            return Role.HR;
        }
        if (d.contains("PAYROLL") || d.contains("FINANCE") || d.contains("ACCOUNTING")) {
            return Role.PAYROLL;
        }
        if (d.equals("IT OPERATIONS AND SYSTEMS") || d.contains("IT ")) {
            return Role.IT;
        }

        return Role.EMPLOYEE;
    }

    private int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String safeCsv(String value) {
        // Your data does not currently contain commas in these fields.
        // Source: data_Legacy_LogIn.csv sample rows
        return value == null ? "" : value.trim();
    }

    @Override
    public boolean deleteByUsername(String username) {
        if (username == null) {
            return false;
        }

        java.nio.file.Path path = java.nio.file.Paths.get(com.motorph.repository.csv.DataPaths.LOGIN_CSV);

        if (!java.nio.file.Files.exists(path)) {
            return false;
        }

        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(path);
            if (lines.isEmpty()) {
                return false;
            }

            String header = lines.get(0);
            java.util.List<String> out = new java.util.ArrayList<>();
            out.add(header);

            boolean removed = false;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length == 0) {
                    continue;
                }

                String rowUsername = unquote(parts[0]).trim();
                if (rowUsername.equals(username.trim())) {
                    removed = true;
                    continue;
                }

                out.add(line);
            }

            if (!removed) {
                return false;
            }

            java.nio.file.Files.write(
                    path,
                    out,
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                    java.nio.file.StandardOpenOption.CREATE
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private String unquote(String s) {
        if (s == null) {
            return "";
        }
        String v = s.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1);
            v = v.replace("\"\"", "\"");
        }
        return v;
    }

}
