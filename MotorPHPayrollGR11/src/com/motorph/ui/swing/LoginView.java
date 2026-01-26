/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;
import com.motorph.repository.csv.CsvUserRepository;
import com.motorph.service.AuthService;
import java.awt.Color; // Import needed for Ghost Text color
import java.awt.event.FocusAdapter; // Import for Focus Listener
import java.awt.event.FocusEvent;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

/**
 *
 * @author ACER
 */
public class LoginView extends javax.swing.JFrame {

    // --- 1. CONNECT TO BACKEND (CORRECTED) ---
    private final UserRepository userRepo;
    private final AuthService authService;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginView.class.getName());

    /**
     * Creates new form LoginView
     */
    public LoginView() {
        // --- 2. INIT SERVICE (CORRECTED) ---
        // We initialize the Repository first, then the Service
        this.userRepo = new CsvUserRepository();
        this.authService = new AuthService(userRepo);
        
        // --- 3. BUILD UI ---
        initComponents(); 
        setLocationRelativeTo(null); // Centers window on screen
        
        // --- 4. APPLY FEATURES ---
        setupNumberOnlyFilter();
        setupGhostText();
        
        // Fix: Ensure focus is not on the text field immediately so "USERNAME" is visible
        this.requestFocusInWindow();
    }

    // --- LOGIC: PERFORM LOGIN ---
    private void performLogin() {
        String username = jTextField1.getText().trim();
        String password = new String(jPasswordField1.getPassword());

        // Check if fields are empty or still show the placeholder
        if (username.equals("USERNAME") || password.equals("PASSWORD") || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // CALL SERVICE (Using AuthService)
        if (authService.login(username, password)) {
            // Success: Get the user and open Dashboard
            User loggedInUser = authService.getCurrentUser();
            new MainDashboard(loggedInUser).setVisible(true);
            this.dispose();
        } else {
            // Failure: Show Error
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE);
            jPasswordField1.setText("");
            
            // Reset placeholder if needed
            jPasswordField1.setForeground(Color.GRAY);
            jPasswordField1.setEchoChar((char) 0);
            jPasswordField1.setText("PASSWORD");
        }
    }

    // --- UI FEATURE: GHOST TEXT ---
    private void setupGhostText() {
        // Username Placeholder
        jTextField1.setForeground(Color.GRAY);
        jTextField1.setText("USERNAME");
        jTextField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jTextField1.getText().equals("USERNAME")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setForeground(Color.GRAY);
                    jTextField1.setText("USERNAME");
                }
            }
        });

        // Password Placeholder
        jPasswordField1.setForeground(Color.GRAY);
        jPasswordField1.setEchoChar((char) 0); // Show text
        jPasswordField1.setText("PASSWORD");
        jPasswordField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String pwd = new String(jPasswordField1.getPassword());
                if (pwd.equals("PASSWORD")) {
                    jPasswordField1.setText("");
                    jPasswordField1.setForeground(Color.BLACK);
                    jPasswordField1.setEchoChar('•'); // Show dots
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (new String(jPasswordField1.getPassword()).isEmpty()) {
                    jPasswordField1.setForeground(Color.GRAY);
                    jPasswordField1.setEchoChar((char) 0); // Show text
                    jPasswordField1.setText("PASSWORD");
                }
            }
        });
    }
    // --- UI FEATURE: NUMBER FILTER ---


    // --- UI FEATURE: NUMBER FILTER ---
    private void setupNumberOnlyFilter() {
        ((javax.swing.text.AbstractDocument) jTextField1.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws javax.swing.text.BadLocationException {
                if (string.matches("\\d+")) super.insertString(fb, offset, string, attr);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws javax.swing.text.BadLocationException {
                if (text.matches("\\d*")) super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(400, 600));
        setMinimumSize(new java.awt.Dimension(400, 600));
        setPreferredSize(new java.awt.Dimension(400, 600));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/Logo2.png"))); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        getContentPane().add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/Username.png"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel2.setMaximumSize(new java.awt.Dimension(30, 30));
        jLabel2.setMinimumSize(new java.awt.Dimension(30, 30));
        jLabel2.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        jPanel1.add(jLabel2, gridBagConstraints);

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("USERNAME");
        jTextField1.setToolTipText("");
        jTextField1.setBorder(new javax.swing.border.MatteBorder(null));
        jTextField1.setMargin(new java.awt.Insets(3, 7, 3, 7));
        jTextField1.setMaximumSize(new java.awt.Dimension(75, 25));
        jTextField1.setMinimumSize(new java.awt.Dimension(75, 25));
        jTextField1.setName(""); // NOI18N
        jTextField1.setPreferredSize(new java.awt.Dimension(75, 25));
        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        jPanel1.add(jTextField1, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/motorph/ui/resources/images/Password.png"))); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel4.setMaximumSize(new java.awt.Dimension(30, 30));
        jLabel4.setMinimumSize(new java.awt.Dimension(30, 30));
        jLabel4.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        jPanel1.add(jLabel4, gridBagConstraints);

        jPasswordField1.setColumns(20);
        jPasswordField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPasswordField1.setText("PASSWORD");
        jPasswordField1.setToolTipText("");
        jPasswordField1.setBorder(new javax.swing.border.MatteBorder(null));
        jPasswordField1.setMargin(new java.awt.Insets(3, 7, 3, 7));
        jPasswordField1.setMaximumSize(new java.awt.Dimension(75, 25));
        jPasswordField1.setMinimumSize(new java.awt.Dimension(75, 25));
        jPasswordField1.setPreferredSize(new java.awt.Dimension(75, 25));
        jPasswordField1.setRequestFocusEnabled(false);
        jPasswordField1.addActionListener(this::jPasswordField1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 7, 3, 7);
        jPanel1.add(jPasswordField1, gridBagConstraints);

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setText("Login");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMargin(new java.awt.Insets(5, 10, 5, 10));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 25));
        jButton1.addActionListener(this::jButton1ActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanel1.add(jButton1, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        performLogin();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordField1ActionPerformed
        // TODO add your handling code here:
        performLogin();
    }//GEN-LAST:event_jPasswordField1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
        performLogin();
    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments
     */
public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
