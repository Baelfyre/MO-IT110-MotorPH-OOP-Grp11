/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.LeaveCredits;
import com.motorph.repository.LeaveCreditsRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * CSV-backed implementation of LeaveCreditsRepository. Reads employee leave
 * credits and cumulative leave taken in hours.
 *
 * Expected header columns: Employee #, Last Name, First Name, Leave Credits,
 * Leave Taken
 *
 * The parser tolerates extra empty trailing columns.
 *
 * @author ACER
 */
public class CsvLeaveCreditsRepository implements LeaveCreditsRepository {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    @Override
    public List<LeaveCredits> findAll() {
        List<LeaveCredits> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.LEAVE_CREDITS_CSV))) {
            String header = br.readLine();
            if (header == null) {
                return list;
            }

            HeaderIndex idx = resolveHeaderIndex(header);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);

                LeaveCredits record = parseRow(data, idx);
                if (record != null) {
                    list.add(record);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public LeaveCredits findByEmpId(int empId) {
        for (LeaveCredits lc : findAll()) {
            if (lc.getEmployeeNumber() == empId) {
                return lc;
            }
        }
        return null;
    }

    @Override
    public boolean updateLeaveTaken(int empId, double leaveTakenHours) {
        List<LeaveCredits> all = findAll();
        if (all.isEmpty()) {
            return false;
        }

        boolean updated = false;
        List<LeaveCredits> rewritten = new ArrayList<>();

        for (LeaveCredits lc : all) {
            if (lc.getEmployeeNumber() == empId) {
                rewritten.add(new LeaveCredits(
                        lc.getEmployeeNumber(),
                        lc.getLastName(),
                        lc.getFirstName(),
                        lc.getLeaveCreditsHours(),
                        leaveTakenHours
                ));
                updated = true;
            } else {
                rewritten.add(lc);
            }
        }

        if (!updated) {
            return false;
        }

        return writeAll(rewritten);
    }

    private LeaveCredits parseRow(String[] data, HeaderIndex idx) {
        try {
            int empId = (int) parseNumber(get(data, idx.empId));
            String last = clean(get(data, idx.lastName));
            String first = clean(get(data, idx.firstName));

            double credits = parseNumber(get(data, idx.leaveCredits));

            double taken = 0.0;
            if (idx.leaveTaken >= 0) {
                taken = parseNumber(get(data, idx.leaveTaken));
            }

            return new LeaveCredits(empId, last, first, credits, taken);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean writeAll(List<LeaveCredits> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DataPaths.LEAVE_CREDITS_CSV, false))) {
            bw.write("Employee #,Last Name,First Name,Leave Credits,Leave Taken");
            bw.newLine();

            for (LeaveCredits lc : list) {
                String row = lc.getEmployeeNumber() + ","
                        + escape(lc.getLastName()) + ","
                        + escape(lc.getFirstName()) + ","
                        + formatHours(lc.getLeaveCreditsHours()) + ","
                        + formatHours(lc.getLeaveTakenHours());
                bw.write(row);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private HeaderIndex resolveHeaderIndex(String headerLine) {
        String[] cols = headerLine.split(CSV_SPLIT_REGEX, -1);
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < cols.length; i++) {
            String key = normalize(cols[i]);
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }

        int empId = map.getOrDefault("employee #", 0);
        int last = map.getOrDefault("last name", 1);
        int first = map.getOrDefault("first name", 2);
        int credits = map.getOrDefault("leave credits", 3);
        int taken = map.getOrDefault("leave taken", -1);

        return new HeaderIndex(empId, last, first, credits, taken);
    }

    private String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\"", "").trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String get(String[] data, int idx) {
        if (idx < 0 || idx >= data.length) {
            return "";
        }
        return data[idx];
    }

    private String clean(String input) {
        return input == null ? "" : input.replace("\"", "").trim();
    }

    private double parseNumber(String input) {
        String v = clean(input).replace(",", "");
        if (v.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(v);
    }

    private String escape(String data) {
        if (data == null) {
            return "";
        }
        if (data.contains(",")) {
            return "\"" + data + "\"";
        }
        return data;
    }

    private String formatHours(double hours) {
        return String.format("%.2f", hours);
    }

    private static class HeaderIndex {

        final int empId;
        final int lastName;
        final int firstName;
        final int leaveCredits;
        final int leaveTaken;

        HeaderIndex(int empId, int lastName, int firstName, int leaveCredits, int leaveTaken) {
            this.empId = empId;
            this.lastName = lastName;
            this.firstName = firstName;
            this.leaveCredits = leaveCredits;
            this.leaveTaken = leaveTaken;
        }
    }
}
