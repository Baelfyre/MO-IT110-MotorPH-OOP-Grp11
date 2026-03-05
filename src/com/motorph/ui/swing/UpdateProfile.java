/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.User;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.motorph.domain.models.Employee;
import com.motorph.service.EmployeeService;
import com.motorph.ops.hr.HROps;
import com.motorph.utils.ValidationUtil;

/**
 *
 * @author ACER
 */

public class UpdateProfile extends JPanel {

    // Text fields
    private final JTextField txtEmpNo = new JTextField(10);
    private final JTextField txtLastName = new JTextField(18);
    private final JTextField txtFirstName = new JTextField(18);

    private final JTextField txtAddress = new JTextField(40);

    // Birthday (JCalendar)
    private final JDateChooser dcBirthday = new JDateChooser();

    private final JTextField txtSSS = new JTextField(16);
    private final JTextField txtPagibig = new JTextField(16);
    private final JTextField txtTIN = new JTextField(16);
    private final JTextField txtPhilHealth = new JTextField(16);
    private final JTextField txtPhone = new JTextField(16);

    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Regular", "Probationary"});
    private final JComboBox<String> cbSupervisor = new JComboBox<>(new String[]{"Select Supervisor"});
    private final JComboBox<String> cbPosition = new JComboBox<>(new String[]{"Select Position"});

    private final JTextField txtBasicSalary = new JTextField(12);
    private final JTextField txtRiceSubsidy = new JTextField(12);
    private final JTextField txtPhoneAllowance = new JTextField(12);
    private final JTextField txtClothingAllowance = new JTextField(12);

    // Computed fields
    private final JTextField txtGrossSemiMonthly = new JTextField(12);
    private final JTextField txtHourlyRate = new JTextField(12);
    
    private final User currentUser;
    private final EmployeeService employeeService;
    private final HROps hrOps;
    private Employee currentEmployee;

    public UpdateProfile(User currentUser, EmployeeService employeeService, HROps hrOps) {
        this.currentUser = currentUser; // Save the user
        this.employeeService = employeeService;
        this.hrOps = hrOps;
        
        // 1. Tell this panel to use a BorderLayout so it fills the screen
        this.setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Field rules
        txtEmpNo.setEditable(false);
        txtGrossSemiMonthly.setEditable(false);
        txtHourlyRate.setEditable(false);

        // JDateChooser setup
        dcBirthday.setDateFormatString("dd-MMM-yy"); // Example: 01-Jan-24
        dcBirthday.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        GridBagConstraints gbc = baseGbc();
        int r = 0;

        // Row 0: Emp # | Last | First
        addLabel(content, gbc, 0, r, "Employee #:");
        addField(content, gbc, 1, r, txtEmpNo);

        addLabel(content, gbc, 2, r, "Last Name:");
        addField(content, gbc, 3, r, txtLastName);

        addLabel(content, gbc, 4, r, "First Name:");
        addField(content, gbc, 5, r, txtFirstName);
        r++;

        // Row 1: Address spans | Birthday (JCalendar)
        addLabel(content, gbc, 0, r, "Address:");
        addFieldSpan(content, gbc, 1, r, 3, txtAddress);

        addLabel(content, gbc, 4, r, "Birthday:");
        addField(content, gbc, 5, r, dcBirthday);
        r++;

        // Row 2: SSS | Pag-ibig | TIN
        addLabel(content, gbc, 0, r, "SSS #:");
        addField(content, gbc, 1, r, txtSSS);

        addLabel(content, gbc, 2, r, "Pag-ibig #:");
        addField(content, gbc, 3, r, txtPagibig);

        addLabel(content, gbc, 4, r, "TIN #:");
        addField(content, gbc, 5, r, txtTIN);
        r++;

        // Row 3: Status | Phone | PhilHealth
        addLabel(content, gbc, 0, r, "Status:");
        addField(content, gbc, 1, r, cbStatus);

        addLabel(content, gbc, 2, r, "Phone #:");
        addField(content, gbc, 3, r, txtPhone);

        addLabel(content, gbc, 4, r, "PhilHealth #:");
        addField(content, gbc, 5, r, txtPhilHealth);
        r++;

        // Row 4: Supervisor | Position
        addLabel(content, gbc, 0, r, "Immediate Supervisor:");
        addField(content, gbc, 1, r, cbSupervisor);

        addLabel(content, gbc, 2, r, "Position:");
        addFieldSpan(content, gbc, 3, r, 3, cbPosition);
        r++;

        // Row 5: Basic | Rice | Phone Allow
        addLabel(content, gbc, 0, r, "Basic Salary:");
        addField(content, gbc, 1, r, txtBasicSalary);

        addLabel(content, gbc, 2, r, "Rice Subsidy:");
        addField(content, gbc, 3, r, txtRiceSubsidy);

        addLabel(content, gbc, 4, r, "Phone Allowance:");
        addField(content, gbc, 5, r, txtPhoneAllowance);
        r++;

        // Row 6: Gross Semi | Clothing
        addLabel(content, gbc, 0, r, "Gross Semi-Monthly Rate:");
        addField(content, gbc, 1, r, txtGrossSemiMonthly);

        addLabel(content, gbc, 2, r, "Clothing Allowance:");
        addField(content, gbc, 3, r, txtClothingAllowance);
        r++;

        // Row 7: Hourly Rate
        addLabel(content, gbc, 0, r, "Hourly Rate:");
        addField(content, gbc, 1, r, txtHourlyRate);
        r++;

        // Buttons row
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        JButton btnClear = new JButton("Clear");
        JButton btnUpdate = new JButton("Update");
        buttons.add(btnClear);
        buttons.add(btnUpdate);

        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.insets = new Insets(18, 0, 0, 0);
        content.add(buttons, gbc);

        // Basic wiring (placeholder)
        btnClear.addActionListener(e -> clearForm());
        btnUpdate.addActionListener(e -> onUpdate());
    
       this.add(content, BorderLayout.CENTER);
       
       loadEmployeeData();
    }

   

    // Annotation: Clears all input fields and resets drop-down selections.
    private void clearForm() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtAddress.setText("");
        dcBirthday.setDate(null);

        txtSSS.setText("");
        txtPagibig.setText("");
        txtTIN.setText("");
        txtPhilHealth.setText("");
        txtPhone.setText("");

        cbStatus.setSelectedIndex(0);
        cbSupervisor.setSelectedIndex(0);
        cbPosition.setSelectedIndex(0);

        txtBasicSalary.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");

        txtGrossSemiMonthly.setText("");
        txtHourlyRate.setText("");
    }

    // Reads the CSV through the service and fills the text fields
    private void loadEmployeeData() {
        int empId = Integer.parseInt(currentUser.getUsername());
        currentEmployee = employeeService.getEmployee(empId);

        if (currentEmployee != null) {
            txtEmpNo.setText(String.valueOf(currentEmployee.getId()));
            txtLastName.setText(currentEmployee.getLastName());
            txtFirstName.setText(currentEmployee.getFirstName());
            txtAddress.setText(currentEmployee.getAddress());
            setBirthdayFromLocalDate(currentEmployee.getBirthday());

            txtSSS.setText(currentEmployee.getSssNumber());
            txtPagibig.setText(currentEmployee.getPagIbigNumber());
            txtTIN.setText(currentEmployee.getTinNumber());
            txtPhilHealth.setText(currentEmployee.getPhilHealthNumber());
            txtPhone.setText(currentEmployee.getPhoneNumber());

            cbStatus.setSelectedItem(currentEmployee.getStatus());
            cbPosition.setSelectedItem(currentEmployee.getPosition());
            cbSupervisor.setSelectedItem(currentEmployee.getImmediateSupervisor());

            txtBasicSalary.setText(String.valueOf(currentEmployee.getBasicSalary()));
            txtRiceSubsidy.setText(String.valueOf(currentEmployee.getRiceAllowance()));
            txtPhoneAllowance.setText(String.valueOf(currentEmployee.getPhoneAllowance()));
            txtClothingAllowance.setText(String.valueOf(currentEmployee.getClothingAllowance()));

            txtGrossSemiMonthly.setText(String.valueOf(currentEmployee.getGrossSemiMonthlyRate()));
            txtHourlyRate.setText(String.valueOf(currentEmployee.getHourlyRate()));
        }
    }
    
    // Gathers text field data, runs validation, updates the model, and saves to CSV
    private void onUpdate() {
        if (currentEmployee == null) return;

        // --- 1. VALIDATION CHECKS ---
        
        // Check Required Fields
        if (ValidationUtil.isEmpty(txtLastName.getText()) || ValidationUtil.isEmpty(txtFirstName.getText())) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop execution
        }

        // Check Number Formats (Salaries and Allowances)
        if (!ValidationUtil.isPositiveDouble(txtBasicSalary.getText()) ||
            !ValidationUtil.isPositiveDouble(txtRiceSubsidy.getText()) ||
            !ValidationUtil.isPositiveDouble(txtPhoneAllowance.getText()) ||
            !ValidationUtil.isPositiveDouble(txtClothingAllowance.getText())) {
            JOptionPane.showMessageDialog(this, "Compensation fields (Salary, Allowances) must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop execution
        }

        // Check Government ID Formats
        if (!ValidationUtil.isValidSssFormat(txtSSS.getText())) {
            JOptionPane.showMessageDialog(this, "SSS Number must follow the format XX-XXXXXXX-X.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop execution
        }
        
        if (!ValidationUtil.isValidTinFormat(txtTIN.getText())) {
            JOptionPane.showMessageDialog(this, "TIN must follow the format XXX-XXX-XXX-XXX.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop execution
        }
        
        if (!ValidationUtil.isNumbersAndDashesOnly(txtPhone.getText()) ||
            !ValidationUtil.isNumbersAndDashesOnly(txtPagibig.getText()) ||
            !ValidationUtil.isNumbersAndDashesOnly(txtPhilHealth.getText())) {
            
            JOptionPane.showMessageDialog(this, "Phone Number, Pag-IBIG, and PhilHealth must only contain numbers and dashes (no letters).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return; // Stop execution
        }

        // --- 2. SAFE PARSING AND SAVING ---
        try {
            // Update the Employee object
            currentEmployee.setLastName(txtLastName.getText().trim());
            currentEmployee.setFirstName(txtFirstName.getText().trim());
            currentEmployee.setAddress(txtAddress.getText().trim());
            currentEmployee.setBirthday(getBirthdayAsLocalDate());

            currentEmployee.setSssNumber(txtSSS.getText().trim());
            currentEmployee.setPagIbigNumber(txtPagibig.getText().trim());
            currentEmployee.setTinNumber(txtTIN.getText().trim());
            currentEmployee.setPhilHealthNumber(txtPhilHealth.getText().trim());
            currentEmployee.setPhoneNumber(txtPhone.getText().trim());

            // Get ComboBox selections safely
            if (cbStatus.getSelectedItem() != null) currentEmployee.setStatus(cbStatus.getSelectedItem().toString());
            if (cbPosition.getSelectedItem() != null) currentEmployee.setPosition(cbPosition.getSelectedItem().toString());
            if (cbSupervisor.getSelectedItem() != null) currentEmployee.setImmediateSupervisor(cbSupervisor.getSelectedItem().toString());

            // Update Compensation (Safely parsed because of our validation above!)
            currentEmployee.setBasicSalary(Double.parseDouble(txtBasicSalary.getText().trim()));
            currentEmployee.setRiceAllowance(Double.parseDouble(txtRiceSubsidy.getText().trim()));
            currentEmployee.setPhoneAllowance(Double.parseDouble(txtPhoneAllowance.getText().trim()));
            currentEmployee.setClothingAllowance(Double.parseDouble(txtClothingAllowance.getText().trim()));

            // Call HROps to save back to CSV!
            int performerId = Integer.parseInt(currentUser.getUsername());
            boolean success = hrOps.updateEmployee(currentEmployee, performerId);

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                } 
            else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            // This acts as a final safety net for unexpected errors
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: \n" + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }

    // Annotation: Converts the selected calendar date to LocalDate for the model layer.
    public LocalDate getBirthdayAsLocalDate() {
        Date d = dcBirthday.getDate();
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Annotation: Loads an existing LocalDate value into the calendar picker.
    public void setBirthdayFromLocalDate(LocalDate date) {
        if (date == null) {
            dcBirthday.setDate(null);
            return;
        }
        Date d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dcBirthday.setDate(d);
    }

    private static GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        return gbc;
    }

    private static void addLabel(JPanel p, GridBagConstraints gbc, int x, int y, String text) {
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 0;
        p.add(new JLabel(text), c);
    }

    private static void addField(JPanel p, GridBagConstraints gbc, int x, int y, JComponent field) {
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 1;
        p.add(field, c);
    }

    private static void addFieldSpan(JPanel p, GridBagConstraints gbc, int x, int y, int span, JComponent field) {
        GridBagConstraints c = (GridBagConstraints) gbc.clone();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = span;
        c.weightx = 1;
        p.add(field, c);
    }
}