/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;
import com.motorph.ops.payslip.PayslipOps;
import com.motorph.ops.payslip.PayslipOpsImpl;
import com.motorph.repository.PayslipRepository;
import com.motorph.repository.csv.CsvPayslipRepository;
import com.motorph.service.LogService;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Payslip viewing panel.
 * @author ACER
 */

public class PayslipPanel extends JPanel {

    private final User currentUser;
    private final PayslipOps payslipOps;

    private final JDateChooser dcAnyDate = new JDateChooser();
    private final JLabel lblPeriod = new JLabel("Period: -");
    private PayPeriod activePeriod;

    private final JButton btnSetPeriod = new JButton("Set Period");
    private final JButton btnViewLatest = new JButton("View Latest");
    private final JButton btnViewPeriod = new JButton("View Period");
    private final JButton btnRefreshHistory = new JButton("Refresh History");

    private final JTextField txtTx = ro(18);
    private final JTextField txtEmp = ro(12);
    private final JTextField txtGross = ro(12);
    private final JTextField txtDeductions = ro(12);
    private final JTextField txtNet = ro(12);

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
        this.currentUser = currentUser;

        PayslipRepository repo = new CsvPayslipRepository();
        this.payslipOps = new PayslipOpsImpl(repo, new LogService());

        buildUi();

        dcAnyDate.setDateFormatString("MM/dd/yyyy");
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

        JPanel details = buildDetailsPanel();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                details,
                new JScrollPane(tblHistory)
        );
        split.setResizeWeight(0.45);

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

        tblHistory.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            onHistorySelected();
        });
    }

    private JPanel buildDetailsPanel() {
        JPanel p = SwingForm.createFormRoot(10);
        GridBagConstraints gbc = SwingForm.baseGbc();
        int r = 0;

        SwingForm.addLabel(p, gbc, 0, r, "Transaction ID:");
        SwingForm.addFieldSpan(p, gbc, 1, r, 3, txtTx);
        r++;

        SwingForm.addLabel(p, gbc, 0, r, "Employee:");
        SwingForm.addFieldSpan(p, gbc, 1, r, 3, txtEmp);
        r++;

        SwingForm.addLabel(p, gbc, 0, r, "Gross Income:");
        SwingForm.addField(p, gbc, 1, r, txtGross);
        SwingForm.addLabel(p, gbc, 2, r, "Total Deductions:");
        SwingForm.addField(p, gbc, 3, r, txtDeductions);
        r++;

        SwingForm.addLabel(p, gbc, 0, r, "Net Pay:");
        SwingForm.addField(p, gbc, 1, r, txtNet);

        return p;
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
            UiDialogs.warn(this, "No payslip found.");
            clearDetails();
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
            UiDialogs.warn(this, "No payslip found for this period.");
            clearDetails();
            return;
        }
        showPayslip(p);
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        List<Payslip> list = payslipOps.listPayslipHistory(empId());
        for (Payslip p : list) {
            String periodKey = (p.getPeriod() == null) ? "" : p.getPeriod().toKey();
            historyModel.addRow(new Object[]{p.getTransactionId(), periodKey, p.getNetPay()});
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
        txtTx.setText(nullSafe(p.getTransactionId()));
        txtEmp.setText(p.getEmployeeId() + " - " + nullSafe(p.getLastName()) + ", " + nullSafe(p.getFirstName()));
        txtGross.setText(String.format("%.2f", p.getGrossIncome()));
        txtDeductions.setText(String.format("%.2f", p.getTotalDeductions()));
        txtNet.setText(String.format("%.2f", p.getNetPay()));
    }

    private void clearDetails() {
        txtTx.setText("");
        txtEmp.setText("");
        txtGross.setText("");
        txtDeductions.setText("");
        txtNet.setText("");
    }

    private static JTextField ro(int cols) {
        JTextField t = new JTextField(cols);
        t.setEditable(false);
        return t;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
