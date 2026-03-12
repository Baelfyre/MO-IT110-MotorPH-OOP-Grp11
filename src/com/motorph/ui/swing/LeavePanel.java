package com.motorph.ui.swing;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LeaveRequest;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.User;
import com.motorph.ops.leave.LeaveOps;
import com.motorph.service.EmployeeService;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
 *
 * @author OngoJ.
 */
public class LeavePanel extends JPanel {

    private final User currentUser;
    private final EmployeeService employeeService;
    private final LeaveOps leaveOps;

    private final JDateChooser dcDate = new JDateChooser();
    private final JComboBox<String> cbStart = new JComboBox<>();
    private final JComboBox<String> cbEnd = new JComboBox<>();
    private final JComboBox<String> cbHistoryYear = new JComboBox<>();
    private final JComboBox<String> cbHistoryMonth = new JComboBox<>();

    private final JLabel lblPeriod = new JLabel("Period: -");
    private final JLabel lblCredits = new JLabel("Current leave credits (hrs): -");
    private final JLabel lblUsed = new JLabel("Used this period (hrs): -");
    private final JLabel lblRemaining = new JLabel("Remaining YTD (hrs): -");

    private PayPeriod activePeriod;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Leave_ID", "Date", "Start", "End", "Status", "Comment"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);

    private final JButton btnSubmit = new JButton("Submit Leave");
    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnResetHistory = new JButton("Clear Filters");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public LeavePanel(User currentUser, LeaveOps leaveOps, EmployeeService employeeService) {
        this.currentUser = currentUser;
        this.employeeService = employeeService;
        this.leaveOps = leaveOps;

        buildUi();
        initTimeOptions();
        initHistoryFilters();

        dcDate.setDateFormatString("MM/dd/yyyy");
        if (dcDate.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcDate.setDate(new java.util.Date());
        dcDate.setMinSelectableDate(new java.util.Date());

        setActivePeriod(LocalDate.now());
        refreshAll();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel requestRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        requestRow.add(new JLabel("Leave Date:"));
        requestRow.add(dcDate);
        requestRow.add(new JLabel("Start:"));
        requestRow.add(cbStart);
        requestRow.add(new JLabel("End:"));
        requestRow.add(cbEnd);
        requestRow.add(btnSubmit);
        requestRow.add(btnRefresh);

        JPanel historyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        historyRow.add(new JLabel("History Year:"));
        historyRow.add(cbHistoryYear);
        historyRow.add(new JLabel("History Month:"));
        historyRow.add(cbHistoryMonth);
        historyRow.add(btnResetHistory);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(requestRow);
        north.add(historyRow);

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        summary.add(lblPeriod);
        summary.add(lblCredits);
        summary.add(lblUsed);
        summary.add(lblRemaining);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);

        btnSubmit.addActionListener(e -> onSubmit());
        btnRefresh.addActionListener(e -> refreshAll());
        btnResetHistory.addActionListener(e -> resetHistoryFilters());
        cbHistoryYear.addActionListener(e -> refreshTable());
        cbHistoryMonth.addActionListener(e -> refreshTable());
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onCommentCellClicked();
            }
        });
    }

    // Annotation: Provides 30-minute steps from 08:00 to 17:00.
    private void initTimeOptions() {
        List<String> times = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        while (!t.isAfter(LocalTime.of(17, 0))) {
            times.add(t.format(TIME_FMT));
            t = t.plusMinutes(30);
        }

        for (String s : times) {
            cbStart.addItem(s);
            cbEnd.addItem(s);
        }

        cbStart.setSelectedItem(LocalTime.of(8, 0).format(TIME_FMT));
        cbEnd.setSelectedItem(LocalTime.of(17, 0).format(TIME_FMT));
    }

    // Annotation: Loads history filter choices separately from the leave request date picker.
    private void initHistoryFilters() {
        cbHistoryYear.addItem("All Years");
        cbHistoryMonth.addItem("All Months");
        for (Month month : Month.values()) {
            cbHistoryMonth.addItem(month.getDisplayName(TextStyle.FULL, Locale.US));
        }
    }

    private int empId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
    }

    private void refreshAll() {
        refreshSummary();
        refreshHistoryFilters();
        refreshTable();
    }

    private void refreshSummary() {
        double credits = leaveOps.getStoredLeaveCreditsHours(empId());
        double used = leaveOps.getLeaveUsedThisPeriod(empId(), activePeriod);
        double remaining = leaveOps.getLeaveRemainingYtd(empId(), activePeriod);

        lblCredits.setText(String.format(Locale.US, "Current leave credits (hrs): %.2f", credits));
        lblUsed.setText(String.format(Locale.US, "Used this period (hrs): %.2f", used));
        lblRemaining.setText(String.format(Locale.US, "Remaining YTD (hrs): %.2f", remaining));
    }

    private void refreshHistoryFilters() {
        List<LeaveRequest> allRows = leaveOps.listLeaveRequests(empId(), null);
        String selectedYear = (String) cbHistoryYear.getSelectedItem();
        String selectedMonth = (String) cbHistoryMonth.getSelectedItem();

        Set<String> years = new LinkedHashSet<>();
        for (LeaveRequest request : allRows) {
            if (request != null && request.getDate() != null) {
                years.add(String.valueOf(request.getDate().getYear()));
            }
        }

        cbHistoryYear.removeAllItems();
        cbHistoryYear.addItem("All Years");
        years.stream().sorted().forEach(cbHistoryYear::addItem);

        if (selectedYear != null) {
            cbHistoryYear.setSelectedItem(selectedYear);
            if (cbHistoryYear.getSelectedIndex() < 0) {
                cbHistoryYear.setSelectedIndex(0);
            }
        }

        if (selectedMonth != null) {
            cbHistoryMonth.setSelectedItem(selectedMonth);
            if (cbHistoryMonth.getSelectedIndex() < 0) {
                cbHistoryMonth.setSelectedIndex(0);
            }
        }
    }

    private void resetHistoryFilters() {
        cbHistoryYear.setSelectedIndex(0);
        cbHistoryMonth.setSelectedIndex(0);
        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);

        List<LeaveRequest> list = leaveOps.listLeaveRequests(empId(), null);
        String yearFilter = (String) cbHistoryYear.getSelectedItem();
        String monthFilter = (String) cbHistoryMonth.getSelectedItem();

        for (LeaveRequest r : list) {
            if (r == null || r.getDate() == null) {
                continue;
            }
            if (!matchesYearFilter(r.getDate(), yearFilter)) {
                continue;
            }
            if (!matchesMonthFilter(r.getDate(), monthFilter)) {
                continue;
            }

            model.addRow(new Object[]{
                    r.getLeaveId(),
                    r.getDate().format(DATE_FMT),
                    r.getStartTime() == null ? "" : r.getStartTime().format(TIME_FMT),
                    r.getEndTime() == null ? "" : r.getEndTime().format(TIME_FMT),
                    r.getStatus() == null ? "" : r.getStatus().name(),
                    hasDecisionNote(r) ? "View" : ""
            });
        }
    }


    // Annotation: Opens the stored supervisor comment for the selected leave row.
    private void onCommentCellClicked() {
        int viewRow = tbl.getSelectedRow();
        int viewCol = tbl.getSelectedColumn();
        if (viewRow < 0 || viewCol != 5) {
            return;
        }

        int modelRow = tbl.convertRowIndexToModel(viewRow);
        Object leaveIdValue = model.getValueAt(modelRow, 0);
        if (leaveIdValue == null) {
            return;
        }

        LeaveRequest request = findLeaveRequestById(String.valueOf(leaveIdValue));
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

    // Annotation: Finds one leave row from the current employee history using the Leave ID.
    private LeaveRequest findLeaveRequestById(String leaveId) {
        if (leaveId == null || leaveId.trim().isEmpty()) {
            return null;
        }
        for (LeaveRequest request : leaveOps.listLeaveRequests(empId(), null)) {
            if (request != null && leaveId.equalsIgnoreCase(request.getLeaveId())) {
                return request;
            }
        }
        return null;
    }

    // Annotation: Checks whether a stored supervisor decision note is available for viewing.
    private boolean hasDecisionNote(LeaveRequest request) {
        return request != null
                && request.getDecisionNote() != null
                && !request.getDecisionNote().trim().isEmpty();
    }

    private boolean matchesYearFilter(LocalDate date, String filter) {
        if (date == null || filter == null || filter.equals("All Years")) {
            return true;
        }
        return String.valueOf(date.getYear()).equals(filter);
    }

    private boolean matchesMonthFilter(LocalDate date, String filter) {
        if (date == null || filter == null || filter.equals("All Months")) {
            return true;
        }
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
        return monthName.equalsIgnoreCase(filter);
    }

    private void onSubmit() {
        LocalDate date = LocalDates.toLocalDate(dcDate.getDate());
        if (date == null) {
            UiDialogs.warn(this, "Select a date.");
            return;
        }

        LocalTime start = parseTime((String) cbStart.getSelectedItem());
        LocalTime end = parseTime((String) cbEnd.getSelectedItem());

        if (start == null || end == null) {
            UiDialogs.warn(this, "Select start and end time.");
            return;
        }

        Employee emp = employeeService.getEmployee(empId());
        String first = emp == null ? "" : emp.getFirstName();
        String last = emp == null ? "" : emp.getLastName();

        double remainingCredits = leaveOps.getLeaveRemainingYtd(empId(), activePeriod);
        double requestedHours = leaveOps.calculateLeaveHours(start, end);

        boolean allowUnpaidFallback = false;

        if (requestedHours > remainingCredits) {
            boolean confirmed = UiDialogs.confirm(
                    this,
                    String.format(
                            Locale.US,
                            "Requested leave is %.2f hours but only %.2f paid leave hours remain.%n%n"
                            + "Do you want to continue through the unpaid leave confirmation path?",
                            requestedHours,
                            remainingCredits
                    )
            );

            if (!confirmed) {
                UiDialogs.warn(this, "Leave request cancelled.");
                return;
            }

            allowUnpaidFallback = true;
        }

        boolean ok = leaveOps.requestLeave(empId(), first, last, date, start, end, allowUnpaidFallback);
        if (ok) {
            UiDialogs.info(this, leaveOps.getLastRequestMessage());
            leaveOps.syncLeaveTakenYtd(empId(), activePeriod);
            refreshAll();
        } else {
            UiDialogs.warn(this, leaveOps.getLastRequestMessage().isEmpty()
                    ? "Leave request not recorded."
                    : leaveOps.getLastRequestMessage());
        }
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(raw.trim(), TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
