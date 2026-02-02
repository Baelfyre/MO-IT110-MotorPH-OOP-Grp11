package com.motorph.test;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.Payslip;

import com.motorph.repository.csv.CsvAuditRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import com.motorph.repository.csv.CsvPayslipRepository;
import com.motorph.repository.csv.CsvTimeRepository;
import com.motorph.repository.csv.DataPaths;

import com.motorph.service.PayrollService;
import com.motorph.service.strategy.DeductionStrategy;
import com.motorph.service.strategy.PayDeductionStrategy;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class BackEndTester {

    // Use a high employee ID to avoid collisions with real data.
    private static final int TEST_EMP_ID = 99001;

    // Any integer is fine here. PayrollService does not validate this ID.
    private static final int TEST_PROCESSED_BY = 10001;

    // Deterministic formatting used by PayrollService.buildTransactionId()
    private static final DateTimeFormatter TX_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    // Assertions tolerance
    private static final double EPS = 0.01;

    private static int pass = 0;
    private static int fail = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("MOTORPH BACKEND TEST RUNNER (NetBeans Console)");
        System.out.println("==================================================");

        // Data pre-check
        ensureDataFolderExists();

        Path employeeCsv = Paths.get(DataPaths.EMPLOYEE_CSV);
        if (!Files.exists(employeeCsv)) {
            System.out.println("[FATAL] Missing required file: " + DataPaths.EMPLOYEE_CSV);
            System.out.println("Create/restore your data folder first, then rerun.");
            return;
        }

        // Backup files that we might modify
        FileBackup empBackup = backupFile(DataPaths.EMPLOYEE_CSV);
        FileBackup auditBackup = backupFile(DataPaths.AUDIT_LOG_CSV);

        // Files we will create and cleanup
        PayPeriod period1 = PayPeriod.fromDateSemiMonthly(LocalDate.of(2024, 7, 10)); // 07/01–07/15
        PayPeriod period2 = PayPeriod.fromDateSemiMonthly(LocalDate.of(2024, 7, 20)); // 07/16–07/31

        Path dtrFile = Paths.get(DataPaths.DTR_FOLDER + "records_dtr_" + TEST_EMP_ID + ".csv");
        Path payslipFileP1 = Paths.get(DataPaths.PAYSLIP_FOLDER + "records_payslips_" + TEST_EMP_ID + "_" + period1.toKey() + ".csv");
        Path payslipFileP2 = Paths.get(DataPaths.PAYSLIP_FOLDER + "records_payslips_" + TEST_EMP_ID + "_" + period2.toKey() + ".csv");

        try {
            // Setup dependencies
            CsvEmployeeRepository empRepo = new CsvEmployeeRepository();
            CsvTimeRepository timeRepo = new CsvTimeRepository(empRepo);
            CsvPayslipRepository payslipRepo = new CsvPayslipRepository();
            CsvAuditRepository auditRepo = new CsvAuditRepository();
            DeductionStrategy strategy = new PayDeductionStrategy();

            PayrollService payrollService = new PayrollService(empRepo, timeRepo, strategy, payslipRepo, auditRepo);

            // Clean any previous leftovers from earlier tester runs
            deleteIfExists(dtrFile);
            deleteIfExists(payslipFileP1);
            deleteIfExists(payslipFileP2);

            // 1) Pay Period Tests
            runPayPeriodBoundaryTests();

            // 2) Create a controlled test employee (restored later via backup)
            createTestEmployee(empRepo);

            // 3) Write controlled DTR entries for period1 and verify attendance-based math
            seedDtrForCoreRules(timeRepo);

            // 4) Payroll run 1 (should succeed)
            System.out.println();
            section("PAYROLL TESTS: PERIOD 1 (FIRST HALF)");

            Payslip p1 = payrollService.generatePayslip(TEST_EMP_ID, period1, TEST_PROCESSED_BY);
            assertNotNull("Payslip generation returns Payslip (not null)", p1);

            if (p1 != null) {
                // Deterministic Transaction ID test
                String expectedTx = expectedTransactionId(TEST_EMP_ID, period1);
                assertEquals("Deterministic Transaction ID", expectedTx, p1.getTransactionId());

                // Compute expected values for the controlled DTR seed
                double hourly = 100.00;

                // Expected lateness: 11 minutes late (08:11) + 30 minutes undertime (16:30)
                double expectedLateDeduction = (11.0 + 30.0) * (hourly / 60.0);

                // Expected OT: 1 hour OT (08:00–18:00 => 9 paid hours after lunch) at 1.25 multiplier
                double expectedOvertimePay = 1.0 * hourly * 1.25;

                // Expected hours (uses same rule as PayrollService.calculateHours: subtract 60 if > 4 hours)
                double expectedHours =
                        calcHours(LocalTime.of(8, 5), LocalTime.of(17, 0)) +     // grace day
                        calcHours(LocalTime.of(8, 11), LocalTime.of(17, 0)) +    // late day
                        calcHours(LocalTime.of(8, 0), LocalTime.of(16, 30)) +    // undertime day
                        calcHours(LocalTime.of(8, 0), LocalTime.of(18, 0));      // OT day

                // Expected gross:
                // semi-basic = 20000/2 = 10000
                // allowances = (1000+500+500)/2 = 1000
                // overtimePay = 125
                // gross = 11125
                double expectedGross = 11125.00;

                assertApprox("Grace+Late+Undertime Deduction", expectedLateDeduction, p1.getLateDeduction(), EPS);
                assertApprox("Overtime Pay", expectedOvertimePay, p1.getOvertimePay(), EPS);
                assertApprox("Total Hours Worked", expectedHours, p1.getTotalHoursWorked(), EPS);
                assertApprox("Gross Income", expectedGross, p1.getGrossIncome(), EPS);

                // Sanity checks that do not depend on gov tables
                assertTrue("Total Deductions >= Late Deduction",
                        p1.getTotalDeductions() + EPS >= p1.getLateDeduction());

                assertTrue("Net Pay <= Gross Income",
                        p1.getNetPay() <= p1.getGrossIncome() + EPS);

                // Print key outputs
                System.out.println();
                System.out.println("Printed Summary:");
                System.out.println("TX ID: " + p1.getTransactionId());
                System.out.println("Period: " + period1.getStartDate() + " to " + period1.getEndDate());
                System.out.printf("Late/Undertime Deduction: %.2f%n", p1.getLateDeduction());
                System.out.printf("Overtime Pay: %.2f%n", p1.getOvertimePay());
                System.out.printf("Total Hours Worked: %.2f%n", p1.getTotalHoursWorked());
                System.out.printf("Gross Income: %.2f%n", p1.getGrossIncome());
                System.out.printf("Total Deductions: %.2f%n", p1.getTotalDeductions());
                System.out.printf("Net Pay: %.2f%n", p1.getNetPay());
            }

            // 5) Payroll run 2 same employee + same period (should be blocked by uniqueness)
            System.out.println();
            section("UNIQUENESS TEST: SAME EMPLOYEE + SAME PERIOD");
            Payslip p1Duplicate = payrollService.generatePayslip(TEST_EMP_ID, period1, TEST_PROCESSED_BY);
            assertTrue("Duplicate payroll run is blocked (returns null)", p1Duplicate == null);

            // 6) Payroll for a different period (should succeed even with no DTR entries)
            System.out.println();
            section("MULTI-PERIOD TEST: SAME EMPLOYEE + DIFFERENT PERIOD");
            Payslip p2 = payrollService.generatePayslip(TEST_EMP_ID, period2, TEST_PROCESSED_BY);
            assertNotNull("Payroll allowed for a different pay period", p2);
            if (p2 != null) {
                assertEquals("Different period yields different TX ID",
                        expectedTransactionId(TEST_EMP_ID, period2),
                        p2.getTransactionId());
                System.out.println("TX ID (Period 2): " + p2.getTransactionId());
            }

        } catch (Exception ex) {
            System.out.println("[FATAL ERROR] " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Cleanup created files first
            deleteIfExists(dtrFile);
            deleteIfExists(payslipFileP1);
            deleteIfExists(payslipFileP2);

            // Restore backups
            restoreFile(empBackup);
            restoreFile(auditBackup);

            // Final summary
            System.out.println();
            System.out.println("==================================================");
            System.out.println("TEST RESULTS SUMMARY");
            System.out.println("PASS: " + pass);
            System.out.println("FAIL: " + fail);
            System.out.println("==================================================");
        }
    }

    // ----------------------------
    // Test Cases
    // ----------------------------

    private static void runPayPeriodBoundaryTests() {
        System.out.println();
        section("PAY PERIOD TESTS");

        // 07/15 should be first half
        PayPeriod a = PayPeriod.fromDateSemiMonthly(LocalDate.of(2024, 7, 15));
        assertEquals("07/15 Start", LocalDate.of(2024, 7, 1), a.getStartDate());
        assertEquals("07/15 End", LocalDate.of(2024, 7, 15), a.getEndDate());

        // 07/16 should be second half, end is end-of-month
        PayPeriod b = PayPeriod.fromDateSemiMonthly(LocalDate.of(2024, 7, 16));
        assertEquals("07/16 Start", LocalDate.of(2024, 7, 16), b.getStartDate());
        assertEquals("07/16 End", YearMonth.of(2024, 7).atEndOfMonth(), b.getEndDate());

        // Generic rule: day > 15 ends at end-of-month
        LocalDate anySecondHalf = LocalDate.of(2024, 2, 20);
        PayPeriod c = PayPeriod.fromDateSemiMonthly(anySecondHalf);
        assertEquals("02/20 Start", LocalDate.of(2024, 2, 16), c.getStartDate());
        assertEquals("02/20 End", YearMonth.of(2024, 2).atEndOfMonth(), c.getEndDate());
    }

    private static void createTestEmployee(CsvEmployeeRepository empRepo) {
        System.out.println();
        section("SEED TEST EMPLOYEE");

        Employee existing = empRepo.findById(TEST_EMP_ID);
        if (existing != null) {
            System.out.println("Test employee already exists in data_Employee.csv: " + TEST_EMP_ID);
            System.out.println("Proceeding without creating a new row.");
            return;
        }

        Employee testEmp = new Employee(
                TEST_EMP_ID,
                "Tester",
                "Backend",
                LocalDate.of(2000, 1, 1),
                "Test Address",
                "09000000000",
                "SSS-TEST",
                "PH-TEST",
                "TIN-TEST",
                "PAGIBIG-TEST",
                "Regular",
                "QA",
                "N/A",
                20000.00,  // basic salary (monthly)
                1000.00,   // rice allowance (monthly)
                500.00,    // phone allowance (monthly)
                500.00,    // clothing allowance (monthly)
                11000.00,  // gross semi-monthly rate (informational)
                100.00     // hourly rate
        );

        empRepo.create(testEmp);
        Employee saved = empRepo.findById(TEST_EMP_ID);
        assertNotNull("Test employee created and retrievable", saved);
    }

    private static void seedDtrForCoreRules(CsvTimeRepository timeRepo) {
        System.out.println();
        section("SEED DTR ENTRIES (CONTROLLED)");

        // We seed 4 days inside period 07/01–07/15
        // Day 1: 08:05–17:00 (within grace, no late)
        // Day 2: 08:11–17:00 (late 11 minutes)
        // Day 3: 08:00–16:30 (undertime 30 minutes)
        // Day 4: 08:00–18:00 (OT, results in 9 paid hours after lunch, so 1 OT hour)

        boolean ok1 = timeRepo.saveEntry(TEST_EMP_ID,
                new TimeEntry(LocalDate.of(2024, 7, 1), LocalTime.of(8, 5), LocalTime.of(17, 0)));
        boolean ok2 = timeRepo.saveEntry(TEST_EMP_ID,
                new TimeEntry(LocalDate.of(2024, 7, 2), LocalTime.of(8, 11), LocalTime.of(17, 0)));
        boolean ok3 = timeRepo.saveEntry(TEST_EMP_ID,
                new TimeEntry(LocalDate.of(2024, 7, 3), LocalTime.of(8, 0), LocalTime.of(16, 30)));
        boolean ok4 = timeRepo.saveEntry(TEST_EMP_ID,
                new TimeEntry(LocalDate.of(2024, 7, 4), LocalTime.of(8, 0), LocalTime.of(18, 0)));

        assertTrue("DTR save day 1", ok1);
        assertTrue("DTR save day 2", ok2);
        assertTrue("DTR save day 3", ok3);
        assertTrue("DTR save day 4", ok4);

        System.out.println("Seeded 4 controlled attendance rows for EmpID " + TEST_EMP_ID);
    }

    // ----------------------------
    // Helpers
    // ----------------------------

    private static double calcHours(LocalTime in, LocalTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        if (minutes > 240) {
            minutes -= 60; // lunch break deduction (matches PayrollService)
        }
        return Math.max(0, minutes / 60.0);
    }

    private static String expectedTransactionId(int empId, PayPeriod period) {
        String start = period.getStartDate().format(TX_DATE_FMT);
        String end = period.getEndDate().format(TX_DATE_FMT);
        return "TX-" + empId + "-" + start + "-" + end;
    }

    private static void section(String title) {
        System.out.println("--------------------------------------------------");
        System.out.println(title);
        System.out.println("--------------------------------------------------");
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            pass++;
            System.out.println("[PASS] " + name);
        } else {
            fail++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void assertNotNull(String name, Object obj) {
        assertTrue(name, obj != null);
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        boolean ok = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (ok) {
            pass++;
            System.out.println("[PASS] " + name + " | expected=" + expected + " actual=" + actual);
        } else {
            fail++;
            System.out.println("[FAIL] " + name + " | expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertApprox(String name, double expected, double actual, double eps) {
        boolean ok = Math.abs(expected - actual) <= eps;
        if (ok) {
            pass++;
            System.out.printf("[PASS] %s | expected=%.4f actual=%.4f%n", name, expected, actual);
        } else {
            fail++;
            System.out.printf("[FAIL] %s | expected=%.4f actual=%.4f (eps=%.4f)%n", name, expected, actual, eps);
        }
    }

    private static void ensureDataFolderExists() {
        try {
            Files.createDirectories(Paths.get("./data"));
        } catch (IOException e) {
            System.out.println("[WARN] Unable to create ./data folder: " + e.getMessage());
        }
    }

    private static void deleteIfExists(Path p) {
        try {
            if (p != null && Files.exists(p)) {
                Files.delete(p);
                System.out.println("[CLEANUP] Deleted: " + p.toString());
            }
        } catch (IOException e) {
            System.out.println("[WARN] Cleanup failed for: " + p + " | " + e.getMessage());
        }
    }

    // ----------------------------
    // Backup/Restore
    // ----------------------------

    private static class FileBackup {
        final Path original;
        final Path backup;
        final boolean existed;

        FileBackup(Path original, Path backup, boolean existed) {
            this.original = original;
            this.backup = backup;
            this.existed = existed;
        }
    }

    private static FileBackup backupFile(String originalPath) {
        Path original = Paths.get(originalPath);
        boolean existed = Files.exists(original);

        Path backup = Paths.get(originalPath + ".bak_backendtester");
        try {
            if (existed) {
                Files.createDirectories(original.getParent() == null ? Paths.get(".") : original.getParent());
                Files.copy(original, backup, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[BACKUP] " + originalPath + " -> " + backup.toString());
            } else {
                // If it does not exist, we still return a backup object so restore logic is consistent
                System.out.println("[BACKUP] Skipped (missing): " + originalPath);
            }
        } catch (IOException e) {
            System.out.println("[WARN] Backup failed for: " + originalPath + " | " + e.getMessage());
        }
        return new FileBackup(original, backup, existed);
    }

    private static void restoreFile(FileBackup fb) {
        if (fb == null) {
            return;
        }

        try {
            if (fb.existed && Files.exists(fb.backup)) {
                Files.copy(fb.backup, fb.original, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(fb.backup);
                System.out.println("[RESTORE] Restored: " + fb.original.toString());
            } else {
                // If original did not exist, delete whatever the test created
                if (!fb.existed) {
                    Files.deleteIfExists(fb.original);
                }
                Files.deleteIfExists(fb.backup);
            }
        } catch (IOException e) {
            System.out.println("[WARN] Restore failed for: " + fb.original + " | " + e.getMessage());
        }
    }
}
