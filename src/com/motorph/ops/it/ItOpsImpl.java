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

/**
 *
 * @author ACER
 */
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
    public boolean resetPasswordToDefault(String username, int performedByUserId) {
        return resetPasswordInternal(username, DataPaths.DEFAULT_PASSWORD, performedByUserId, true);
    }

    @Override
    public boolean resetPassword(String username, String newPassword, int performedByUserId) {
        return resetPasswordInternal(username, newPassword, performedByUserId, false);
    }

    private boolean resetPasswordInternal(String username, String newPassword, int performedByUserId, boolean isDefault) {
        String actor = String.valueOf(performedByUserId);
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
    public boolean lockAccount(String username, int performedByUserId) {
        return setLockStatus(username, true, performedByUserId);
    }

    @Override
    public boolean unlockAccount(String username, int performedByUserId) {
        return setLockStatus(username, false, performedByUserId);
    }

    @Override
    public boolean setLockStatus(String username, boolean locked, int performedByUserId) {
        String actor = String.valueOf(performedByUserId);
        String uname = safeTrim(username);

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
