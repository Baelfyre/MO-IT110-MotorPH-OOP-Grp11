/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;
import com.motorph.service.TimeService;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Employee attendance panel.
 *
 * @author ACER
 */
public class TimekeepingPanel extends JPanel {

    private final User currentUser;
    private final com.motorph.ops.time.TimeOps timeOps;

    private final JDateChooser dcAnyDate = new JDateChooser();
    private final JLabel lblPeriod = new JLabel("Period: -");
    private final JLabel lblDtrStatus = new JLabel("DTR Status: -");
    private final JLabel lblWorkedHours = new JLabel("Worked today (hrs): -");
    private PayPeriod activePeriod;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Date", "Time In", "Time Out"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);
    private final JButton btnSetPeriod = new JButton("Set Period");
    private final JButton btnClockIn = new JButton("Clock In");
    private final JButton btnClockOut = new JButton("Clock Out");
    private final JButton btnRefresh = new JButton("Refresh");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm", Locale.US);

    public TimekeepingPanel(User currentUser, com.motorph.ops.time.TimeOps timeOps) {
        this.currentUser = currentUser;
        this.timeOps = timeOps;

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
        top.add(lblDtrStatus);
        top.add(lblWorkedHours);
        top.add(btnClockIn);
        top.add(btnClockOut);
        top.add(btnRefresh);

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
        btnClockIn.addActionListener(e -> onClockIn());
        btnClockOut.addActionListener(e -> onClockOut());
        btnRefresh.addActionListener(e -> reload());
    }

    private int empId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
        int empId = empId();
        var dtr = timeOps.getMyDtrStatus(empId, activePeriod);
        lblDtrStatus.setText("DTR Status: " + (dtr == null ? "N/A" : dtr.name()));
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        int empId = empId();
        List<TimeEntry> entries = timeOps.viewMyTimeEntriesForPeriod(empId, activePeriod);
        for (TimeEntry t : entries) {
            model.addRow(new Object[]{
                    t.getDate() == null ? "" : t.getDate().format(DATE_FMT),
                    t.getTimeIn() == null ? "" : t.getTimeIn().format(TIME_FMT),
                    t.getTimeOut() == null ? "" : t.getTimeOut().format(TIME_FMT)
            });
        }
        refreshWorkedHoursLabel();
    }

    private void refreshWorkedHoursLabel() {
        TimeEntry todayEntry = timeOps.getEntryForDate(empId(), LocalDate.now());
        if (todayEntry == null || todayEntry.getTimeIn() == null) {
            lblWorkedHours.setText("Worked today (hrs): -");
            return;
        }

        double hours = timeOps.getWorkedHours(todayEntry);
        if (todayEntry.getTimeOut() == null) {
            lblWorkedHours.setText("Worked today (hrs): In progress");
            return;
        }

        lblWorkedHours.setText(String.format(Locale.US, "Worked today (hrs): %.2f", hours));
    }

    private void onClockIn() {
        int empId = empId();
        if (empId <= 0) {
            UiDialogs.error(this, "Invalid EmpID.");
            return;
        }
        boolean ok = timeOps.clockIn(empId);
        if (ok) {
            UiDialogs.info(this, "Time In recorded.");
            if (isOutsideWorkingHours()) {
                UiDialogs.warn(this, "Logged in beyond working hours. Time was recorded but may require supervisor approval.");
            }
        } else {
            UiDialogs.warn(this, "Time In not recorded. Weekend entries should be processed through supervisor manual DTR, or today's time-in already exists.");
        }
        reload();
    }

    private void onClockOut() {
        int empId = empId();
        if (empId <= 0) {
            UiDialogs.error(this, "Invalid EmpID.");
            return;
        }
        boolean ok = timeOps.clockOut(empId);
        if (ok) {
            UiDialogs.info(this, "Time Out recorded.");
            TimeEntry todayEntry = timeOps.getEntryForDate(empId, LocalDate.now());
            if (timeOps.isWorkedDurationTooShort(todayEntry)) {
                UiDialogs.warn(this, "Recorded work duration is below the minimum review threshold of "
                        + TimeService.MIN_VALID_WORK_HOURS
                        + " hour(s). Please contact the supervisor for DTR correction.");
            }
            if (isOutsideWorkingHours()) {
                UiDialogs.warn(this, "Logged out beyond working hours. Time was recorded but may require supervisor approval.");
            }
        } else {
            UiDialogs.warn(this, "Time Out not recorded. Weekend entries should be processed through supervisor manual DTR, or today's time-out already exists.");
        }
        reload();
    }

    private boolean isOutsideWorkingHours() {
        LocalTime now = LocalTime.now();
        return now.isBefore(LocalTime.of(8, 0)) || now.isAfter(LocalTime.of(17, 0));
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
