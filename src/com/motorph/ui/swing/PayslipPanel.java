/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LeaveCredits;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;
import com.motorph.ops.payslip.PayslipOps;
import com.motorph.ops.payslip.PayslipOpsImpl;
import com.motorph.repository.PayslipRepository;
import com.motorph.repository.csv.CsvPayslipRepository;
import com.motorph.service.EmployeeService;
import com.motorph.service.LeaveCreditsService;
import com.motorph.service.LogService;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;

/**
 * Payslip viewing panel.
 *
 * @author ACER
 */
public class PayslipPanel extends JPanel {

    private final User currentUser;
    private final PayslipOps payslipOps;
    private final EmployeeService employeeService;
    private final LeaveCreditsService leaveCreditsService;

    private final JDateChooser dcAnyDate = new JDateChooser();
    private final JLabel lblPeriod = new JLabel("Period: -");
    private PayPeriod activePeriod;

    private final JButton btnSetPeriod = new JButton("Set Period");
    private final JButton btnViewLatest = new JButton("View Latest");
    private final JButton btnViewPeriod = new JButton("View Period");
    private final JButton btnRefreshHistory = new JButton("Refresh History");

    private final JTextPane txtPayslip = new JTextPane();

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"TX", "Period", "Net Pay"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblHistory = new JTable(historyModel);

    public PayslipPanel(User currentUser) {
        this(currentUser, new PayslipOpsImpl(new CsvPayslipRepository(), new LogService()), new EmployeeService(new com.motorph.repository.csv.CsvEmployeeRepository()), new LeaveCreditsService(new com.motorph.repository.csv.CsvLeaveCreditsRepository(), new com.motorph.service.LeaveService(new com.motorph.repository.csv.CsvLeaveRepository())));
    }

    public PayslipPanel(User currentUser, PayslipOps payslipOps, EmployeeService employeeService, LeaveCreditsService leaveCreditsService) {
        this.currentUser = currentUser;
        this.payslipOps = payslipOps;
        this.employeeService = employeeService;
        this.leaveCreditsService = leaveCreditsService;

        buildUi();

        dcAnyDate.setDateFormatString("MM/dd/yyyy");
        if (dcAnyDate.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcAnyDate.setDate(new Date());
        setActivePeriod(LocalDate.now());
        refreshHistory();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Any date inside period:"));
        top.add(dcAnyDate);
        top.add(btnSetPeriod);
        top.add(lblPeriod);
        top.add(btnViewLatest);
        top.add(btnViewPeriod);
        top.add(btnRefreshHistory);

        txtPayslip.setContentType("text/html");
        txtPayslip.setEditable(false);

        JScrollPane historyScroll = new JScrollPane(tblHistory);
        historyScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Payslip History"));

        JScrollPane payslipScroll = new JScrollPane(txtPayslip);
        payslipScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Payslip Preview"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, historyScroll, payslipScroll);
        split.setResizeWeight(0.24);
        split.setDividerLocation(320);
        split.setContinuousLayout(true);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        btnSetPeriod.addActionListener(e -> {
            LocalDate d = LocalDates.toLocalDate(dcAnyDate.getDate());
            if (d == null) {
                UiDialogs.warn(this, "Select a date.");
                return;
            }
            setActivePeriod(d);
        });

        btnViewLatest.addActionListener(e -> viewLatest());
        btnViewPeriod.addActionListener(e -> viewPeriod());
        btnRefreshHistory.addActionListener(e -> refreshHistory());

        tblHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblHistory.setRowHeight(24);
        tblHistory.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onHistorySelected();
        });
    }

    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
    }

    private int empId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void viewLatest() {
        Payslip p = payslipOps.viewLatestPayslip(empId());
        if (p == null) {
            clearDetails();
            UiDialogs.warn(this, "No payslip found.");
            return;
        }
        showPayslip(p);
    }

    private void viewPeriod() {
        if (activePeriod == null) {
            UiDialogs.warn(this, "Set the period first.");
            return;
        }
        Payslip p = payslipOps.viewPayslipForPeriod(empId(), activePeriod);
        if (p == null) {
            clearDetails();
            UiDialogs.warn(this, "No payslip found for this period.");
            return;
        }
        showPayslip(p);
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        List<Payslip> list = payslipOps.listPayslipHistory(empId());
        for (Payslip p : list) {
            String periodKey = (p.getPeriod() == null) ? "" : p.getPeriod().toKey();
            historyModel.addRow(new Object[]{p.getTransactionId(), periodKey, peso(p.getNetPay())});
        }
    }

    private void onHistorySelected() {
        int viewRow = tblHistory.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tblHistory.convertRowIndexToModel(viewRow);
        Object txObj = historyModel.getValueAt(modelRow, 0);
        if (txObj == null) return;

        String tx = String.valueOf(txObj).trim();
        if (tx.isEmpty()) return;

        List<Payslip> list = payslipOps.listPayslipHistory(empId());
        for (Payslip p : list) {
            if (tx.equals(p.getTransactionId())) {
                showPayslip(p);
                return;
            }
        }
    }

    private void showPayslip(Payslip p) {
        Employee emp = employeeService == null ? null : employeeService.getEmployee(p.getEmployeeId());
        String positionDept = emp == null ? "" : safe(emp.getPosition());
        int daysWorked = (int) Math.round(Math.min(10.0, p.getTotalHoursWorked() / 8.0));
        double overtime = p.getOvertimePay();
        double benefitsTotal = p.getRiceAllowance() + p.getPhoneAllowance() + p.getClothingAllowance();
        double dailyRate = p.getBasicSalary() / 20.0;

        double leaveCredits = p.getLeaveCreditsSnapshot();
        double leaveTaken = p.getLeaveTakenSnapshot();
        double leaveBalance = p.getLeaveBalanceSnapshot();
        if (leaveCredits == 0.0 && leaveTaken == 0.0 && leaveBalance == 0.0 && leaveCreditsService != null && p.getPeriod() != null) {
            leaveCredits = leaveCreditsService.getStoredLeaveCreditsHours(p.getEmployeeId());
            leaveTaken = leaveCreditsService.getStoredLeaveTakenHours(p.getEmployeeId());
            leaveBalance = leaveCreditsService.getStoredRemainingHours(p.getEmployeeId());
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI; font-size:12px; margin:8px;'>");
        html.append("<div style='font-size:28px; font-weight:bold; color:#1f3f75; margin-bottom:2px;'>MotorPH</div>");
        html.append("<div style='font-size:11px; color:#333;'>7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City<br>");
        html.append("Phone: (028) 911-507 / (028) 91-5073<br>Email: corporate@motorph.com</div>");
        html.append("<div style='text-align:center; font-size:24px; font-weight:bold; color:#1f3f75; margin:18px 0 12px 0;'>EMPLOYEE PAYSLIP</div>");

        html.append(twoColHeader("PAYSLIP NO", safe(p.getPayslipNumber()), "PERIOD START DATE", fmtDate(p.getPeriod() == null ? null : p.getPeriod().getStartDate())));
        html.append(twoColHeader("EMPLOYEE ID", String.valueOf(p.getEmployeeId()), "PERIOD END DATE", fmtDate(p.getPeriod() == null ? null : p.getPeriod().getEndDate())));
        html.append(twoColHeader("EMPLOYEE NAME", safe(p.getLastName()) + ", " + safe(p.getFirstName()), "EMPLOYEE POSITION/DEPARTMENT", safe(positionDept)));

        html.append(sectionTitle("EARNINGS"));
        html.append(line("Monthly Salary", peso(p.getBasicSalary())));
        html.append(line("Daily Rate", peso(dailyRate)));
        html.append(line("Days Worked", String.valueOf(daysWorked)));
        html.append(line("Overtime", peso(overtime)));
        html.append(totalLine("GROSS INCOME", peso(p.getGrossIncome())));

        html.append(sectionTitle("BENEFITS"));
        html.append(line("Rice Subsidy", peso(p.getRiceAllowance())));
        html.append(line("Phone Allowance", peso(p.getPhoneAllowance())));
        html.append(line("Clothing Allowance", peso(p.getClothingAllowance())));
        html.append(totalLine("TOTAL", peso(benefitsTotal)));

        html.append(sectionTitle("DEDUCTIONS"));
        html.append(line("Social Security System", peso(p.getSss())));
        html.append(line("PhilHealth", peso(p.getPhilHealth())));
        html.append(line("Pag-ibig", peso(p.getPagIbig())));
        html.append(line("Withholding Tax", peso(p.getWithholdingTax())));
        if (p.getLateDeduction() > 0.0) {
            html.append(line("Late/Undertime", peso(p.getLateDeduction())));
        }
        html.append(totalLine("TOTAL DEDUCTIONS", peso(p.getTotalDeductions())));

        html.append(sectionTitle("SUMMARY"));
        html.append(line("Gross Income", peso(p.getGrossIncome())));
        html.append(line("Benefits", peso(benefitsTotal)));
        html.append(line("Deductions", peso(p.getTotalDeductions())));
        html.append(totalLine("TAKE HOME PAY", peso(p.getNetPay())));

        html.append(sectionTitle("LEAVE SNAPSHOT"));
        html.append(line("Leave Credits", fmtHours(leaveCredits)));
        html.append(line("Leave Taken", fmtHours(leaveTaken)));
        html.append(totalLine("Remaining Leave Balance", fmtHours(leaveBalance)));

        html.append("</body></html>");
        txtPayslip.setText(html.toString());
        txtPayslip.setCaretPosition(0);
    }

    private String sectionTitle(String title) {
        return "<div style='background:#25364b; color:white; font-weight:bold; padding:6px 8px; margin-top:14px;'>" + esc(title) + "</div>";
    }

    private String line(String label, String value) {
        return "<table style='width:100%; border-collapse:collapse;'><tr><td style='padding:4px 8px;'>" + esc(label) + "</td><td style='padding:4px 8px; text-align:right;'>" + esc(value) + "</td></tr></table>";
    }

    private String totalLine(String label, String value) {
        return "<table style='width:100%; border-collapse:collapse; background:#ececec;'><tr><td style='padding:4px 8px; font-weight:bold;'>" + esc(label) + "</td><td style='padding:4px 8px; text-align:right; font-weight:bold;'>" + esc(value) + "</td></tr></table>";
    }

    private String twoColHeader(String aLabel, String aVal, String bLabel, String bVal) {
        return "<table style='width:100%; border-collapse:collapse; margin-bottom:2px;'><tr>"
                + "<td style='width:18%; background:#25364b; color:white; padding:6px 8px; font-weight:bold;'>" + esc(aLabel) + "</td>"
                + "<td style='width:32%; border:1px solid #c3c3c3; padding:6px 8px;'>" + esc(aVal) + "</td>"
                + "<td style='width:18%; background:#25364b; color:white; padding:6px 8px; font-weight:bold;'>" + esc(bLabel) + "</td>"
                + "<td style='width:32%; border:1px solid #c3c3c3; padding:6px 8px;'>" + esc(bVal) + "</td>"
                + "</tr></table>";
    }

    private void clearDetails() {
        txtPayslip.setText("");
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String fmtDate(LocalDate d) { return d == null ? "" : (d.getMonthValue() + "/" + d.getDayOfMonth() + "/" + d.getYear()); }
    private String peso(double amount) { return "₱" + String.format(Locale.US, "%,.2f", amount); }
    private String fmtHours(double h) { return String.format(Locale.US, "%,.2f hours", h); }
    private String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
