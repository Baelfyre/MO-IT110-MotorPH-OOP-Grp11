/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.repository.csv.CsvUserAccountRepository;
import com.motorph.service.AuthService;
import javax.swing.SwingUtilities;

/**
 *
 * @author ACER
 */
public class SwingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CsvUserAccountRepository userRepo = new CsvUserAccountRepository();
            AuthService authService = new AuthService(userRepo);
            LoginView loginView = new LoginView(authService);
            loginView.setVisible(true);
        });
    }
}
