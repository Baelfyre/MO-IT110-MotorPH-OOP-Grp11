package com.motorph.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Simple self-service portal with shortcuts for employee actions.
 */
public class SelfServicePanel extends JPanel {

    public SelfServicePanel(Runnable openHome, Runnable openAttendance, Runnable openLeave, Runnable openUpdateProfile) {
        setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("Use the self-service portal to manage personal requests and records.");
        add(title, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(0, 1, 10, 10));

        JButton btnAttendance = new JButton("My Attendance");
        JButton btnLeave = new JButton("Request Leave / View Leave Requests");
        JButton btnUpdate = new JButton("Update Address and Contact Information");
        JButton btnHome = new JButton("Back to Home");

        btnAttendance.addActionListener(e -> openAttendance.run());
        btnLeave.addActionListener(e -> openLeave.run());
        btnUpdate.addActionListener(e -> openUpdateProfile.run());
        btnHome.addActionListener(e -> openHome.run());

        actions.add(btnAttendance);
        actions.add(btnLeave);
        actions.add(btnUpdate);
        actions.add(btnHome);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.add(actions);
        add(wrap, BorderLayout.CENTER);
    }
}
