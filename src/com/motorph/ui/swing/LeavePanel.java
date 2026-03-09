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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private final JLabel lblPeriod = new JLabel("Period: -");
    private final JLabel lblCredits = new JLabel("Current leave credits (hrs): -");
    private final JLabel lblUsed = new JLabel("Used this period (hrs): -");
    private final JLabel lblRemaining = new JLabel("Remaining YTD (hrs): -");

    private PayPeriod activePeriod;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Leave_ID", "Date", "Start", "End", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);

    private final JButton btnSubmit = new JButton("Submit Leave");
    private final JButton btnRefresh = new JButton("Refresh");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public LeavePanel(User currentUser, LeaveOps leaveOps, EmployeeService employeeService) {
        this.currentUser = currentUser;
        this.employeeService = employeeService;
        this.leaveOps = leaveOps;

        buildUi();
        initTimeOptions();

        dcDate.setDateFormatString("MM/dd/yyyy");
        if (dcDate.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }
        dcDate.setDate(new java.util.Date());

        setActivePeriod(LocalDate.now());
        refreshAll();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Leave Date:"));
        top.add(dcDate);
        top.add(new JLabel("Start:"));
        top.add(cbStart);
        top.add(new JLabel("End:"));
        top.add(cbEnd);
        top.add(btnSubmit);
        top.add(btnRefresh);

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        summary.add(lblPeriod);
        summary.add(lblCredits);
        summary.add(lblUsed);
        summary.add(lblRemaining);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);

        btnSubmit.addActionListener(e -> onSubmit());
        btnRefresh.addActionListener(e -> refreshAll());

        dcDate.getDateEditor().addPropertyChangeListener("date", evt -> {
            LocalDate d = LocalDates.toLocalDate(dcDate.getDate());
            if (d != null) {
                setActivePeriod(d);
                refreshAll();
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

    private int empId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
    }

    private void refreshAll() {
        refreshSummary();
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

    private void refreshTable() {
        model.setRowCount(0);

        List<LeaveRequest> list = leaveOps.listLeaveRequests(empId(), activePeriod);
        for (LeaveRequest r : list) {
            model.addRow(new Object[]{
                    r.getLeaveId(),
                    r.getDate() == null ? "" : r.getDate().format(DATE_FMT),
                    r.getStartTime() == null ? "" : r.getStartTime().format(TIME_FMT),
                    r.getEndTime() == null ? "" : r.getEndTime().format(TIME_FMT),
                    r.getStatus() == null ? "" : r.getStatus().name()
            });
        }
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

        boolean allowUnpaidFallback = false;
        if (leaveOps.getStoredLeaveCreditsHours(empId()) <= 0.0) {
            boolean confirmed = UiDialogs.confirm(this,
                    "Paid leave credits are 0. Continue and record this request through the unpaid leave confirmation path?");
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
