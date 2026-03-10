/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.JobPosition;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.ProbationaryEmployee;
import com.motorph.domain.models.RegularEmployee;
import com.motorph.utils.ValidationUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author OngoJ.
 */
public class EmployeeFormPanel extends JPanel {

    private final JTextField txtEmpNo = new JTextField(10);
    private final JTextField txtLastName = new JTextField(18);
    private final JTextField txtFirstName = new JTextField(18);

    private final JDateChooser dcBirthday = new JDateChooser();
    private final JTextField txtAddress = new JTextField(40);
    private final JTextField txtPhone = new JTextField(16);

    private final JTextField txtSSS = new JTextField(16);
    private final JTextField txtPhilHealth = new JTextField(16);
    private final JTextField txtTIN = new JTextField(16);
    private final JTextField txtPagibig = new JTextField(16);

    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary", "Archived"});
    private final JComboBox<String> cbPosition = new JComboBox<>();
    private final JComboBox<String> cbSupervisor = new JComboBox<>();

    private final JTextField txtBasicSalary = new JTextField(12);
    private final JTextField txtRice = new JTextField(12);
    private final JTextField txtPhoneAllow = new JTextField(12);
    private final JTextField txtClothingAllow = new JTextField(12);
    private final JTextField txtGrossSemi = new JTextField(12);
    private final JTextField txtHourlyRate = new JTextField(12);
    private final JTextField txtLeaveCreditsSummary = new JTextField(20);

    public EmployeeFormPanel() {
        setLayout(new BorderLayout());

        JPanel form = SwingForm.createFormRoot(12);
        GridBagConstraints gbc = SwingForm.baseGbc();
        int r = 0;

        // Annotation: Date chooser provides a calendar popup for convenience.
        dcBirthday.setDateFormatString("MM/dd/yyyy");
        if (dcBirthday.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor editor) {
            editor.setEditable(false);
        }

        txtEmpNo.setEditable(false);
        txtGrossSemi.setEditable(false);
        txtHourlyRate.setEditable(false);
        txtLeaveCreditsSummary.setEditable(false);

        setReadOnlyStyle(txtEmpNo);
        setReadOnlyStyle(txtGrossSemi);
        setReadOnlyStyle(txtHourlyRate);
        setReadOnlyStyle(txtLeaveCreditsSummary);

        SwingForm.addLabel(form, gbc, 0, r, "Employee #:");
        SwingForm.addField(form, gbc, 1, r, txtEmpNo);
        SwingForm.addLabel(form, gbc, 2, r, "Last Name:");
        SwingForm.addField(form, gbc, 3, r, txtLastName);
        SwingForm.addLabel(form, gbc, 4, r, "First Name:");
        SwingForm.addField(form, gbc, 5, r, txtFirstName);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Birthday:");
        SwingForm.addField(form, gbc, 1, r, dcBirthday);
        SwingForm.addLabel(form, gbc, 2, r, "Phone #:");
        SwingForm.addField(form, gbc, 3, r, txtPhone);
        SwingForm.addLabel(form, gbc, 4, r, "Status:");
        SwingForm.addField(form, gbc, 5, r, cbStatus);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Address:");
        SwingForm.addFieldSpan(form, gbc, 1, r, 5, txtAddress);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "SSS #:");
        SwingForm.addField(form, gbc, 1, r, txtSSS);
        SwingForm.addLabel(form, gbc, 2, r, "PhilHealth #:");
        SwingForm.addField(form, gbc, 3, r, txtPhilHealth);
        SwingForm.addLabel(form, gbc, 4, r, "TIN #:");
        SwingForm.addField(form, gbc, 5, r, txtTIN);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Pag-IBIG #:");
        SwingForm.addField(form, gbc, 1, r, txtPagibig);
        SwingForm.addLabel(form, gbc, 2, r, "Position:");
        SwingForm.addField(form, gbc, 3, r, cbPosition);
        SwingForm.addLabel(form, gbc, 4, r, "Supervisor:");
        SwingForm.addField(form, gbc, 5, r, cbSupervisor);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Basic Salary:");
        SwingForm.addField(form, gbc, 1, r, txtBasicSalary);
        SwingForm.addLabel(form, gbc, 2, r, "Rice Allow:");
        SwingForm.addField(form, gbc, 3, r, txtRice);
        SwingForm.addLabel(form, gbc, 4, r, "Phone Allow:");
        SwingForm.addField(form, gbc, 5, r, txtPhoneAllow);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Clothing Allow:");
        SwingForm.addField(form, gbc, 1, r, txtClothingAllow);
        SwingForm.addLabel(form, gbc, 2, r, "Gross Semi-Monthly:");
        SwingForm.addField(form, gbc, 3, r, txtGrossSemi);
        SwingForm.addLabel(form, gbc, 4, r, "Hourly Rate:");
        SwingForm.addField(form, gbc, 5, r, txtHourlyRate);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Leave Credits:");
        SwingForm.addFieldSpan(form, gbc, 1, r, 5, txtLeaveCreditsSummary);

        add(form, BorderLayout.CENTER);

        installDerivedPayListeners();
        installStatusListener();
        loadDefaultPositions();
        seedDefaultSupervisor();
        refreshDerivedPayFields();
        refreshLeaveCreditsSummary();
    }

    private void setReadOnlyStyle(JTextField field) {
        field.setBackground(new Color(240, 240, 240));
    }

    private void loadDefaultPositions() {
        cbPosition.removeAllItems();
        for (JobPosition jobPosition : JobPosition.values()) {
            cbPosition.addItem(jobPosition.getLabel());
        }
    }

    private void seedDefaultSupervisor() {
        cbSupervisor.removeAllItems();
        cbSupervisor.addItem("N/A");
        cbSupervisor.setSelectedIndex(0);
    }

    private void installDerivedPayListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshDerivedPayFields();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshDerivedPayFields();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshDerivedPayFields();
            }
        };

        txtBasicSalary.getDocument().addDocumentListener(listener);
    }

    private void installStatusListener() {
        cbStatus.addActionListener(e -> refreshLeaveCreditsSummary());
    }

    private void refreshDerivedPayFields() {
        double basic = safeMoney(txtBasicSalary.getText());
        double grossSemi = basic > 0.0 ? basic / 2.0 : 0.0;
        double hourly = basic > 0.0 ? basic / 26.0 / 8.0 : 0.0;

        txtGrossSemi.setText(formatMoney(grossSemi));
        txtHourlyRate.setText(formatMoney(hourly));
    }

    private void refreshLeaveCreditsSummary() {
        String status = selectedValue(cbStatus);
        if ("Probationary".equalsIgnoreCase(status)) {
            txtLeaveCreditsSummary.setText("Default seeded leave credits: 0.00 hrs");
        } else if ("Archived".equalsIgnoreCase(status)) {
            txtLeaveCreditsSummary.setText("No new leave credits will be seeded for archived status.");
        } else {
            txtLeaveCreditsSummary.setText("Default seeded leave credits: 40.00 hrs");
        }
    }

    // Annotation: Keeps Employee # read-only in both add and edit flows.
    public void setEmployeeNumberEditable(boolean editable) {
        txtEmpNo.setEditable(editable);
    }

    public void setSuggestedEmployeeNumber(int empId) {
        txtEmpNo.setText(empId > 0 ? String.valueOf(empId) : "");
    }

    public void setPositionOptions(List<String> positions) {
        cbPosition.removeAllItems();
        if (positions == null || positions.isEmpty()) {
            loadDefaultPositions();
            return;
        }
        for (String position : positions) {
            cbPosition.addItem(position);
        }
    }

    public void setSupervisorOptions(List<String> supervisors) {
        cbSupervisor.removeAllItems();
        cbSupervisor.addItem("N/A");
        if (supervisors != null) {
            for (String supervisor : supervisors) {
                if (supervisor == null || supervisor.trim().isEmpty()) {
                    continue;
                }
                if (!"N/A".equalsIgnoreCase(supervisor.trim())) {
                    cbSupervisor.addItem(supervisor.trim());
                }
            }
        }
        cbSupervisor.setSelectedIndex(0);
    }

    public void setLeaveCreditsSummary(String summary) {
        txtLeaveCreditsSummary.setText(summary == null ? "" : summary);
    }

    // Annotation: Loads an Employee record into the form fields.
    public void setEmployee(Employee e) {
        if (e == null) {
            clear();
            return;
        }

        txtEmpNo.setText(String.valueOf(e.getEmployeeNumber()));
        txtLastName.setText(nullToEmpty(e.getLastName()));
        txtFirstName.setText(nullToEmpty(e.getFirstName()));

        LocalDate bday = e.getBirthday();
        if (bday != null) {
            dcBirthday.setDate(Date.from(bday.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        } else {
            dcBirthday.setDate(null);
        }

        txtAddress.setText(nullToEmpty(e.getAddress()));
        txtPhone.setText(nullToEmpty(e.getPhoneNumber()));

        txtSSS.setText(nullToEmpty(e.getSssNumber()));
        txtPhilHealth.setText(nullToEmpty(e.getPhilHealthNumber()));
        txtTIN.setText(nullToEmpty(e.getTinNumber()));
        txtPagibig.setText(nullToEmpty(e.getPagIbigNumber()));

        cbStatus.setSelectedItem(nullToEmpty(e.getStatus()));
        cbPosition.setSelectedItem(nullToEmpty(e.getPosition()));
        cbSupervisor.setSelectedItem(nullToEmpty(e.getImmediateSupervisor()).isEmpty() ? "N/A" : nullToEmpty(e.getImmediateSupervisor()));

        txtBasicSalary.setText(formatMoney(e.getBasicSalary()));
        txtRice.setText(formatMoney(e.getRiceAllowance()));
        txtPhoneAllow.setText(formatMoney(e.getPhoneAllowance()));
        txtClothingAllow.setText(formatMoney(e.getClothingAllowance()));
        txtGrossSemi.setText(formatMoney(e.getGrossSemiMonthlyRate()));
        txtHourlyRate.setText(formatMoney(e.getHourlyRate()));
        refreshLeaveCreditsSummary();
    }

    // Annotation: Clears all form fields.
    public void clear() {
        txtEmpNo.setText("");
        txtLastName.setText("");
        txtFirstName.setText("");
        dcBirthday.setDate(null);
        txtAddress.setText("");
        txtPhone.setText("");
        txtSSS.setText("");
        txtPhilHealth.setText("");
        txtTIN.setText("");
        txtPagibig.setText("");
        cbStatus.setSelectedIndex(0);
        if (cbPosition.getItemCount() > 0) {
            cbPosition.setSelectedIndex(0);
        }
        cbSupervisor.setSelectedIndex(0);
        txtBasicSalary.setText("");
        txtRice.setText("");
        txtPhoneAllow.setText("");
        txtClothingAllow.setText("");
        refreshDerivedPayFields();
        refreshLeaveCreditsSummary();
    }

    // Annotation: Builds an Employee object from the form values.
    public Employee buildEmployeeOrNull(Component parentForErrors) {
        String empNoStr = txtEmpNo.getText().trim();
        int empNo;
        try {
            empNo = Integer.parseInt(empNoStr);
        } catch (NumberFormatException ex) {
            UiDialogs.error(parentForErrors, "Employee # must be a number.");
            return null;
        }

        String last = txtLastName.getText().trim();
        String first = txtFirstName.getText().trim();
        LocalDate bday = LocalDates.toLocalDate(dcBirthday.getDate());
        String position = selectedValue(cbPosition);
        String supervisor = selectedValue(cbSupervisor);

        List<String> errors = new ArrayList<>(ValidationUtil.validateEmployeeFields(
                last,
                first,
                bday,
                txtPhone.getText(),
                txtSSS.getText(),
                txtPhilHealth.getText(),
                txtTIN.getText(),
                txtPagibig.getText(),
                position,
                supervisor,
                txtBasicSalary.getText(),
                txtRice.getText(),
                txtPhoneAllow.getText(),
                txtClothingAllow.getText()
        ));

        if (!errors.isEmpty()) {
            UiDialogs.error(parentForErrors, String.join("\n", errors));
            return null;
        }

        double basic = safeMoney(txtBasicSalary.getText());
        double rice = safeMoney(txtRice.getText());
        double phoneAllow = safeMoney(txtPhoneAllow.getText());
        double clothing = safeMoney(txtClothingAllow.getText());
        double grossSemi = safeMoney(txtGrossSemi.getText());
        double hourly = safeMoney(txtHourlyRate.getText());

        String status = selectedValue(cbStatus);

        Employee emp;
        if ("Probationary".equalsIgnoreCase(status)) {
            emp = new ProbationaryEmployee(empNo, last, first);
        } else {
            emp = new RegularEmployee(empNo, last, first);
        }

        emp.setBirthday(bday);
        emp.setAddress(txtAddress.getText().trim());
        emp.setPhoneNumber(txtPhone.getText().trim());

        emp.setSssNumber(txtSSS.getText().trim());
        emp.setPhilHealthNumber(txtPhilHealth.getText().trim());
        emp.setTinNumber(txtTIN.getText().trim());
        emp.setPagIbigNumber(txtPagibig.getText().trim());

        emp.setStatus(status);
        emp.setPosition(position);
        emp.setImmediateSupervisor("N/A".equalsIgnoreCase(supervisor) ? "N/A" : supervisor);

        emp.setBasicSalary(basic);
        emp.setRiceAllowance(rice);
        emp.setPhoneAllowance(phoneAllow);
        emp.setClothingAllowance(clothing);
        emp.setGrossSemiMonthlyRate(grossSemi);
        emp.setHourlyRate(hourly);

        return emp;
    }

    private String selectedValue(JComboBox<String> comboBox) {
        Object value = comboBox.getSelectedItem();
        return value == null ? "" : value.toString().trim();
    }

    private double safeMoney(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private String formatMoney(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
