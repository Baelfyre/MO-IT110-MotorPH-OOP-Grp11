/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.Payslip;
import com.motorph.domain.models.User;
import com.motorph.ops.payroll.PayrollOps;
import com.motorph.ops.payroll.PayrollQueueItem;
import com.motorph.ops.payroll.PayrollRunResult;
import com.motorph.ops.payslip.PayslipOps;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Displays payroll queue, processing actions, and payslip preview.
 *
 * @author OngoJ.
 */
public class PayrollPanel extends javax.swing.JPanel {

    private static final int COL_EMP_ID = 0;
    private static final int COL_PAYSLIP = 4;
    private static final String ACTION_VIEW = "View";
    private static final String ACTION_NONE = "-";

    private final User currentUser;
    private final PayrollOps payrollOps;
    private final PayslipOps payslipOps;
    private final JDateChooser dcAnyDate = new JDateChooser();
    private final JLabel lblPeriod = new JLabel("Period: -");
    private PayPeriod activePeriod;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"EmpID", "Employee", "DTR Status", "Payroll Status", "Payslip"}, 0
    ) {
        /**
         * Keeps the table read-only.
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable tbl = new JTable(model);

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
        this.payslipOps = payslipOps;
        initComponents();
        buildUi();
        setActivePeriod(LocalDate.now());
    }

    /**
     * Builds the payroll screen layout and actions.
     */
    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dcAnyDate.setDateFormatString("MM/dd/yyyy");
        if (dcAnyDate.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcAnyDate.setDate(new Date());

        JButton btnSetPeriod = new JButton("Set Period");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnProcessSelected = new JButton("Process Selected");
        JButton btnProcessAll = new JButton("Process All Approved");

        top.add(new JLabel("Any date inside period:"));
        top.add(dcAnyDate);
        top.add(btnSetPeriod);
        top.add(lblPeriod);
        top.add(btnRefresh);
        top.add(btnProcessSelected);
        top.add(btnProcessAll);

        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setRowHeight(28);
        installPayslipLinkColumn();

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnSetPeriod.addActionListener(e -> {
            LocalDate date = LocalDates.toLocalDate(dcAnyDate.getDate());
            if (date == null) {
                UiDialogs.warn(this, "Select a date.");
                return;
            }
            setActivePeriod(date);
        });
        btnRefresh.addActionListener(e -> reload());
        btnProcessSelected.addActionListener(e -> onProcessSelected());
        btnProcessAll.addActionListener(e -> onProcessAll());
    }

    /**
     * Installs the payslip action column behavior.
     */
    private void installPayslipLinkColumn() {
        if (tbl.getColumnModel().getColumnCount() <= COL_PAYSLIP) {
            return;
        }

        tbl.getColumnModel().getColumn(COL_PAYSLIP).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(COL_PAYSLIP).setMaxWidth(100);
        tbl.getColumnModel().getColumn(COL_PAYSLIP).setMinWidth(80);
        tbl.getColumnModel().getColumn(COL_PAYSLIP).setCellRenderer(new PayslipLinkRenderer());

        tbl.addMouseListener(new MouseAdapter() {
            /**
             * Opens the saved payslip when the link cell is clicked.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = tbl.rowAtPoint(e.getPoint());
                int viewCol = tbl.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol < 0) {
                    return;
                }

                int modelCol = tbl.convertColumnIndexToModel(viewCol);
                if (modelCol != COL_PAYSLIP) {
                    return;
                }

                int modelRow = tbl.convertRowIndexToModel(viewRow);
                Object value = model.getValueAt(modelRow, COL_PAYSLIP);
                if (ACTION_VIEW.equals(String.valueOf(value))) {
                    onViewPayslip(modelRow);
                }
            }
        });

        tbl.addMouseMotionListener(new MouseAdapter() {
            /**
             * Changes cursor only for clickable payslip cells.
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                int viewRow = tbl.rowAtPoint(e.getPoint());
                int viewCol = tbl.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol < 0) {
                    tbl.setCursor(Cursor.getDefaultCursor());
                    return;
                }

                int modelCol = tbl.convertColumnIndexToModel(viewCol);
                int modelRow = tbl.convertRowIndexToModel(viewRow);
                Object value = modelCol == COL_PAYSLIP ? model.getValueAt(modelRow, COL_PAYSLIP) : null;
                boolean clickable = ACTION_VIEW.equals(String.valueOf(value));
                tbl.setCursor(clickable
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        });
    }

    /**
     * Sets the active payroll period and refreshes the table.
     */
    private void setActivePeriod(LocalDate date) {
        activePeriod = payrollOps.resolvePeriod(date);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
        reload();
    }

    /**
     * Reloads payroll rows for the active period.
     */
    private void reload() {
        model.setRowCount(0);
        if (activePeriod == null) {
            return;
        }

        List<PayrollQueueItem> rows = payrollOps.listEmployeesForPeriod(activePeriod);
        for (PayrollQueueItem row : rows) {
            model.addRow(new Object[]{
                    row.getEmployeeId(),
                    row.getEmployeeName(),
                    row.getDtrStatus().name(),
                    row.getPayrollStatus().name(),
                    hasApprovedPayroll(row) ? ACTION_VIEW : ACTION_NONE
            });
        }
    }

    /**
     * Checks if the row already has an approved payroll record.
     */
    private boolean hasApprovedPayroll(PayrollQueueItem row) {
        return row != null && row.getPayrollStatus() == ApprovalStatus.APPROVED;
    }

    /**
     * Reads the selected employee ID from the table.
     */
    private Integer selectedEmpId() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tbl.convertRowIndexToModel(viewRow);
        Object value = model.getValueAt(modelRow, COL_EMP_ID);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Processes payroll for the selected employee.
     */
    private void onProcessSelected() {
        Integer empId = selectedEmpId();
        if (empId == null) {
            UiDialogs.warn(this, "Select an employee first.");
            return;
        }

        Payslip payslip = payrollOps.processPayrollForEmployee(empId, activePeriod, actorId());
        if (payslip != null) {
            UiDialogs.info(this, "Payroll processed. Transaction ID: " + payslip.getTransactionId());
            reload();
        } else {
            UiDialogs.warn(this, "Payroll not processed. DTR may not be approved yet, or payroll for this period already exists.");
        }
    }

    /**
     * Processes payroll for all eligible employees.
     */
    private void onProcessAll() {
        List<PayrollRunResult> results = payrollOps.processPayrollForPeriod(activePeriod, actorId());
        showResults(results);
        reload();
    }

    /**
     * Opens the saved payslip for the selected row.
     */
    private void onViewPayslip(int modelRow) {
        if (payslipOps == null) {
            UiDialogs.warn(this, "Payslip view is not available in this screen.");
            return;
        }
        if (activePeriod == null) {
            UiDialogs.warn(this, "Set the payroll period first.");
            return;
        }

        Object empIdValue = model.getValueAt(modelRow, COL_EMP_ID);
        int empId;
        try {
            empId = Integer.parseInt(String.valueOf(empIdValue));
        } catch (NumberFormatException ex) {
            UiDialogs.warn(this, "Invalid employee ID.");
            return;
        }

        Payslip payslip = payslipOps.viewPayslipForPeriod(empId, activePeriod);
        if (payslip == null) {
            UiDialogs.warn(this, "No saved payslip found for this employee and period.");
            return;
        }

        showPayslipDialog(payslip);
    }

    /**
     * Shows a payslip preview dialog.
     */
    private void showPayslipDialog(Payslip payslip) {
        JTextPane txtPayslip = new JTextPane();
        txtPayslip.setEditable(false);
        txtPayslip.setContentType("text/html");
        txtPayslip.setText(buildPayslipHtml(payslip));
        txtPayslip.setCaretPosition(0);

        JButton close = new JButton("Close");
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Payslip Details", JDialog.ModalityType.APPLICATION_MODAL);
        close.addActionListener(e -> dlg.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.add(close);

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(txtPayslip), south));
        dlg.setSize(760, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /**
     * Builds the payslip preview markup.
     */
    private String buildPayslipHtml(Payslip payslip) {
        String periodText = payslip.getPeriod() == null
                ? "-"
                : fmtDate(payslip.getPeriod().getStartDate()) + " to " + fmtDate(payslip.getPeriod().getEndDate());

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI; font-size:12px; padding:12px;'>");
        html.append("<div style='font-size:24px; font-weight:bold; color:#1f3f75; margin-bottom:6px;'>MotorPH Payslip Preview</div>");
        html.append("<table style='width:100%; border-collapse:collapse; margin-bottom:12px;'>");
        appendRow(html, "Transaction ID", safe(payslip.getTransactionId()), "Payslip No.", safe(payslip.getPayslipNumber()));
        appendRow(html, "Employee ID", String.valueOf(payslip.getEmployeeId()), "Employee Name", safe(payslip.getLastName()) + ", " + safe(payslip.getFirstName()));
        appendRow(html, "Pay Period", periodText, "Processed", fmtDateTime(payslip.getDateProcessed()));
        html.append("</table>");

        html.append(sectionTitle("Earnings"));
        html.append(amountLine("Basic Salary", payslip.getBasicSalary()));
        html.append(amountLine("Gross Semi-Monthly", payslip.getGrossSemiMonthlyRate()));
        html.append(amountLine("Hourly Rate", payslip.getHourlyRate()));
        html.append(amountLine("Hours Worked", payslip.getTotalHoursWorked()));
        html.append(amountLine("Gross Income", payslip.getGrossIncome()));

        html.append(sectionTitle("Allowances"));
        html.append(amountLine("Rice Allowance", payslip.getRiceAllowance()));
        html.append(amountLine("Phone Allowance", payslip.getPhoneAllowance()));
        html.append(amountLine("Clothing Allowance", payslip.getClothingAllowance()));

        html.append(sectionTitle("Deductions"));
        html.append(amountLine("SSS", payslip.getSss()));
        html.append(amountLine("PhilHealth", payslip.getPhilHealth()));
        html.append(amountLine("Pag-IBIG", payslip.getPagIbig()));
        html.append(amountLine("Withholding Tax", payslip.getWithholdingTax()));
        html.append(amountLine("Total Deductions", payslip.getTotalDeductions()));

        html.append(sectionTitle("Net Pay"));
        html.append("<div style='font-size:18px; font-weight:bold; margin-top:6px;'>" + peso(payslip.getNetPay()) + "</div>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Adds one detail row to the payslip preview table.
     */
    private void appendRow(StringBuilder html, String leftLabel, String leftValue, String rightLabel, String rightValue) {
        html.append("<tr>")
                .append(cellLabel(leftLabel))
                .append(cellValue(leftValue))
                .append(cellLabel(rightLabel))
                .append(cellValue(rightValue))
                .append("</tr>");
    }

    /**
     * Formats one label cell in the payslip preview.
     */
    private String cellLabel(String value) {
        return "<td style='padding:6px; font-weight:bold; width:18%; border:1px solid #d9d9d9; background:#f5f7fb;'>" + safe(value) + "</td>";
    }

    /**
     * Formats one value cell in the payslip preview.
     */
    private String cellValue(String value) {
        return "<td style='padding:6px; width:32%; border:1px solid #d9d9d9;'>" + safe(value) + "</td>";
    }

    /**
     * Builds one payslip section title.
     */
    private String sectionTitle(String title) {
        return "<div style='margin-top:14px; margin-bottom:6px; font-size:14px; font-weight:bold; color:#1f3f75;'>" + safe(title) + "</div>";
    }

    /**
     * Formats one labeled amount line.
     */
    private String amountLine(String label, double amount) {
        return "<div style='padding:3px 0;'><span style='display:inline-block; min-width:180px; font-weight:bold;'>" + safe(label) + "</span>" + peso(amount) + "</div>";
    }

    /**
     * Formats peso values for preview.
     */
    private String peso(double value) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        return "₱" + format.format(value);
    }

    /**
     * Formats date values for preview.
     */
    private String fmtDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US));
    }

    /**
     * Formats date-time values for preview.
     */
    private String fmtDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Locale.US));
    }

    /**
     * Returns a safe text value for preview.
     */
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Returns the current user ID for audit logging.
     */
    private int actorId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    /**
     * Shows batch processing results in a dialog.
     */
    private void showResults(List<PayrollRunResult> results) {
        DefaultTableModel resultModel = new DefaultTableModel(new Object[]{"EmpID", "Transaction", "Success", "Message"}, 0) {
            /**
             * Keeps the result table read-only.
             */
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (PayrollRunResult result : results) {
            resultModel.addRow(new Object[]{
                    result.getEmployeeId(),
                    result.getTransactionId(),
                    result.isSuccess() ? "Yes" : "No",
                    result.getMessage()
            });
        }

        JTable table = new JTable(resultModel);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Payroll Run Results", JDialog.ModalityType.APPLICATION_MODAL);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);
        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(table), south));
        dlg.setSize(800, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    @SuppressWarnings("unchecked")
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
