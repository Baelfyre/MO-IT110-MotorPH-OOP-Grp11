/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.EmployeeProfile;
import com.motorph.repository.TeamRepository;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles retrieving a list of employees under a specific supervisor.
 *
 * @author ACER
 */
public class CsvTeamRepository implements TeamRepository {

    // Matches your CSV date format
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    public List<EmployeeProfile> findSubordinates(String supervisorName) {
        List<EmployeeProfile> team = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.EMPLOYEE_CSV))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                // 1. Safe Split (Handles "Garcia, Manuel")
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // 2. Check Supervisor (Column 12)
                // Ensure we have enough columns to check supervisor
                if (data.length > 12) {
                    String supervisorInFile = clean(data[12]);

                    // Case-insensitive match (e.g., "lim, antonio" vs "Lim, Antonio")
                    if (supervisorInFile.equalsIgnoreCase(supervisorName)) {

                        try {
                            // 3. Parse Data (Reuse the logic from EmployeeRepository)
                            int id = Integer.parseInt(data[0].trim());
                            String last = clean(data[1]);
                            String first = clean(data[2]);

                            // Handle potential date parsing errors safely
                            LocalDate bday = null;
                            try {
                                bday = LocalDate.parse(clean(data[3]), DATE_FMT);
                            } catch (Exception e) {
                            }

                            String address = clean(data[4]);
                            String phone = clean(data[5]);
                            String sss = clean(data[6]);
                            String phil = clean(data[7]);
                            String tin = clean(data[8]);
                            String pagibig = clean(data[9]);
                            String status = clean(data[10]);
                            String pos = clean(data[11]);
                            String supervisor = clean(data[12]);

                            // Numbers (Safe Parse)
                            double basic = parseCurrency(data[13]);
                            double rice = parseCurrency(data[14]);
                            double phoneAllow = parseCurrency(data[15]);
                            double clothing = parseCurrency(data[16]);
                            double grossSemi = parseCurrency(data[17]);
                            double hourly = parseCurrency(data[18]);

                            // 4. Create Object using the CONSTRUCTOR (Not Setters)
                            EmployeeProfile emp = new EmployeeProfile(
                                    id, last, first, bday, address, phone, sss, phil,
                                    tin, pagibig, status, pos, supervisor,
                                    basic, rice, phoneAllow, clothing, grossSemi, hourly
                            );

                            team.add(emp);

                        } catch (Exception e) {
                            // Skip rows that are corrupted
                            System.err.println("Skipping invalid row in Team search: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return team;
    }

    // --- Reuse these Helpers to prevent crashes ---
    private String clean(String input) {
        return input.replaceAll("\"", "").trim();
    }

    private double parseCurrency(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(input.replaceAll("\"", "").replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
