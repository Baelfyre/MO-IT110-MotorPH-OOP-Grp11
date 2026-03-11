/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.LogEntry;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;
import com.motorph.ops.supervisor.SupervisorDtrSummary;
import com.motorph.ops.supervisor.SupervisorOps;
import com.motorph.service.LogService;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final LogService logService;

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
    private final JButton btnLogs = new JButton("Change Logs");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm", Locale.US);
    private static final DateTimeFormatter TIME_INPUT_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public SupervisorPanel(User currentUser, SupervisorOps supervisorOps) {
        this(currentUser, supervisorOps, new LogService());
    }

    public SupervisorPanel(User currentUser, SupervisorOps supervisorOps, LogService logService) {
        this.currentUser = currentUser;
        this.supervisorOps = supervisorOps;
        this.logService = logService;

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
        top.add(btnLogs);

        JScrollPane topScroll = new JScrollPane(
            top,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        topScroll.setBorder(BorderFactory.createEmptyBorder());
        topScroll.getHorizontalScrollBar().setUnitIncrement(16);

        add(topScroll, BorderLayout.NORTH);
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
        btnLogs.addActionListener(e -> showLogs());
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
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "DTR Entries: " + reportId, Dialog.ModalityType.APPLICATION_MODAL);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnEditEntry = new JButton("Manual Edit");
        JButton close = new JButton("Close");

        btnEditEntry.addActionListener(e -> onManualEdit(reportId, table, tm, dlg));
        close.addActionListener(e -> dlg.dispose());
        south.add(btnEditEntry);
        south.add(close);

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(table), south));
        dlg.setSize(550, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onManualEdit(int reportId, JTable table, DefaultTableModel tm, JDialog parentDialog) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UiDialogs.warn(parentDialog, "Select an entry first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(String.valueOf(tm.getValueAt(modelRow, 0)), DATE_FMT);
        } catch (Exception ex) {
            UiDialogs.warn(parentDialog, "Invalid date selected.");
            return;
        }

        JDateChooser dcEditDate = new JDateChooser();
        dcEditDate.setDateFormatString("MM/dd/yyyy");
        if (dcEditDate.getDateEditor() instanceof JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcEditDate.setDate(java.sql.Date.valueOf(selectedDate));

        JComboBox<String> cbTimeIn = new JComboBox<>();
        JComboBox<String> cbTimeOut = new JComboBox<>();
        for (String value : buildTimeChoices()) {
            cbTimeIn.addItem(value);
            cbTimeOut.addItem(value);
        }
        cbTimeIn.setSelectedItem(toDisplayTime(String.valueOf(tm.getValueAt(modelRow, 1))));
        cbTimeOut.setSelectedItem(toDisplayTime(String.valueOf(tm.getValueAt(modelRow, 2))));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Date:"));
        form.add(dcEditDate);
        form.add(new JLabel("Time In:"));
        form.add(cbTimeIn);
        form.add(new JLabel("Time Out:"));
        form.add(cbTimeOut);

        int result = JOptionPane.showConfirmDialog(parentDialog, form, "Manual DTR Edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        LocalDate date = LocalDates.toLocalDate(dcEditDate.getDate());
        LocalTime timeIn = parseInputTime((String) cbTimeIn.getSelectedItem());
        LocalTime timeOut = parseInputTime((String) cbTimeOut.getSelectedItem());
        if (date == null || timeIn == null || timeOut == null) {
            UiDialogs.warn(parentDialog, "Provide a valid date and time values.");
            return;
        }
        if (!timeOut.isAfter(timeIn)) {
            UiDialogs.warn(parentDialog, "Time Out must be after Time In.");
            return;
        }

        boolean ok = supervisorOps.updateDirectReportTimeEntry(supervisorEmpId(), reportId, date, timeIn, timeOut);
        if (ok) {
            UiDialogs.info(parentDialog, "DTR entry updated.");
            parentDialog.dispose();
            reload();
        } else {
            UiDialogs.error(parentDialog, "DTR entry update failed.");
        }
    }

    private List<String> buildTimeChoices() {
        List<String> times = new ArrayList<>();
        LocalTime t = LocalTime.of(0, 0);
        while (!t.isAfter(LocalTime.of(23, 30))) {
            times.add(t.format(TIME_INPUT_FMT));
            t = t.plusMinutes(30);
        }
        return times;
    }

    private String toDisplayTime(String raw24) {
        if (raw24 == null || raw24.trim().isEmpty()) {
            return LocalTime.of(8, 0).format(TIME_INPUT_FMT);
        }
        try {
            return LocalTime.parse(raw24.trim(), TIME_FMT).format(TIME_INPUT_FMT);
        } catch (Exception ex) {
            return LocalTime.of(8, 0).format(TIME_INPUT_FMT);
        }
    }

    private LocalTime parseInputTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(raw.trim(), TIME_INPUT_FMT);
        } catch (Exception ex) {
            return null;
        }
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
            return;
        }

        try {
            // Replaced supervisorEmpId() with currentUser
            boolean ok = supervisorOps.decideDirectReportLeave(currentUser, reportId, leaveId, status, note);
            if (ok) {
                UiDialogs.info(dlg, "Leave request " + status.name().toLowerCase() + ".");
                tm.removeRow(modelRow);
            } else {
                UiDialogs.warn(dlg, "Leave decision failed.");
            }
        } catch (SecurityException ex) {
            // Catches self-approval or unauthorized access
            UiDialogs.error(dlg, ex.getMessage());
        }
    }

    private void onApprove() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        try {
            // Replaced supervisorEmpId() with currentUser
            boolean ok = supervisorOps.approveDirectReportDtr(currentUser, reportId, activePeriod);
            if (ok) {
                UiDialogs.info(this, "DTR approved.");
            } else {
                UiDialogs.warn(this, "Approve failed. Check if payroll already approved.");
            }
        } catch (SecurityException ex) {
            // Catches self-approval or unauthorized access
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void onReject() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        try {
            // Replaced supervisorEmpId() with currentUser
            boolean ok = supervisorOps.rejectDirectReportDtr(currentUser, reportId, activePeriod);
            if (ok) {
                UiDialogs.info(this, "DTR rejected.");
            } else {
                UiDialogs.warn(this, "Reject failed. Check if payroll already approved.");
            }
        } catch (SecurityException ex) {
            // Catches self-rejection or unauthorized access
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void showLogs() {
        List<LogEntry> logs = logService.getLogsForUserByActionPrefix(
                String.valueOf(supervisorEmpId()),
                "SUPERVISOR_"
        );
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Supervisor Logs", Dialog.ModalityType.APPLICATION_MODAL);
        DefaultTableModel logModel = new DefaultTableModel(new Object[]{"Log_ID", "Timestamp", "User", "Action", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(logModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (LogEntry entry : logs) {
            logModel.addRow(new Object[]{entry.getId(), entry.getTimestamp(), entry.getUser(), entry.getAction(), entry.getDetails()});
        }
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);
        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(table), south));
        dlg.setSize(900, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

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
