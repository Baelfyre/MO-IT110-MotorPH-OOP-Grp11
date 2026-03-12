/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Employee attendance panel.
 *
 * @author ACER
 */
public class TimekeepingPanel extends JPanel {

    private final User currentUser;
    private final com.motorph.ops.time.TimeOps timeOps;

    private final JLabel lblPeriod = new JLabel("Current Period: -");
    private final JLabel lblDtrStatus = new JLabel("DTR Status: -");
    private final JLabel lblWorkedHours = new JLabel("Worked today (hrs): -");
    private final JComboBox<String> cbHistoryYear = new JComboBox<>();
    private final JComboBox<String> cbHistoryMonth = new JComboBox<>();
    private final JButton btnClearFilters = new JButton("Clear Filters");
    private final JButton btnRefresh = new JButton("Refresh");
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public TimekeepingPanel(User currentUser, com.motorph.ops.time.TimeOps timeOps) {
        initComponents();
        this.currentUser = currentUser;
        this.timeOps = timeOps;

        buildUi();
        initHistoryFilters();
        setCurrentPeriod();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(lblPeriod);
        top.add(lblDtrStatus);
        top.add(btnRefresh);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.add(new JLabel("History Year:"));
        filters.add(cbHistoryYear);
        filters.add(new JLabel("History Month:"));
        filters.add(cbHistoryMonth);
        filters.add(btnClearFilters);
        filters.add(lblWorkedHours);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(top);
        north.add(filters);

        tbl.setRowHeight(24);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> reload());
        btnClearFilters.addActionListener(e -> resetHistoryFilters());
        cbHistoryYear.addActionListener(e -> refreshHistoryTable());
        cbHistoryMonth.addActionListener(e -> refreshHistoryTable());
    }

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

    private void setCurrentPeriod() {
        activePeriod = PayPeriod.fromDateSemiMonthly(LocalDate.now());
        lblPeriod.setText("Current Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());
        var dtr = timeOps.getMyDtrStatus(empId(), activePeriod);
        lblDtrStatus.setText("DTR Status: " + (dtr == null ? "N/A" : dtr.name()));
        reload();
    }

    // Annotation: Loads all attendance history, then applies year and month filters for table display.
    private void reload() {
        List<TimeEntry> entries = timeOps.viewMyTimeEntries(empId());
        refreshHistoryFilters(entries);
        refreshHistoryTable();
        refreshWorkedHours(entries);
    }

    private void refreshHistoryFilters(List<TimeEntry> entries) {
        String selectedYear = (String) cbHistoryYear.getSelectedItem();
        String selectedMonth = (String) cbHistoryMonth.getSelectedItem();

        Set<String> years = new LinkedHashSet<>();
        for (TimeEntry entry : entries) {
            if (entry != null && entry.getDate() != null) {
                years.add(String.valueOf(entry.getDate().getYear()));
            }
        }

        cbHistoryYear.removeAllItems();
        cbHistoryYear.addItem("All Years");
        years.stream().sorted().forEach(cbHistoryYear::addItem);
        restoreSelection(cbHistoryYear, selectedYear);
        restoreSelection(cbHistoryMonth, selectedMonth == null ? "All Months" : selectedMonth);
    }

    private void restoreSelection(JComboBox<String> comboBox, String selectedValue) {
        if (selectedValue != null) {
            comboBox.setSelectedItem(selectedValue);
            if (comboBox.getSelectedIndex() < 0) {
                comboBox.setSelectedIndex(0);
            }
        }
    }

    private void resetHistoryFilters() {
        cbHistoryYear.setSelectedIndex(0);
        cbHistoryMonth.setSelectedIndex(0);
        refreshHistoryTable();
    }

    private void refreshHistoryTable() {
        model.setRowCount(0);
        List<TimeEntry> entries = timeOps.viewMyTimeEntries(empId());
        String yearFilter = (String) cbHistoryYear.getSelectedItem();
        String monthFilter = (String) cbHistoryMonth.getSelectedItem();

        for (TimeEntry entry : entries) {
            if (entry == null || entry.getDate() == null) {
                continue;
            }
            if (!matchesYearFilter(entry.getDate(), yearFilter)) {
                continue;
            }
            if (!matchesMonthFilter(entry.getDate(), monthFilter)) {
                continue;
            }
            model.addRow(new Object[]{
                    entry.getDate().format(DATE_FMT),
                    entry.getTimeIn() == null ? "" : entry.getTimeIn().format(TIME_FMT),
                    entry.getTimeOut() == null ? "" : entry.getTimeOut().format(TIME_FMT)
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

    // Annotation: Shows current-day worked hours when both time-in and time-out are present.
    private void refreshWorkedHours(List<TimeEntry> entries) {
        LocalDate today = LocalDate.now();
        for (TimeEntry entry : entries) {
            if (entry == null || entry.getDate() == null) {
                continue;
            }
            if (!today.equals(entry.getDate())) {
                continue;
            }
            if (entry.getTimeIn() == null || entry.getTimeOut() == null) {
                lblWorkedHours.setText("Worked today (hrs): -");
                return;
            }
            long minutes = java.time.Duration.between(entry.getTimeIn(), entry.getTimeOut()).toMinutes();
            lblWorkedHours.setText(String.format(Locale.US, "Worked today (hrs): %.2f", minutes / 60.0));
            return;
        }
        lblWorkedHours.setText("Worked today (hrs): -");
    }

    // Annotation: Checks whether the live clock is outside the normal shift range.
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
