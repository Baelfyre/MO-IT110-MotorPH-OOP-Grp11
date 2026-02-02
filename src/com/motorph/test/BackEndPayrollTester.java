/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.test;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.repository.csv.CsvAuditRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import com.motorph.repository.csv.CsvPayslipRepository;
import com.motorph.repository.csv.CsvTimeRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.PayrollService;
import com.motorph.service.strategy.DeductionStrategy;
import com.motorph.service.strategy.PayDeductionStrategy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
/**
 * BACKEND-ONLY PAYROLL TEST (NO UI) Tests: 1) Payslip generation returns a
 * payslip 2) Payroll invariants hold (gross/deductions/net equation) 3)
 * Duplicate prevention works (same emp + same period cannot be saved twice)
 *
 * @author ACER
 */


public class BackEndPayrollTester {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== BACKEND PAYROLL TEST (NO UI) ===");

        // Defaults match your BackEndTester (BackEndTester.java line 34–37)
        int employeeId = (args.length >= 1) ? safeParseInt(args[0], 10001) : 10001;
        LocalDate start = (args.length >= 2) ? safeParseDate(args[1], LocalDate.of(2024, 1, 1)) : LocalDate.of(2024, 1, 1);
        LocalDate end = (args.length >= 3) ? safeParseDate(args[2], LocalDate.of(2024, 6, 30)) : LocalDate.of(2024, 6, 30);
        int processedByUserId = (args.length >= 4) ? safeParseInt(args[3], 10002) : 10002;

        PayPeriod period = new PayPeriod(start, end);

        // Same dependency wiring as BackEndTester (BackEndTester.java lines 20–32)
        CsvEmployeeRepository empRepo = new CsvEmployeeRepository();
        CsvTimeRepository timeRepo = new CsvTimeRepository();
        CsvPayslipRepository payslipRepo = new CsvPayslipRepository();
        CsvAuditRepository auditRepo = new CsvAuditRepository();
        DeductionStrategy strategy = new PayDeductionStrategy();

        PayrollService payrollService = new PayrollService(empRepo, timeRepo, strategy, payslipRepo, auditRepo);

        // Backup existing payslip file for clean testing
        Path payslipFilePath = buildPayslipFilePath(employeeId, period);
        String originalPayslipFile = readIfExists(payslipFilePath);

        try {
            // T1: Generate payslip (PayrollService.generatePayslip exists) (PayrollService.java line 43)
            Payslip p1 = payrollService.generatePayslip(employeeId, period, processedByUserId);
            check(p1 != null, "T1 generatePayslip returns non-null (and saves snapshot)");

            if (p1 == null) {
                finish();
                return;
            }

            // T2: Invariants
            double gross = p1.getGrossIncome();
            double deductions = p1.getTotalDeductions();
            double net = p1.getNetPay();

            check(gross >= 0.0, "T2.1 grossIncome >= 0");
            check(deductions >= 0.0, "T2.2 totalDeductions >= 0");
            check(net >= 0.0, "T2.3 netPay >= 0");

            double expectedNet = Math.max(0.0, gross - deductions);
            check(almostEqual(net, expectedNet, 0.01), "T2.4 netPay == max(0, gross - totalDeductions) (tol 0.01)");

            // T3: Duplicate prevention (2nd call should fail because same TX-ID already saved)
            Payslip p2 = payrollService.generatePayslip(employeeId, period, processedByUserId);
            check(p2 == null, "T3 second generatePayslip returns null (duplicate prevented)");

        } finally {
            // Restore file so your repo stays clean after the test
            restoreFile(payslipFilePath, originalPayslipFile);
            finish();
        }
    }

    private static Path buildPayslipFilePath(int employeeId, PayPeriod period) {
        // CsvPayslipRepository saves to: DataPaths.PAYSLIP_FOLDER + "records_payslips_{empId}_{periodKey}.csv"
        // (CsvPayslipRepository.java uses FILE_PREFIX + empId + "_" + period.toKey() + ".csv")
        String fileName = "records_payslips_" + employeeId + "_" + period.toKey() + ".csv";
        return Path.of(DataPaths.PAYSLIP_FOLDER, fileName);
    }

    private static String readIfExists(Path p) {
        try {
            if (p != null && Files.exists(p)) {
                return Files.readString(p, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) { }
        return null;
    }

    private static void restoreFile(Path p, String original) {
        try {
            if (p == null) {
                return;
            }
            if (original == null) {
                // File did not exist before test, delete it if created
                if (Files.exists(p)) {
                    Files.delete(p);
                }
            } else {
                // Restore original content
                Files.createDirectories(p.getParent());
                Files.writeString(p, original, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.out.println("WARN | Could not restore payslip file: " + e.getMessage());
        }
    }

    private static int safeParseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static LocalDate safeParseDate(String s, LocalDate fallback) {
        try {
            return LocalDate.parse(s.trim()); // expects YYYY-MM-DD
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean almostEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    private static void check(boolean condition, String label) {
        if (condition) {
            passed++;
            System.out.println("PASS | " + label);
        } else {
            failed++;
            System.out.println("FAIL | " + label);
        }
    }

    private static void finish() {
        System.out.println();
        System.out.println("=== SUMMARY ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }
}