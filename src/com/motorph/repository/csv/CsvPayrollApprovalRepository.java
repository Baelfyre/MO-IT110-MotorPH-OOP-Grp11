/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.PayPeriod;
import com.motorph.repository.PayrollApprovalRepository;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Shared approval tracker for DTR and Payroll. One file per employee:
 * records_payroll_{empId}.csv One row per PayPeriod.
 *
 * @author ACER
 */
public class CsvPayrollApprovalRepository implements PayrollApprovalRepository {

    private static final String HEADER
            = "Transaction_ID,Employee_ID,Pay_Period_Start,Pay_Period_End,"
            + "DTR_Approved_By,DTR_Status,DTR_Approved_Date,"
            + "Payroll_Approved_By,Payroll_Status,Payroll_Approved_Date";

    private static final DateTimeFormatter PERIOD_DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("M/d/yyyy H:mm", Locale.US);

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    // column indexes based on HEADER
    private static final int IDX_TX = 0;
    private static final int IDX_EMP = 1;
    private static final int IDX_START = 2;
    private static final int IDX_END = 3;

    private static final int IDX_DTR_BY = 4;
    private static final int IDX_DTR_STATUS = 5;
    private static final int IDX_DTR_DATE = 6;

    private static final int IDX_PAYROLL_BY = 7;
    private static final int IDX_PAYROLL_STATUS = 8;
    private static final int IDX_PAYROLL_DATE = 9;

    @Override
    public ApprovalStatus getDtrStatus(int empId, PayPeriod period) {
        String[] row = findRow(empId, period);
        if (row == null) {
            return ApprovalStatus.PENDING;
        }
        return parseStatus(clean(row[IDX_DTR_STATUS]));
    }

    @Override
    public ApprovalStatus getPayrollStatus(int empId, PayPeriod period) {
        String[] row = findRow(empId, period);
        if (row == null) {
            return ApprovalStatus.PENDING;
        }
        return parseStatus(clean(row[IDX_PAYROLL_STATUS]));
    }

    @Override
    public boolean ensureRowExists(int empId, PayPeriod period) {
        if (period == null) {
            return false;
        }
        ensureHeader(empId);

        List<String[]> rows = readAll(empId);
        if (rows == null) {
            return false;
        }

        for (String[] r : rows) {
            if (isSamePeriodRow(r, empId, period)) {
                return true;
            }
        }

        // Append default row
        String tx = buildTransactionId(empId, period);
        String[] newRow = new String[]{
            tx,
            String.valueOf(empId),
            period.getStartDate().format(PERIOD_DATE_FMT),
            period.getEndDate().format(PERIOD_DATE_FMT),
            "", ApprovalStatus.PENDING.name(), "",
            "", ApprovalStatus.PENDING.name(), ""
        };
        rows.add(newRow);
        return writeAll(empId, rows);
    }

    @Override
    public boolean upsertDtrApproval(int empId, PayPeriod period, int approvedBy, ApprovalStatus status, LocalDateTime approvedAt) {
        return upsert(empId, period, approvedBy, status, approvedAt, true);
    }

    @Override
    public boolean upsertPayrollApproval(int empId, PayPeriod period, int approvedBy, ApprovalStatus status, LocalDateTime approvedAt) {
        return upsert(empId, period, approvedBy, status, approvedAt, false);
    }

    private boolean upsert(int empId, PayPeriod period, int approvedBy, ApprovalStatus status, LocalDateTime approvedAt, boolean isDtr) {
        if (period == null || status == null) {
            return false;
        }

        ensureHeader(empId);

        List<String[]> rows = readAll(empId);
        if (rows == null) {
            return false;
        }

        boolean updated = false;

        for (String[] r : rows) {
            if (!isSamePeriodRow(r, empId, period)) {
                continue;
            }

            // Always keep TX consistent with your payroll scheme
            r[IDX_TX] = buildTransactionId(empId, period);

            String by = (approvedBy > 0) ? String.valueOf(approvedBy) : "";
            String at = (approvedAt != null) ? approvedAt.format(TS_FMT) : LocalDateTime.now().format(TS_FMT);

            if (isDtr) {
                r[IDX_DTR_BY] = by;
                r[IDX_DTR_STATUS] = status.name();
                r[IDX_DTR_DATE] = at;
            } else {
                r[IDX_PAYROLL_BY] = by;
                r[IDX_PAYROLL_STATUS] = status.name();
                r[IDX_PAYROLL_DATE] = at;
            }

            updated = true;
            break;
        }

        if (!updated) {
            // Create row then update
            if (!ensureRowExists(empId, period)) {
                return false;
            }
            rows = readAll(empId);
            if (rows == null) {
                return false;
            }
            return upsert(empId, period, approvedBy, status, approvedAt, isDtr);
        }

        return writeAll(empId, rows);
    }

    private void ensureHeader(int empId) {
        try {
            File f = new File(DataPaths.PAYROLL_FOLDER + "records_payroll_" + empId + ".csv");
            if (!f.exists()) {
                writeHeader(f);
                return;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String first = br.readLine();
                if (first == null || first.trim().isEmpty()) {
                    writeHeader(f);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void writeHeader(File f) {
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
                bw.write(HEADER);
            }
        } catch (Exception ignored) {
        }
    }

    private String[] findRow(int empId, PayPeriod period) {
        ensureHeader(empId);
        List<String[]> rows = readAll(empId);
        if (rows == null) {
            return null;
        }
        for (String[] r : rows) {
            if (isSamePeriodRow(r, empId, period)) {
                return r;
            }
        }
        return null;
    }

    private boolean isSamePeriodRow(String[] r, int empId, PayPeriod period) {
        if (r == null || r.length < 10) {
            return false;
        }
        if (parseInt(clean(r[IDX_EMP])) != empId) {
            return false;
        }

        LocalDate start = parseDate(clean(r[IDX_START]));
        LocalDate end = parseDate(clean(r[IDX_END]));
        if (start == null || end == null) {
            return false;
        }

        return start.equals(period.getStartDate()) && end.equals(period.getEndDate());
    }

    private List<String[]> readAll(int empId) {
        File f = new File(DataPaths.PAYROLL_FOLDER + "records_payroll_" + empId + ".csv");
        if (!f.exists()) {
            return new ArrayList<>();
        }

        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line = br.readLine(); // header
            if (line == null) {
                return rows;
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                rows.add(line.split(CSV_SPLIT_REGEX, -1));
            }
            return rows;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean writeAll(int empId, List<String[]> rows) {
        File f = new File(DataPaths.PAYROLL_FOLDER + "records_payroll_" + empId + ".csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
            bw.write(HEADER);
            for (String[] r : rows) {
                bw.newLine();
                bw.write(toCsvRow(r));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String toCsvRow(String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(cols[i]));
        }
        return sb.toString();
    }

    private String buildTransactionId(int empId, PayPeriod period) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyMMdd", Locale.US);
        return "TX-" + empId + "-" + period.getStartDate().format(fmt) + "-" + period.getEndDate().format(fmt);
    }

    private ApprovalStatus parseStatus(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return ApprovalStatus.PENDING;
            }
            return ApprovalStatus.valueOf(raw.trim().toUpperCase(Locale.US));
        } catch (Exception e) {
            return ApprovalStatus.PENDING;
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return LocalDate.parse(raw.trim(), PERIOD_DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseInt(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return 0;
            }
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String clean(String v) {
        return v == null ? "" : v.replace("\"", "").trim();
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
