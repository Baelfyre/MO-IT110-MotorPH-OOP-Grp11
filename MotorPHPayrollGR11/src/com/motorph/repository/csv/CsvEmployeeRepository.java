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

/**
 * Handles reading/writing of data_Employee.csv
 *
 * @author ACER
 */
public class CsvEmployeeRepository implements EmployeeRepository {

    // Matches your CSV date format: 10/11/1983
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public Employee findByEmployeeNumber(int employeeNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.EMPLOYEE_CSV))) {
            String line;
            br.readLine(); // Skip Header

            while ((line = br.readLine()) != null) {
                // Regex split handles commas inside quotes (e.g., "Garcia, Manuel")
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Ensure we have enough columns (Allow 18 or 19 depending on trailing commas)
                if (data.length >= 19) {
                    try {
                        int id = Integer.parseInt(data[0].trim());

                        if (id == employeeNumber) {
                            // Extract String Data (removing extra quotes if present)
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

                            // Extract Numbers (Handle "90,000" format)
                            double basic = parseCurrency(data[13]);
                            double rice = parseCurrency(data[14]);
                            double phoneAllow = parseCurrency(data[15]);
                            double clothing = parseCurrency(data[16]);
                            double grossSemi = parseCurrency(data[17]);
                            double hourly = parseCurrency(data[18]);

                            return new Employee(id, last, first, bday, address, phone, sss, phil,
                                    tin, pagibig, status, pos, supervisor,
                                    basic, rice, phoneAllow, clothing, grossSemi, hourly);
                        }
                    } catch (Exception e) {
                        // Skip rows with bad data
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void create(Employee emp) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.EMPLOYEE_CSV, true))) {
            String bdayStr = (emp.getBirthday() != null) ? emp.getBirthday().format(DATE_FMT) : "";

            // Format numbers nicely (e.g., "90,000.00") requires quoting in CSV to avoid breaking structure
            // We wrap fields in quotes just to be safe and match the existing file format.
            String line = String.format("%d,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,\"%s\",\"%.2f\",\"%.2f\",\"%.2f\",\"%.2f\",\"%.2f\",%.2f",
                    emp.getEmployeeNumber(),
                    emp.getLastName(),
                    emp.getFirstName(),
                    bdayStr,
                    emp.getAddress(),
                    emp.getPhoneNumber(),
                    emp.getSssNumber(),
                    emp.getPhilHealthNumber(),
                    emp.getTinNumber(),
                    emp.getPagIbigNumber(),
                    emp.getStatus(),
                    emp.getPosition(),
                    emp.getImmediateSupervisor(),
                    emp.getBasicSalary(),
                    emp.getriceAllowance(),
                    emp.getPhoneAllowance(),
                    emp.getClothingAllowance(),
                    emp.getGrossSemiMonthlyRate(),
                    emp.getHourlyRate()
            );

            bw.write(System.lineSeparator()); // Move to new line
            bw.write(line);
            System.out.println("Employee " + emp.getEmployeeNumber() + " saved.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPERS ---
    /**
     * Removes quotes from CSV strings. e.g., "Valero Street" -> Valero Street
     */
    private String clean(String input) {
        return input.replaceAll("\"", "").trim();
    }

    /**
     * Parses numbers that might have commas or quotes. e.g., "90,000" ->
     * 90000.0
     */
    private double parseCurrency(String input) {
        try {
            // Remove quotes and commas
            String cleanStr = input.replaceAll("\"", "").replaceAll(",", "").trim();
            return Double.parseDouble(cleanStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
