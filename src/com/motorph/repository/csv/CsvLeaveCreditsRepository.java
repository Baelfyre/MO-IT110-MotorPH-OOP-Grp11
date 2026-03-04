package com.motorph.repository.csv;

import com.motorph.domain.models.LeaveCredits;
import com.motorph.repository.LeaveCreditsRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public class CsvLeaveCreditsRepository implements LeaveCreditsRepository {

    private static final String FILE_PATH = DataPaths.LEAVE_CREDITS_CSV;

    @Override
    public List<LeaveCredits> findAll() {
        List<LeaveCredits> out = new ArrayList<>();

        Path p = Paths.get(FILE_PATH);
        if (!Files.exists(p)) {
            return out;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH, StandardCharsets.UTF_8))) {
            String line;
            boolean headerChecked = false;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] d = line.split(",", -1);

                if (!headerChecked) {
                    headerChecked = true;
                    if (d.length > 0 && d[0].trim().toLowerCase().startsWith("employee")) {
                        continue;
                    }
                }

                if (d.length < 4) {
                    continue;
                }

                int empId = safeParseInt(d[0], 0);
                String last = safe(d, 1);
                String first = safe(d, 2);
                double credits = safeParseDouble(d[3], 0.0);
                double taken = (d.length >= 5) ? safeParseDouble(d[4], 0.0) : 0.0;

                if (empId <= 0) {
                    continue;
                }

                out.add(new LeaveCredits(empId, last, first, credits, taken));
            }

        } catch (Exception e) {
            return out;
        }

        return out;
    }

    @Override
    public LeaveCredits findByEmpId(int empId) {
        for (LeaveCredits c : findAll()) {
            if (c.getEmployeeNumber() == empId) {
                return c;
            }
        }
        return null;
    }

    @Override
    public boolean updateLeaveTaken(int empId, double leaveTakenHours) {
        Path p = Paths.get(FILE_PATH);
        if (!Files.exists(p)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return false;
            }

            String header = lines.get(0);
            List<String> out = new ArrayList<>();
            out.add(header);

            boolean updated = false;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String[] d = line.split(",", -1);
                if (d.length < 4) {
                    out.add(line);
                    continue;
                }

                int rowId = safeParseInt(d[0], -1);
                if (rowId == empId) {
                    String takenStr = formatDouble(Math.max(0.0, leaveTakenHours));

                    // Ensure 5 columns
                    if (d.length < 5) {
                        String[] nd = new String[5];
                        nd[0] = safe(d, 0);
                        nd[1] = safe(d, 1);
                        nd[2] = safe(d, 2);
                        nd[3] = safe(d, 3);
                        nd[4] = takenStr;
                        out.add(String.join(",", nd));
                    } else {
                        d[4] = takenStr;
                        out.add(String.join(",", d));
                    }

                    updated = true;
                    continue;
                }

                out.add(line);
            }

            if (!updated) {
                return false;
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, StandardCharsets.UTF_8, false))) {
                for (String l : out) {
                    pw.println(l);
                }
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private String safe(String[] d, int idx) {
        if (d == null || idx < 0 || idx >= d.length) {
            return "";
        }
        return d[idx] == null ? "" : d[idx].trim();
    }

    private int safeParseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private double safeParseDouble(String raw, double fallback) {
        try {
            String v = raw == null ? "" : raw.trim();
            if (v.isEmpty()) {
                return fallback;
            }
            return Double.parseDouble(v.replace(",", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String formatDouble(double v) {
        long rounded = Math.round(v);
        if (Math.abs(v - rounded) < 0.0000001) {
            return Long.toString(rounded);
        }
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
