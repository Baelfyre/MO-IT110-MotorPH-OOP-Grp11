/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.Employee;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Date;
import com.motorph.ui.swing.UiHelper.SwingFormHelper;


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
    private final JTextField txtPosition = new JTextField(22);
    private final JTextField txtSupervisor = new JTextField(22);

    private final JTextField txtBasicSalary = new JTextField(12);
    private final JTextField txtRice = new JTextField(12);
    private final JTextField txtPhoneAllow = new JTextField(12);
    private final JTextField txtClothingAllow = new JTextField(12);
    private final JTextField txtGrossSemi = new JTextField(12);
    private final JTextField txtHourlyRate = new JTextField(12);

    public EmployeeFormPanel() {
        setLayout(new BorderLayout());

        JPanel form = SwingForm.createFormRoot(12);
        GridBagConstraints gbc = SwingForm.baseGbc();
        int r = 0;

        // Annotation: Date chooser provides a calendar popup for convenience.
        dcBirthday.setDateFormatString("MM/dd/yyyy");

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
        SwingForm.addField(form, gbc, 3, r, txtPosition);
        SwingForm.addLabel(form, gbc, 4, r, "Supervisor:");
        SwingForm.addField(form, gbc, 5, r, txtSupervisor);
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

        add(form, BorderLayout.CENTER);
    }

    // Annotation: Enables or disables editing of Employee # based on flow.
    public void setEmployeeNumberEditable(boolean editable) {
        txtEmpNo.setEditable(editable);
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
        txtPosition.setText(nullToEmpty(e.getPosition()));
        txtSupervisor.setText(nullToEmpty(e.getImmediateSupervisor()));

        txtBasicSalary.setText(String.valueOf(e.getBasicSalary()));
        txtRice.setText(String.valueOf(e.getRiceAllowance()));
        txtPhoneAllow.setText(String.valueOf(e.getPhoneAllowance()));
        txtClothingAllow.setText(String.valueOf(e.getClothingAllowance()));
        txtGrossSemi.setText(String.valueOf(e.getGrossSemiMonthlyRate()));
        txtHourlyRate.setText(String.valueOf(e.getHourlyRate()));
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
        txtPosition.setText("");
        txtSupervisor.setText("");
        txtBasicSalary.setText("");
        txtRice.setText("");
        txtPhoneAllow.setText("");
        txtClothingAllow.setText("");
        txtGrossSemi.setText("");
        txtHourlyRate.setText("");
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
        if (last.isEmpty() || first.isEmpty()) {
            UiDialogs.error(parentForErrors, "First Name and Last Name are required.");
            return null;
        }

        LocalDate bday = LocalDates.toLocalDate(dcBirthday.getDate());

        double basic = parseMoney(parentForErrors, txtBasicSalary, "Basic Salary");
        if (Double.isNaN(basic)) return null;
        double rice = parseMoney(parentForErrors, txtRice, "Rice Allowance");
        if (Double.isNaN(rice)) return null;
        double phoneAllow = parseMoney(parentForErrors, txtPhoneAllow, "Phone Allowance");
        if (Double.isNaN(phoneAllow)) return null;
        double clothing = parseMoney(parentForErrors, txtClothingAllow, "Clothing Allowance");
        if (Double.isNaN(clothing)) return null;
        double grossSemi = parseMoney(parentForErrors, txtGrossSemi, "Gross Semi-Monthly Rate");
        if (Double.isNaN(grossSemi)) return null;
        double hourly = parseMoney(parentForErrors, txtHourlyRate, "Hourly Rate");
        if (Double.isNaN(hourly)) return null;

        String status = String.valueOf(cbStatus.getSelectedItem());

        com.motorph.domain.models.Employee emp;
        if ("Probationary".equalsIgnoreCase(status)) {
            emp = new com.motorph.domain.models.ProbationaryEmployee(empNo, last, first);
        } else {
            emp = new com.motorph.domain.models.RegularEmployee(empNo, last, first);
        }

        emp.setBirthday(bday);
        emp.setAddress(txtAddress.getText().trim());
        emp.setPhoneNumber(txtPhone.getText().trim());

        emp.setSssNumber(txtSSS.getText().trim());
        emp.setPhilHealthNumber(txtPhilHealth.getText().trim());
        emp.setTinNumber(txtTIN.getText().trim());
        emp.setPagIbigNumber(txtPagibig.getText().trim());

        emp.setStatus(status);
        emp.setPosition(txtPosition.getText().trim());
        emp.setImmediateSupervisor(txtSupervisor.getText().trim());

        emp.setBasicSalary(basic);
        emp.setRiceAllowance(rice);
        emp.setPhoneAllowance(phoneAllow);
        emp.setClothingAllowance(clothing);
        emp.setGrossSemiMonthlyRate(grossSemi);
        emp.setHourlyRate(hourly);

        return emp;
    }

    private double parseMoney(Component parent, JTextField field, String label) {
        String raw = field.getText().trim();
        if (raw.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.replace(",", ""));
        } catch (NumberFormatException ex) {
            UiDialogs.error(parent, label + " must be a number.");
            return Double.NaN;
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
