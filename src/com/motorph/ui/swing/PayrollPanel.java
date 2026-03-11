/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.User;
import com.motorph.ops.payroll.PayrollOps;
import com.motorph.ops.payroll.PayrollRunResult;
import com.motorph.ops.payslip.PayslipOps;
import java.awt.Color;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PayrollPanel extends JPanel {

    private static final int COL_EMP_ID = 0;
    private static final int COL_PAYSLIP = 4;
    private static final String ACTION_VIEW = "View";
    private static final String ACTION_NONE = "-";

    private final User currentUser;
    private final PayrollOps payrollOps;
    private final JTextArea txtResults;
    private final JButton btnRunPayroll;

    /**
     * Overloaded constructor for screens without payslip preview.
     */
    public PayrollPanel(User currentUser, PayrollOps payrollOps) {
        this(currentUser, payrollOps, null);
    }

    /**
     * Overloaded constructor for screens with payslip preview support.
     */
    public PayrollPanel(User currentUser, PayrollOps payrollOps, PayslipOps payslipOps) {
        this.currentUser = currentUser;
        this.payrollOps = payrollOps;
        
        setLayout(new BorderLayout(10, 10));

        // Top Control Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Payroll Management"));
        
        btnRunPayroll = new JButton("Run Batch Payroll (Current Period)");
        topPanel.add(btnRunPayroll);
        add(topPanel, BorderLayout.NORTH);

        // Results Area
        txtResults = new JTextArea();
        txtResults.setEditable(false);
        add(new JScrollPane(txtResults), BorderLayout.CENTER);

        // UI RBAC: Hide button if they lack permission
        if (currentUser == null || !currentUser.hasPermission("CAN_PROCESS_PAYROLL")) {
            btnRunPayroll.setVisible(false);
            txtResults.setText("You do not have permission to view or process payroll.");
        }

        // Action Listener with crash-prevention
        btnRunPayroll.addActionListener(e -> runBatchPayroll());
    }

    private void runBatchPayroll() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to process payroll for all approved DTRs in the current period?", 
            "Confirm Batch Run", JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        txtResults.setText("Processing...\n");
        PayPeriod currentPeriod = payrollOps.resolvePeriod(LocalDate.now());

        try {
            List<PayrollRunResult> results = payrollOps.processPayrollForPeriod(currentPeriod, currentUser);
            
            for (PayrollRunResult res : results) {
                String status = res.isSuccess() ? "SUCCESS" : "SKIPPED/FAILED";
                txtResults.append("EmpID: " + res.getEmployeeId() + " | " + status + " | " + res.getMessage() + "\n");
            }
            
            txtResults.append("\nBatch process complete.");
            
        } catch (SecurityException ex) {
            // Catches backend verification to prevent runtime crashes
            UiDialogs.error(this, ex.getMessage());
        } catch (Exception ex) {
            UiDialogs.error(this, "An unexpected error occurred: " + ex.getMessage());
        }
    }

    /**
     * Renders the payslip action link in the table.
     */
    private static final class PayslipLinkRenderer extends DefaultTableCellRenderer {

        /**
         * Overrides cell rendering for the payslip action column.
         */
        @Override
        protected void setValue(Object value) {
            String text = value == null ? "" : String.valueOf(value);
            setHorizontalAlignment(CENTER);
            if (ACTION_VIEW.equals(text)) {
                setForeground(new Color(0, 102, 204));
                setText("<html><u>View</u></html>");
            } else {
                setForeground(Color.GRAY);
                setText(ACTION_NONE);
            }
        }
    }
}
