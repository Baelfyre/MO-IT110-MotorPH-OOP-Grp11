package com.motorph.ui.swing;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.User;
import com.motorph.ops.it.ItOps;
import com.motorph.ops.it.ItOpsImpl;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.CsvUserRepository;
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

    private final UserRepository userRepo;
    private final ItOps itOps;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Username", "EmpID", "Roles", "Locked"}, 0
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

    public ITPanel(User currentUser) {
        this.currentUser = currentUser;
        this.userRepo = new CsvUserRepository();
        this.itOps = new ItOpsImpl(userRepo, new LogService());

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

        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadUsers());
        btnLock.addActionListener(e -> onSetLock(true));
        btnUnlock.addActionListener(e -> onSetLock(false));
        btnResetDefault.addActionListener(e -> onResetDefault());
        btnResetCustom.addActionListener(e -> onResetCustom());
    }

    private void applyPermissions() {
        boolean isIT = currentUser != null && currentUser.getRoles().contains(Role.IT);

        btnLock.setEnabled(isIT);
        btnUnlock.setEnabled(isIT);
        btnResetDefault.setEnabled(isIT);
        btnResetCustom.setEnabled(isIT);

        if (!isIT) {
            btnLock.setVisible(false);
            btnUnlock.setVisible(false);
            btnResetDefault.setVisible(false);
            btnResetCustom.setVisible(false);
        }
    }

    private void loadUsers() {
        model.setRowCount(0);

        List<User> users = userRepo.findAll();
        for (User u : users) {
            model.addRow(new Object[]{
                    u.getUsername(),
                    u.getId(),
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

    private int actorId() {
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void onSetLock(boolean lock) {
        String username = selectedUsername();
        if (username == null || username.isEmpty()) {
            UiDialogs.warn(this, "Select a user first.");
            return;
        }

        boolean ok = itOps.setLockStatus(username, lock, actorId());
        if (ok) {
            UiDialogs.info(this, lock ? "Account locked." : "Account unlocked.");
            loadUsers();
        } else {
            UiDialogs.error(this, "Lock status update failed.");
        }
    }

    private void onResetDefault() {
        String username = selectedUsername();
        if (username == null || username.isEmpty()) {
            UiDialogs.warn(this, "Select a user first.");
            return;
        }

        boolean ok = itOps.resetPasswordToDefault(username, actorId());
        if (ok) {
            UiDialogs.info(this, "Password reset to default.");
            loadUsers();
        } else {
            UiDialogs.error(this, "Reset failed.");
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

        boolean ok = itOps.resetPassword(username, newPass, actorId());
        if (ok) {
            UiDialogs.info(this, "Password updated.");
            loadUsers();
        } else {
            UiDialogs.error(this, "Reset failed.");
        }
    }
}
