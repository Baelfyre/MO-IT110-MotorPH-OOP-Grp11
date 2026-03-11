/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.Employee;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.TimeEntryRepository;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * CSV-backed DTR repository for records_dtr_{empId}.csv. Format:
 * Attendance_ID,Employee #,Date,Log In,Log Out,First Name,Last Name
 *
 * @author ACER
 *
 * When multiple rows exist for the same date, the latest row in file order is
 * treated as the active snapshot.
 */
public class CsvTimeRepository implements TimeEntryRepository {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private static final String FILE_PREFIX = "records_dtr_";
    private static final String FILE_SUFFIX = ".csv";

    private static final String HEADER
            = "Attendance_ID,Employee #,Date,Log In,Log Out,First Name,Last Name";

    private static final int IDX_ATT_ID = 0;
    private static final int IDX_EMP_ID = 1;
    private static final int IDX_DATE = 2;
    private static final int IDX_IN = 3;
    private static final int IDX_OUT = 4;
    private static final int IDX_FIRST = 5;
    private static final int IDX_LAST = 6;

    private final EmployeeRepository empRepo; // optional dependency for name columns

    public CsvTimeRepository() {
        this.empRepo = null;
    }

    public CsvTimeRepository(EmployeeRepository empRepo) {
        this.empRepo = empRepo;
    }

    @Override
    public boolean saveEntry(int empId, TimeEntry entry) {
        if (entry == null || entry.getDate() == null) {
            return false;
        }

        ensureFolder(DataPaths.DTR_FOLDER);

        File file = new File(DataPaths.DTR_FOLDER + FILE_PREFIX + empId + FILE_SUFFIX);
        ensureHeader(file);

        String attendanceId = buildAttendanceId(empId, entry.getDate());

        List<String> lines = readAllLines(file);
        if (lines.isEmpty()) {
            lines.add(HEADER);
        }

        boolean updated = false;

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) {
                continue;
            }

            String[] d = line.split(CSV_SPLIT_REGEX, -1);
            if (d.length < 7) {
                continue;
            }

            String existingId = clean(d[IDX_ATT_ID]);
            if (!attendanceId.equals(existingId)) {
                continue;
            }

            // Existing row found: merge values without losing earlier saved values.
            LocalTime existingIn = parseTime(clean(d[IDX_IN]));
            LocalTime existingOut = parseTime(clean(d[IDX_OUT]));

            LocalTime finalIn = (entry.getTimeIn() != null) ? entry.getTimeIn() : existingIn;
            LocalTime finalOut = (entry.getTimeOut() != null) ? entry.getTimeOut() : existingOut;

            String firstName = clean(d[IDX_FIRST]);
            String lastName = clean(d[IDX_LAST]);

            // Name fallback uses employee master if available.
            if ((firstName.isEmpty() || lastName.isEmpty()) && empRepo != null) {
                Employee emp = empRepo.findById(empId);
                if (emp != null) {
                    if (firstName.isEmpty()) {
                        firstName = emp.getFirstName();
                    }
                    if (lastName.isEmpty()) {
                        lastName = emp.getLastName();
                    }
                }
            }

            lines.set(i, buildRow(attendanceId, empId, entry.getDate(), finalIn, finalOut, firstName, lastName));
            updated = true;
            break;
        }

        if (!updated) {
            String firstName = "";
            String lastName = "";

            if (empRepo != null) {
                Employee emp = empRepo.findById(empId);
                if (emp != null) {
                    firstName = emp.getFirstName();
                    lastName = emp.getLastName();
                }
            }

            lines.add(buildRow(attendanceId, empId, entry.getDate(), entry.getTimeIn(), entry.getTimeOut(), firstName, lastName));
        }

        return writeAllLines(file, lines);
    }

    @Override
    public List<TimeEntry> getEntries(int empId) {
        return readFromFile(empId, null);
    }

    @Override
    public List<TimeEntry> findByEmployeeAndPeriod(int empId, PayPeriod period) {
        return readFromFile(empId, period);
    }

    private List<TimeEntry> readFromFile(int empId, PayPeriod period) {
        File file = new File(DataPaths.DTR_FOLDER + FILE_PREFIX + empId + FILE_SUFFIX);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        // Dedupe by Attendance_ID; last occurrence wins (supports older append-based files).
        Map<String, TimeEntry> byId = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // header excluded during parsing

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] d = line.split(CSV_SPLIT_REGEX, -1);
                if (d.length < 7) {
                    continue;
                }

                String attId = clean(d[IDX_ATT_ID]);
                LocalDate date = parseDate(clean(d[IDX_DATE]));
                if (date == null) {
                    continue;
                }

                if (period != null && !period.includes(date)) {
                    continue;
                }

                LocalTime in = parseTime(clean(d[IDX_IN]));
                LocalTime out = parseTime(clean(d[IDX_OUT]));

                // TimeIn is required for a valid workday record.
                if (in == null) {
                    continue;
                }

                byId.put(attId, new TimeEntry(date, in, out));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        return new ArrayList<>(byId.values());
    }

    private String buildRow(String attendanceId, int empId, LocalDate date,
            LocalTime in, LocalTime out,
            String firstName, String lastName) {
        return escape(attendanceId) + ","
                + empId + ","
                + escape(date.format(DATE_FMT)) + ","
                + escape(in == null ? "" : in.format(TIME_FMT)) + ","
                + escape(out == null ? "" : out.format(TIME_FMT)) + ","
                + escape(firstName) + ","
                + escape(lastName);
    }

    private String buildAttendanceId(int empId, LocalDate date) {
        long excelSerial = ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), date);
        return empId + "-" + excelSerial;
    }

    private void ensureFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private void ensureHeader(File file) {
        if (!file.exists()) {
            writeHeader(file);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first == null || first.trim().isEmpty()) {
                writeHeader(file);
            }
        } catch (Exception e) {
            writeHeader(file);
        }
    }

    private void writeHeader(File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write(HEADER);
        } catch (Exception e) {
            // file creation failure remains non-blocking
        }
    }

    private List<String> readAllLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String s;
            while ((s = br.readLine()) != null) {
                lines.add(s);
            }
        } catch (Exception e) {
            return lines;
        }
        return lines;
    }

    private boolean writeAllLines(File file, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) {
                    bw.newLine();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(raw.trim(), TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String input) {
        return input == null ? "" : input.replace("\"", "").trim();
    }

    private String escape(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
