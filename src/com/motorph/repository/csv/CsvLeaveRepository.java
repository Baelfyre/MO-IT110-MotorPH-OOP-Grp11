/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveRepository;

import java.io.*;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * CSV-backed leave repository for records_leave_{empId}.csv.
 *
 * Columns (11): Leave_ID, Employee #, Date, Start_Time, End_Time, First Name,
 * Last Name, Status, Reviewed_By, Reviewed_At, Decision_Note
 *
 * @author ACER
 */
public class CsvLeaveRepository implements LeaveRepository {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private static final String FILE_PREFIX = "records_leave_";
    private static final String FILE_SUFFIX = ".csv";

    private static final String HEADER = String.join(",",
            "Leave_ID",
            "Employee #",
            "Date",
            "Start_Time",
            "End_Time",
            "First Name",
            "Last Name",
            "Status",
            "Reviewed_By",
            "Reviewed_At",
            "Decision_Note"
    );

    @Override
    public List<LeaveRequest> findByEmployee(int empId) {
        return read(empId, null);
    }

    @Override
    public List<LeaveRequest> findByEmployeeAndPeriod(int empId, PayPeriod period) {
        return read(empId, period);
    }

    @Override
    public double getLeaveHoursUsed(int empId, PayPeriod period) {
        if (period == null) {
            return 0.0;
        }

        List<LeaveRequest> rows = findByEmployeeAndPeriod(empId, period);
        Set<String> seen = new HashSet<>();

        double total = 0.0;

        for (LeaveRequest r : rows) {
            if (r == null || r.getDate() == null) {
                continue;
            }

            String leaveId = r.getLeaveId() == null ? "" : r.getLeaveId().trim();
            if (leaveId.isEmpty() || !seen.add(leaveId)) {
                continue;
            }

            // Annotation: Weekends are excluded from leave usage.
            DayOfWeek dow = r.getDate().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                continue;
            }

            total += calculateHours(r.getStartTime(), r.getEndTime());
        }

        return total;
    }


    @Override
    public boolean updateDecision(int empId, String leaveId, LeaveStatus status, int reviewedBy, String reviewedAt, String note) {
        if (leaveId == null || leaveId.trim().isEmpty() || status == null) {
            return false;
        }

        File file = new File(DataPaths.LEAVE_FOLDER + FILE_PREFIX + empId + FILE_SUFFIX);
        if (!file.exists()) {
            return false;
        }

        List<LeaveRequest> rows = read(empId, null);
        boolean updated = false;
        List<LeaveRequest> out = new ArrayList<>();

        for (LeaveRequest r : rows) {
            if (!updated && leaveId.equalsIgnoreCase(r.getLeaveId())) {
                LeaveRequest revised = new LeaveRequest(
                        r.getLeaveId(),
                        r.getEmployeeId(),
                        r.getDate(),
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getFirstName(),
                        r.getLastName(),
                        status,
                        reviewedBy,
                        reviewedAt == null ? "" : reviewedAt,
                        note == null ? "" : note
                );
                out.add(revised);
                updated = true;
            } else {
                out.add(r);
            }
        }

        if (!updated) {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write(HEADER);
            for (LeaveRequest r : out) {
                bw.newLine();
                bw.write(r.toCsvRow());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean create(LeaveRequest request) {
        if (request == null) {
            return false;
        }

        ensureFolder(DataPaths.LEAVE_FOLDER);
        File file = new File(DataPaths.LEAVE_FOLDER + FILE_PREFIX + request.getEmployeeId() + FILE_SUFFIX);
        ensureHeader(file);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.newLine();
            bw.write(request.toCsvRow());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<LeaveRequest> read(int empId, PayPeriod period) {
        File file = new File(DataPaths.LEAVE_FOLDER + FILE_PREFIX + empId + FILE_SUFFIX);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<LeaveRequest> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String first = br.readLine();
            if (first == null) {
                return out;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] d = line.split(CSV_SPLIT_REGEX, -1);
                if (d.length < 7) {
                    continue;
                }

                String leaveId = clean(d, 0);
                int eId = parseInt(clean(d, 1), empId);
                LocalDate date = parseDate(clean(d, 2));
                LocalTime start = parseTime(clean(d, 3));
                LocalTime end = parseTime(clean(d, 4));
                String firstName = clean(d, 5);
                String lastName = clean(d, 6);

                LeaveStatus status = (d.length >= 8) ? LeaveStatus.fromCsv(clean(d, 7)) : LeaveStatus.PENDING;
                Integer reviewedBy = null;
                if (d.length >= 9) {
                    String rb = clean(d, 8);
                    if (!rb.isEmpty()) {
                        try {
                            reviewedBy = Integer.parseInt(rb);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                String reviewedAt = (d.length >= 10) ? clean(d, 9) : "";
                String note = (d.length >= 11) ? clean(d, 10) : "";

                if (date == null) {
                    continue;
                }
                if (period != null && !period.includes(date)) {
                    continue;
                }

                out.add(new LeaveRequest(leaveId, eId, date, start, end, firstName, lastName, status, reviewedBy, reviewedAt, note));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }

        return out;
    }

    private double calculateHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return 0.0;
        }
        if (end.isBefore(start)) {
            return 0.0;
        }

        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > 240) {
            minutes -= 60;
        }
        double hours = Math.max(0.0, minutes / 60.0);
        return Math.min(hours, 8.0);
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
            // header failure remains non-blocking
        }
    }

    private String clean(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length) {
            return "";
        }
        return unquote(arr[idx]).trim();
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

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return fallback;
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
}
