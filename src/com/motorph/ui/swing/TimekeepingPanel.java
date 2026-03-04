/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.ApprovalStatus;
import com.motorph.domain.models.PayPeriod;
import com.motorph.domain.models.TimeEntry;
import com.motorph.domain.models.User;
import com.motorph.repository.PayrollApprovalRepository;
import com.motorph.repository.TimeEntryRepository;
import com.motorph.repository.csv.CsvPayrollApprovalRepository;
import com.motorph.repository.csv.CsvTimeRepository;
import com.motorph.repository.csv.CsvEmployeeRepository;
import com.motorph.service.TimeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Employee attendance panel.
 *
 * Annotation: Uses TimeService for clock in and out. Uses repository read for table.
 * @author ACER
 */
public class TimekeepingPanel extends JPanel {

    private final User currentUser;

    private final TimeEntryRepository timeRepo;
    private final TimeService timeService;
    private final PayrollApprovalRepository approvalRepo;

    private final JSpinner spAnyDate = new JSpinner(new SpinnerDateModel());
    private final JLabel lblPeriod = new JLabel("Period: -");
    private final JLabel lblDtrStatus = new JLabel("DTR Status: -");

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

    public TimekeepingPanel(User currentUser) {
        this.currentUser = currentUser;

        // Annotation: Time repository uses employee repo only for name columns in CSV.
        this.timeRepo = new CsvTimeRepository(new CsvEmployeeRepository());
        this.timeService = new TimeService(timeRepo);
        this.approvalRepo = new CsvPayrollApprovalRepository();

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
        top.add(lblDtrStatus);
        top.add(btnClockIn);
        top.add(btnClockOut);
        top.add(btnRefresh);

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

        btnClockIn.addActionListener(e -> onClockIn());
        btnClockOut.addActionListener(e -> onClockOut());
        btnRefresh.addActionListener(e -> reload());
    }

    private int empId() {
        if (currentUser == null) {
            return 0;
        }
        return currentUser.getId();
    }

    // Annotation: Updates active PayPeriod and reloads table.
    private void setActivePeriod(LocalDate anyDate) {
        this.activePeriod = PayPeriod.fromDateSemiMonthly(anyDate);
        lblPeriod.setText("Period: " + activePeriod.getStartDate() + " to " + activePeriod.getEndDate());

        int empId = empId();
        approvalRepo.ensureRowExists(empId, activePeriod);
        ApprovalStatus dtr = approvalRepo.getDtrStatus(empId, activePeriod);
        lblDtrStatus.setText("DTR Status: " + (dtr == null ? "N/A" : dtr.name()));

        reload();
    }

    private void reload() {
        model.setRowCount(0);
        int empId = empId();
        List<TimeEntry> entries = timeRepo.findByEmployeeAndPeriod(empId, activePeriod);
        for (TimeEntry t : entries) {
            model.addRow(new Object[]{
                    t.getDate() == null ? "" : t.getDate().format(DATE_FMT),
                    t.getTimeIn() == null ? "" : t.getTimeIn().format(TIME_FMT),
                    t.getTimeOut() == null ? "" : t.getTimeOut().format(TIME_FMT)
            });
        }
    }

    // Annotation: Saves time-in using TimeService and refreshes table.
    private void onClockIn() {
        int empId = empId();
        if (empId <= 0) {
            UiDialogs.error(this, "Invalid EmpID.");
            return;
        }

        boolean ok = timeService.logTimeIn(empId);
        if (ok) {
            UiDialogs.info(this, "Time In recorded.");
        } else {
            UiDialogs.warn(this, "Time In not recorded. Check if today is a workday or already logged.");
        }
        reload();
    }

    // Annotation: Saves time-out using TimeService and refreshes table.
    private void onClockOut() {
        int empId = empId();
        if (empId <= 0) {
            UiDialogs.error(this, "Invalid EmpID.");
            return;
        }

        boolean ok = timeService.logTimeOut(empId);
        if (ok) {
            UiDialogs.info(this, "Time Out recorded.");
        } else {
            UiDialogs.warn(this, "Time Out not recorded. Check if Time In exists and Time Out is not yet logged.");
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
