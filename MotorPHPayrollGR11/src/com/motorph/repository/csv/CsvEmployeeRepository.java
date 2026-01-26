/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.Employee;
import com.motorph.repository.EmployeeRepository;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading/writing of data_Employee.csv Implements the "How" for the
 * EmployeeRepository interface.
 *
 * @author ACER
 */
public class CsvEmployeeRepository implements EmployeeRepository {

    private static final String FILE_PATH = "src/com/motorph/resources/data_Employee.csv";
    // M/d/yyyy handles both 10/11/1983 and 6/19/1988
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            br.readLine(); // Skip Header

            while ((line = br.readLine()) != null) {
                // Regex splits by comma unless inside quotes
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Now checking for 20 columns (0 to 19)
                if (data.length >= 20) { 
                    try {
                        Employee emp = parseEmployee(data);
                        if (emp != null) employees.add(emp);
                    } catch (Exception e) {
                        System.err.println("Skipping invalid row: " + data[0]);
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
            if (emp.getId() == id) return emp;
        }
        return null;
    }

    @Override
    public void create(Employee emp) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(System.lineSeparator());
            bw.write(emp.toCsvRow());
            System.out.println("Employee " + emp.getEmployeeNumber() + " saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Employee parseEmployee(String[] data) {
        try {
            int id = Integer.parseInt(data[0].trim());
            String last = clean(data[1]);
            String first = clean(data[2]);
            LocalDate bday = LocalDate.parse(clean(data[3]), DATE_FMT);
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
            
            // New Column 20
            int leaveCredits = Integer.parseInt(data[19].trim());

            return new Employee(id, last, first, bday, address, phone, sss, phil,
                    tin, pagibig, status, pos, supervisor,
                    basic, rice, phoneAllow, clothing, grossSemi, hourly, leaveCredits);
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String input) {
        return input.replaceAll("\"", "").trim();
    }

    private double parseCurrency(String input) {
        try {
            return Double.parseDouble(input.replaceAll("\"", "").replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}