/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.User;
import com.motorph.domain.enums.Role;
import com.motorph.service.EmployeeService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Timer;

import com.motorph.domain.models.TimeEntry;
import com.motorph.ops.auth.AuthOps;
import com.motorph.ops.time.TimeOps;
import com.motorph.ops.hr.HROps;
import com.motorph.ops.it.ItOps;
import com.motorph.ops.leave.LeaveOps;
import com.motorph.ops.payroll.PayrollOps;
import com.motorph.ops.payslip.PayslipOps;
import com.motorph.ops.supervisor.SupervisorOps;
import com.motorph.repository.csv.CsvAddressReferenceRepository;
import com.motorph.service.LeaveCreditsService;

/**
 *
 * @author ACER
 */
public class MainDashboard extends javax.swing.JFrame {

    private final User currentUser;
    private final EmployeeService employeeService; // Replaced the inline 'new' assignment
    private final AuthOps authOps;
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private HomePanel homePanel;
    private Timer clockTimer;
    private final TimeOps timeOps;
    private final HROps hrOps;
    private final PayrollOps payrollOps;
    private final PayslipOps payslipOps;
    private final SupervisorOps supervisorOps;
    private final LeaveOps leaveOps;
    private final ItOps itOps;
    private final LeaveCreditsService leaveCreditsService;
    private final CsvAddressReferenceRepository addressRepo;

    public MainDashboard(User loggedInUser, EmployeeService employeeService, AuthOps authOps, TimeOps timeOps, HROps hrOps,
            PayrollOps payrollOps, PayslipOps payslipOps, SupervisorOps supervisorOps, LeaveOps leaveOps, ItOps itOps,
            LeaveCreditsService leaveCreditsService, CsvAddressReferenceRepository addressRepo) {
        this.currentUser = loggedInUser;
        this.employeeService = employeeService;
        this.authOps = authOps;
        this.timeOps = timeOps;
        this.hrOps = hrOps;
        this.payrollOps = payrollOps;
        this.payslipOps = payslipOps;
        this.supervisorOps = supervisorOps;
        this.leaveOps = leaveOps;
        this.itOps = itOps;
        this.leaveCreditsService = leaveCreditsService;
        this.addressRepo = addressRepo;

        initComponents();
        initCustomLayout();
        loadDashboardProfile();
        loadTodayAttendanceLabels();

        setResizable(true);
        setLocationRelativeTo(null);
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        startClock();
        customizeSidebar();
    }

    private void customizeSidebar() {
        // Null-safe fallback
        boolean hasHR = currentUser != null && currentUser.getRoles().contains(Role.HR);
        boolean hasPayroll = currentUser != null && currentUser.getRoles().contains(Role.PAYROLL);
        boolean hasIT = currentUser != null && currentUser.getRoles().contains(Role.IT);

        // 1. ROLE-BASED FEATURES 
        // Explicitly set true OR false so they don't get stuck hidden on a UI refresh
        jButton3.setVisible(hasHR);                // Employee Management = HR only
        jButton2.setVisible(hasPayroll);           // Payroll Management = Payroll only
        jButton9.setVisible(hasIT);                // System Maintenance

        // 2. DYNAMIC MANAGER CHECK
        boolean isASupervisor = (currentUser != null)
                && employeeService.isSupervisor(currentUser.getUsername());

        jButton7.setVisible(isASupervisor);        // Attendance Management
    }

    private void initCustomLayout() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.removeAll();
        mainContentPanel.setLayout(cardLayout);

        homePanel = new HomePanel(currentUser);

        // Annotation: Register dashboard cards.
        mainContentPanel.add(homePanel, "HOME");
        mainContentPanel.add(new SelfServicePanel(
                () -> showCard("HOME"),
                () -> showCard("ATTENDANCE"),
                () -> showCard("LEAVE"),
                () -> showCard("PAYSLIP"),
                this::openUpdateProfileDialog
        ), "SELF_SERVICE");
        mainContentPanel.add(new HrPanel(currentUser, hrOps), "HR");
        mainContentPanel.add(new PayrollPanel(currentUser, payrollOps), "PAYROLL");
        mainContentPanel.add(new TimekeepingPanel(currentUser, timeOps), "ATTENDANCE");
        mainContentPanel.add(new LeavePanel(currentUser, leaveOps, employeeService), "LEAVE");
        mainContentPanel.add(new SupervisorPanel(currentUser, supervisorOps), "SUPERVISOR");
        mainContentPanel.add(new ITPanel(currentUser, employeeService, itOps), "IT");
        mainContentPanel.add(new PayslipPanel(currentUser, payslipOps, employeeService, leaveCreditsService), "PAYSLIP");

        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(mainContentPanel, BorderLayout.CENTER);

        showCard("HOME");

        jPanel2.revalidate();
        jPanel2.repaint();
    }

    // Annotation: Updates the section title based on the active card.
    private void showCard(String cardKey) {
        if (cardLayout == null || mainContentPanel == null) {
            return;
        }

        cardLayout.show(mainContentPanel, cardKey);
        updateSectionTitle(cardKey);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

// Annotation: Update the section title based on the active card.
    private void updateSectionTitle(String cardKey) {
        switch (cardKey) {
            case "HOME":
                jLabel16.setText("Additional Employee Details");
                break;
            case "SELF_SERVICE":
                jLabel16.setText("Self Service Portal");
                break;
            case "HR":
                jLabel16.setText("Employee Management");
                break;
            case "PAYROLL":
                jLabel16.setText("Payroll Management");
                break;
            case "PAYSLIP":
                jLabel16.setText("View Payroll Details");
                break;
            case "ATTENDANCE":
                jLabel16.setText("My Attendance");
                break;
            case "LEAVE":
                jLabel16.setText("Leave Requests");
                break;
            case "SUPERVISOR":
                jLabel16.setText("Attendance Management");
                break;
            case "IT":
                jLabel16.setText("System Maintenance");
                break;
            default:
                jLabel16.setText("Additional Details");
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        javax.swing.JButton jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jButton11 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.darkGray));

        jLabel1.setFont(new java.awt.Font("Segoe UI Variable", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(47, 35, 57));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/MotorPHLogo.png"))); // NOI18N
        jLabel1.setToolTipText("");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 0, 10));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setIconTextGap(-20);
        jLabel1.setMaximumSize(new java.awt.Dimension(200, 200));
        jLabel1.setMinimumSize(new java.awt.Dimension(150, 150));
        jLabel1.setPreferredSize(new java.awt.Dimension(195, 191));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(47, 35, 58));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("MENU");

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setText("Payroll Management");
        jButton2.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton2.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton2.setOpaque(true);
        jButton2.setPreferredSize(new java.awt.Dimension(122, 22));
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setText("Self Service Portal");
        jButton1.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton1.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton1.setOpaque(true);
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setText("Employee Management");
        jButton3.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton3.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton3.setOpaque(true);
        jButton3.setPreferredSize(new java.awt.Dimension(138, 22));
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Date:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Time:");

        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/clockIn.png"))); // NOI18N
        jButton4.setText("Clock In");
        jButton4.setMaximumSize(new java.awt.Dimension(110, 30));
        jButton4.setMinimumSize(new java.awt.Dimension(110, 30));
        jButton4.setOpaque(true);
        jButton4.setPreferredSize(new java.awt.Dimension(110, 30));
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/clockOut.png"))); // NOI18N
        jButton5.setText("Clock Out");
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton5.setMaximumSize(new java.awt.Dimension(110, 30));
        jButton5.setMinimumSize(new java.awt.Dimension(110, 30));
        jButton5.setOpaque(true);
        jButton5.setPreferredSize(new java.awt.Dimension(110, 30));
        jButton5.setVerifyInputWhenFocusTarget(false);
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setText("Attendance Management");
        jButton7.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton7.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton7.setOpaque(true);
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton9.setText("System Maintenance");
        jButton9.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton9.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton9.setOpaque(true);
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton10.setText("View Payroll Details");
        jButton10.setMaximumSize(new java.awt.Dimension(130, 20));
        jButton10.setMinimumSize(new java.awt.Dimension(130, 20));
        jButton10.setOpaque(true);
        jButton10.setPreferredSize(new java.awt.Dimension(116, 22));
        jButton10.addActionListener(this::jButton10ActionPerformed);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Time In: ");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Time Out: ");

        jLabel17.setText("jLabel17");

        jLabel18.setText("jLabel18");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("Total Worked Hours");

        jLabel21.setText("jLabel21");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton10, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel13)
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(1, 1, 1))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(29, 29, 29))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(jLabel20))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel17)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/Logout.png"))); // NOI18N
        jButton6.setText("Log out");
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton6.setMaximumSize(new java.awt.Dimension(75, 25));
        jButton6.setMinimumSize(new java.awt.Dimension(75, 25));
        jButton6.setOpaque(true);
        jButton6.setPreferredSize(new java.awt.Dimension(75, 25));
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addGap(14, 14, 14))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Immediate Supervisor");
        jLabel12.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel12.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel12.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Employee ID:");
        jLabel6.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel6.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel6.setName(""); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Date of Birth:");
        jLabel8.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel8.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel8.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Name:");
        jLabel7.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel7.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel7.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Status");
        jLabel10.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel10.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel10.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Phone Number:");
        jLabel11.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel11.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel11.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Position");
        jLabel9.setMaximumSize(new java.awt.Dimension(70, 15));
        jLabel9.setMinimumSize(new java.awt.Dimension(70, 15));
        jLabel9.setPreferredSize(new java.awt.Dimension(70, 15));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/NULL.png"))); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setMaximumSize(new java.awt.Dimension(150, 150));
        jLabel5.setMinimumSize(new java.awt.Dimension(150, 150));
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 150));

        jTextField1.setText(" jTextField1");
        jTextField1.setMaximumSize(null);
        jTextField1.setMinimumSize(new java.awt.Dimension(340, 25));
        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jTextField2.setText("jTextField2");
        jTextField2.setMaximumSize(null);
        jTextField2.setMinimumSize(new java.awt.Dimension(340, 25));

        jTextField3.setText("jTextField3");
        jTextField3.setMaximumSize(null);
        jTextField3.setMinimumSize(new java.awt.Dimension(340, 25));

        jTextField4.setText("jTextField4");
        jTextField4.setMaximumSize(null);
        jTextField4.setMinimumSize(new java.awt.Dimension(340, 25));

        jTextField5.setText("jTextField5");
        jTextField5.setMaximumSize(null);
        jTextField5.setMinimumSize(new java.awt.Dimension(340, 25));

        jTextField6.setText("jTextField6");
        jTextField6.setMaximumSize(null);
        jTextField6.setMinimumSize(new java.awt.Dimension(340, 25));

        jTextField7.setText("jTextField7");
        jTextField7.setMaximumSize(null);
        jTextField7.setMinimumSize(new java.awt.Dimension(340, 25));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jButton11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton11.setText("Refresh");
        jButton11.setOpaque(true);
        jButton11.addActionListener(this::jButton11ActionPerformed);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("jLabel16");
        jLabel16.setMinimumSize(new java.awt.Dimension(200, 15));
        jLabel16.setOpaque(true);
        jLabel16.setPreferredSize(new java.awt.Dimension(100, 15));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Address:");
        jLabel14.setMaximumSize(new java.awt.Dimension(450, 15));
        jLabel14.setMinimumSize(new java.awt.Dimension(450, 15));
        jLabel14.setPreferredSize(new java.awt.Dimension(450, 15));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jButton11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Payroll Management
        showCard("PAYROLL");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showCard("SELF_SERVICE");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Employee Management
        showCard("HR");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Clock In
        int empId = Integer.parseInt(currentUser.getUsername());

        boolean success = timeOps.clockIn(empId);
        loadTodayAttendanceLabels();

        if (success) {
            JOptionPane.showMessageDialog(this, "Clock In successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            if (isOutsideWorkingHours()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Logged in beyond working hours. Time was recorded, but it may be subject for supervisor approval.",
                        "Notice",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } else if (isWeekendToday()) {
            JOptionPane.showMessageDialog(this, "Clock In is disabled on weekends. Please use supervisor-approved manual DTR entry.", "Notice", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Clock In failed. A time-in may already exist for today.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Clock Out
        int empId = Integer.parseInt(currentUser.getUsername());

        boolean success = timeOps.clockOut(empId);
        loadTodayAttendanceLabels();

        if (success) {
            JOptionPane.showMessageDialog(this, "Clock Out successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            if (isOutsideWorkingHours()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Logged out beyond working hours. Time was recorded, but it may be subject for supervisor approval.",
                        "Notice",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } else if (isWeekendToday()) {
            JOptionPane.showMessageDialog(this, "Clock Out is disabled on weekends. Please use supervisor-approved manual DTR entry.", "Notice", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Clock Out failed. A time-in may be missing, or a time-out may already exist for today.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // Attendance Management
        showCard("SUPERVISOR");

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // System Maintenance
        showCard("IT");
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // View Payroll Details
        showCard("PAYSLIP");
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Logout
        new LoginPanel(authOps, employeeService, timeOps, hrOps, payrollOps, payslipOps, supervisorOps, leaveOps, itOps, leaveCreditsService, addressRepo).setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // Refresh cached data used by supervisor checks.
        employeeService.refreshCache();
        loadDashboardProfile();
        loadTodayAttendanceLabels();
        customizeSidebar();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    // End of variables declaration//GEN-END:variables

    private boolean isOutsideWorkingHours() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);
        return now.isBefore(start) || now.isAfter(end);
    }

    private boolean isWeekendToday() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    // Annotation: Loads today's time-in and time-out into the sidebar labels.
    private void loadTodayAttendanceLabels() {
        if (currentUser == null) {
            jLabel13.setText("Time In: ");
            jLabel15.setText("Time Out: ");
            return;
        }

        int empId;
        try {
            empId = Integer.parseInt(currentUser.getUsername());
        } catch (NumberFormatException e) {
            jLabel13.setText("Time In: ");
            jLabel15.setText("Time Out: ");
            return;
        }

        List<TimeEntry> entries = timeOps.viewMyTimeEntries(empId);
        LocalDate today = LocalDate.now();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm:ss a");

        String timeInText = "";
        String timeOutText = "";

        for (TimeEntry entry : entries) {
            if (entry.getDate() != null && entry.getDate().equals(today)) {
                if (entry.getTimeIn() != null) {
                    timeInText = entry.getTimeIn().format(timeFmt);
                }
                if (entry.getTimeOut() != null) {
                    timeOutText = entry.getTimeOut().format(timeFmt);
                }
                break;
            }
        }

        jLabel13.setText("Time In: " + timeInText);
        jLabel15.setText("Time Out: " + timeOutText);
    }

    private void openUpdateProfileDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Update Address and Contact Information", true);
        dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        UpdateProfile updatePanel = new UpdateProfile(currentUser, employeeService, hrOps, addressRepo);
        dialog.getContentPane().add(updatePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        employeeService.refreshCache();
        loadDashboardProfile();
        loadTodayAttendanceLabels();
        customizeSidebar();
    }

    private void startClock() {
        updateDateTimeLabels(); // show immediately

        clockTimer = new Timer(1000, e -> updateDateTimeLabels());
        clockTimer.start();
    }

    // Grabs the Employee profile and fills the Dashboard text fields
    private void loadDashboardProfile() {
        if (currentUser == null) {
            return;
        }

        try {
            int empId = Integer.parseInt(currentUser.getUsername());
            com.motorph.domain.models.Employee emp = employeeService.getEmployee(empId);

            if (emp != null) {
                // Top profile fields
                jTextField1.setText(String.valueOf(emp.getId()));
                jTextField2.setText(emp.getFirstName() + " " + emp.getLastName());

                if (emp.getBirthday() != null) {
                    java.time.format.DateTimeFormatter df
                            = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    jTextField3.setText(emp.getBirthday().format(df));
                } else {
                    jTextField3.setText("N/A");
                }

                jTextField4.setText(emp.getPosition());
                jTextField5.setText(emp.getStatus());
                jTextField6.setText(emp.getImmediateSupervisor());
                jTextField7.setText(emp.getPhoneNumber());
                jLabel14.setText("Address: " + emp.getAddress());

                jTextField1.setEditable(false);
                jTextField2.setEditable(false);
                jTextField3.setEditable(false);
                jTextField4.setEditable(false);
                jTextField5.setEditable(false);
                jTextField6.setEditable(false);
                jTextField7.setEditable(false);

                boolean probationary = "Probationary".equalsIgnoreCase(emp.getStatus());
                double standardPaidLeaveHours = probationary ? 0.0 : 40.0;
                double recordedLeaveTakenHours = leaveCreditsService.getStoredLeaveTakenHours(empId);
                double remainingPaidLeaveHours = Math.max(0.0, standardPaidLeaveHours - recordedLeaveTakenHours);
                String eligibilityNote = probationary
                        ? "Probationary employees are shown with 0.00 paid leave credits in this portal. Leave requests may still be filed for supervisor review as unpaid leave."
                        : "Paid leave is shown here using the current portal policy baseline of 5 days or 40.00 hours per year.";

                StringBuilder html = new StringBuilder();

                html.append("<html>");
                html.append("<body style='font-family:Segoe UI; font-size:12px; margin:8px 10px; color:#222222;'>");
                html.append("<div style='margin:8px 0 4px 0; font-size:12px; font-weight:bold;'>Government Identification</div>");
                html.append(buildDetailLine("SSS No:", safeText(emp.getSssNumber())));
                html.append(buildDetailLine("PhilHealth No:", safeText(emp.getPhilHealthNumber())));
                html.append(buildDetailLine("TIN:", safeText(emp.getTinNumber())));
                html.append(buildDetailLine("Pag-IBIG No:", safeText(emp.getPagIbigNumber())));

                html.append("<div style='margin:14px 0 4px 0; font-size:12px; font-weight:bold;'>Compensation Overview</div>");
                html.append(buildDetailLine("Basic Salary:", "₱" + formatMoney(emp.getBasicSalary())));
                html.append(buildDetailLine("Hourly Rate:", "₱" + formatMoney(emp.getHourlyRate())));
                html.append("<div style='margin:8px 0 2px 0; font-weight:bold;'>Allowances</div>");
                html.append("<div style='margin-left:18px;'>");
                html.append(buildDetailLine("Rice Allowance:", "₱" + formatMoney(emp.getRiceAllowance())));
                html.append(buildDetailLine("Phone Allowance:", "₱" + formatMoney(emp.getPhoneAllowance())));
                html.append(buildDetailLine("Clothing Allowance:", "₱" + formatMoney(emp.getClothingAllowance())));
                html.append("</div>");

                html.append("<div style='margin:14px 0 4px 0; font-size:12px; font-weight:bold;'>Leave Summary</div>");
                html.append(buildDetailLine("Paid Leave Credits:", formatHours(standardPaidLeaveHours) + " hours"));
                html.append(buildDetailLine("Recorded Leave Taken:", formatHours(recordedLeaveTakenHours) + " hours"));
                html.append(buildDetailLine("Remaining Paid Leave Balance:", formatHours(remainingPaidLeaveHours) + " hours"));
                html.append("<div style='margin-top:6px; color:#555555;'>" + escapeHtml(eligibilityNote) + "</div>");

                html.append("</body>");
                html.append("</html>");

                if (homePanel != null) {
                    homePanel.setDetailsHtml(html.toString());
                    homePanel.setDeductionContext(emp.getBasicSalary(), Math.max(0.0, emp.getBasicSalary() / 2.0));
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Could not parse user ID for dashboard: " + e.getMessage());
        }
    }

    // Annotation: Format label-value rows for the Home panel.
    private String buildDetailLine(String label, String value) {
        return "<div style='margin:4px 0;'><span style='font-family:Segoe UI; font-size:12px; font-weight:bold; min-width:180px; display:inline-block;'>" + escapeHtml(label) + "</span>"
                + "<span style='font-family:Courier New; font-size:12px;'>" + escapeHtml(value) + "</span></div>";
    }

// Annotation: Format money for display.
    private String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }

// Annotation: Format leave hour values for display.
    private String formatHours(double hours) {
        return String.format("%,.2f", hours);
    }

// Annotation: Prevent null output in detail fields.
    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value.trim();
    }

// Annotation: Escape HTML special characters in values.
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void updateDateTimeLabels() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");

        jLabel18.setText(now.format(dateFormat)); // Date
        jLabel17.setText(now.format(timeFormat)); // Time
    }

    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }
}
