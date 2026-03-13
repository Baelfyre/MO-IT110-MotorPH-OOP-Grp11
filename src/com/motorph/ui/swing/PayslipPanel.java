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
import com.motorph.service.EmployeeService;
import com.motorph.service.LeaveCreditsService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

    private final JComboBox<String> cbYear = new JComboBox<>();
    private final JComboBox<String> cbMonth = new JComboBox<>();
    private final JComboBox<String> cbCycle = new JComboBox<>();
    private final JButton btnClearFilters = new JButton("Clear Filters");
    private final JButton btnRefreshHistory = new JButton("Refresh History");
    private final JLabel lblCurrentPeriod = new JLabel("Current Period: -");

    private final JTextPane txtPayslip = new JTextPane();

    private final DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"TX", "Period", "Cycle", "Net Pay"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tblHistory = new JTable(historyModel);
    private List<Payslip> cachedHistory = new ArrayList<>();

    public PayslipPanel(User currentUser, PayslipOps payslipOps, EmployeeService employeeService, LeaveCreditsService leaveCreditsService) {
        initComponents();
        this.currentUser = currentUser;
        this.payslipOps = payslipOps;
        this.employeeService = employeeService;
        this.leaveCreditsService = leaveCreditsService;

        buildUi();
        initFilters();
        updateCurrentPeriodLabel();
        refreshHistory();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(lblCurrentPeriod);
        top.add(new JLabel("Year:"));
        top.add(cbYear);
        top.add(new JLabel("Month:"));
        top.add(cbMonth);
        top.add(new JLabel("Cycle:"));
        top.add(cbCycle);
        top.add(btnClearFilters);
        top.add(btnRefreshHistory);

        txtPayslip.setContentType("text/html");
        txtPayslip.setEditable(false);

        JScrollPane historyScroll = new JScrollPane(tblHistory);
        historyScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Payslip History"));

        JScrollPane payslipScroll = new JScrollPane(txtPayslip);
        payslipScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Payslip Preview"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, historyScroll, payslipScroll);
        split.setResizeWeight(0.32);
        split.setDividerLocation(430);
        split.setContinuousLayout(true);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        btnRefreshHistory.addActionListener(e -> refreshHistory());
        btnClearFilters.addActionListener(e -> resetFilters());
        cbYear.addActionListener(e -> applyHistoryFilters());
        cbMonth.addActionListener(e -> applyHistoryFilters());
        cbCycle.addActionListener(e -> applyHistoryFilters());

        tblHistory.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblHistory.setRowHeight(24);
        tblHistory.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            onHistorySelected();
        });
    }

    private void initFilters() {
        cbYear.addItem("All Years");
        cbMonth.addItem("All Months");
        cbCycle.addItem("All Cycles");
        cbCycle.addItem("1st Half");
        cbCycle.addItem("2nd Half");
        for (Month month : Month.values()) {
            cbMonth.addItem(month.getDisplayName(TextStyle.FULL, Locale.US));
        }
    }

    private void updateCurrentPeriodLabel() {
        PayPeriod currentPeriod = PayPeriod.fromDateSemiMonthly(LocalDate.now());
        lblCurrentPeriod.setText("Current Period: " + currentPeriod.getStartDate() + " to " + currentPeriod.getEndDate());
    }

    private int empId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void refreshHistory() {
        try {
            cachedHistory = payslipOps.listPayslipHistory(empId(), currentUser);
            refreshYearFilterValues();
            applyHistoryFilters();
        } catch (SecurityException ex) {
            UiDialogs.error(this, ex.getMessage());
            cachedHistory = new ArrayList<>();
            historyModel.setRowCount(0);
            clearDetails();
        }
    }

    private void refreshYearFilterValues() {
        String selectedYear = (String) cbYear.getSelectedItem();
        Set<String> years = new LinkedHashSet<>();
        for (Payslip payslip : cachedHistory) {
            if (payslip != null && payslip.getPeriod() != null && payslip.getPeriod().getStartDate() != null) {
                years.add(String.valueOf(payslip.getPeriod().getStartDate().getYear()));
            }
        }

        cbYear.removeAllItems();
        cbYear.addItem("All Years");
        years.stream().sorted().forEach(cbYear::addItem);
        restoreSelection(cbYear, selectedYear);
    }

    private void restoreSelection(JComboBox<String> comboBox, String selectedValue) {
        if (selectedValue != null) {
            comboBox.setSelectedItem(selectedValue);
            if (comboBox.getSelectedIndex() < 0) {
                comboBox.setSelectedIndex(0);
            }
        }
    }

    private void resetFilters() {
        cbYear.setSelectedIndex(0);
        cbMonth.setSelectedIndex(0);
        cbCycle.setSelectedIndex(0);
        applyHistoryFilters();
    }

    private void applyHistoryFilters() {
        historyModel.setRowCount(0);
        String yearFilter = (String) cbYear.getSelectedItem();
        String monthFilter = (String) cbMonth.getSelectedItem();
        String cycleFilter = (String) cbCycle.getSelectedItem();

        for (Payslip payslip : cachedHistory) {
            if (payslip == null || payslip.getPeriod() == null || payslip.getPeriod().getStartDate() == null) {
                continue;
            }
            if (!matchesYear(payslip, yearFilter)) {
                continue;
            }
            if (!matchesMonth(payslip, monthFilter)) {
                continue;
            }
            if (!matchesCycle(payslip, cycleFilter)) {
                continue;
            }

            historyModel.addRow(new Object[]{
                    payslip.getTransactionId(),
                    payslip.getPeriod().toKey(),
                    getCycleLabel(payslip.getPeriod()),
                    peso(payslip.getNetPay())
            });
        }

        if (historyModel.getRowCount() == 0) {
            clearDetails();
        }
    }

    private boolean matchesYear(Payslip payslip, String filter) {
        if (filter == null || "All Years".equals(filter)) {
            return true;
        }
        return String.valueOf(payslip.getPeriod().getStartDate().getYear()).equals(filter);
    }

    private boolean matchesMonth(Payslip payslip, String filter) {
        if (filter == null || "All Months".equals(filter)) {
            return true;
        }
        return payslip.getPeriod().getStartDate().getMonth().getDisplayName(TextStyle.FULL, Locale.US).equalsIgnoreCase(filter);
    }

    private boolean matchesCycle(Payslip payslip, String filter) {
        if (filter == null || "All Cycles".equals(filter)) {
            return true;
        }
        return getCycleLabel(payslip.getPeriod()).equalsIgnoreCase(filter);
    }

    private String getCycleLabel(PayPeriod period) {
        if (period == null || period.getStartDate() == null) {
            return "";
        }
        return period.getStartDate().getDayOfMonth() <= 15 ? "1st Half" : "2nd Half";
    }

    private void onHistorySelected() {
        int viewRow = tblHistory.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int modelRow = tblHistory.convertRowIndexToModel(viewRow);
        Object txObj = historyModel.getValueAt(modelRow, 0);
        if (txObj == null) {
            return;
        }

        String tx = String.valueOf(txObj).trim();
        if (tx.isEmpty()) {
            return;
        }

        for (Payslip payslip : cachedHistory) {
            if (payslip != null && tx.equals(payslip.getTransactionId())) {
                showPayslip(payslip);
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
