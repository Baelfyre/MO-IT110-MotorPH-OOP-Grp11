/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.enums.LeaveStatus;
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

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

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

    private final JDateChooser dcAnyDate = new JDateChooser();
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
    private final JButton btnLeaveApproval = new JButton("Leave Request Approval");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    public SupervisorPanel(User currentUser, SupervisorOps supervisorOps) {
        this.currentUser = currentUser;
        this.supervisorOps = supervisorOps;

        buildUi();

        dcAnyDate.setDateFormatString("MM/dd/yyyy");
        if (dcAnyDate.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcAnyDate.setDate(new Date());
        setActivePeriod(LocalDate.now());
    }

    public SupervisorPanel(User currentUser) {
        this.currentUser = currentUser;

        EmployeeRepository empRepo = new CsvEmployeeRepository();
        EmployeeService employeeService = new EmployeeService(empRepo);
        TimeEntryRepository timeRepo = new CsvTimeRepository(empRepo);
        PayrollApprovalRepository approvalRepo = new CsvPayrollApprovalRepository();
        LeaveRepository leaveRepo = new CsvLeaveRepository();
        AuditRepository auditRepo = new CsvAuditRepository();
        DtrApprovalOps dtrApprovalOps = new DtrApprovalOpsImpl(approvalRepo, auditRepo);
        LogService logService = new LogService();

        this.supervisorOps = new SupervisorOpsImpl(employeeService, timeRepo, approvalRepo, leaveRepo, dtrApprovalOps, logService);

        buildUi();

        dcAnyDate.setDateFormatString("MM/dd/yyyy");
        if (dcAnyDate.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcAnyDate.setDate(new Date());
        setActivePeriod(LocalDate.now());
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Any date inside period:"));
        top.add(dcAnyDate);
        top.add(btnSetPeriod);
        top.add(lblPeriod);
        top.add(btnRefresh);
        top.add(btnView);
        top.add(btnApprove);
        top.add(btnReject);
        top.add(btnLeaveApproval);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnSetPeriod.addActionListener(e -> {
            LocalDate d = LocalDates.toLocalDate(dcAnyDate.getDate());
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
        btnLeaveApproval.addActionListener(e -> onLeaveApproval());
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


    private void onLeaveApproval() {
        if (activePeriod == null) {
            UiDialogs.warn(this, "Set a pay period first.");
            return;
        }

        List<LeaveRequest> requests = supervisorOps.listDirectReportLeaveRequests(supervisorEmpId(), activePeriod);

        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Leave ID", "EmpID", "Employee", "Date", "Start", "End", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (LeaveRequest r : requests) {
            tm.addRow(new Object[]{
                    r.getLeaveId(),
                    r.getEmployeeId(),
                    r.getLastName() + ", " + r.getFirstName(),
                    r.getDate() == null ? "" : r.getDate().format(DATE_FMT),
                    r.getStartTime() == null ? "" : r.getStartTime().format(TIME_FMT),
                    r.getEndTime() == null ? "" : r.getEndTime().format(TIME_FMT),
                    r.getStatus().name()
            });
        }

        JTable leaveTable = new JTable(tm);
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnApproveLeave = new JButton("Approve Leave");
        JButton btnRejectLeave = new JButton("Reject Leave");
        JButton btnClose = new JButton("Close");

        final JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Leave Request Approval", Dialog.ModalityType.APPLICATION_MODAL);

        btnApproveLeave.addActionListener(e -> onDecideLeave(leaveTable, tm, dlg, LeaveStatus.APPROVED));
        btnRejectLeave.addActionListener(e -> onDecideLeave(leaveTable, tm, dlg, LeaveStatus.REJECTED));
        btnClose.addActionListener(e -> dlg.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.add(btnApproveLeave);
        south.add(btnRejectLeave);
        south.add(btnClose);

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(leaveTable), south));
        dlg.setSize(850, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onDecideLeave(JTable leaveTable, DefaultTableModel tm, JDialog dlg, LeaveStatus status) {
        int viewRow = leaveTable.getSelectedRow();
        if (viewRow < 0) {
            UiDialogs.warn(dlg, "Select a leave request first.");
            return;
        }

        int modelRow = leaveTable.convertRowIndexToModel(viewRow);
        String leaveId = String.valueOf(tm.getValueAt(modelRow, 0));
        int reportId;
        try {
            reportId = Integer.parseInt(String.valueOf(tm.getValueAt(modelRow, 1)));
        } catch (NumberFormatException ex) {
            UiDialogs.warn(dlg, "Invalid employee selected.");
            return;
        }

        String note = JOptionPane.showInputDialog(dlg, status == LeaveStatus.APPROVED ? "Approval note (optional):" : "Rejection note (optional):", "Leave Decision", JOptionPane.PLAIN_MESSAGE);
        if (note == null) {
            note = "";
        }

        boolean ok = supervisorOps.decideDirectReportLeave(supervisorEmpId(), reportId, leaveId, status, note);
        if (ok) {
            UiDialogs.info(dlg, "Leave request " + status.name().toLowerCase() + ".");
            tm.removeRow(modelRow);
        } else {
            UiDialogs.warn(dlg, "Leave decision failed. Check if the employee is a direct report.");
        }
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
