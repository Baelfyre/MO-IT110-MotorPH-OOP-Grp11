package com.motorph.ui.swing;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LogEntry;
import com.motorph.domain.models.User;
import com.motorph.ops.it.ItOps;
import com.motorph.service.EmployeeService;
import com.motorph.service.LogService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public class ITPanel extends JPanel {

    private final User currentUser;
    private final EmployeeService employeeService;
    private final ItOps itOps;
    private final LogService logService;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Username", "Name", "Roles", "Locked"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);

    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnLock = new JButton("Lock");
    private final JButton btnUnlock = new JButton("Unlock");
    private final JButton btnResetDefault = new JButton("Reset Default Password");
    private final JButton btnResetCustom = new JButton("Reset Custom Password");
    private final JButton btnLogs = new JButton("System Logs");

    public ITPanel(User currentUser, EmployeeService employeeService, ItOps itOps) {
        this(currentUser, employeeService, itOps, new LogService());
    }

    public ITPanel(User currentUser, EmployeeService employeeService, ItOps itOps, LogService logService) {
        this.currentUser = currentUser;
        this.employeeService = employeeService;
        this.itOps = itOps;
        this.logService = logService;

        buildUi();
        applyPermissions();
        loadUsers();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(btnRefresh);
        top.add(btnLock);
        top.add(btnUnlock);
        top.add(btnResetDefault);
        top.add(btnResetCustom);
        top.add(btnLogs);

        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadUsers());
        btnLock.addActionListener(e -> onSetLock(true));
        btnUnlock.addActionListener(e -> onSetLock(false));
        btnResetDefault.addActionListener(e -> onResetDefault());
        btnResetCustom.addActionListener(e -> onResetCustom());
        btnLogs.addActionListener(e -> showLogs());
    }

    private void applyPermissions() {
        if (currentUser == null) {
            btnLock.setVisible(false);
            btnUnlock.setVisible(false);
            btnResetDefault.setVisible(false);
            btnResetCustom.setVisible(false);
            return;
        }

        // Advanced RBAC: Checking specific permissions instead of hardcoded roles
        boolean canLock = currentUser.hasPermission("CAN_LOCK_ACCOUNTS");
        boolean canReset = currentUser.hasPermission("CAN_RESET_PASSWORD");

        btnLock.setVisible(canLock);
        btnUnlock.setVisible(canLock);
        btnResetDefault.setVisible(canReset);
        btnResetCustom.setVisible(canReset);
    }

    private void loadUsers() {
        model.setRowCount(0);

        List<User> users = itOps.listUsers();
        for (User u : users) {
            String fullName = "N/A";
            Employee emp = employeeService.getEmployee(u.getId());
            if (emp != null) {
                fullName = emp.getFirstName() + " " + emp.getLastName();
            }
            model.addRow(new Object[]{
                    u.getUsername(),
                    fullName,
                    String.valueOf(u.getRoles()),
                    u.isLocked() ? "Yes" : "No"
            });
        }
    }

    private String selectedUsername() {
        int row = tbl.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = tbl.convertRowIndexToModel(row);
        Object v = model.getValueAt(modelRow, 0);
        return v == null ? null : String.valueOf(v).trim();
    }

    private void onSetLock(boolean lock) {
        String username = selectedUsername();
        if (username == null || username.isEmpty()) {
            UiDialogs.warn(this, "Select a user first.");
            return;
        }

        try {
            boolean ok = itOps.setLockStatus(username, lock, currentUser);
            if (ok) {
                UiDialogs.info(this, lock ? "Account locked." : "Account unlocked.");
                loadUsers();
            } else {
                UiDialogs.error(this, "Lock status update failed.");
            }
        } catch (SecurityException ex) {
            // Catches the backend RBAC or self-lockout check
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void onResetDefault() {
        String username = selectedUsername();
        if (username == null || username.isEmpty()) {
            UiDialogs.warn(this, "Select a user first.");
            return;
        }

        try {
            boolean ok = itOps.resetPasswordToDefault(username, currentUser);
            if (ok) {
                UiDialogs.info(this, "Password reset to default.");
                loadUsers();
            } else {
                UiDialogs.error(this, "Reset failed.");
            }
        } catch (SecurityException ex) {
            // Catches the backend RBAC check
            UiDialogs.error(this, ex.getMessage());
        }
    }

    private void onResetCustom() {
        String username = selectedUsername();
        if (username == null || username.isEmpty()) {
            UiDialogs.warn(this, "Select a user first.");
            return;
        }

        String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPass == null) {
            return;
        }

        newPass = newPass.trim();
        if (newPass.isEmpty()) {
            UiDialogs.warn(this, "Password cannot be blank.");
            return;
        }

        try {
            boolean ok = itOps.resetPassword(username, newPass, currentUser);
            if (ok) {
                UiDialogs.info(this, "Password updated.");
                loadUsers();
            } else {
                UiDialogs.error(this, "Reset failed.");
            }
        } catch (SecurityException ex) {
            // Catches the backend RBAC check
            UiDialogs.error(this, ex.getMessage());
        }
    }
    
    private void showLogs() {

        List<LogEntry> logs = logService.getLogsByCategory("IT");

        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "System Logs",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        DefaultTableModel logModel = new DefaultTableModel(
                new Object[]{"Log_ID", "Timestamp", "User", "Action", "Details"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(logModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (LogEntry entry : logs) {
            logModel.addRow(new Object[]{
                entry.getId(),
                entry.getTimestamp(),
                entry.getUser(),
                entry.getAction(),
                entry.getDetails()
            });
        }

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        south.add(close);

        dlg.setContentPane(
                SwingForm.wrapNorthCenterSouth(
                        null,
                        new JScrollPane(table),
                        south
                )
        );

        dlg.setSize(900, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
