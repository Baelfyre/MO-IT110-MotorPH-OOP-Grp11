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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author ACER
 */
public class HROpsImpl implements HROps {

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
    public boolean createEmployee(Employee emp, int performedByUserId) {
        if (emp == null) {
            return false;
        }

        int empId = emp.getId();
        String username = String.valueOf(empId);

        // deny duplicate employee
        if (empRepo.findById(empId) != null) {
            logService.recordAction(
                    String.valueOf(performedByUserId),
                    "HR_CREATE_DENIED_DUPLICATE",
                    "Denied create. Employee already exists EmpID=" + empId
            );
            return false;
        }

        // deny duplicate login before writing employee
        if (userRepo.findByUsername(username) != null) {
            logService.recordAction(
                    String.valueOf(performedByUserId),
                    "HR_CREATE_DENIED_LOGIN_DUPLICATE",
                    "Denied create. Username already exists Username=" + username
            );
            return false;
        }

        // create employee
        empRepo.create(emp);
        employeeService.refreshCache();

        // auto-create login
        User login = new User(
                empId,
                username,
                DataPaths.DEFAULT_PASSWORD,
                employeeService.determineRoleFromPosition(emp.getPosition()),
                false
        );
        userRepo.save(login, emp.getFirstName(), emp.getLastName(), emp.getPosition());

        // related records are created only after the employee master record exists.
        boolean provisionOk = provisionBaseRecords(emp);

        // combined audit log
        logService.recordAction(
                String.valueOf(performedByUserId),
                provisionOk ? "HR_CREATE_OK" : "HR_CREATE_PARTIAL",
                "Created employee profile and login. EmpID=" + empId
                + ", Username=" + username
                + ", Roles=" + login.getRoles()
                + ", Position=" + emp.getPosition()
                + ", Provisioned=" + provisionOk
        );

        return true;
    }

    @Override
    public boolean updateEmployee(Employee emp, int performedByUserId) {
        if (emp == null) {
            return false;
        }

        if (isRestrictedSelfHrAction(performedByUserId, emp.getId())) {
            logService.recordAction(
                    String.valueOf(performedByUserId),
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
                String.valueOf(performedByUserId),
                ok ? "HR_UPDATE_OK" : "HR_UPDATE_FAILED",
                "Update employee EmpID=" + emp.getId()
        );

        return ok;
    }

    @Override
    public boolean deleteEmployee(int empId, int performedByUserId) {

        Employee existing = empRepo.findById(empId);
        if (existing == null) {
            logService.recordAction(
                    String.valueOf(performedByUserId),
                    "HR_DELETE_DENIED_NOT_FOUND",
                    "Denied delete. Employee not found EmpID=" + empId
            );
            return false;
        }

        if (isRestrictedSelfHrAction(performedByUserId, empId)) {
            logService.recordAction(
                    String.valueOf(performedByUserId),
                    "HR_SELF_DELETE_DENIED",
                    "Denied self-delete for HR user. EmpID=" + empId
            );
            return false;
        }

        boolean empDeleted = empRepo.delete(empId);

        // delete login as well to prevent orphan accounts
        String username = String.valueOf(empId);
        boolean loginDeleted = userRepo.deleteByUsername(username);

        if (empDeleted) {
            employeeService.refreshCache();
        }

        logService.recordAction(
                String.valueOf(performedByUserId),
                empDeleted ? "HR_DELETE_OK" : "HR_DELETE_FAILED",
                "Deleted employee and login. EmpID=" + empId
                + ", Username=" + username
                + ", EmployeeDeleted=" + empDeleted
                + ", LoginDeleted=" + loginDeleted
        );

        return empDeleted;
    }

    private boolean isRestrictedSelfHrAction(int actorEmpId, int targetEmpId) {
        if (actorEmpId <= 0 || actorEmpId != targetEmpId) {
            return false;
        }
        User actor = userRepo.findByUsername(String.valueOf(actorEmpId));
        return actor != null && actor.getRoles().contains(Role.HR);
    }

    // Annotation: Creates DTR, leave, and leave-credits defaults after successful employee creation.
    private boolean provisionBaseRecords(Employee emp) {
        boolean dtr = ensureFileWithHeader(DataPaths.DTR_FOLDER + "records_dtr_" + emp.getId() + ".csv",
                "Attendance_ID,Employee #,Date,Log In,Log Out,First Name,Last Name");

        boolean leave = ensureFileWithHeader(DataPaths.LEAVE_FOLDER + "records_leave_" + emp.getId() + ".csv",
                "Leave_ID,Employee #,Date,Start_Time,End_Time,First Name,Last Name,Status,Reviewed_By,Reviewed_At,Decision_Note");

        boolean credits = ensureLeaveCreditsRow(emp);
        return dtr && leave && credits;
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
}
