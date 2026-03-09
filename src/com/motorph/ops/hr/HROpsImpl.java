/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.hr;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;

import java.util.ArrayList;
import java.util.List;

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

        int empId = emp.getId();
        String username = String.valueOf(empId);

        // deny duplicate employee
        if (empRepo.findById(empId) != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
                    "HR_CREATE_DENIED_DUPLICATE",
                    "Denied create. Employee already exists EmpID=" + empId
            );
            return false;
        }

        // deny duplicate login before writing employee
        if (userRepo.findByUsername(username) != null) {
            logService.recordAction(
                    String.valueOf(currentUser.getId()),
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
                com.motorph.repository.csv.DataPaths.DEFAULT_PASSWORD,
                employeeService.determineRoleFromPosition(emp.getPosition()),
                false
        );
        userRepo.save(login, emp.getFirstName(), emp.getLastName(), emp.getPosition());

        // combined audit log
        logService.recordAction(
                String.valueOf(currentUser.getId()),
                "HR_CREATE_OK",
                "Created employee profile and login. EmpID=" + empId
                + ", Username=" + username
                + ", Roles=" + login.getRoles()
                + ", Position=" + emp.getPosition()
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
        boolean loginDeleted = userRepo.deleteByUsername(username);

        if (empDeleted) {
            employeeService.refreshCache();
        }

        logService.recordAction(
                String.valueOf(currentUser.getId()),
                empDeleted ? "HR_DELETE_OK" : "HR_DELETE_FAILED",
                "Deleted employee. EmpID=" + empId
        );

        return empDeleted;
    }
    @Override
    public boolean isEmployeeIdDuplicate(int empId) {
        // Query the repository to see if the employee ID is already taken
        return empRepo.findById(empId) != null;
    }
}
