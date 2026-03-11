/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.enums.JobPosition;
import com.motorph.domain.models.Employee;
import com.motorph.domain.models.ProbationaryEmployee;
import com.motorph.domain.models.RegularEmployee;
import com.motorph.repository.csv.CsvAddressReferenceRepository;
import com.motorph.utils.AddressFormatter;
import com.motorph.utils.AddressParser;
import com.motorph.utils.InputRestrictionUtil;
import com.motorph.utils.ValidationUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import com.motorph.ui.swing.UiHelper.SwingFormHelper;
import com.motorph.ops.hr.HROps;
import com.motorph.utils.ValidationUtil;
import com.motorph.ui.swing.UiDialogs;


/**
 *
 * @author OngoJ.
 */
public class EmployeeFormPanel extends JPanel {

    private static final String PROVINCE_PLACEHOLDER = "Select Province";
    private static final String CITY_PLACEHOLDER = "Select City / Municipality";

    private final JTextField txtEmpNo = new JTextField(10);
    private final JTextField txtLastName = new JTextField(18);
    private final JTextField txtFirstName = new JTextField(18);

    private final JDateChooser dcBirthday = new JDateChooser();
    private final JTextField txtAddressLine1 = new JTextField(24);
    private final JTextField txtAddressLine2 = new JTextField(24);
    private final JComboBox<String> cbProvinceAddress = new JComboBox<>();
    private final JComboBox<String> cbCityAddress = new JComboBox<>();
    private final JTextField txtZipCode = new JTextField(10);
    private final JTextField txtPhone = new JTextField(16);

    private final JTextField txtSSS = new JTextField(16);
    private final JTextField txtPhilHealth = new JTextField(16);
    private final JTextField txtTIN = new JTextField(16);
    private final JTextField txtPagibig = new JTextField(16);

    private final JLabel lblPhoneFormat = buildHintLabel("Format: 09XX-XXX-XXXX");
    private final JLabel lblSssFormat = buildHintLabel("Format: 12-1234567-1");
    private final JLabel lblPhilHealthFormat = buildHintLabel("Format: 12-123456789-1");
    private final JLabel lblTinFormat = buildHintLabel("Format: 123-456-789-000");
    private final JLabel lblPagibigFormat = buildHintLabel("Format: 1234-5678-9012");

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

    private final CsvAddressReferenceRepository addressRepo;
    private double minimumBasicSalary = 1.0;

    public EmployeeFormPanel(CsvAddressReferenceRepository addressRepo) {
        this.addressRepo = addressRepo;

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
        txtZipCode.setEditable(false);

        setReadOnlyStyle(txtEmpNo);
        setReadOnlyStyle(txtGrossSemi);
        setReadOnlyStyle(txtHourlyRate);
        setReadOnlyStyle(txtLeaveCreditsSummary);
        setReadOnlyStyle(txtZipCode);

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

        SwingForm.addFieldSpan(form, gbc, 0, r, 2, new JLabel(""));
        SwingForm.addField(form, gbc, 3, r, lblPhoneFormat);
        SwingForm.addFieldSpan(form, gbc, 4, r, 2, new JLabel(""));
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Address Line 1:");
        SwingForm.addField(form, gbc, 1, r, txtAddressLine1);
        SwingForm.addLabel(form, gbc, 2, r, "Address Line 2:");
        SwingForm.addField(form, gbc, 3, r, txtAddressLine2);
        SwingForm.addLabel(form, gbc, 4, r, "ZIP Code:");
        SwingForm.addField(form, gbc, 5, r, txtZipCode);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Province:");
        SwingForm.addField(form, gbc, 1, r, cbProvinceAddress);
        SwingForm.addLabel(form, gbc, 2, r, "City / Municipality:");
        SwingForm.addField(form, gbc, 3, r, cbCityAddress);
        SwingForm.addFieldSpan(form, gbc, 4, r, 2, new JLabel(""));
        r++;

        JPanel groupedDetails = new JPanel(new GridLayout(1, 3, 12, 0));
        groupedDetails.add(buildGovernmentPanel());
        groupedDetails.add(buildOrganizationPanel());
        groupedDetails.add(buildCompensationPanel());
        SwingForm.addFieldSpan(form, gbc, 0, r, 6, groupedDetails);
        r++;

        JPanel salaryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints salaryGbc = new GridBagConstraints();
        salaryGbc.insets = new Insets(4, 4, 4, 4);
        salaryGbc.anchor = GridBagConstraints.WEST;
        salaryGbc.fill = GridBagConstraints.HORIZONTAL;
        salaryGbc.weightx = 0;
        salaryGbc.gridx = 0;
        salaryGbc.gridy = 0;
        salaryPanel.add(new JLabel("Basic Salary:"), salaryGbc);
        salaryGbc.gridx = 1;
        salaryGbc.weightx = 1.0;
        salaryPanel.add(txtBasicSalary, salaryGbc);
        salaryGbc.gridx = 2;
        salaryGbc.weightx = 0;
        JLabel minimumSalaryNote = buildHintLabel("Minimum allowed follows the current lowest positive employee salary.");
        salaryPanel.add(minimumSalaryNote, salaryGbc);
        SwingForm.addFieldSpan(form, gbc, 0, r, 6, salaryPanel);
        r++;

        SwingForm.addLabel(form, gbc, 0, r, "Leave Credits:");
        SwingForm.addFieldSpan(form, gbc, 1, r, 5, txtLeaveCreditsSummary);

        add(form, BorderLayout.CENTER);

        initializeAddressDropdowns();
        initializeRestrictions();
        installDerivedPayListeners();
        installStatusListener();
        loadDefaultPositions();
        seedDefaultSupervisor();
        refreshDerivedPayFields();
        refreshLeaveCreditsSummary();
    }

    private JPanel buildGovernmentPanel() {
        JPanel panel = buildGroupPanel("Government IDs");
        GridBagConstraints gbc = buildGroupGbc();
        int r = 0;

        addGroupField(panel, gbc, 0, r++, "SSS #:", txtSSS);
        addGroupHint(panel, gbc, 0, r++, lblSssFormat);
        addGroupField(panel, gbc, 0, r++, "PhilHealth #:", txtPhilHealth);
        addGroupHint(panel, gbc, 0, r++, lblPhilHealthFormat);
        addGroupField(panel, gbc, 0, r++, "TIN #:", txtTIN);
        addGroupHint(panel, gbc, 0, r++, lblTinFormat);
        addGroupField(panel, gbc, 0, r++, "Pag-IBIG #:", txtPagibig);
        addGroupHint(panel, gbc, 0, r, lblPagibigFormat);

        return panel;
    }

    private JPanel buildOrganizationPanel() {
        JPanel panel = buildGroupPanel("Position Details");
        GridBagConstraints gbc = buildGroupGbc();
        int r = 0;

        addGroupField(panel, gbc, 0, r++, "Position:", cbPosition);
        addGroupField(panel, gbc, 0, r, "Supervisor:", cbSupervisor);

        return panel;
    }

    private JPanel buildCompensationPanel() {
        JPanel panel = buildGroupPanel("Allowances and Rates");
        GridBagConstraints gbc = buildGroupGbc();
        int r = 0;

        addGroupField(panel, gbc, 0, r++, "Rice Allow:", txtRice);
        addGroupField(panel, gbc, 0, r++, "Phone Allow:", txtPhoneAllow);
        addGroupField(panel, gbc, 0, r++, "Clothing Allow:", txtClothingAllow);
        addGroupField(panel, gbc, 0, r++, "Gross Semi-Monthly:", txtGrossSemi);
        addGroupField(panel, gbc, 0, r, "Hourly Rate:", txtHourlyRate);

        return panel;
    }

    private JPanel buildGroupPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private GridBagConstraints buildGroupGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private void addGroupField(JPanel panel, GridBagConstraints base, int x, int y, String label, JComponent field) {
        GridBagConstraints left = (GridBagConstraints) base.clone();
        left.gridx = x;
        left.gridy = y;
        left.weightx = 0;
        panel.add(new JLabel(label), left);

        GridBagConstraints right = (GridBagConstraints) base.clone();
        right.gridx = x + 1;
        right.gridy = y;
        right.weightx = 1.0;
        panel.add(field, right);
    }

    private void addGroupHint(JPanel panel, GridBagConstraints base, int x, int y, JLabel label) {
        GridBagConstraints hint = (GridBagConstraints) base.clone();
        hint.gridx = x + 1;
        hint.gridy = y;
        hint.weightx = 1.0;
        panel.add(label, hint);
    }

    private JLabel buildHintLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        label.setForeground(new Color(110, 110, 110));
        return label;
    }

    private void initializeAddressDropdowns() {
        cbProvinceAddress.removeAllItems();
        cbCityAddress.removeAllItems();
        cbProvinceAddress.addItem(PROVINCE_PLACEHOLDER);
        cbCityAddress.addItem(CITY_PLACEHOLDER);

        if (addressRepo != null) {
            for (String province : addressRepo.getProvinces()) {
                cbProvinceAddress.addItem(province);
            }
        }

        cbProvinceAddress.addActionListener(e -> onProvinceAddressChanged());
        cbCityAddress.addActionListener(e -> onCityAddressChanged());
    }

    private void initializeRestrictions() {
        InputRestrictionUtil.applyPhoneRestriction(txtPhone);
        InputRestrictionUtil.applyGovernmentIdRestrictions(txtSSS, txtPagibig, txtTIN, txtPhilHealth);
        InputRestrictionUtil.applyCompensationRestrictions(txtBasicSalary, txtRice, txtPhoneAllow, txtClothingAllow);
    }

    private void onProvinceAddressChanged() {
        cbCityAddress.removeAllItems();
        cbCityAddress.addItem(CITY_PLACEHOLDER);
        txtZipCode.setText("");

        if (addressRepo == null) {
            return;
        }

        String province = selectedValue(cbProvinceAddress);
        if (ValidationUtil.isEmpty(province) || PROVINCE_PLACEHOLDER.equalsIgnoreCase(province)) {
            return;
        }

        for (String city : addressRepo.getCitiesByProvince(province)) {
            cbCityAddress.addItem(city);
        }
    }

    private void onCityAddressChanged() {
        txtZipCode.setText("");

        if (addressRepo == null) {
            return;
        }

        String province = selectedValue(cbProvinceAddress);
        String city = selectedValue(cbCityAddress);
        if (ValidationUtil.isEmpty(province)
                || ValidationUtil.isEmpty(city)
                || PROVINCE_PLACEHOLDER.equalsIgnoreCase(province)
                || CITY_PLACEHOLDER.equalsIgnoreCase(city)) {
            return;
        }

        txtZipCode.setText(addressRepo.getZipCode(province, city));
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

    public void setMinimumBasicSalary(double minimumBasicSalary) {
        this.minimumBasicSalary = minimumBasicSalary > 0.0 ? minimumBasicSalary : 1.0;
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

        loadParsedAddress(e.getAddress());
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

    private void loadParsedAddress(String fullAddress) {
        txtAddressLine1.setText("");
        txtAddressLine2.setText("");
        cbProvinceAddress.setSelectedIndex(0);
        cbCityAddress.removeAllItems();
        cbCityAddress.addItem(CITY_PLACEHOLDER);
        txtZipCode.setText("");

        if (ValidationUtil.isEmpty(fullAddress) || addressRepo == null) {
            txtAddressLine1.setText(nullToEmpty(fullAddress));
            return;
        }

        AddressParser.ParsedAddress parsedAddress = AddressParser.parse(fullAddress, addressRepo);
        txtAddressLine1.setText(parsedAddress.getAddressLine1());
        txtAddressLine2.setText(parsedAddress.getAddressLine2());

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

    // Annotation: Clears all form fields.
    public void clear() {
        txtEmpNo.setText("");
        txtLastName.setText("");
        txtFirstName.setText("");
        dcBirthday.setDate(null);
        txtAddressLine1.setText("");
        txtAddressLine2.setText("");
        cbProvinceAddress.setSelectedIndex(0);
        cbCityAddress.removeAllItems();
        cbCityAddress.addItem(CITY_PLACEHOLDER);
        txtZipCode.setText("");
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
    // Annotation: Builds an Employee object from the form values.
    // Now requires HROps for duplicate checking, and a flag for new employee creation.
    public Employee buildEmployeeOrNull(Component parentForErrors, HROps hrOps, boolean isNewEmployee) {
        String empNoStr = txtEmpNo.getText().trim();
        int empNo;
        try {
            empNo = Integer.parseInt(empNoStr);
        } catch (NumberFormatException ex) {
            UiDialogs.error(parentForErrors, "Employee # must be a number.");
            return null;
        }

        // --- 1. NEW DUPLICATE ID CHECK ---
        if (isNewEmployee && hrOps != null) {
            if (hrOps.isEmployeeIdDuplicate(empNo)) {
                UiDialogs.error(parentForErrors, "Employee ID " + empNo + " already exists. Please use a different ID.");
                return null;
            }
        }

        // --- 2. REQUIRED FIELDS CHECK ---
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

        if (ValidationUtil.isEmpty(txtAddressLine1.getText())) {
            errors.add("Address Line 1 is required.");
        }
        if (ValidationUtil.isEmpty(selectedValue(cbProvinceAddress))
                || PROVINCE_PLACEHOLDER.equalsIgnoreCase(selectedValue(cbProvinceAddress))
                || ValidationUtil.isEmpty(selectedValue(cbCityAddress))
                || CITY_PLACEHOLDER.equalsIgnoreCase(selectedValue(cbCityAddress))) {
            errors.add("Province and City / Municipality are required.");
        }

        double basic = safeMoney(txtBasicSalary.getText());
        if (basic < minimumBasicSalary) {
            errors.add(String.format(java.util.Locale.US,
                    "Basic Salary must be at least %.2f based on the current employee minimum.",
                    minimumBasicSalary));
        }

        if (!errors.isEmpty()) {
            UiDialogs.error(parentForErrors, String.join("\n", errors));
            return null;
        }

        double rice = safeMoney(txtRice.getText());
        double phoneAllow = safeMoney(txtPhoneAllow.getText());
        double clothing = safeMoney(txtClothingAllow.getText());
        double grossSemi = safeMoney(txtGrossSemi.getText());
        double hourly = safeMoney(txtHourlyRate.getText());

        String status = selectedValue(cbStatus);

        // --- 3. MILESTONE 2: STRICT GOVERNMENT ID VALIDATION ---
        String sss = txtSSS.getText().trim();
        if (!ValidationUtil.isValidSssFormat(sss) && !sss.isEmpty()) {
            UiDialogs.error(parentForErrors, "Invalid SSS Format. Use XX-XXXXXXX-X");
            return null;
        }

        String philHealth = txtPhilHealth.getText().trim();
        if (!ValidationUtil.isValidPhilHealthFormat(philHealth) && !philHealth.isEmpty()) {
            UiDialogs.error(parentForErrors, "Invalid PhilHealth Format. Use XX-XXXXXXXXX-X");
            return null;
        }

        String tin = txtTIN.getText().trim();
        if (!ValidationUtil.isValidTinFormat(tin) && !tin.isEmpty()) {
            UiDialogs.error(parentForErrors, "Invalid TIN Format. Use XXX-XXX-XXX-XXX");
            return null;
        }

        String pagibig = txtPagibig.getText().trim();
        if (!ValidationUtil.isValidPagIbigFormat(pagibig) && !pagibig.isEmpty()) {
            UiDialogs.error(parentForErrors, "Invalid Pag-IBIG Format. Use XXXX-XXXX-XXXX");
            return null;
        }

        // --- 4. NUMERIC PAYROLL VALIDATION ---
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

        // --- 5. BUILD EMPLOYEE OBJECT ---
        com.motorph.domain.models.Employee emp;
        if ("Probationary".equalsIgnoreCase(status)) {
            emp = new ProbationaryEmployee(empNo, last, first);
        } else {
            emp = new RegularEmployee(empNo, last, first);
        }

        emp.setBirthday(bday);
        emp.setAddress(AddressFormatter.buildFullAddress(
                txtAddressLine1.getText(),
                txtAddressLine2.getText(),
                selectedValue(cbCityAddress),
                selectedValue(cbProvinceAddress),
                txtZipCode.getText()
        ));
        emp.setPhoneNumber(txtPhone.getText().trim());

        emp.setSssNumber(sss);
        emp.setPhilHealthNumber(philHealth);
        emp.setTinNumber(tin);
        emp.setPagIbigNumber(pagibig);

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
