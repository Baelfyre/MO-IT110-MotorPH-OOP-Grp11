/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.domain.models.UserAccount;
import javax.swing.*;

/**
 *
 * @author ACER
 */
public class HomeView extends JFrame {
    public HomeView(UserAccount user) {
        setTitle("MotorPH - " + user.getRole());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        switch (user.getRole()) {
            case EMPLOYEE:
                add(new JLabel("Welcome Employee! (Dashboard Coming Soon)"));
                break;
            case PAYROLL:
                add(new JLabel("Welcome Payroll Admin! (Dashboard Coming Soon)"));
                break;
            default:
                add(new JLabel("Welcome " + user.getUsername()));
        }
    }
}
