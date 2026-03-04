/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;
import com.motorph.ops.approval.DtrApprovalOps;
import com.motorph.ops.approval.DtrApprovalOpsImpl;
import com.motorph.ops.supervisor.SupervisorDtrSummary;
import com.motorph.ops.supervisor.SupervisorOps;
import com.motorph.ops.supervisor.SupervisorOpsImpl;
import com.motorph.repository.*;
import com.motorph.repository.csv.*;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Supervisor panel for DTR review and approval.
 * @author ACER
 */
public class SupervisorPanel extends JPanel {

    private final User currentUser;
    private final SupervisorOps supervisorOps;

    private final JSpinner spAnyDate = new JSpinner(new SpinnerDateModel());
    private final JLabel lblPeriod = new JLabel("Period: -");
    private PayPeriod activePeriod;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"EmpID", "Name", "DTR Status", "Payroll Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);

    private final JButton btnSetPeriod = new JButton("Set Period");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnView = new JButton("View Entries");
    private final JButton btnApprove = new JButton("Approve DTR");
    private final JButton btnReject = new JButton("Reject DTR");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    public SupervisorPanel(User currentUser) {
        this.currentUser = currentUser;

        EmployeeRepository empRepo = new CsvEmployeeRepository();
        EmployeeService employeeService = new EmployeeService(empRepo);
        TimeEntryRepository timeRepo = new CsvTimeRepository(empRepo);
        PayrollApprovalRepository approvalRepo = new CsvPayrollApprovalRepository();
        AuditRepository auditRepo = new CsvAuditRepository();
        DtrApprovalOps dtrApprovalOps = new DtrApprovalOpsImpl(approvalRepo, auditRepo);
        LogService logService = new LogService();

        this.supervisorOps = new SupervisorOpsImpl(employeeService, timeRepo, approvalRepo, dtrApprovalOps, logService);

        buildUi();

        spAnyDate.setEditor(new JSpinner.DateEditor(spAnyDate, "MM/dd/yyyy"));
        setActivePeriod(LocalDate.now());
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Any date inside period:"));
        top.add(spAnyDate);
        top.add(btnSetPeriod);
        top.add(lblPeriod);
        top.add(btnRefresh);
        top.add(btnView);
        top.add(btnApprove);
        top.add(btnReject);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnSetPeriod.addActionListener(e -> {
            LocalDate d = LocalDates.toLocalDate((Date) spAnyDate.getValue());
            if (d == null) {
                UiDialogs.warn(this, "Select a date.");
                return;
            }
            setActivePeriod(d);
        });

        btnRefresh.addActionListener(e -> reload());
        btnView.addActionListener(e -> onView());
        btnApprove.addActionListener(e -> onApprove());
        btnReject.addActionListener(e -> onReject());
    }

    private int supervisorEmpId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
        reload();
    }

    // Annotation: Loads direct report statuses into a table.
    private void reload() {
        model.setRowCount(0);
        if (activePeriod == null) {
            return;
        }

        List<SupervisorDtrSummary> list = supervisorOps.listDirectReportStatuses(supervisorEmpId(), activePeriod);
        for (SupervisorDtrSummary s : list) {
            model.addRow(new Object[]{
                    s.getEmployeeId(),
                    s.getEmployeeName(),
                    s.getDtrStatus().name(),
                    s.getPayrollStatus().name()
            });
        }
    }

    private Integer selectedReportId() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tbl.convertRowIndexToModel(viewRow);
        Object v = model.getValueAt(modelRow, 0);
        if (v == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void onView() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        List<TimeEntry> entries = supervisorOps.viewDirectReportTimeEntries(supervisorEmpId(), reportId, activePeriod);

        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Date", "In", "Out"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (TimeEntry t : entries) {
            tm.addRow(new Object[]{
                    t.getDate() == null ? "" : t.getDate().format(DATE_FMT),
                    t.getTimeIn() == null ? "" : t.getTimeIn().format(TIME_FMT),
                    t.getTimeOut() == null ? "" : t.getTimeOut().format(TIME_FMT)
            });
        }

        JTable table = new JTable(tm);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "DTR Entries: " + reportId, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(table), closeRow(dlg)));
        dlg.setSize(550, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel closeRow(JDialog dlg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        p.add(close);
        return p;
    }

    private void onApprove() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        boolean ok = supervisorOps.approveDirectReportDtr(supervisorEmpId(), reportId, activePeriod);
        if (ok) {
            UiDialogs.info(this, "DTR approved.");
        } else {
            UiDialogs.warn(this, "Approve failed. Check if payroll already approved or report is not direct.");
        }
        reload();
    }

    private void onReject() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        boolean ok = supervisorOps.rejectDirectReportDtr(supervisorEmpId(), reportId, activePeriod);
        if (ok) {
            UiDialogs.info(this, "DTR rejected.");
        } else {
            UiDialogs.warn(this, "Reject failed. Check if payroll already approved or report is not direct.");
        }
        reload();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );

        add(jPanel2, java.awt.BorderLayout.LINE_END);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );

        add(jPanel4, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
