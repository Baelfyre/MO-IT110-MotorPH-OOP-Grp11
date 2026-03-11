/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.JobPosition;
import com.motorph.domain.enums.Role;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.LogEntry;
import com.motorph.domain.models.User;
import com.motorph.ops.hr.HROps;
import com.motorph.repository.csv.CsvAddressReferenceRepository;
import com.motorph.service.LogService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final LogService logService;
    private final CsvAddressReferenceRepository addressRepo;

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

    public HrPanel(User currentUser, HROps hrOps, CsvAddressReferenceRepository addressRepo) {
        this(currentUser, hrOps, new LogService(), addressRepo);
    }

    public HrPanel(User currentUser, HROps hrOps, LogService logService, CsvAddressReferenceRepository addressRepo) {
        this.currentUser = currentUser;
        this.hrOps = hrOps;
        this.logService = logService;
        this.addressRepo = addressRepo;

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
        if (currentUser == null) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
            return;
        }
        // Annotation: Permission-based RBAC instead of hardcoded roles.
        boolean canManage = currentUser.hasPermission("CAN_MANAGE_EMPLOYEES");

        btnAdd.setVisible(canManage);
        btnEdit.setVisible(canManage);
        btnDelete.setVisible(canManage);
    }

    private void applyFilter() {
        String q = txtSearch.getText() == null ? "" : txtSearch.getText().trim();
        if (q.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
    }

    private void loadEmployees() {
        model.setRowCount(0);
        List<Employee> rows = hrOps.listEmployees(chkIncludeArchived.isSelected());
        rows.sort(Comparator.comparingInt(Employee::getEmployeeNumber));

        for (Employee e : rows) {
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

    // Annotation: Opens a validation-safe dialog and creates employee profile plus auto-login via HROps.
    private void onAdd() {
        EmployeeFormPanel form = new EmployeeFormPanel();
        form.setEmployeeNumberEditable(true);

        int r = JOptionPane.showConfirmDialog(this, form, "Add Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        // Updated to pass hrOps and true for duplicate ID checking
        Employee emp = form.buildEmployeeOrNull(this, hrOps, true);
        if (emp == null) {
            return; // Validation failed inside the form
        }

        try {
            boolean ok = hrOps.createEmployee(emp, currentUser);
            if (ok) {
                UiDialogs.info(this, "Employee created.");
                loadEmployees();
            } else {
                UiDialogs.error(this, "Create failed. Check duplicates or required fields.");
            }
        } catch (SecurityException ex) {
            // Catches the backend RBAC check to prevent crashes
            UiDialogs.error(this, ex.getMessage());
        }
    }

    // Annotation: Opens a validation-safe dialog for selected employee and updates through HROps.
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

        EmployeeFormPanel form = buildPreparedForm(existing);
        form.setEmployee(existing);
        form.setEmployeeNumberEditable(false);

        int r = JOptionPane.showConfirmDialog(this, form, "Edit Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        Employee updated = form.buildEmployeeOrNull(this, hrOps, false);
        if (updated == null) {
            return;
        }

        boolean ok = hrOps.updateEmployee(updated, currentUser);
        if (ok) {
            UiDialogs.info(this, "Employee updated.");
            loadEmployees();
        } else {
            UiDialogs.error(this, "Update failed.");
        }
    }

    // Annotation: Deletes selected employee and linked CSV records using HROps.
    private void onDelete() {
        Integer empId = selectedEmpId();
        if (empId == null) {
            UiDialogs.warn(this, "Select a row first.");
            return;
        }

        if (!UiDialogs.confirm(this, "Delete EmpID " + empId + "? This removes the employee profile and linked CSV records.")) {
            return;
        }

        boolean ok = hrOps.deleteEmployee(empId, currentUser);
        if (ok) {
            UiDialogs.info(this, "Employee deleted.");
            loadEmployees();
        } else {
            UiDialogs.error(this, "Delete failed.");
        }
    }

    private interface EmployeeSaveAction {
        boolean save(Employee employee);
    }

    private void showEmployeeDialog(String title, EmployeeFormPanel form, EmployeeSaveAction saveAction) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        btnOk.addActionListener(e -> {
            Employee built = form.buildEmployeeOrNull(dialog);
            if (built == null) {
                return;
            }
            if (saveAction.save(built)) {
                dialog.dispose();
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.add(btnOk);
        south.add(btnCancel);

        dialog.setContentPane(SwingForm.wrapNorthCenterSouth(null, new JScrollPane(form), south));
        dialog.setSize(1240, 760);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private EmployeeFormPanel buildPreparedForm(Employee existing) {
        EmployeeFormPanel form = new EmployeeFormPanel(addressRepo);
        form.setPositionOptions(positionOptions());
        form.setSupervisorOptions(supervisorOptions(existing == null ? null : existing.getId()));
        form.setMinimumBasicSalary(currentMinimumBasicSalary(existing));
        if (existing != null) {
            String creditsHint = "Probationary".equalsIgnoreCase(existing.getStatus())
                    ? "Current leave credits: 0.00 hrs"
                    : "Current leave credits: seeded from leave credits file";
            form.setLeaveCreditsSummary(creditsHint);
        }
        return form;
    }

    private double currentMinimumBasicSalary(Employee existing) {
        double min = Double.MAX_VALUE;
        for (Employee employee : hrOps.listEmployees(true)) {
            if (employee == null) {
                continue;
            }
            double basic = employee.getBasicSalary();
            if (basic > 0.0 && basic < min) {
                min = basic;
            }
        }

        if (min == Double.MAX_VALUE) {
            return 1.0;
        }

        if (existing != null && existing.getBasicSalary() > 0.0 && existing.getBasicSalary() < min) {
            return existing.getBasicSalary();
        }

        return min;
    }

    private List<String> positionOptions() {
        List<String> positions = new ArrayList<>();
        for (JobPosition position : JobPosition.values()) {
            positions.add(position.getLabel());
        }
        positions.sort(String.CASE_INSENSITIVE_ORDER);
        return positions;
    }

    private List<String> supervisorOptions(Integer excludeEmpId) {
        List<Employee> employees = hrOps.listEmployees(false);
        List<String> supervisors = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee == null) {
                continue;
            }
            if (excludeEmpId != null && employee.getId() == excludeEmpId) {
                continue;
            }
            JobPosition position = JobPosition.fromLabel(employee.getPosition());
            if (position != null && position.isSupervisorEligible()) {
                supervisors.add(employee.getLastName() + ", " + employee.getFirstName());
            }
        }
        supervisors.sort(String.CASE_INSENSITIVE_ORDER);
        return supervisors;
    }

    private int nextEmployeeId() {
        List<Employee> employees = hrOps.listEmployees(true);
        int max = 10000;
        for (Employee employee : employees) {
            if (employee != null && employee.getId() > max) {
                max = employee.getId();
            }
        }
        return max + 1;
    }

    // Annotation: Displays HR-scoped logs from system_logs.csv.
    private void showSystemLogs() {
        List<LogEntry> logs = new ArrayList<>();
        logs.addAll(logService.getLogsByCategory("HR"));
        logs.sort(Comparator.comparing(LogEntry::getId).reversed());

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "HR Logs", Dialog.ModalityType.APPLICATION_MODAL);

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
        t.setRowSelectionAllowed(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);

        for (LogEntry entry : logs) {
            logModel.addRow(new Object[]{
                    entry.getId(),
                    entry.getTimestamp(),
                    entry.getUser(),
                    entry.getAction(),
                    entry.getDetails()
            });
        }

        if (t.getColumnModel().getColumnCount() >= 5) {
            t.getColumnModel().getColumn(0).setPreferredWidth(70);
            t.getColumnModel().getColumn(1).setPreferredWidth(170);
            t.getColumnModel().getColumn(2).setPreferredWidth(80);
            t.getColumnModel().getColumn(3).setPreferredWidth(180);
            t.getColumnModel().getColumn(4).setPreferredWidth(520);
        }

        t.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showSelectedLogDetail(t);
                }
            }
        });

        dlg.setContentPane(SwingForm.wrapNorthCenterSouth(
                null,
                new JScrollPane(t),
                buttonRow(dlg, t)
        ));
        dlg.setSize(1100, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buttonRow(JDialog dlg, JTable logTable) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton view = new JButton("View Selected");
        JButton close = new JButton("Close");
        view.addActionListener(e -> showSelectedLogDetail(logTable));
        close.addActionListener(e -> dlg.dispose());
        p.add(view);
        p.add(close);
        return p;
    }

    private void showSelectedLogDetail(JTable logTable) {
        int viewRow = logTable.getSelectedRow();
        if (viewRow < 0) {
            UiDialogs.warn(this, "Select a log row first.");
            return;
        }

        int row = logTable.convertRowIndexToModel(viewRow);
        String message = "Log ID: " + valueOf(logTable.getModel().getValueAt(row, 0))
                + "\nTimestamp: " + valueOf(logTable.getModel().getValueAt(row, 1))
                + "\nUser: " + valueOf(logTable.getModel().getValueAt(row, 2))
                + "\nAction: " + valueOf(logTable.getModel().getValueAt(row, 3))
                + "\n\nDetails:\n" + valueOf(logTable.getModel().getValueAt(row, 4));

        JTextArea area = new JTextArea(message, 12, 60);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Log Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
