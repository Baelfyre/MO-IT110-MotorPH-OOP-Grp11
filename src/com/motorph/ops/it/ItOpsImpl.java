/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.motorph.ops.it;

import com.motorph.domain.models.User;
import com.motorph.repository.UserRepository;
import java.util.List;
import com.motorph.repository.csv.DataPaths;
import com.motorph.service.LogService;
import com.motorph.utils.ValidationUtil;

public class ItOpsImpl implements ItOps {

    private final UserRepository userRepo;
    private final LogService logService;
    private String lastActionMessage = "";

    public ItOpsImpl(UserRepository userRepo, LogService logService) {
        this.userRepo = userRepo;
        this.logService = logService;
    }

    @Override
    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @Override
    public boolean resetPasswordToDefault(String username, User currentUser) {
        return resetPasswordInternal(username, DataPaths.DEFAULT_PASSWORD, currentUser, true);
    }

    @Override
    public boolean resetPassword(String username, String newPassword, User currentUser) {
        return resetPasswordInternal(username, newPassword, currentUser, false);
    }

    // VERIFICATION 1: Placed in the internal method that handles all password resets
    private boolean resetPasswordInternal(String username, String newPassword, User currentUser, boolean isDefault) {
        // --- 1. BACKEND RBAC VERIFICATION ---
        if (currentUser == null || !currentUser.hasPermission("CAN_RESET_PASSWORD")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to reset password for user: " + username
            );
            throw new SecurityException("Access Denied: You do not have IT permissions to reset passwords.");
        }

        String actor = String.valueOf(currentUser.getId());
        String uname = safeTrim(username);
        String pass = safeTrim(newPassword);

        if (uname.isEmpty() || pass.isEmpty()) {
            lastActionMessage = "Username and password are required.";
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", lastActionMessage);
            return false;
        }

        String policyMessage = ValidationUtil.getPasswordPolicyMessage(pass);
        if (policyMessage != null) {
            lastActionMessage = policyMessage;
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", "Target=" + uname + " Reason=" + policyMessage);
            return false;
        }

        User existing = userRepo.findByUsername(uname);
        if (existing == null) {
            lastActionMessage = "Selected user was not found.";
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED", "User not found. Target=" + uname);
            return false;
        }

        try {
            userRepo.updatePassword(uname, pass);

            User reloaded = userRepo.findByUsername(uname);
            boolean ok = (reloaded != null) && reloaded.verifyPassword(pass);

            String action = ok ? "IT_RESET_PASSWORD_OK" : "IT_RESET_PASSWORD_FAILED";
            String mode = isDefault ? "default" : "custom";
            lastActionMessage = ok
                    ? (isDefault ? "Password reset to default." : "Password updated.")
                    : "Password reset failed.";

            logService.recordAction(actor, action,
                    "Actor=" + actor
                    + " Target=" + uname
                    + " ResetMode=" + mode
                    + " Result=" + (ok ? "success" : "failed"));

            return ok;
        } catch (Exception ex) {
            lastActionMessage = "Password reset failed.";
            logService.recordAction(actor, "IT_RESET_PASSWORD_FAILED",
                    "Actor=" + actor + " Target=" + uname + " ResetMode=" + (isDefault ? "default" : "custom") + " Error=" + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean lockAccount(String username, User currentUser) {
        return setLockStatus(username, true, currentUser);
    }

    @Override
    public boolean unlockAccount(String username, User currentUser) {
        return setLockStatus(username, false, currentUser);
    }

    // VERIFICATION 2: Placed directly in the overridden method that handles all locks/unlocks
    @Override
    public boolean setLockStatus(String username, boolean locked, User currentUser) {
        // --- 1. BACKEND RBAC VERIFICATION ---
        if (currentUser == null || !currentUser.hasPermission("CAN_LOCK_ACCOUNTS")) {
            logService.recordAction(
                currentUser != null ? String.valueOf(currentUser.getId()) : "UNKNOWN",
                "SECURITY_VIOLATION",
                "Unauthorized attempt to modify account lock status for user: " + username
            );
            throw new SecurityException("Access Denied: You do not have IT permissions to lock or unlock accounts.");
        }

        String actor = String.valueOf(currentUser.getId());
        String uname = safeTrim(username);

        // --- 2. PREVENT SELF-LOCKOUT ---
        if (locked && currentUser.getUsername().equalsIgnoreCase(uname)) {
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "Admin attempted to lock their own account.");
            throw new SecurityException("Action Denied: You cannot lock your own account.");
        }

        if (uname.isEmpty()) {
            lastActionMessage = "Select a user first.";
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "Blank username.");
            return false;
        }

        User existing = userRepo.findByUsername(uname);
        if (existing == null) {
            lastActionMessage = "Selected user was not found.";
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "User not found: " + uname);
            return false;
        }

        try {
            userRepo.updateLockStatus(uname, locked);

            User reloaded = userRepo.findByUsername(uname);
            boolean ok = (reloaded != null) && (reloaded.isLocked() == locked);

            String action;
            if (ok) {
                action = locked ? "IT_LOCK_OK" : "IT_UNLOCK_OK";
                lastActionMessage = locked ? "Account locked." : "Account unlocked.";
            } else {
                action = "IT_SET_LOCK_FAILED";
                lastActionMessage = "Lock status update failed.";
            }

            logService.recordAction(actor, action,
                    "Actor=" + actor + " Target=" + uname + " LockState=" + (locked ? "locked" : "unlocked"));
            return ok;
        } catch (Exception ex) {
            lastActionMessage = "Lock status update failed.";
            logService.recordAction(actor, "IT_SET_LOCK_FAILED", "Exception updating lock for " + uname + ": " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String getLastActionMessage() {
        return lastActionMessage == null ? "" : lastActionMessage;
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }
}