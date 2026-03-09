/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.Employee;
import com.motorph.domain.models.User;
import com.motorph.ops.hr.HROps;
import com.motorph.repository.csv.CsvAddressReferenceRepository;
import com.motorph.service.EmployeeService;
import com.motorph.utils.AddressFormatter;
import com.motorph.utils.AddressParser;
import com.motorph.utils.InputRestrictionUtil;
import com.motorph.utils.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 *
 * @author OngoJ.
 */
public class UpdateProfile extends JPanel {

    private static final String PROVINCE_PLACEHOLDER = "Select Province";
    private static final String CITY_PLACEHOLDER = "Select City / Municipality";

    // Annotation: Self-service fields only.
    private final JTextField txtAddressLine1 = new JTextField(28);
    private final JTextField txtAddressLine2 = new JTextField(28);
    private final JComboBox<String> cbProvinceAddress = new JComboBox<>();
    private final JComboBox<String> cbCityAddress = new JComboBox<>();
    private final JTextField txtZipCode = new JTextField(10);
    private final JTextField txtPhone = new JTextField(16);

    private final User currentUser;
    private final EmployeeService employeeService;
    private final HROps hrOps;
    private final CsvAddressReferenceRepository addressRepo;

    private Employee currentEmployee;

    public UpdateProfile(User currentUser, EmployeeService employeeService, HROps hrOps, com.motorph.repository.csv.CsvAddressReferenceRepository addressRepo) {
        this.currentUser = currentUser;
        this.employeeService = employeeService;
        this.hrOps = hrOps;
        this.addressRepo = addressRepo;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseGbc();
        int r = 0;

        addLabel(form, gbc, 0, r, "Address Line 1:");
        addField(form, gbc, 1, r, txtAddressLine1);
        addLabel(form, gbc, 2, r, "Address Line 2:");
        addField(form, gbc, 3, r, txtAddressLine2);
        r++;

        addLabel(form, gbc, 0, r, "Province:");
        addField(form, gbc, 1, r, cbProvinceAddress);
        addLabel(form, gbc, 2, r, "City / Municipality:");
        addField(form, gbc, 3, r, cbCityAddress);
        r++;

        addLabel(form, gbc, 0, r, "ZIP Code:");
        txtZipCode.setEditable(false);
        addField(form, gbc, 1, r, txtZipCode);
        addLabel(form, gbc, 2, r, "Phone Number:");
        addField(form, gbc, 3, r, txtPhone);

        content.add(form, BorderLayout.CENTER);

        JButton btnUpdate = new JButton("Update");
        JButton btnClear = new JButton("Reset");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.add(btnClear);
        south.add(btnUpdate);
        content.add(south, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        initializeAddressDropdowns();
        initializeRestrictions();
        loadEmployeeData();

        btnUpdate.addActionListener(e -> onUpdate());
        btnClear.addActionListener(e -> clearForm());
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerPanel.setBorder(new EmptyBorder(8, 8, 0, 8));
        headerPanel.setOpaque(false);

        JButton btnInfo = new JButton("ⓘ");
        btnInfo.putClientProperty("JButton.buttonType", "roundRect");
        btnInfo.setToolTipText("Profile update information");
        btnInfo.setFocusable(false);
        btnInfo.setMargin(new Insets(2, 8, 2, 8));

        btnInfo.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Only Address and Contact Number can be updated in this form.\n"
                + "For changes to other employee details, please use Employee Management or contact HR.",
                "Profile Update Information",
                JOptionPane.INFORMATION_MESSAGE
        ));

        headerPanel.add(btnInfo);
        return headerPanel;
    }

    private void initializeAddressDropdowns() {
        cbProvinceAddress.removeAllItems();
        cbCityAddress.removeAllItems();

        cbProvinceAddress.addItem(PROVINCE_PLACEHOLDER);
        cbCityAddress.addItem(CITY_PLACEHOLDER);

        for (String province : addressRepo.getProvinces()) {
            cbProvinceAddress.addItem(province);
        }

        cbProvinceAddress.addActionListener(e -> onProvinceAddressChanged());
        cbCityAddress.addActionListener(e -> onCityAddressChanged());
    }

    private void initializeRestrictions() {
        InputRestrictionUtil.applyPhoneRestriction(txtPhone);
    }

    private void onProvinceAddressChanged() {
        String province = getSelectedComboValue(cbProvinceAddress);

        cbCityAddress.removeAllItems();
        cbCityAddress.addItem(CITY_PLACEHOLDER);
        txtZipCode.setText("");

        if (ValidationUtil.isEmpty(province) || PROVINCE_PLACEHOLDER.equalsIgnoreCase(province)) {
            return;
        }

        for (String city : addressRepo.getCitiesByProvince(province)) {
            cbCityAddress.addItem(city);
        }
    }

    private void onCityAddressChanged() {
        String province = getSelectedComboValue(cbProvinceAddress);
        String city = getSelectedComboValue(cbCityAddress);

        txtZipCode.setText("");

        if (ValidationUtil.isEmpty(province) || ValidationUtil.isEmpty(city)
                || PROVINCE_PLACEHOLDER.equalsIgnoreCase(province)
                || CITY_PLACEHOLDER.equalsIgnoreCase(city)) {
            return;
        }

        txtZipCode.setText(addressRepo.getZipCode(province, city));
    }

    private void clearForm() {
        loadEmployeeData();
    }

    private void loadEmployeeData() {
        int empId = Integer.parseInt(currentUser.getUsername());
        currentEmployee = employeeService.getEmployee(empId);

        if (currentEmployee == null) {
            return;
        }

        AddressParser.ParsedAddress parsedAddress
                = AddressParser.parse(currentEmployee.getAddress(), addressRepo);

        txtAddressLine1.setText(parsedAddress.getAddressLine1());
        txtAddressLine2.setText(parsedAddress.getAddressLine2());
        txtPhone.setText(currentEmployee.getPhoneNumber());

        cbProvinceAddress.setSelectedIndex(0);
        cbCityAddress.removeAllItems();
        cbCityAddress.addItem(CITY_PLACEHOLDER);
        txtZipCode.setText("");

        if (!ValidationUtil.isEmpty(parsedAddress.getProvince())) {
            cbProvinceAddress.setSelectedItem(parsedAddress.getProvince());

            cbCityAddress.removeAllItems();
            cbCityAddress.addItem(CITY_PLACEHOLDER);

            for (String city : addressRepo.getCitiesByProvince(parsedAddress.getProvince())) {
                cbCityAddress.addItem(city);
            }

            if (!ValidationUtil.isEmpty(parsedAddress.getCityMunicipality())) {
                cbCityAddress.setSelectedItem(parsedAddress.getCityMunicipality());
            }

            txtZipCode.setText(parsedAddress.getZipCode());
        }
    }

    private void onUpdate() {
        if (currentEmployee == null) {
            JOptionPane.showMessageDialog(this, "Employee profile could not be loaded.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ValidationUtil.isEmpty(txtAddressLine1.getText()) || ValidationUtil.isEmpty(txtPhone.getText())) {
            JOptionPane.showMessageDialog(this, "Address Line 1 and Phone Number are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (ValidationUtil.isEmpty(getSelectedComboValue(cbProvinceAddress))
                || PROVINCE_PLACEHOLDER.equalsIgnoreCase(getSelectedComboValue(cbProvinceAddress))
                || ValidationUtil.isEmpty(getSelectedComboValue(cbCityAddress))
                || CITY_PLACEHOLDER.equalsIgnoreCase(getSelectedComboValue(cbCityAddress))) {
            JOptionPane.showMessageDialog(this, "Please select a valid Province and City / Municipality.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPhoneFormat(txtPhone.getText())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid phone number.\n"
                    + "Required format: 09XX-XXX-XXXX or 09XXXXXXXXX.\n"
                    + "Example: 0912-345-6789 or 09123456789.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            currentEmployee.setAddress(
                    AddressFormatter.buildFullAddress(
                            txtAddressLine1.getText(),
                            txtAddressLine2.getText(),
                            getSelectedComboValue(cbCityAddress),
                            getSelectedComboValue(cbProvinceAddress),
                            txtZipCode.getText()
                    )
            );
            currentEmployee.setPhoneNumber(txtPhone.getText().trim());

            int performerId = Integer.parseInt(currentUser.getUsername());
            boolean success = hrOps.updateEmployee(currentEmployee, performerId);

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) {
                    window.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred:\n" + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedComboValue(JComboBox<String> comboBox) {
        Object value = comboBox.getSelectedItem();
        return value == null ? "" : value.toString().trim();
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
