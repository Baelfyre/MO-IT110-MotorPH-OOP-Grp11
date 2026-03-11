/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.hr;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;
import com.motorph.utils.ValidationUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author ACER
 */
public class HROpsImpl implements HROps {

    private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;
    private final EmployeeService employeeService;
    private final LogService logService;

    public HROpsImpl(
            EmployeeRepository empRepo,
            EmployeeService employeeService,
            UserRepository userRepo,
            LogService logService
    ) {
        this.empRepo = empRepo;
        this.employeeService = employeeService;
        this.userRepo = userRepo;
        this.logService = logService;
    }

    @Override
    public List<Employee> listEmployees(boolean includeArchived) {
        List<Employee> all = empRepo.findAll();
        if (includeArchived) {
            return all;
        }

        List<Employee> activeOnly = new ArrayList<>();
        for (Employee e : all) {
            if (e == null) {
                continue;
            }
            String status = e.getStatus();
            if (status == null) {
                continue;
            }
            if (!status.trim().equalsIgnoreCase("Archived")) {
                activeOnly.add(e);
            }
        }
        return activeOnly;
    }

    @Override
    public Employee getEmployee(int empId) {
        return empRepo.findById(empId);
    }

    @Override
    public boolean createEmployee(Employee emp, User currentUser) {
        // 1. BACKEND RBAC VERIFICATION
        if (currentUser == null || !currentUser.hasPermission("CAN_MANAGE_EMPLOYEES")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to create an employee."
            );
            throw new SecurityException("Access Denied: You do not have permission to create employees.");
        }

        if (emp == null) {
            return false;
        }

        List<String> validationErrors = validateEmployeeBeforeSave(emp);
        if (!validationErrors.isEmpty()) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_CREATE_DENIED_VALIDATION",
                    "Denied create. " + String.join(" | ", validationErrors)
            );
            return false;
        }

        int empId = emp.getId();
        String username = String.valueOf(empId);

        // Annotation: Deny duplicate employee record.
        if (empRepo.findById(empId) != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_CREATE_DENIED_DUPLICATE",
                    "Denied create. Employee already exists EmpID=" + empId
            );
            return false;
        }

        // Annotation: Deny duplicate login before writing employee.
        if (userRepo.findByUsername(username) != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_CREATE_DENIED_LOGIN_DUPLICATE",
                    "Denied create. Username already exists Username=" + username
            );
            return false;
        }

        // Annotation: Create employee master record first.
        empRepo.create(emp);
        employeeService.refreshCache();

        // Annotation: Auto-create linked login record.
        User login = new User(
                empId,
                username,
                com.motorph.repository.csv.DataPaths.DEFAULT_PASSWORD,
                employeeService.determineRoleFromPosition(emp.getPosition()),
                false
        );
        userRepo.save(login, emp.getFirstName(), emp.getLastName(), emp.getPosition());

        // Annotation: Create related records only after employee master exists.
        boolean provisionOk = provisionBaseRecords(emp);

        logService.recordAction(
                String.valueOf(currentUser.getId()),
                "HR_CREATE_OK",
                "Created employee profile and login. EmpID=" + empId
                + ", Username=" + username
                + ", Roles=" + login.getRoles()
                + ", Position=" + emp.getPosition()
                + ", Provisioned=" + provisionOk
        );

        return true;
    }

    @Override
    public boolean updateEmployee(Employee emp, User currentUser) {
        // 1. BACKEND RBAC VERIFICATION
        if (currentUser == null || !currentUser.hasPermission("CAN_MANAGE_EMPLOYEES")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to update employee ID: " + (emp != null ? emp.getId() : "N/A")
            );
            throw new SecurityException("Access Denied: You do not have permission to update employees.");
        }

        if (emp == null) {
            return false;
        }

        List<String> validationErrors = validateEmployeeBeforeSave(emp);
        if (!validationErrors.isEmpty()) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_UPDATE_DENIED_VALIDATION",
                    "Denied update. EmpID=" + emp.getId() + ". " + String.join(" | ", validationErrors)
            );
            return false;
        }

        if (isRestrictedSelfHrAction(currentUser.getId(), emp.getId())) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_SELF_EDIT_DENIED",
                    "Denied self-edit for HR user. EmpID=" + emp.getId()
            );
            return false;
        }

        boolean ok = empRepo.update(emp);

        if (ok) {
            employeeService.refreshCache();
        }

        logService.recordAction(
                String.valueOf(currentUser.getId()),
                ok ? "HR_UPDATE_OK" : "HR_UPDATE_FAILED",
                "Update employee EmpID=" + emp.getId()
        );

        return ok;
    }
    
    @Override
    public boolean deleteEmployee(int empId, User currentUser) {
        // 1. BACKEND RBAC VERIFICATION
        if (currentUser == null || !currentUser.hasPermission("CAN_MANAGE_EMPLOYEES")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to delete employee ID: " + empId
            );
            throw new SecurityException("Access Denied: You do not have permission to delete employees.");
        }

        // 2. Normal deletion logic continues here...
        Employee existing = empRepo.findById(empId);
        if (existing == null) {
            return false;
        }

        boolean empDeleted = empRepo.delete(empId);
        String username = String.valueOf(empId);
        CleanupSummary cleanup = cleanupEmployeeLinkedData(empId, username);

        if (empDeleted) {
            employeeService.refreshCache();
        }

        boolean deleteOk = empDeleted && cleanup.isCoreCleanupSuccessful();

        logService.recordAction(
                String.valueOf(currentUser.getId()),
                empDeleted ? "HR_DELETE_OK" : "HR_DELETE_FAILED",
                "Deleted employee. EmpID=" + empId
        );

        return deleteOk;
    }

    // Annotation: Reuse shared validation rules so create and update block invalid employee data.
    private List<String> validateEmployeeBeforeSave(Employee emp) {
        List<String> errors = new ArrayList<>();
        if (emp == null) {
            errors.add("Employee record is required.");
            return errors;
        }

        ValidationUtil.requirePastOrPresentDate(errors, "Birthday", emp.getBirthday());
        ValidationUtil.requireMinimumEmployeeAge(errors, emp.getBirthday());

        if (!ValidationUtil.isValidPhoneFormat(emp.getPhoneNumber())) {
            errors.add("Phone # must use 09XX-XXX-XXXX format.");
        }
        if (!ValidationUtil.isValidSssFormat(emp.getSssNumber())) {
            errors.add("SSS # must use XX-XXXXXXX-X format.");
        }
        if (!ValidationUtil.isValidPhilHealthFormat(emp.getPhilHealthNumber())) {
            errors.add("PhilHealth # must use XX-XXXXXXXXX-X format.");
        }
        if (!ValidationUtil.isValidTinFormat(emp.getTinNumber())) {
            errors.add("TIN # must use XXX-XXX-XXX-XXX format.");
        }
        if (!ValidationUtil.isValidPagIbigFormat(emp.getPagIbigNumber())) {
            errors.add("Pag-IBIG # must use XXXX-XXXX-XXXX format.");
        }

        if (emp.getBasicSalary() <= 0.0) {
            errors.add("Basic Salary must be greater than 0.");
        }

        double minimumBasicSalary = currentMinimumBasicSalary();
        if (emp.getBasicSalary() < minimumBasicSalary) {
            errors.add(String.format(
                    Locale.US,
                    "Basic Salary must be at least %.2f based on the current employee minimum.",
                    minimumBasicSalary
            ));
        }

        // Annotation: Validate exact allowed allowance amounts based on CSV-supported values.
        if (!ValidationUtil.isAllowedAmount(String.valueOf(emp.getRiceAllowance()), ValidationUtil.ALLOWED_RICE_ALLOWANCES)) {
            errors.add(ValidationUtil.getRiceAllowanceMessage());
        }
        if (!ValidationUtil.isAllowedAmount(String.valueOf(emp.getPhoneAllowance()), ValidationUtil.ALLOWED_PHONE_ALLOWANCES)) {
            errors.add(ValidationUtil.getPhoneAllowanceMessage());
        }
        if (!ValidationUtil.isAllowedAmount(String.valueOf(emp.getClothingAllowance()), ValidationUtil.ALLOWED_CLOTHING_ALLOWANCES)) {
            errors.add(ValidationUtil.getClothingAllowanceMessage());
        }

        return errors;
    }

    // Annotation: Read the current lowest positive basic salary from employee data.
    private double currentMinimumBasicSalary() {
        double minimum = Double.MAX_VALUE;
        for (Employee employee : empRepo.findAll()) {
            if (employee == null) {
                continue;
            }
            double salary = employee.getBasicSalary();
            if (salary > 0.0 && salary < minimum) {
                minimum = salary;
            }
        }
        return minimum == Double.MAX_VALUE ? 1.0 : minimum;
    }

    private boolean isRestrictedSelfHrAction(int actorEmpId, int targetEmpId) {
        if (actorEmpId <= 0 || actorEmpId != targetEmpId) {
            return false;
        }
        User actor = userRepo.findByUsername(String.valueOf(actorEmpId));
        return actor != null && actor.getRoles().contains(Role.HR);
    }

    // Annotation: Create DTR, leave, payroll-approval, and leave-credit defaults after successful employee creation.
    private boolean provisionBaseRecords(Employee emp) {
        boolean dtr = ensureFileWithHeader(
                DataPaths.DTR_FOLDER + "records_dtr_" + emp.getId() + ".csv",
                "Attendance_ID,Employee #,Date,Log In,Log Out,First Name,Last Name"
        );

        boolean leave = ensureFileWithHeader(
                DataPaths.LEAVE_FOLDER + "records_leave_" + emp.getId() + ".csv",
                "Leave_ID,Employee #,Date,Start_Time,End_Time,First Name,Last Name,Status,Reviewed_By,Reviewed_At,Decision_Note"
        );

        boolean payroll = ensureFileWithHeader(
                DataPaths.PAYROLL_FOLDER + "records_payroll_" + emp.getId() + ".csv",
                "Transaction_ID,Employee_ID,Pay_Period_Start,Pay_Period_End,DTR_Approved_By,DTR_Status,DTR_Approved_Date,Payroll_Approved_By,Payroll_Status,Payroll_Approved_Date"
        );

        boolean credits = ensureLeaveCreditsRow(emp);
        return dtr && leave && payroll && credits;
    }

    private boolean ensureFileWithHeader(String filePath, String header) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (file.exists() && file.length() > 0) {
                return true;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                bw.write(header);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean ensureLeaveCreditsRow(Employee emp) {
        try {
            File file = new File(DataPaths.LEAVE_CREDITS_CSV);
            if (!file.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                    bw.write("Employee #,Last Name,First Name,Leave Credits,Leave Taken");
                }
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] data = line.split(",", -1);
                    if (data.length > 0 && String.valueOf(emp.getId()).equals(data[0].trim())) {
                        return true;
                    }
                }
            }

            double defaultCredits = "Probationary".equalsIgnoreCase(emp.getStatus()) ? 0.0 : 40.0;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                bw.newLine();
                bw.write(emp.getId() + ","
                        + safeCsv(emp.getLastName()) + ","
                        + safeCsv(emp.getFirstName()) + ","
                        + String.format(Locale.US, "%.2f", defaultCredits) + ",0.00");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Annotation: Delete linked CSV rows and per-employee files so HR delete does not leave orphan records.
    private CleanupSummary cleanupEmployeeLinkedData(int empId, String username) {
        CleanupSummary summary = new CleanupSummary();

        summary.loginDeleted = deleteLogin(username);
        summary.leaveCreditsRowsRemoved = deleteRowsByExactMatch(DataPaths.LEAVE_CREDITS_CSV, 0, String.valueOf(empId));

        summary.dtrFileDeleted = deleteFileIfExists(DataPaths.DTR_FOLDER + "records_dtr_" + empId + ".csv");
        summary.leaveFileDeleted = deleteFileIfExists(DataPaths.LEAVE_FOLDER + "records_leave_" + empId + ".csv");
        summary.payrollFileDeleted = deleteFileIfExists(DataPaths.PAYROLL_FOLDER + "records_payroll_" + empId + ".csv");
        summary.payslipFilesDeleted = deleteFilesByPrefix(DataPaths.PAYSLIP_FOLDER, "records_payslips_" + empId + "_");

        summary.systemLogRowsRemoved = deleteSystemLogRows(empId);
        summary.auditLogRowsRemoved = deleteAuditLogRows(empId);
        summary.dtrLogRowsRemoved = deleteRowsContainingEmployeeId(DataPaths.LOG_DTR, empId);
        summary.employeeDataLogRowsRemoved = deleteRowsContainingEmployeeId(DataPaths.LOG_EMP_DATA, empId);
        summary.payrollLogRowsRemoved = deleteRowsContainingEmployeeId(DataPaths.LOG_PAYROLL, empId);

        return summary;
    }

    private boolean deleteLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return true;
        }
        User existingUser = userRepo.findByUsername(username);
        if (existingUser == null) {
            return true;
        }
        return userRepo.deleteByUsername(username);
    }

    private int deleteRowsByExactMatch(String filePath, int columnIndex, String expectedValue) {
        return rewriteCsvExcludingRows(filePath, columns -> columnValueEquals(columns, columnIndex, expectedValue));
    }

    private int deleteSystemLogRows(int empId) {
        String probe = String.valueOf(empId);
        return rewriteCsvExcludingRows(DataPaths.SYSTEM_LOG_CSV, columns -> {
            String userValue = csvValue(columns, 3);
            String actionValue = csvValue(columns, 4);
            String detailsValue = csvValue(columns, 5);
            return probe.equals(userValue)
                    || containsEmployeeIdToken(actionValue, empId)
                    || containsEmployeeIdToken(detailsValue, empId);
        });
    }

    private int deleteAuditLogRows(int empId) {
        String probe = String.valueOf(empId);
        return rewriteCsvExcludingRows(DataPaths.AUDIT_LOG_CSV, columns -> {
            String recordId = csvValue(columns, 3);
            String performedBy = csvValue(columns, 4);
            String oldValue = csvValue(columns, 6);
            String newValue = csvValue(columns, 7);
            return probe.equals(performedBy)
                    || containsEmployeeIdToken(recordId, empId)
                    || containsEmployeeIdToken(oldValue, empId)
                    || containsEmployeeIdToken(newValue, empId);
        });
    }

    private int deleteRowsContainingEmployeeId(String filePath, int empId) {
        return rewriteCsvExcludingRows(filePath, columns -> {
            for (String value : columns) {
                if (containsEmployeeIdToken(value, empId)) {
                    return true;
                }
            }
            return false;
        });
    }

    private int rewriteCsvExcludingRows(String filePath, CsvRowDeleteRule rule) {
        if (rule == null || filePath == null || filePath.trim().isEmpty()) {
            return 0;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return 0;
            }

            List<String> out = new ArrayList<>();
            out.add(lines.get(0));
            int removed = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(CSV_SPLIT_REGEX, -1);
                if (rule.shouldDelete(columns)) {
                    removed++;
                    continue;
                }
                out.add(line);
            }

            if (removed == 0) {
                return 0;
            }

            Files.write(path, out, StandardCharsets.UTF_8);
            return removed;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean deleteFileIfExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return true;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return true;
        }

        return file.delete();
    }

    private int deleteFilesByPrefix(String folderPath, String filePrefix) {
        if (folderPath == null || filePrefix == null) {
            return 0;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return 0;
        }

        File[] matchingFiles = folder.listFiles((dir, name)
                -> name != null
                && name.startsWith(filePrefix)
                && name.toLowerCase(Locale.US).endsWith(".csv"));

        if (matchingFiles == null || matchingFiles.length == 0) {
            return 0;
        }

        int deleted = 0;
        for (File file : matchingFiles) {
            if (file != null && (!file.exists() || file.delete())) {
                deleted++;
            }
        }
        return deleted;
    }

    private boolean columnValueEquals(String[] columns, int columnIndex, String expectedValue) {
        return expectedValue != null && expectedValue.equals(csvValue(columns, columnIndex));
    }

    private String csvValue(String[] columns, int index) {
        if (columns == null || index < 0 || index >= columns.length) {
            return "";
        }
        return unquote(columns[index]);
    }

    private boolean containsEmployeeIdToken(String text, int empId) {
        if (empId <= 0 || text == null || text.trim().isEmpty()) {
            return false;
        }
        String token = String.valueOf(empId);
        return Pattern.compile("(?<!\\d)" + Pattern.quote(token) + "(?!\\d)").matcher(text).find();
    }

    private String unquote(String value) {
        if (value == null) {
            return "";
        }
        String clean = value.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
            clean = clean.substring(1, clean.length() - 1).replace("\"\"", "\"");
        }
        return clean.trim();
    }

    private interface CsvRowDeleteRule {

        boolean shouldDelete(String[] columns);
    }

    private static final class CleanupSummary {

        private boolean loginDeleted = true;
        private int leaveCreditsRowsRemoved = 0;
        private boolean dtrFileDeleted = true;
        private boolean leaveFileDeleted = true;
        private boolean payrollFileDeleted = true;
        private int payslipFilesDeleted = 0;
        private int systemLogRowsRemoved = 0;
        private int auditLogRowsRemoved = 0;
        private int dtrLogRowsRemoved = 0;
        private int employeeDataLogRowsRemoved = 0;
        private int payrollLogRowsRemoved = 0;

        private boolean isCoreCleanupSuccessful() {
            return loginDeleted && dtrFileDeleted && leaveFileDeleted && payrollFileDeleted;
        }
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim().replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }
    @Override
    public boolean isEmployeeIdDuplicate(int empId) {
        // Query the repository to see if the employee ID is already taken
        return empRepo.findById(empId) != null;
    }
}
