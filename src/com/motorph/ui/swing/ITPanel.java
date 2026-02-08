/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.User;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ACER
 */
public class ITPanel extends javax.swing.JPanel {

    // Annotation: Store logged-in user context for role-based access control.
    private final User currentUser;

    // Annotation: Table model for user accounts.
    private DefaultTableModel model;

    // Annotation: UI components created outside Form Editor block.
    private JTable table;

    private JButton btnLoad;
    private JButton btnResetDefault;
    private JButton btnResetCustom;
    private JButton btnToggleLock;

    /**
     * Creates new form ITPanel (Form Editor compatible).
     */
    public ITPanel() {
        this(null);
    }

    // Annotation: Preferred constructor for CardLayout screens.
    public ITPanel(User currentUser) {
        initComponents();
        this.currentUser = currentUser;
        buildCustomUi();
        if (currentUser != null) {
            // Example: show role somewhere or use it to control buttons later
            System.out.println("Logged in role: " + currentUser.getRole());
        }

    }

    // Annotation: Build actual UI outside guarded code so Form Editor does not overwrite it.
    private void buildCustomUi() {
        removeAll();
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnLoad = new JButton("Load Accounts");
        btnResetDefault = new JButton("Reset Password Default");
        btnResetCustom = new JButton("Reset Password Custom");
        btnToggleLock = new JButton("Toggle Lock");

        top.add(new JLabel("System Maintenance (IT)"));
        top.add(btnLoad);
        top.add(btnResetDefault);
        top.add(btnResetCustom);
        top.add(btnToggleLock);

        model = new DefaultTableModel(
                new String[]{"Username", "Role", "Locked"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Annotation: Button handlers are stubs; connect to ItOps and UserRepository later.
        btnLoad.addActionListener(e -> loadAccounts());
        btnResetDefault.addActionListener(e -> resetPasswordDefault());
        btnResetCustom.addActionListener(e -> resetPasswordCustom());
        btnToggleLock.addActionListener(e -> toggleLock());

        revalidate();
        repaint();
    }

    // Annotation: Load accounts into the table (stub).
    private void loadAccounts() {
        model.setRowCount(0);

        // Annotation: Temporary rows for UI validation; replace with repository results later.
        model.addRow(new Object[]{"10001", "HR", "No"});
        model.addRow(new Object[]{"10002", "PAYROLL", "No"});
        model.addRow(new Object[]{"10003", "IT", "No"});

        JOptionPane.showMessageDialog(this, "Accounts loaded (demo rows). Connect repository next.");
    }

    // Annotation: Reset password to system default for selected user (stub).
    private void resetPasswordDefault() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select one account first.");
            return;
        }

        String username = String.valueOf(model.getValueAt(row, 0));
        JOptionPane.showMessageDialog(this, "Default password reset (stub) for: " + username);
    }

    // Annotation: Reset password to a custom value for selected user (stub).
    private void resetPasswordCustom() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select one account first.");
            return;
        }

        String username = String.valueOf(model.getValueAt(row, 0));
        String newPass = JOptionPane.showInputDialog(this, "Enter new password for: " + username);

        if (newPass == null) {
            return; // cancel
        }

        if (newPass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password must not be empty.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Custom password reset (stub) for: " + username);
    }

    // Annotation: Toggle lock status Yes/No for selected user (stub).
    private void toggleLock() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select one account first.");
            return;
        }

        String locked = String.valueOf(model.getValueAt(row, 2));
        String newLocked = locked.equalsIgnoreCase("Yes") ? "No" : "Yes";
        model.setValueAt(newLocked, row, 2);

        JOptionPane.showMessageDialog(this, "Lock status updated (stub).");
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
