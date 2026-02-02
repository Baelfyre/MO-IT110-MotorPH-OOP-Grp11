/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.Employee;
import com.motorph.repository.EmployeeRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV-backed implementation of EmployeeRepository. Reads and appends rows for
 * the employee master dataset.
 *
 * @author ACER
 */
public class CsvEmployeeRepository implements EmployeeRepository {

    private static final String FILE_PATH = DataPaths.EMPLOYEE_CSV;
    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            br.readLine(); // header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);

                // Accept:
                // - 19 columns (new)
                // - 20 columns (legacy, last column is leaveCredits)
                if (data.length == 19 || data.length == 20) {
                    Employee emp = parseEmployee(data);
                    if (emp != null) {
                        employees.add(emp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return employees;
    }

    @Override
    public Employee findById(int id) {
        for (Employee emp : findAll()) {
            if (emp.getId() == id) {
                return emp;
            }
        }
        return null;
    }

    @Override
    public void create(Employee emp) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.newLine();
            bw.write(emp.toCsvRow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean update(Employee emp) {
        if (emp == null) {
            return false;
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String header = br.readLine();
            if (header == null) {
                return false;
            }
            lines.add(header);

            String line;
            boolean updated = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length < 1) {
                    continue;
                }

                int rowId;
                try {
                    rowId = Integer.parseInt(clean(data[0]));
                } catch (NumberFormatException ex) {
                    lines.add(line);
                    continue;
                }

                if (rowId == emp.getId()) {
                    lines.add(emp.toCsvRow());
                    updated = true;
                } else {
                    lines.add(line);
                }
            }

            if (!updated) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) {
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean delete(int empId) {
        java.nio.file.Path path = java.nio.file.Paths.get(com.motorph.repository.csv.DataPaths.EMPLOYEE_CSV);

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

                // Employee # is the first CSV column and is numeric in this project
                String[] parts = line.split(",", -1);
                if (parts.length == 0) {
                    continue;
                }

                int rowEmpId;
                try {
                    rowEmpId = Integer.parseInt(unquote(parts[0]).trim());
                } catch (NumberFormatException ex) {
                    // keep malformed rows instead of deleting anything unexpectedly
                    out.add(line);
                    continue;
                }

                if (rowEmpId == empId) {
                    removed = true;
                    continue; // skip this row
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

    private Employee parseEmployee(String[] data) {
        try {
            int id = Integer.parseInt(clean(data[0]));
            String last = clean(data[1]);
            String first = clean(data[2]);

            String bdayRaw = clean(data[3]);
            LocalDate bday = bdayRaw.isEmpty() ? null : LocalDate.parse(bdayRaw, DATE_FMT);

            String address = clean(data[4]);
            String phone = clean(data[5]);

            String sss = clean(data[6]);
            String phil = clean(data[7]);
            String tin = clean(data[8]);
            String pagibig = clean(data[9]);

            String status = clean(data[10]);
            String pos = clean(data[11]);
            String supervisor = clean(data[12]);

            double basic = parseCurrency(data[13]);
            double rice = parseCurrency(data[14]);
            double phoneAllow = parseCurrency(data[15]);
            double clothing = parseCurrency(data[16]);
            double grossSemi = parseCurrency(data[17]);
            double hourly = parseCurrency(data[18]);

            // data[19] is legacy leaveCredits, intentionally ignored
            return new Employee(
                    id, last, first, bday,
                    address, phone,
                    sss, phil, tin, pagibig,
                    status, pos, supervisor,
                    basic, rice, phoneAllow, clothing,
                    grossSemi, hourly
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String input) {
        return input == null ? "" : input.replace("\"", "").trim();
    }

    private double parseCurrency(String input) {
        try {
            return Double.parseDouble(
                    (input == null ? "" : input).replace("\"", "").replace(",", "").trim()
            );
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
