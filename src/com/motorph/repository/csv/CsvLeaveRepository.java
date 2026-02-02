/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.LeaveRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * CSV-backed LeaveRepository for records_leave_{empId}.csv. Computation rules:
 * - Dedupe by Leave_ID - Ignore weekend dates - Workday-based hours: lunch
 * excluded for spans longer than 4 hours, capped to 8 hours per day
 *
 * @author ACER
 */
public class CsvLeaveRepository implements LeaveRepository {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private static final String FILE_PREFIX = "records_leave_";
    private static final String FILE_SUFFIX = ".csv";

    private static final String HEADER
            = "Leave_ID,Employee #,Date,Start_Time,End_Time,First Name,Last Name,Status,Reviewed_By,Reviewed_At,Decision_Note";

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
        Set<String> seenIds = new HashSet<>();
        double total = 0.0;

        for (LeaveRequest r : rows) {
            if (r == null || r.getDate() == null) {
                continue;
            }

            String id = (r.getLeaveId() == null) ? "" : r.getLeaveId().trim();
            if (id.isEmpty()) {
                continue;
            }

            boolean firstOccurrence = seenIds.add(id);
            if (!firstOccurrence) {
                continue;
            }

            if (!isWorkday(r.getDate())) {
                continue;
            }

            total += calculateWorkdayLeaveHours(r.getStartTime(), r.getEndTime());
        }

        return total;
    }

    private List<LeaveRequest> read(int empId, PayPeriod period) {
        List<LeaveRequest> out = new ArrayList<>();

        File file = new File(DataPaths.LEAVE_FOLDER + FILE_PREFIX + empId + FILE_SUFFIX);
        if (!file.exists()) {
            return out;
        }

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

                LeaveRequest r = parseLeaveRow(d);
                if (r == null) {
                    continue;
                }

                if (period != null && r.getDate() != null && !period.includes(r.getDate())) {
                    continue;
                }

                out.add(r);
            }
        } catch (Exception e) {
            return out;
        }

        return out;
    }

    private LeaveRequest parseLeaveRow(String[] c) {
        try {
            // c[0..6] are always required (legacy)
            String leaveId = clean(c[0]);
            int empId = Integer.parseInt(clean(c[1]));

            LocalDate date = parseDate(clean(c[2]));
            LocalTime start = parseTime(clean(c[3]));
            LocalTime end = parseTime(clean(c[4]));

            String first = clean(c[5]);
            String last = clean(c[6]);

            // Option B defaults
            LeaveStatus status = LeaveStatus.PENDING;
            Integer reviewedBy = null;
            String reviewedAt = "";
            String note = "";

            // New format (11 columns)
            if (c.length >= 11) {
                status = LeaveStatus.fromCsv(clean(c[7]));
                reviewedBy = parseIntNullable(clean(c[8]));
                reviewedAt = safe(clean(c[9]));
                note = safe(clean(c[10]));
            }

            return new LeaveRequest(
                    leaveId, empId, date, start, end, first, last,
                    status, reviewedBy, reviewedAt, note
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    private Integer parseIntNullable(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        if (v.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    private double calculateWorkdayLeaveHours(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return 0.0;
        }
        if (end.isBefore(start)) {
            return 0.0;
        }

        long minutes = Duration.between(start, end).toMinutes();

        // Long leave spans exclude a lunch break, aligned with the system workday model.
        if (minutes > 240) {
            minutes -= 60;
        }

        double hours = Math.max(0.0, minutes / 60.0);

        // Daily leave usage is constrained to a standard workday.
        return Math.min(hours, 8.0);
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

    private String clean(String input) {
        return input == null ? "" : input.replace("\"", "").trim();
    }
}
