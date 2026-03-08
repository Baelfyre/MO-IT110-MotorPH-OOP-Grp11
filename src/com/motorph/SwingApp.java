/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.motorph;

import com.motorph.ops.approval.DtrApprovalOps;
import com.motorph.ops.approval.DtrApprovalOpsImpl;
import com.motorph.ops.hr.HROps;
import com.motorph.ops.hr.HROpsImpl;
import com.motorph.ops.payroll.PayrollOps;
import com.motorph.ops.payroll.PayrollOpsImpl;
import com.motorph.ops.payslip.PayslipOps;
import com.motorph.ops.payslip.PayslipOpsImpl;
import com.motorph.ops.supervisor.SupervisorOps;
import com.motorph.ops.supervisor.SupervisorOpsImpl;
import com.motorph.ops.time.TimeOps;
import com.motorph.ops.time.TimeOpsImpl;
import com.motorph.ops.it.ItOps;
import com.motorph.ops.it.ItOpsImpl;

import com.motorph.repository.AuditRepository;
import com.motorph.repository.EmployeeRepository;
import com.motorph.repository.PayslipRepository;
import com.motorph.repository.TimeEntryRepository;
import com.motorph.repository.UserRepository;
import com.motorph.repository.PayrollApprovalRepository;

import com.motorph.repository.csv.CsvAuditRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import com.motorph.repository.csv.CsvPayslipRepository;
import com.motorph.repository.csv.CsvTimeRepository;
import com.motorph.repository.csv.CsvUserRepository;
import com.motorph.repository.csv.CsvPayrollApprovalRepository;

import com.motorph.service.AuthService;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;
import com.motorph.service.PayrollService;
import com.motorph.service.TimeService;

import com.motorph.service.strategy.DeductionStrategy;
import com.motorph.service.strategy.PayDeductionStrategy;

import com.motorph.ops.auth.AuthOps;         // Import AuthOps
import com.motorph.ops.auth.AuthOpsImpl;     // Import AuthOpsImpl

/**
 * The starting point. It initializes all repositories and services, then opens
 * the LoginView.
 *
 * @author OngoJ
 */
public class SwingApp {

    public static void main(String[] args) {

// Repositories
        TimeEntryRepository timeRepo = new CsvTimeRepository();
        EmployeeRepository empRepo = new CsvEmployeeRepository();
        UserRepository userRepo = new CsvUserRepository();
        AuditRepository auditRepo = new CsvAuditRepository();
        PayslipRepository payslipRepo = new CsvPayslipRepository();
        PayrollApprovalRepository approvalRepo = new CsvPayrollApprovalRepository();

// Services
        TimeService timeService = new TimeService(timeRepo);
        AuthService authService = new AuthService(userRepo);
        LogService logService = new LogService();
        EmployeeService employeeService = new EmployeeService(empRepo);

        DeductionStrategy deductionStrategy = new PayDeductionStrategy();
        PayrollService payrollService = new PayrollService(empRepo, timeRepo, deductionStrategy, payslipRepo, auditRepo);

// Ops
        TimeOps timeOps = new TimeOpsImpl(timeService, timeRepo, logService);
        PayrollOps payrollOps = new PayrollOpsImpl(payrollService, empRepo, logService, approvalRepo);
        DtrApprovalOps dtrApprovalOps = new DtrApprovalOpsImpl(approvalRepo, auditRepo);
        PayslipOps payslipOps = new PayslipOpsImpl(payslipRepo, logService);
        SupervisorOps supervisorOps = new SupervisorOpsImpl(
                employeeService,
                timeRepo,
                approvalRepo,
                dtrApprovalOps,
                logService
        );
        HROps hrOps = new HROpsImpl(empRepo, employeeService, userRepo, logService);
        ItOps itOps = new ItOpsImpl(userRepo, logService);

        //Initialize the AuthOps layer
        AuthOps authOps = new AuthOpsImpl(authService, logService);

        // --- FINAL UI WIRING ---
        java.awt.EventQueue.invokeLater(() -> {
            // Ensure Nimbus Theme is set globally before launching UI
            com.motorph.ui.swing.UiHelper.UiThemeHelper.useFlatLaf();

            // Pass the successfully wired backend directly into the UI
            com.motorph.ui.swing.LoginPanel login = new com.motorph.ui.swing.LoginPanel(authOps, employeeService, timeOps, hrOps);
            login.setVisible(true);
        });
        // UI wiring goes here (later)
        // LoginView login = new LoginView(authService, ...);
        // login.setVisible(true);
    }
}
