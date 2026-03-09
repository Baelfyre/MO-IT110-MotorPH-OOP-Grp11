/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.Role;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;
import com.motorph.ops.hr.HROps;
import com.motorph.repository.csv.DataPaths;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * Employee Management panel.
 *
 * Annotation: UI stays thin by calling HROps for CRUD.
 *
 * @author ACER
 */
public class HrPanel extends JPanel {

    private final User currentUser;
    private final HROps hrOps;

    private final JTextField txtSearch = new JTextField(14);
    private final JCheckBox chkIncludeArchived = new JCheckBox("Include Archived");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"EmpID", "Last Name", "First Name", "Status", "Position", "Supervisor"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable tbl = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JButton btnRefresh = new JButton("Refresh");
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnEdit = new JButton("Edit");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnLogs = new JButton("System Logs");

    public HrPanel(User currentUser, HROps hrOps) {
        this.currentUser = currentUser;
        this.hrOps = hrOps;

        buildUi();
        applyPermissions();
        loadEmployees();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.add(new JLabel("Search EmpID or Name:"));
        top.add(txtSearch);
        JButton btnFind = new JButton("Find");
        JButton btnClear = new JButton("Clear");

        top.add(btnFind);
        top.add(btnClear);
        top.add(chkIncludeArchived);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnLogs);

        tbl.setRowSorter(sorter);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        // Annotation: Search applies a table row filter.
        btnFind.addActionListener(e -> applyFilter());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            sorter.setRowFilter(null);
        });
        btnRefresh.addActionListener(e -> loadEmployees());
        chkIncludeArchived.addActionListener(e -> loadEmployees());

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnLogs.addActionListener(e -> showSystemLogs());
    }

    private void applyPermissions() {
        boolean hasHR = currentUser != null && currentUser.getRoles().contains(Role.HR);
        boolean hasIT = currentUser != null && currentUser.getRoles().contains(Role.IT);

        boolean canCrud = hasHR || hasIT;
        boolean canDelete = hasIT;

        btnAdd.setEnabled(canCrud);
        btnEdit.setEnabled(canCrud);
        btnDelete.setEnabled(canDelete);

        // Annotation: Hide actions for non-HR/IT roles.
        if (!canCrud) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
        }
        if (!canDelete) {
            btnDelete.setVisible(false);
        }
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim();
        if (q.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        // Annotation: Case-insensitive contains match.
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
    }

    // Annotation: Reloads employee rows from repository through HROps.
    private void loadEmployees() {
        model.setRowCount(0);
        List<Employee> list = hrOps.listEmployees(chkIncludeArchived.isSelected());
        for (Employee e : list) {
            model.addRow(new Object[]{
                e.getEmployeeNumber(),
                e.getLastName(),
                e.getFirstName(),
                e.getStatus(),
                e.getPosition(),
                e.getImmediateSupervisor()
            });
        }
    }

    private Integer selectedEmpId() {
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

    // Annotation: Opens a dialog and creates employee profile plus auto-login via HROps.
    private void onAdd() {
        EmployeeFormPanel form = new EmployeeFormPanel();
        form.setEmployeeNumberEditable(true);

        int r = JOptionPane.showConfirmDialog(this, form, "Add Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        Employee emp = form.buildEmployeeOrNull(this);
        if (emp == null) {
            return;
        }

        boolean ok = hrOps.createEmployee(emp, currentUser == null ? 0 : currentUser.getId());
        if (ok) {
            UiDialogs.info(this, "Employee created.");
            loadEmployees();
        } else {
            UiDialogs.error(this, "Create failed. Check duplicates or required fields.");
        }
    }

    // Annotation: Opens a dialog for selected employee and updates through HROps.
    private void onEdit() {
        Integer empId = selectedEmpId();
        if (empId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        Employee existing = hrOps.getEmployee(empId);
        if (existing == null) {
            UiDialogs.error(this, "Employee not found.");
            return;
        }

        EmployeeFormPanel form = new EmployeeFormPanel();
        form.setEmployee(existing);
        form.setEmployeeNumberEditable(false);

        int r = JOptionPane.showConfirmDialog(this, form, "Edit Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        Employee updated = form.buildEmployeeOrNull(this);
        if (updated == null) {
            return;
        }

        boolean ok = hrOps.updateEmployee(updated, currentUser == null ? 0 : currentUser.getId());
        if (ok) {
            UiDialogs.info(this, "Employee updated.");
            loadEmployees();
        } else {
            UiDialogs.error(this, "Update failed.");
        }
    }

    // Annotation: Deletes selected employee and related login using HROps.
    private void onDelete() {
        Integer empId = selectedEmpId();
        if (empId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        if (!UiDialogs.confirm(this, "Delete EmpID " + empId + "? This also deletes the login account.")) {
            return;
        }

        boolean ok = hrOps.deleteEmployee(empId, currentUser == null ? 0 : currentUser.getId());
        if (ok) {
            UiDialogs.info(this, "Employee deleted.");
            loadEmployees();
        } else {
            UiDialogs.error(this, "Delete failed.");
        }
    }

    // Annotation: Displays system logs from system_logs.csv.
    private void showSystemLogs() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "System Logs", Dialog.ModalityType.APPLICATION_MODAL);

        DefaultTableModel logModel = new DefaultTableModel(
                new Object[]{"Log_ID", "Timestamp", "User", "Action", "Details"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable t = new JTable(logModel);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        loadSystemLogs(logModel);

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(
                null,
                new JScrollPane(t),
                buttonRow(dlg)
        ));
        dlg.setSize(900, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buttonRow(JDialog dlg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dlg.dispose());
        p.add(close);
        return p;
    }

    private void loadSystemLogs(DefaultTableModel logModel) {
        logModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.SYSTEM_LOG_CSV))) {
            String header = br.readLine();
            if (header == null) {
                return;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] d = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (d.length < 5) {
                    continue;
                }
                logModel.addRow(new Object[]{
                    unquote(d[0]),
                    unquote(d[1]),
                    unquote(d[2]),
                    unquote(d[3]),
                    unquote(d[4])
                });
            }
        } catch (Exception e) {
            UiDialogs.error(this, "Unable to read system logs.");
        }
    }

    private String unquote(String s) {
        if (s == null) {
            return "";
        }
        String v = s.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1);
            v = v.replace("\"\"", "\"");
        }
        return v;
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
