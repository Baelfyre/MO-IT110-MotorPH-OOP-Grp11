/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.repository.csv;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.repository.PayslipRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * CSV-backed implementation of PayslipRepository. Saves one payslip snapshot
 * per employee and per pay period file.
 *
 * @author ACER
 */
public class CsvPayslipRepository implements PayslipRepository {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US);
    private static final DateTimeFormatter PROCESSED_FMT = DateTimeFormatter.ofPattern("M/d/yyyy H:mm", Locale.US);

    private static final String FILE_PREFIX = "records_payslips_";
    private static final String FILE_SUFFIX = ".csv";

    private static final String HEADER
            = "Payslip_ID,Transaction_ID,EmployeeID,LastName,FirstName,PayPeriodStart,PayPeriodEnd,"
            + "BasicSalary,RiceAllowance,PhoneAllowance,ClothingAllowance,GrossSemiMonthlyRate,"
            + "HourlyRate,TotalHoursWorked,GrossIncome,SSS,PhilHealth,Pagibig,WithholdingTax,"
            + "TotalDeductions,NetPay,ProcessedBy,DateProcessed";

    @Override
    public boolean save(Payslip p) {
        if (p == null || p.getPeriod() == null) {
            return false;
        }

        ensureFolder(DataPaths.PAYSLIP_FOLDER);

        PayPeriod period = p.getPeriod();
        String fileName = FILE_PREFIX + p.getEmployeeId() + "_" + period.toKey() + FILE_SUFFIX;
        File file = new File(DataPaths.PAYSLIP_FOLDER + fileName);

        ensureHeader(file);

        if (existsTransactionId(file, p.getTransactionId())) {
            return false;
        }

        String payslipId = buildPayslipId(p);
        String processedBy = (p.getProcessedByUserId() > 0) ? String.valueOf(p.getProcessedByUserId()) : "";
        String dateProcessed = (p.getDateProcessed() != null ? p.getDateProcessed() : LocalDateTime.now())
                .format(PROCESSED_FMT);

        String row
                = escape(payslipId) + ","
                + escape(p.getTransactionId()) + ","
                + p.getEmployeeId() + ","
                + escape(p.getLastName()) + ","
                + escape(p.getFirstName()) + ","
                + escape(period.getStartDate().format(DATE_FMT)) + ","
                + escape(period.getEndDate().format(DATE_FMT)) + ","
                + fmt(p.getBasicSalary()) + ","
                + fmt(p.getRiceAllowance()) + ","
                + fmt(p.getPhoneAllowance()) + ","
                + fmt(p.getClothingAllowance()) + ","
                + fmt(p.getGrossSemiMonthlyRate()) + ","
                + fmt(p.getHourlyRate()) + ","
                + fmt(p.getTotalHoursWorked()) + ","
                + fmt(p.getGrossIncome()) + ","
                + fmt(p.getSss()) + ","
                + fmt(p.getPhilHealth()) + ","
                + fmt(p.getPagIbig()) + ","
                + fmt(p.getWithholdingTax()) + ","
                + fmt(p.getTotalDeductions()) + ","
                + fmt(p.getNetPay()) + ","
                + escape(processedBy) + ","
                + escape(dateProcessed);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.newLine();
            bw.write(row);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Payslip findByEmployeeAndPeriod(int empId, PayPeriod period) {
        if (empId <= 0 || period == null) {
            return null;
        }

        String fileName = FILE_PREFIX + empId + "_" + period.toKey() + FILE_SUFFIX;
        File file = new File(DataPaths.PAYSLIP_FOLDER + fileName);

        if (!file.exists()) {
            return null;
        }

        return readLastPayslipFromFile(file);
    }

    @Override
    public Payslip findLatestByEmployee(int empId) {
        if (empId <= 0) {
            return null;
        }

        File[] files = listEmployeePayslipFiles(empId);
        if (files.length == 0) {
            return null;
        }

        Payslip best = null;
        LocalDate bestEnd = null;

        for (File f : files) {
            Payslip candidate = readLastPayslipFromFile(f);
            if (candidate == null || candidate.getPeriod() == null || candidate.getPeriod().getEndDate() == null) {
                continue;
            }

            LocalDate end = candidate.getPeriod().getEndDate();
            if (bestEnd == null || end.isAfter(bestEnd)) {
                bestEnd = end;
                best = candidate;
            }
        }

        return best;
    }

    @Override
    public List<Payslip> findAllByEmployee(int empId) {
        if (empId <= 0) {
            return Collections.emptyList();
        }

        File[] files = listEmployeePayslipFiles(empId);
        if (files.length == 0) {
            return Collections.emptyList();
        }

        List<Payslip> all = new ArrayList<>();
        for (File f : files) {
            Payslip p = readLastPayslipFromFile(f);
            if (p != null) {
                all.add(p);
            }
        }

        all.sort((a, b) -> {
            if (a.getPeriod() == null || a.getPeriod().getEndDate() == null) {
                return -1;
            }
            if (b.getPeriod() == null || b.getPeriod().getEndDate() == null) {
                return 1;
            }
            return a.getPeriod().getEndDate().compareTo(b.getPeriod().getEndDate());
        });

        return all;
    }

    private File[] listEmployeePayslipFiles(int empId) {
        ensureFolder(DataPaths.PAYSLIP_FOLDER);

        File folder = new File(DataPaths.PAYSLIP_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            return new File[0];
        }

        String prefix = FILE_PREFIX + empId + "_";
        File[] files = folder.listFiles((dir, name)
                -> name != null && name.startsWith(prefix) && name.endsWith(FILE_SUFFIX));

        if (files == null) {
            return new File[0];
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }

    private Payslip readLastPayslipFromFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // header
            if (line == null) {
                return null;
            }

            String lastData = null;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lastData = line;
                }
            }

            if (lastData == null) {
                return null;
            }

            return parsePayslipRow(lastData);

        } catch (Exception e) {
            return null;
        }
    }

    private Payslip parsePayslipRow(String row) {
        String[] data = row.split(CSV_SPLIT_REGEX, -1);
        if (data.length < 21) {
            return null;
        }

        Payslip p = new Payslip();

        p.setTransactionId(clean(data[1]));
        p.setEmployeeId(parseIntSafe(data[2]));
        p.setLastName(clean(data[3]));
        p.setFirstName(clean(data[4]));

        LocalDate start = parseDateSafe(clean(data[5]));
        LocalDate end = parseDateSafe(clean(data[6]));
        if (start != null && end != null) {
            p.setPeriod(new PayPeriod(start, end));
        }

        p.setBasicSalary(parseDoubleSafe(data[7]));
        p.setRiceAllowance(parseDoubleSafe(data[8]));
        p.setPhoneAllowance(parseDoubleSafe(data[9]));
        p.setClothingAllowance(parseDoubleSafe(data[10]));
        p.setHourlyRate(parseDoubleSafe(data[12]));
        p.setTotalHoursWorked(parseDoubleSafe(data[13]));
        p.setGrossIncome(parseDoubleSafe(data[14]));

        p.setSss(parseDoubleSafe(data[15]));
        p.setPhilHealth(parseDoubleSafe(data[16]));
        p.setPagIbig(parseDoubleSafe(data[17]));
        p.setWithholdingTax(parseDoubleSafe(data[18]));
        p.setTotalDeductions(parseDoubleSafe(data[19]));
        p.setNetPay(parseDoubleSafe(data[20]));

        return p;
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
            // file creation failures keep save non-blocking
        }
    }

    private boolean existsTransactionId(File file, String txId) {
        if (txId == null || txId.trim().isEmpty()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = line.split(CSV_SPLIT_REGEX, -1);
                if (data.length > 1) {
                    String existingTx = clean(data[1]);
                    if (txId.equals(existingTx)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private String buildPayslipId(Payslip p) {
        if (p.getTransactionId() == null || p.getTransactionId().trim().isEmpty()) {
            return "PS-" + p.getEmployeeId() + "-" + System.currentTimeMillis();
        }
        return "PS-" + p.getTransactionId();
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

    private String fmt(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        long rounded = Math.round(v);
        if (Math.abs(v - rounded) < 0.0000001) {
            return Long.toString(rounded);
        }
        return String.format(Locale.US, "%.2f", v);
    }

    private int parseIntSafe(String raw) {
        try {
            String v = clean(raw);
            if (v.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String raw) {
        try {
            String v = clean(raw);
            if (v.isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(v.replace(",", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private LocalDate parseDateSafe(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
