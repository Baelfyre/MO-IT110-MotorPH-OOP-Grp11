/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.auth;

import com.motorph.domain.models.User;
import com.motorph.service.AuthService;
import com.motorph.service.LogService;

/**
 * Coordinates the Log In use case by combining AuthService with activity
 * logging. The Ops layer remains UI-agnostic and focuses on system actions and
 * outcomes.
 *
 * @author ACER
 */
public class AuthOpsImpl implements AuthOps {

    private final AuthService authService;
    private final LogService logService;

    public AuthOpsImpl(AuthService authService, LogService logService) {
        this.authService = authService;
        this.logService = logService;
    }

    @Override
    public User login(String username, String password) {
        // Login outcome is produced by AuthService; the resolved user is stored internally on success.
        boolean ok = authService.login(username, password);

        // The returned user is derived from AuthService state only when authentication succeeds.
        User user = ok ? authService.getCurrentUser() : null;

        // The log actor uses the attempted username to preserve traceability on failures.
        String actor = (username == null) ? "" : username.trim();

        if (ok && user != null) {
            logService.recordAction(actor, "LOGIN_SUCCESS", "User logged in successfully.");
        } else {
            logService.recordAction(actor, "LOGIN_FAILED", "Invalid credentials or account locked.");
        }

        return user;
    }

}
