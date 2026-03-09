/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.User;
import com.motorph.ops.payroll.PayrollOps;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Payroll processing panel placeholder.
 *
 * @author ACER
 */
public class PayrollPanel extends javax.swing.JPanel {

    private final User currentUser;
    private final PayrollOps payrollOps;

    public PayrollPanel(User currentUser, PayrollOps payrollOps) {
        this.currentUser = currentUser;
        this.payrollOps = payrollOps;
        initComponents();
        setLayout(new BorderLayout());
        add(new JLabel("Payroll processing tools are available through authorized payroll actions."), BorderLayout.NORTH);
    }

    @SuppressWarnings("unchecked")
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
    }
}
