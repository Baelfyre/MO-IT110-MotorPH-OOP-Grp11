/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.enums.LeaveStatus;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.LogEntry;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;
import com.motorph.ops.supervisor.SupervisorDtrSummary;
import com.motorph.ops.supervisor.SupervisorOps;
import com.motorph.service.LogService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Supervisor panel for DTR and leave review.
 *
 * @author ACER
 */
public class SupervisorPanel extends JPanel {

    private final User currentUser;
    private final SupervisorOps supervisorOps;
    private final LogService logService;

    private final JLabel lblAttendancePeriod = new JLabel("Approval Period: -");
    private final JLabel lblPendingDtr = new JLabel("Pending DTR: 0");
    private final JLabel lblPendingLeave = new JLabel("Pending Leave Requests: 0");
    private final JLabel lblPendingPayroll = new JLabel("Pending Payroll: 0");

    private final JComboBox<String> cbTeamEmp = new JComboBox<>();
    private final JComboBox<String> cbTeamYear = new JComboBox<>();
    private final JComboBox<String> cbTeamMonth = new JComboBox<>();
    private final JButton btnResetAttendanceFilters = new JButton("Clear Filters");

    private final JComboBox<String> cbLeaveEmp = new JComboBox<>();
    private final JComboBox<String> cbLeaveYear = new JComboBox<>();
    private final JComboBox<String> cbLeaveMonth = new JComboBox<>();
    private final JButton btnResetLeaveFilters = new JButton("Clear Filters");

    private PayPeriod activePeriod;
    private final List<SupervisorDtrSummary> cachedTeamSummaries = new ArrayList<>();

    private final DefaultTableModel teamModel = new DefaultTableModel(
            new Object[]{"EmpID", "Name", "DTR Status", "Payroll Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final DefaultTableModel leaveModel = new DefaultTableModel(
            new Object[]{"Leave ID", "EmpID", "Employee", "Date", "Start", "End", "Status", "Comment"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tblTeam = new JTable(teamModel);
    private final JTable tblLeave = new JTable(leaveModel);

    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnView = new JButton("View Entries");
    private final JButton btnApprove = new JButton("Approve DTR");
    private final JButton btnReject = new JButton("Reject DTR");
    private final JButton btnApproveLeave = new JButton("Approve Leave");
    private final JButton btnRejectLeave = new JButton("Reject Leave");
    private final JButton btnLogs = new JButton("Change Logs");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter TIME_INPUT_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public SupervisorPanel(User currentUser, SupervisorOps supervisorOps) {
        this(currentUser, supervisorOps, new LogService());
    }

    public SupervisorPanel(User currentUser, SupervisorOps supervisorOps, LogService logService) {
        initComponents();
        this.currentUser = currentUser;
        this.supervisorOps = supervisorOps;
        this.logService = logService;

        buildUi();
        initFilters();
        setCurrentAttendanceFilters();
        updateActivePeriodFromAttendanceFilters();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topRow.add(lblAttendancePeriod);
        topRow.add(btnRefresh);
        topRow.add(btnLogs);

        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 5));
        summaryRow.add(lblPendingDtr);
        summaryRow.add(lblPendingLeave);
        summaryRow.add(lblPendingPayroll);

        north.add(topRow);
        north.add(summaryRow);
        add(north, BorderLayout.NORTH);

        tblTeam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTeam.setRowHeight(24);
        tblLeave.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLeave.setRowHeight(24);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Attendance Management", buildAttendanceTab());
        tabs.addTab("Leave Requests", buildLeaveTab());
        add(tabs, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> reload());
        btnLogs.addActionListener(e -> showLogs());
        btnView.addActionListener(e -> onView());
        btnApprove.addActionListener(e -> onApprove());
        btnReject.addActionListener(e -> onReject());
        btnApproveLeave.addActionListener(e -> onApproveLeave());
        btnRejectLeave.addActionListener(e -> onRejectLeave());

        btnResetAttendanceFilters.addActionListener(e -> {
            cbTeamEmp.setSelectedIndex(0);
            setCurrentAttendanceFilters();
            updateActivePeriodFromAttendanceFilters();
        });
        btnResetLeaveFilters.addActionListener(e -> {
            cbLeaveEmp.setSelectedIndex(0);
            cbLeaveYear.setSelectedIndex(0);
            cbLeaveMonth.setSelectedIndex(0);
            refreshLeaveTable();
        });

        cbTeamEmp.addActionListener(e -> refreshTeamTable());
        cbTeamYear.addActionListener(e -> updateActivePeriodFromAttendanceFilters());
        cbTeamMonth.addActionListener(e -> updateActivePeriodFromAttendanceFilters());
        cbLeaveEmp.addActionListener(e -> refreshLeaveTable());
        cbLeaveYear.addActionListener(e -> refreshLeaveTable());
        cbLeaveMonth.addActionListener(e -> refreshLeaveTable());

        tblLeave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onLeaveCommentCellClicked();
            }
        });
    }

    private JPanel buildAttendanceTab() {
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterRow.add(new JLabel("Team EmpID:"));
        filterRow.add(cbTeamEmp);
        filterRow.add(new JLabel("Year:"));
        filterRow.add(cbTeamYear);
        filterRow.add(new JLabel("Month:"));
        filterRow.add(cbTeamMonth);
        filterRow.add(btnResetAttendanceFilters);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTeam), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttons.add(btnView);
        buttons.add(btnApprove);
        buttons.add(btnReject);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildLeaveTab() {
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterRow.add(new JLabel("Team EmpID:"));
        filterRow.add(cbLeaveEmp);
        filterRow.add(new JLabel("Year:"));
        filterRow.add(cbLeaveYear);
        filterRow.add(new JLabel("Month:"));
        filterRow.add(cbLeaveMonth);
        filterRow.add(btnResetLeaveFilters);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblLeave), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buttons.add(btnApproveLeave);
        buttons.add(btnRejectLeave);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void initFilters() {
        cbTeamEmp.addItem("All Team Members");
        cbLeaveEmp.addItem("All Team Members");

        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            cbTeamYear.addItem(String.valueOf(year));
        }

        cbLeaveYear.addItem("All Years");
        cbLeaveMonth.addItem("All Months");
        for (Month month : Month.values()) {
            String display = month.getDisplayName(TextStyle.FULL, Locale.US);
            cbTeamMonth.addItem(display);
            cbLeaveMonth.addItem(display);
        }
    }

    private void setCurrentAttendanceFilters() {
        LocalDate today = LocalDate.now();
        cbTeamYear.setSelectedItem(String.valueOf(today.getYear()));
        cbTeamMonth.setSelectedItem(today.getMonth().getDisplayName(TextStyle.FULL, Locale.US));
    }

    private int supervisorEmpId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    // Annotation: Builds the active approval period from selected year/month without exposing half-cycle controls in the UI.
    private void updateActivePeriodFromAttendanceFilters() {
        String yearValue = (String) cbTeamYear.getSelectedItem();
        String monthValue = (String) cbTeamMonth.getSelectedItem();
        if (yearValue == null || monthValue == null) {
            return;
        }

        try {
            int year = Integer.parseInt(yearValue);
            Month month = Month.valueOf(monthValue.toUpperCase(Locale.US));
            LocalDate today = LocalDate.now();
            int baseDay = (today.getYear() == year && today.getMonth() == month && today.getDayOfMonth() > 15) ? 16 : 1;
            activePeriod = PayPeriod.fromDateSemiMonthly(LocalDate.of(year, month, baseDay));
            lblAttendancePeriod.setText("Approval Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
            reload();
        } catch (Exception ex) {
            UiDialogs.error(this, "Invalid attendance filter selection.");
        }
    }

    // Annotation: Loads direct report status rows and leave history rows for the current supervisor.
    private void reload() {
        if (activePeriod == null) {
            return;
        }

        cachedTeamSummaries.clear();
        List<SupervisorDtrSummary> statuses = supervisorOps.listDirectReportStatuses(supervisorEmpId(), activePeriod);
        cachedTeamSummaries.addAll(statuses);

        int pendingDtr = 0;
        int pendingPayroll = 0;
        for (SupervisorDtrSummary summary : statuses) {
            if (summary != null && summary.getDtrStatus() == ApprovalStatus.PENDING) {
                pendingDtr++;
            }
            if (summary != null && summary.getPayrollStatus() == ApprovalStatus.PENDING) {
                pendingPayroll++;
            }
        }

        List<LeaveRequest> leaveRows = supervisorOps.listDirectReportLeaveRequests(supervisorEmpId(), null);
        int pendingLeave = 0;
        for (LeaveRequest request : leaveRows) {
            if (request != null && request.getStatus() == LeaveStatus.PENDING) {
                pendingLeave++;
            }
        }

        lblPendingDtr.setText("Pending DTR: " + pendingDtr);
        lblPendingLeave.setText("Pending Leave Requests: " + pendingLeave);
        lblPendingPayroll.setText("Pending Payroll: " + pendingPayroll);

        refreshEmployeeFilters();
        refreshTeamTable();
        refreshLeaveFilters(leaveRows);
        refreshLeaveTable();
    }

    private void refreshEmployeeFilters() {
        String selectedTeam = (String) cbTeamEmp.getSelectedItem();
        String selectedLeave = (String) cbLeaveEmp.getSelectedItem();

        List<Employee> reports = supervisorOps.listDirectReports(supervisorEmpId());
        cbTeamEmp.removeAllItems();
        cbTeamEmp.addItem("All Team Members");
        cbLeaveEmp.removeAllItems();
        cbLeaveEmp.addItem("All Team Members");

        for (Employee employee : reports) {
            if (employee == null) {
                continue;
            }
            String empId = String.valueOf(employee.getEmployeeNumber());
            cbTeamEmp.addItem(empId);
            cbLeaveEmp.addItem(empId);
        }

        restoreComboSelection(cbTeamEmp, selectedTeam);
        restoreComboSelection(cbLeaveEmp, selectedLeave);
    }

    private void restoreComboSelection(JComboBox<String> comboBox, String selectedValue) {
        if (selectedValue != null) {
            comboBox.setSelectedItem(selectedValue);
            if (comboBox.getSelectedIndex() < 0) {
                comboBox.setSelectedIndex(0);
            }
        }
    }

    private void refreshTeamTable() {
        teamModel.setRowCount(0);
        String selectedEmp = (String) cbTeamEmp.getSelectedItem();
        for (SupervisorDtrSummary summary : cachedTeamSummaries) {
            if (summary == null) {
                continue;
            }
            if (selectedEmp != null
                    && !"All Team Members".equals(selectedEmp)
                    && !selectedEmp.equals(String.valueOf(summary.getEmployeeId()))) {
                continue;
            }
            teamModel.addRow(new Object[]{
                    summary.getEmployeeId(),
                    summary.getEmployeeName(),
                    summary.getDtrStatus().name(),
                    summary.getPayrollStatus().name()
            });
        }
    }

    private void refreshLeaveFilters(List<LeaveRequest> allLeaveRows) {
        String selectedYear = (String) cbLeaveYear.getSelectedItem();
        String selectedMonth = (String) cbLeaveMonth.getSelectedItem();

        Set<String> years = new LinkedHashSet<>();
        for (LeaveRequest request : allLeaveRows) {
            if (request != null && request.getDate() != null) {
                years.add(String.valueOf(request.getDate().getYear()));
            }
        }

        cbLeaveYear.removeAllItems();
        cbLeaveYear.addItem("All Years");
        years.stream().sorted().forEach(cbLeaveYear::addItem);
        restoreComboSelection(cbLeaveYear, selectedYear);
        restoreComboSelection(cbLeaveMonth, selectedMonth == null ? "All Months" : selectedMonth);
    }

    private void refreshLeaveTable() {
        leaveModel.setRowCount(0);
        List<LeaveRequest> requests = supervisorOps.listDirectReportLeaveRequests(supervisorEmpId(), null);
        String empFilter = (String) cbLeaveEmp.getSelectedItem();
        String yearFilter = (String) cbLeaveYear.getSelectedItem();
        String monthFilter = (String) cbLeaveMonth.getSelectedItem();

        for (LeaveRequest request : requests) {
            if (request == null || request.getDate() == null) {
                continue;
            }
            if (empFilter != null && !"All Team Members".equals(empFilter)
                    && !empFilter.equals(String.valueOf(request.getEmployeeId()))) {
                continue;
            }
            if (!matchesYearFilter(request.getDate(), yearFilter)) {
                continue;
            }
            if (!matchesMonthFilter(request.getDate(), monthFilter)) {
                continue;
            }

            leaveModel.addRow(new Object[]{
                    request.getLeaveId(),
                    request.getEmployeeId(),
                    request.getLastName() + ", " + request.getFirstName(),
                    request.getDate().format(DATE_FMT),
                    request.getStartTime() == null ? "" : request.getStartTime().format(TIME_INPUT_FMT),
                    request.getEndTime() == null ? "" : request.getEndTime().format(TIME_INPUT_FMT),
                    request.getStatus() == null ? "" : request.getStatus().name(),
                    hasDecisionNote(request) ? "View" : ""
            });
        }
    }

    private boolean matchesYearFilter(LocalDate date, String filter) {
        if (date == null || filter == null || "All Years".equals(filter)) {
            return true;
        }
        return String.valueOf(date.getYear()).equals(filter);
    }

    private boolean matchesMonthFilter(LocalDate date, String filter) {
        if (date == null || filter == null || "All Months".equals(filter)) {
            return true;
        }
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.US).equalsIgnoreCase(filter);
    }

    private void onLeaveCommentCellClicked() {
        int viewRow = tblLeave.getSelectedRow();
        int viewCol = tblLeave.getSelectedColumn();
        if (viewRow < 0 || viewCol != 7) {
            return;
        }

        int modelRow = tblLeave.convertRowIndexToModel(viewRow);
        Object leaveIdValue = leaveModel.getValueAt(modelRow, 0);
        if (leaveIdValue == null) {
            return;
        }

        LeaveRequest request = findDirectReportLeaveById(String.valueOf(leaveIdValue));
        if (request == null || !hasDecisionNote(request)) {
            UiDialogs.info(this, "No supervisor comment recorded for this leave request.");
            return;
        }

        JTextArea area = new JTextArea(request.getDecisionNote());
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(420, 180));

        String title = "Leave Comment";
        if (request.getStatus() != null) {
            title += " - " + request.getStatus().name();
        }

        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private LeaveRequest findDirectReportLeaveById(String leaveId) {
        if (leaveId == null || leaveId.trim().isEmpty()) {
            return null;
        }
        for (LeaveRequest request : supervisorOps.listDirectReportLeaveRequests(supervisorEmpId(), null)) {
            if (request != null && leaveId.equalsIgnoreCase(request.getLeaveId())) {
                return request;
            }
        }
        return null;
    }

    private boolean hasDecisionNote(LeaveRequest request) {
        return request != null
                && request.getDecisionNote() != null
                && !request.getDecisionNote().trim().isEmpty();
    }

    private Integer selectedReportId() {
        int viewRow = tblTeam.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblTeam.convertRowIndexToModel(viewRow);
        Object value = teamModel.getValueAt(modelRow, 0);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer selectedLeaveReportId() {
        int viewRow = tblLeave.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblLeave.convertRowIndexToModel(viewRow);
        try {
            return Integer.parseInt(String.valueOf(leaveModel.getValueAt(modelRow, 1)));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String selectedLeaveId() {
        int viewRow = tblLeave.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblLeave.convertRowIndexToModel(viewRow);
        Object leaveId = leaveModel.getValueAt(modelRow, 0);
        return leaveId == null ? null : String.valueOf(leaveId);
    }

    private String selectedLeaveStatus() {
        int viewRow = tblLeave.getSelectedRow();
        if (viewRow < 0) {
            return "";
        }
        int modelRow = tblLeave.convertRowIndexToModel(viewRow);
        Object status = leaveModel.getValueAt(modelRow, 6);
        return status == null ? "" : String.valueOf(status);
    }

    private void onView() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a team member first.");
            return;
        }

        List<TimeEntry> entries = supervisorOps.viewDirectReportTimeEntries(supervisorEmpId(), reportId, activePeriod);
        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Date", "Time In", "Time Out"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (TimeEntry entry : entries) {
            tm.addRow(new Object[]{
                    entry.getDate() == null ? "" : entry.getDate().format(DATE_FMT),
                    entry.getTimeIn() == null ? "" : entry.getTimeIn().format(TIME_FMT),
                    entry.getTimeOut() == null ? "" : entry.getTimeOut().format(TIME_FMT)
            });
        }

        JTable table = new JTable(tm);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "DTR Entries: " + reportId, Dialog.ModalityType.APPLICATION_MODAL);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnEditEntry = new JButton("Manual Edit");
        JButton close = new JButton("Close");

        btnEditEntry.addActionListener(e -> onManualEdit(reportId, table, tm, dlg));
        close.addActionListener(e -> dlg.dispose());
        south.add(btnEditEntry);
        south.add(close);

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(table), south));
        dlg.setSize(560, 400);
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
        form.add(new JLabel(selectedDate.format(DATE_FMT)));
        form.add(new JLabel("Time In:"));
        form.add(cbTimeIn);
        form.add(new JLabel("Time Out:"));
        form.add(cbTimeOut);

        int result = JOptionPane.showConfirmDialog(parentDialog, form, "Manual DTR Edit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        LocalTime timeIn = parseInputTime((String) cbTimeIn.getSelectedItem());
        LocalTime timeOut = parseInputTime((String) cbTimeOut.getSelectedItem());
        if (timeIn == null || timeOut == null) {
            UiDialogs.warn(parentDialog, "Provide valid time values.");
            return;
        }
        if (!timeOut.isAfter(timeIn)) {
            UiDialogs.warn(parentDialog, "Time Out must be after Time In.");
            return;
        }

        boolean ok = supervisorOps.updateDirectReportTimeEntry(supervisorEmpId(), reportId, selectedDate, timeIn, timeOut);
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
        LocalTime time = LocalTime.of(0, 0);
        while (!time.isAfter(LocalTime.of(23, 30))) {
            times.add(time.format(TIME_INPUT_FMT));
            time = time.plusMinutes(30);
        }
        return times;
    }

    private String toDisplayTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return LocalTime.of(8, 0).format(TIME_INPUT_FMT);
        }
        try {
            return LocalTime.parse(raw.trim(), TIME_FMT).format(TIME_INPUT_FMT);
        } catch (Exception ex) {
            try {
                return LocalTime.parse(raw.trim(), DateTimeFormatter.ofPattern("HH:mm", Locale.US)).format(TIME_INPUT_FMT);
            } catch (Exception ignored) {
                return LocalTime.of(8, 0).format(TIME_INPUT_FMT);
            }
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

    private void onApproveLeave() {
        decideLeave(LeaveStatus.APPROVED);
    }

    private void onRejectLeave() {
        decideLeave(LeaveStatus.REJECTED);
    }

    private void decideLeave(LeaveStatus newStatus) {
        Integer reportId = selectedLeaveReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a leave entry first.");
            return;
        }

        String leaveId = selectedLeaveId();
        if (leaveId == null || leaveId.isBlank()) {
            UiDialogs.warn(this, "Selected leave entry has no Leave ID.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selectedLeaveStatus())) {
            UiDialogs.warn(this, "Only pending leave requests can be updated.");
            return;
        }

        String note = JOptionPane.showInputDialog(this, "Enter optional supervisor comment:", "Leave Decision Note", JOptionPane.PLAIN_MESSAGE);
        boolean ok = supervisorOps.decideDirectReportLeave(currentUser, reportId, leaveId, newStatus, note == null ? "" : note.trim());
        if (ok) {
            UiDialogs.info(this, "Leave status updated to " + newStatus.name() + ".");
            reload();
        } else {
            UiDialogs.error(this, "Leave action failed.");
        }
    }

    private void onApprove() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a team member first.");
            return;
        }
        try {
            boolean ok = supervisorOps.approveDirectReportDtr(currentUser, reportId, activePeriod);
            if (ok) {
                UiDialogs.info(this, "DTR approved.");
            } else {
                UiDialogs.error(this, "DTR approval failed.");
            }
            reload();
        } catch (SecurityException ex) {
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void onReject() {
        Integer reportId = selectedReportId();
        if (reportId == null) {
            UiDialogs.warn(this, "Select a team member first.");
            return;
        }
        try {
            boolean ok = supervisorOps.rejectDirectReportDtr(currentUser, reportId, activePeriod);
            if (ok) {
                UiDialogs.info(this, "DTR rejected.");
            } else {
                UiDialogs.error(this, "DTR rejection failed.");
            }
            reload();
        } catch (SecurityException ex) {
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void showLogs() {
        List<LogEntry> logs = logService.getLogsForUser(String.valueOf(supervisorEmpId()));
        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Log ID", "Timestamp", "User", "Action", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (LogEntry entry : logs) {
            tm.addRow(new Object[]{
                    entry.getId(),
                    entry.getTimestamp(),
                    entry.getUser(),
                    entry.getAction(),
                    entry.getDetails()
            });
        }

        JTable table = new JTable(tm);
        table.setRowHeight(24);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "My Change Logs", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setContentPane(new JScrollPane(table));
        dlg.setSize(760, 380);
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
